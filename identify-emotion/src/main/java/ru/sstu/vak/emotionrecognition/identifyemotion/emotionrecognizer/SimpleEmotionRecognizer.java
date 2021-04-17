package ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import ru.sstu.vak.emotionrecognition.cnn.FeedForwardCNN;
import static ru.sstu.vak.emotionrecognition.cnn.FeedForwardCNN.INPUT_HEIGHT;
import static ru.sstu.vak.emotionrecognition.cnn.FeedForwardCNN.INPUT_WIDTH;
import ru.sstu.vak.emotionrecognition.common.Prediction;
import ru.sstu.vak.emotionrecognition.facedetector.BoundingBox;
import ru.sstu.vak.emotionrecognition.facedetector.HaarFaceDetector;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.FacePreProcessing;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageConverter;
import ru.sstu.vak.emotionrecognition.graphicprep.iterators.frameiterator.FrameIterator;
import ru.sstu.vak.emotionrecognition.graphicprep.iterators.frameiterator.impl.FrameIteratorBase;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.ImageFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.MediaFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.FrameInfo;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.ImageInfo;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoInfo;

public class SimpleEmotionRecognizer implements EmotionRecognizer {

    private static final Logger log = LogManager.getLogger(SimpleEmotionRecognizer.class.getName());

    protected static final String VIDEO_INFO_POSTFIX = "-videoInfo";
    protected static final String IMAGE_INFO_POSTFIX = "-imageInfo";
    protected static final String PROCESSED_IMAGE_POSTFIX = "-processed";
    protected static final String PROCESSED_IMAGE_FACE_POSTFIX = "-face";

    private final ObjectMapper toJson;
    private FileOutputStream fileOutputStream;

    protected FrameIterator frameIterator;
    protected HaarFaceDetector haarFaceDetector;
    protected FeedForwardCNN feedForwardCNN;

    protected StopListener stopListener;
    protected NetInputListener videoNetInputListener;
    protected NetInputListener imageNetInputListener;
    protected FrameIterator.FrameListener frameListener;
    protected FrameIterator.ExceptionListener onExceptionListener;

    protected List<VideoFrame> frames;


    public SimpleEmotionRecognizer(String modelPath) throws IOException {
        this.frameIterator = new FrameIteratorBase();
        this.haarFaceDetector = new HaarFaceDetector();
        this.feedForwardCNN = new FeedForwardCNN(modelPath);
        this.frames = new ArrayList<>();
        this.toJson = new ObjectMapper();

        this.frameIterator.setDeviceFrameRate(null);
        this.frameIterator.setFileFrameRate(30);
        this.frameIterator.setOnStopListener(() -> {
            if (stopListener != null) {
                stopListener.onVideoStopped(new VideoInfo(frames));
            }
            frames.clear();
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.write("]}".getBytes());
                    fileOutputStream.close();
                    fileOutputStream = null;
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throwException(e);
            }
        });
    }

    @Override
    public synchronized ImageInfo processImage(BufferedImage image) {
        log.info("Finding faces and recognize emotions on them in the image...");
        List<ImageFace> imageFaceList = new ArrayList<>();

        Mat matImage = ImageConverter.toMat(image);
        Map<Rect, Mat> faces = haarFaceDetector.detect(matImage, false);
        faces.forEach((rect, face) -> {
            try {
                BufferedImage faceImage = ImageConverter.toBufferedImage(matImage.apply(rect));
                MediaFace.Location faceLocation = new MediaFace.Location(rect.x(), rect.y(), rect.width(), rect.height());
                Mat preparedFace = FacePreProcessing.process(matImage.apply(rect), INPUT_WIDTH, INPUT_HEIGHT);
                if (imageNetInputListener != null) {
                    imageNetInputListener.onNextFace(preparedFace.clone());
                }
                Prediction predict = feedForwardCNN.predict(preparedFace);
                BoundingBox.draw(image, rect, predict);
                imageFaceList.add(new ImageFace(predict, faceLocation, faceImage));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throwException(e);
            }
        });
        return new ImageInfo(image, imageFaceList);
    }

    @Override
    public synchronized void processVideo(String readFrom, ProcessedFrameListener listener) throws FrameGrabber.Exception {
        log.info("Starting video with emotion recognition...");
        frameIterator.start(readFrom, frame -> listener.onNextFrame(new FrameInfo(processedFrame(frame))));
    }

    @Override
    public synchronized void processVideo(String readFrom, Path writeTo, ProcessedFrameListener listener) throws FrameGrabber.Exception {
        log.info("Starting video with emotion recognition...");
        initOutputStream(writeTo);
        frameIterator.start(readFrom, writeTo, frame -> {
            FrameInfo frameInfo = processedFrame(frame);
            Frame procFrame = ImageConverter.toFrame(frameInfo.getProcessedImage());
            listener.onNextFrame(new FrameInfo(frameInfo));
            return procFrame;
        });
    }

    @Override
    public void writeVideoInfo(VideoInfo videoInfo, Path writeTo) throws IOException {
        log.info("Write video info...");

        final String fileName = FilenameUtils.removeExtension(writeTo.getFileName().toString());
        toJson.writeValue(
                new File(writeTo.getParent() + "\\" + fileName + VIDEO_INFO_POSTFIX + ".json"),
                videoInfo
        );
    }

    @Override
    public void writeImageInfo(ImageInfo imageInfo, Path writeTo, boolean saveFaces) throws IOException {
        log.info("Write image info...");

        final String fileName = FilenameUtils.removeExtension(writeTo.getFileName().toString());

        if (saveFaces) {
            List<ImageFace> imageFaces = imageInfo.getImageFaces();
            for (int i = 0; i < imageFaces.size(); i++) {
                ImageFace imageFace = imageFaces.get(i);
                String emotionMame = imageFace.getPrediction().getEmotion().getName();
                ImageIO.write(
                        imageFace.getFaceImage(),
                        "png",
                        new File(writeTo.getParent() + "\\" + fileName
                                + PROCESSED_IMAGE_FACE_POSTFIX + i + "-" + emotionMame + ".png")
                );
            }
        }
        ImageIO.write(
                imageInfo.getProcessedImage(),
                "png",
                new File(writeTo.getParent() + "\\" + fileName + PROCESSED_IMAGE_POSTFIX + ".png")
        );
        toJson.writeValue(
                new File(writeTo.getParent() + "\\" + fileName + IMAGE_INFO_POSTFIX + ".json"),
                imageInfo
        );
    }

    @Override
    public boolean isRun() {
        return frameIterator.isRun();
    }

    @Override
    public void stop() {
        this.frameIterator.stop();
    }


    public void setOnExceptionListener(FrameIterator.ExceptionListener exceptionListener) {
        this.onExceptionListener = exceptionListener;
        this.frameIterator.setOnExceptionListener(onExceptionListener);
    }

    @Override
    public void setOnStopListener(StopListener stopListener) {
        this.stopListener = stopListener;
    }

    @Override
    public void setFrameListener(FrameIterator.FrameListener frameListener) {
        this.frameListener = frameListener;
    }

    @Override
    public void setImageNetInputListener(NetInputListener imageNetInputListener) {
        this.imageNetInputListener = imageNetInputListener;
    }

    @Override
    public void setVideoNetInputListener(NetInputListener videoNetInputListener) {
        this.videoNetInputListener = videoNetInputListener;
    }


    protected FrameInfo processedFrame(Frame frame) {
        if (frameListener != null) {
            frameListener.onNextFrame(frame);
        }
        List<VideoFace> videoFacesList = new ArrayList<>();

        Mat matImage = ImageConverter.toMat(frame);
        BufferedImage image = ImageConverter.toBufferedImage(frame);
        Map<Rect, Mat> faces = haarFaceDetector.detect(matImage, false);

        faces.forEach((rect, face) -> {
            try {
                MediaFace.Location faceLocation = new MediaFace.Location(rect.x(), rect.y(), rect.width(), rect.height());
                Mat preparedFace = FacePreProcessing.process(matImage.apply(rect), INPUT_WIDTH, INPUT_HEIGHT);
                if (videoNetInputListener != null) {
                    videoNetInputListener.onNextFace(preparedFace.clone());
                }
                Prediction prediction = feedForwardCNN.predict(preparedFace);
                BoundingBox.draw(image, rect, prediction);
                videoFacesList.add(new VideoFace(prediction, faceLocation));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throwException(e);
            }
        });

        VideoFrame videoFrame = new VideoFrame(frames.size(), videoFacesList);
        writeVideoFrame(videoFrame);
        frames.add(videoFrame);
        return new FrameInfo(frames.size(), image, videoFacesList);
    }

    private void initOutputStream(Path writeTo) {
        try {
            final String fileName = FilenameUtils.removeExtension(writeTo.getFileName().toString());
            final Path jsonFile = Paths.get(writeTo.getParent() + "\\" + fileName + "-videoInfo.json");
            Files.deleteIfExists(jsonFile);
            Files.createFile(jsonFile);
            fileOutputStream = new FileOutputStream(jsonFile.toFile());
            fileOutputStream.write("{\"frames\":[".getBytes());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throwException(e);
        }
    }

    private void writeVideoFrame(VideoFrame videoFrame) {
        try {
            if (fileOutputStream != null) {
                byte[] frame = toJson.writeValueAsBytes(videoFrame);
                byte[] coma = ",".getBytes();
                byte[] res = ArrayUtils.addAll(frame, coma);
                fileOutputStream.write(res);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throwException(e);
        }
    }


    protected void throwException(Throwable e) {
        if (onExceptionListener != null) {
            onExceptionListener.onException(e);
        }
    }
}
