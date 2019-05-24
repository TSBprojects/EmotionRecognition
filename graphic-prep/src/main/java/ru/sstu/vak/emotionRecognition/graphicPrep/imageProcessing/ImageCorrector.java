package ru.sstu.vak.emotionRecognition.graphicPrep.imageProcessing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.Frame;
import ru.sstu.vak.emotionRecognition.graphicPrep.iterators.PixelIterator;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.bytedeco.javacpp.opencv_imgproc.*;
import static ru.sstu.vak.emotionRecognition.graphicPrep.imageProcessing.ImageConverter.*;

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

    public static BufferedImage eqBrightness(BufferedImage img) {
        //TODO найти способ выровнять яркость
        return PixelSmoother.smoothImage(img, 0.5);
    }

    @Deprecated
    public static void applyCLAHE(Mat srcArry, Mat dstArry) {
        //Function that applies the CLAHE algorithm to "dstArry".


        // READ RGB color image and convert it to Lab
        Mat channel = new Mat();


        // apply the CLAHE algorithm to the L channel
        CLAHE clahe = createCLAHE();
        clahe.setClipLimit(4);
        clahe.setTilesGridSize(new opencv_core.Size(8, 8));

        clahe.apply(srcArry, dstArry);


//
//        if (srcArry.channels() >= 3) {
//            // READ RGB color image and convert it to Lab
//            Mat channel = new Mat();
//            cvtColor(srcArry, dstArry, COLOR_BGR2Lab);
//
//            // Extract the L channel
//            extractChannel(dstArry, channel, 0);
//
//            // apply the CLAHE algorithm to the L channel
//            CLAHE clahe = createCLAHE();
//            clahe.setClipLimit(4);
//            clahe.apply(channel, channel);
//
//            // Merge the the color planes back into an Lab image
//            insertChannel(channel, dstArry, 0);
//
//            // convert back to RGB
//            cvtColor(dstArry, dstArry, COLOR_Lab2BGR);
//
//            // Temporary Mat not reused, so release from memory.
//            channel.release();
//        }


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
