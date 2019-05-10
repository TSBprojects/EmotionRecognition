package ru.sstu.vak.emotionRecognition.graphicPrep.imageLoader;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.*;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.nd4j.linalg.api.concurrency.AffinityManager;
import org.nd4j.linalg.api.memory.pointers.PagedPointer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;

import java.io.IOException;

public class NativeImageLoader {
    public static final String[] ALLOWED_FORMATS = new String[]{"bmp", "gif", "jpg", "jpeg", "jp2", "pbm", "pgm", "ppm", "pnm", "png", "tif", "tiff", "exr", "webp", "BMP", "GIF", "JPG", "JPEG", "JP2", "PBM", "PGM", "PPM", "PNM", "PNG", "TIF", "TIFF", "EXR", "WEBP"};
    protected OpenCVFrameConverter.ToMat converter;
    protected long height = -1L;
    protected long width = -1L;
    protected long channels = -1L;
    protected boolean centerCropIfNeeded = false;
    boolean direct;

    public NativeImageLoader() {
        this.converter = new OpenCVFrameConverter.ToMat();
        this.direct = !Loader.getPlatform().startsWith("android");
    }

    public String[] getAllowedFormats() {
        return ALLOWED_FORMATS;
    }

    protected INDArray transformImage(Mat image, INDArray ret) throws IOException {
        Mat image2 = null;
        Mat image3 = null;
        Mat image4 = null;
        if (this.channels > 0L && (long) image.channels() != this.channels) {
            byte code;
            code = -1;
            label64:
            switch (image.channels()) {
                case 1:
                    switch ((int) this.channels) {
                        case 3:
                            code = 8;
                            break;
                        case 4:
                            code = 9;
                    }
                case 2:
                default:
                    break;
                case 3:
                    switch ((int) this.channels) {
                        case 1:
                            code = 6;
                            break label64;
                        case 4:
                            code = 2;
                        default:
                            break label64;
                    }
                case 4:
                    switch ((int) this.channels) {
                        case 1:
                            code = 11;
                            break;
                        case 3:
                            code = 3;
                    }
            }

            if (code < 0) {
                throw new IOException("Cannot convert from " + image.channels() + " to " + this.channels + " channels.");
            }

            image2 = new Mat();
            opencv_imgproc.cvtColor(image, image2, code);
            image = image2;
        }

        if (this.centerCropIfNeeded) {
            image3 = this.centerCropIfNeeded(image);
            if (image3 != image) {
                image = image3;
            } else {
                image3 = null;
            }
        }

        image4 = this.scalingIfNeed(image);
        if (image4 != image) {
            image = image4;
        } else {
            image4 = null;
        }

        if (ret == null) {
            int rows = image.rows();
            int cols = image.cols();
            int channels = image.channels();
            ret = Nd4j.create(new int[]{channels, rows, cols});
        }

        this.fillNDArray(image, ret);
        image.data();
        if (image2 != null) {
            image2.deallocate();
        }

        if (image3 != null) {
            image3.deallocate();
        }

        if (image4 != null) {
            image4.deallocate();
        }

        return ret;
    }

    protected Mat scalingIfNeed(Mat image) {
        return this.scalingIfNeed(image, this.height, this.width);
    }

    protected Mat scalingIfNeed(Mat image, long dstHeight, long dstWidth) {
        Mat scaled = image;
        if (dstHeight > 0L && dstWidth > 0L && ((long) image.rows() != dstHeight || (long) image.cols() != dstWidth)) {
            opencv_imgproc.resize(image, scaled = new Mat(), new opencv_core.Size((int) Math.min(dstWidth, 2147483647L), (int) Math.min(dstHeight, 2147483647L)));
        }

        return scaled;
    }

    protected Mat centerCropIfNeeded(Mat img) {
        int x = 0;
        int y = 0;
        int height = img.rows();
        int width = img.cols();
        int diff = Math.abs(width - height) / 2;
        if (width > height) {
            x = diff;
            width -= diff;
        } else if (height > width) {
            y = diff;
            height -= diff;
        }

        return img.apply(new opencv_core.Rect(x, y, width, height));
    }

    protected void fillNDArray(Mat image, INDArray ret) {
        long rows = (long) image.rows();
        long cols = (long) image.cols();
        long channels = (long) image.channels();
        if (ret.lengthLong() != rows * cols * channels) {
            throw new ND4JIllegalStateException("INDArray provided to store image not equal to image: {channels: " + channels + ", rows: " + rows + ", columns: " + cols + "}");
        } else {
            Indexer idx = image.createIndexer(this.direct);
            Pointer pointer = ret.data().pointer();
            long[] stride = ret.stride();
            boolean done = false;
            PagedPointer pagedPointer = new PagedPointer(pointer, rows * cols * channels, ret.data().offset() * (long) Nd4j.sizeOfDataType(ret.data().dataType()));
            FloatIndexer floatidx;
            long k;
            long i;
            long j;
            IntIndexer intidx;
            UShortIndexer ushortidx;
            UByteIndexer ubyteidx;
            if (pointer instanceof FloatPointer) {
                FloatIndexer retidx = FloatIndexer.create(pagedPointer.asFloatPointer(), new long[]{channels, rows, cols}, new long[]{stride[0], stride[1], stride[2]}, this.direct);
                if (idx instanceof UByteIndexer) {
                    ubyteidx = (UByteIndexer) idx;
                    k = 0L;

                    while (true) {
                        if (k >= channels) {
                            done = true;
                            break;
                        }

                        for (i = 0L; i < rows; ++i) {
                            for (j = 0L; j < cols; ++j) {
                                retidx.put(k, i, j, (float) ubyteidx.get(i, j, k));
                            }
                        }

                        ++k;
                    }
                } else if (idx instanceof UShortIndexer) {
                    ushortidx = (UShortIndexer) idx;
                    k = 0L;

                    while (true) {
                        if (k >= channels) {
                            done = true;
                            break;
                        }

                        for (i = 0L; i < rows; ++i) {
                            for (j = 0L; j < cols; ++j) {
                                retidx.put(k, i, j, (float) ushortidx.get(i, j, k));
                            }
                        }

                        ++k;
                    }
                } else if (idx instanceof IntIndexer) {
                    intidx = (IntIndexer) idx;
                    k = 0L;

                    while (true) {
                        if (k >= channels) {
                            done = true;
                            break;
                        }

                        for (i = 0L; i < rows; ++i) {
                            for (j = 0L; j < cols; ++j) {
                                retidx.put(k, i, j, (float) intidx.get(i, j, k));
                            }
                        }

                        ++k;
                    }
                } else if (idx instanceof FloatIndexer) {
                    floatidx = (FloatIndexer) idx;
                    k = 0L;

                    while (true) {
                        if (k >= channels) {
                            done = true;
                            break;
                        }

                        for (i = 0L; i < rows; ++i) {
                            for (j = 0L; j < cols; ++j) {
                                retidx.put(k, i, j, floatidx.get(i, j, k));
                            }
                        }

                        ++k;
                    }
                }

                retidx.release();
            } else if (pointer instanceof DoublePointer) {
                DoubleIndexer retidx = DoubleIndexer.create(pagedPointer.asDoublePointer(), new long[]{channels, rows, cols}, new long[]{stride[0], stride[1], stride[2]}, this.direct);
                if (idx instanceof UByteIndexer) {
                    ubyteidx = (UByteIndexer) idx;
                    k = 0L;

                    while (true) {
                        if (k >= channels) {
                            done = true;
                            break;
                        }

                        for (i = 0L; i < rows; ++i) {
                            for (j = 0L; j < cols; ++j) {
                                retidx.put(k, i, j, (double) ubyteidx.get(i, j, k));
                            }
                        }

                        ++k;
                    }
                } else if (idx instanceof UShortIndexer) {
                    ushortidx = (UShortIndexer) idx;
                    k = 0L;

                    while (true) {
                        if (k >= channels) {
                            done = true;
                            break;
                        }

                        for (i = 0L; i < rows; ++i) {
                            for (j = 0L; j < cols; ++j) {
                                retidx.put(k, i, j, (double) ushortidx.get(i, j, k));
                            }
                        }

                        ++k;
                    }
                } else if (idx instanceof IntIndexer) {
                    intidx = (IntIndexer) idx;
                    k = 0L;

                    while (true) {
                        if (k >= channels) {
                            done = true;
                            break;
                        }

                        for (i = 0L; i < rows; ++i) {
                            for (j = 0L; j < cols; ++j) {
                                retidx.put(k, i, j, (double) intidx.get(i, j, k));
                            }
                        }

                        ++k;
                    }
                } else if (idx instanceof FloatIndexer) {
                    floatidx = (FloatIndexer) idx;
                    k = 0L;

                    while (true) {
                        if (k >= channels) {
                            done = true;
                            break;
                        }

                        for (i = 0L; i < rows; ++i) {
                            for (j = 0L; j < cols; ++j) {
                                retidx.put(k, i, j, (double) floatidx.get(i, j, k));
                            }
                        }

                        ++k;
                    }
                }

                retidx.release();
            }

            if (!done) {
                for (long l = 0L; l < channels; ++l) {
                    for (l = 0L; l < rows; ++l) {
                        for (i = 0L; i < cols; ++i) {
                            if (channels > 1L) {
                                ret.putScalar(l, l, i, idx.getDouble(new long[]{l, i, l}));
                            } else {
                                ret.putScalar(l, i, idx.getDouble(new long[]{l, i}));
                            }
                        }
                    }
                }
            }

            idx.release();
            image.data();
            Nd4j.getAffinityManager().tagLocation(ret, AffinityManager.Location.HOST);
        }
    }

    public INDArray asMatrix(Mat image) throws IOException {
        INDArray ret = this.transformImage((Mat) image, (INDArray) null);
        return ret.reshape(ArrayUtil.combine(new long[][]{{1L}, ret.shape()}));
    }
}
