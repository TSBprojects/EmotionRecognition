package ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.CV_MINMAX;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_32F;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.cvConvert;
import static org.bytedeco.javacpp.opencv_core.cvConvertScale;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvExp;
import static org.bytedeco.javacpp.opencv_core.cvLog;
import static org.bytedeco.javacpp.opencv_core.cvNormalize;
import static org.bytedeco.javacpp.opencv_core.cvSub;
import static org.bytedeco.javacpp.opencv_core.cvZero;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGRA2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_GAUSSIAN;
import static org.bytedeco.javacpp.opencv_imgproc.cvResize;
import static org.bytedeco.javacpp.opencv_imgproc.cvSmooth;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import org.bytedeco.javacv.Frame;
import static ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageConverter.toFrame;
import static ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageConverter.toIplImage;
import static ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageConverter.toMat;
import ru.sstu.vak.emotionrecognition.graphicprep.iterators.PixelIterator;

public class ImageCorrector {

    private static final Logger log = LogManager.getLogger(ImageCorrector.class.getName());

    private ImageCorrector() {
    }


    public static BufferedImage toGrayScale(BufferedImage image) {
        log.debug("Convert BufferedImage image to grayscale format...");
        Mat img = toMat(image);
        cvtColor(img, img, COLOR_BGRA2GRAY);
        return ImageConverter.toBufferedImage(img);
    }

    public static Mat toGrayScale(Mat img) {
        log.debug("Convert Mat image to grayscale format...");
        cvtColor(img, img, COLOR_BGRA2GRAY);
        return img;
    }


    public static BufferedImage eqHist(BufferedImage image) {
        log.debug("Apply equalize histogram to BufferedImage image...");
        Mat img = toMat(image);
        equalizeHist(img, img);
        return ImageConverter.toBufferedImage(img);
    }

    public static Mat eqHist(Mat img) {
        log.debug("Apply equalize histogram to Mat image...");
        equalizeHist(img, img);
        return img;
    }


    /**
     * Implementation of retinex
     */
    public static void eqBrightness(IplImage src, IplImage dst) {

        final double sigma = 5;

        IplImage[] tmpImages = new IplImage[2];
        for (int i = 0; i < tmpImages.length; i++) {
            tmpImages[i] = cvCreateImage(new opencv_core.CvSize(src.width(), src.height()), IPL_DEPTH_32F, src.nChannels());
            cvZero(tmpImages[i]);
        }

        // R = w * ( log(I)-log(gauss(I)) )

        cvConvert(src, tmpImages[0]); // конвертируем данные во float
        cvConvertScale(tmpImages[0], tmpImages[0], 1.0, 1.0); // сдвигаем на 1 для вычисления логарифма
        cvLog(tmpImages[0], tmpImages[0]); // log(I)

        // сглаживаем и конвертируем данные во float
        cvSmooth(src, src, CV_GAUSSIAN, 0, 0, sigma, 0);
        cvConvert(src, tmpImages[1]);
        cvConvertScale(tmpImages[1], tmpImages[1], 1.0, 1.0); // сдвигаем на 1 для вычисления логарифма
        cvLog(tmpImages[1], tmpImages[1]); // log(gauss(I))

        cvSub(tmpImages[0], tmpImages[1], tmpImages[0], null); // корректируем освещение
        cvExp(tmpImages[0], tmpImages[0]);


        cvNormalize(tmpImages[0], tmpImages[0], 0.0, 255.0, CV_MINMAX, null); // растягиваем в [0,255]
        cvConvertScale(tmpImages[0], tmpImages[0], 1, 0.0); // корректируем каналы

        cvConvert(tmpImages[0], dst);
    }

    public static BufferedImage gammaCorrection(BufferedImage img, double gamma) {
        log.debug("Apply gamma correction to BufferedImage image with gamma " + gamma + "...");
        /*
          GCLT - gamma correction lookup table
         */
        int[] gclt = new int[256];

        for (int i = 0; i < gclt.length; i++) {
            gclt[i] = (int) (255 * (Math.pow((double) i / (double) 255, 1 / gamma)));
        }

        return PixelIterator.change(img, (x, y, pixel) -> {
            for (int i = 0; i < pixel.length; i++) {
                pixel[i] = gclt[pixel[i]];
            }
            return pixel;
        });
    }


    public static Frame resize(Frame frame, int width, int height) {
        log.debug("Resizing Frame to {}x{}...", width, height);
        IplImage origImg = toIplImage(frame);
        IplImage resizedImage = IplImage.create(width, height, frame.imageDepth, frame.imageChannels);
        cvResize(origImg, resizedImage);
        return toFrame(resizedImage);
    }

    public static Mat resize(Mat mat, int width, int height) {
        log.debug("Resizing Mat to {}x{}...", width, height);
        IplImage origImg = toIplImage(mat);
        IplImage resizedImage = IplImage.create(width, height, origImg.depth(), origImg.nChannels());
        cvResize(origImg, resizedImage);
        return toMat(resizedImage);
    }


    public static BufferedImage copyBufferedImage(BufferedImage bufferedImage) {
        log.debug("Copy buffered image...");
        BufferedImage b = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
        Graphics g = b.getGraphics();
        g.drawImage(bufferedImage, 0, 0, null);
        g.dispose();
        return b;
    }


}
