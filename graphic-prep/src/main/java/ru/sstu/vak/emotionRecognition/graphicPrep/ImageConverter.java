package ru.sstu.vak.emotionRecognition.graphicPrep;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.nd4j.linalg.api.ndarray.INDArray;
import ru.sstu.vak.emotionRecognition.graphicPrep.imageLoader.NativeImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_imgproc.cvResize;

public class ImageConverter {

//    private static OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
//    private static Java2DFrameConverter frameConverter = new Java2DFrameConverter();

    private static final Logger log = LogManager.getLogger(ImageConverter.class.getName());

    private static NativeImageLoader NDArrayConverter = new NativeImageLoader();

    private static JavaFXFrameConverter javaFXFrameConverter = new JavaFXFrameConverter();

    private ImageConverter() {
    }


    public static BufferedImage copyBufferedImage(BufferedImage bufferedImage) {
        log.debug("Copy buffered image...");
        BufferedImage b = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
        Graphics g = b.getGraphics();
        g.drawImage(bufferedImage, 0, 0, null);
        g.dispose();
        return b;
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


    public static Mat toMat(IplImage image) {
        log.debug("Convert IplImage to Mat");
        return Java2DFrameUtils.toMat(image);
    }

    public static Mat toMat(Frame frame) {
        log.debug("Convert Frame to Mat");
        return Java2DFrameUtils.toMat(frame);
    }

    public static Mat toMat(BufferedImage bufferedImage) {
        log.debug("Convert BufferedImage to Mat");
        return Java2DFrameUtils.toMat(bufferedImage);
    }

    @Deprecated
    public static Mat toMat(Image image) {
        log.error("Convert javafx Image to Mat unsupported");
        throw new UnsupportedOperationException();
    }


    public static Frame toFrame(IplImage image) {
        log.debug("Convert IplImage to Frame");
        return Java2DFrameUtils.toFrame(image);
    }

    public static Frame toFrame(Mat mat) {
        log.debug("Convert Mat to Frame");
        return Java2DFrameUtils.toFrame(mat);
    }

    public static Frame toFrame(BufferedImage bufferedImage) {
        log.debug("Convert BufferedImage to Frame");
        return Java2DFrameUtils.toFrame(bufferedImage);
    }

    @Deprecated
    public static Frame toFrame(Image image) {
        log.error("Convert javafx Image to Frame unsupported");
        return javaFXFrameConverter.convert(image);
    }


    public static IplImage toIplImage(Mat mat) {
        log.debug("Convert Mat to IplImage");
        return Java2DFrameUtils.toIplImage(mat);
    }

    public static IplImage toIplImage(Frame frame) {
        log.debug("Convert Frame to IplImage");
        return Java2DFrameUtils.toIplImage(frame);
    }

    public static IplImage toIplImage(BufferedImage bufferedImage) {
        log.debug("Convert BufferedImage to IplImage");
        return Java2DFrameUtils.toIplImage(bufferedImage);
    }

    @Deprecated
    public static IplImage toIplImage(Image image) {
        log.error("Convert javafx Image to IplImage unsupported");
        throw new UnsupportedOperationException();
    }


    public static BufferedImage toBufferedImage(Mat mat) {
        log.debug("Convert Mat to BufferedImage");
        return Java2DFrameUtils.toBufferedImage(mat);
    }

    public static BufferedImage toBufferedImage(IplImage image) {
        log.debug("Convert IplImage to BufferedImage");
        return Java2DFrameUtils.toBufferedImage(image);
    }

    public static BufferedImage toBufferedImage(Frame frame) {
        log.debug("Convert Frame to BufferedImage");
        return Java2DFrameUtils.toBufferedImage(frame);
    }

    public static BufferedImage toBufferedImage(Image image) {
        log.debug("Convert javafx Image to BufferedImage");
        return SwingFXUtils.fromFXImage(image, null);
    }


    public static Image toJavaFXImage(Mat mat) {
        log.debug("Convert Mat to javafx Image");
        return toJavaFXImage(toBufferedImage(mat));
    }

    public static Image toJavaFXImage(Frame frame) {
        log.debug("Convert Frame to javafx Image");
        return javaFXFrameConverter.convert(frame);
    }

    @Deprecated
    public static Image toJavaFXImage(IplImage image) {
        log.error("Convert IplImage to javafx Image unsupported");
        throw new UnsupportedOperationException();
    }

    public static Image toJavaFXImage(BufferedImage bufferedImage) {
        log.debug("Convert BufferedImage to javafx Image");
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }


    public static INDArray toNDArray(Mat image) throws IOException {
        log.debug("Convert Mat to INDArray");
        return NDArrayConverter.asMatrix(image);
    }

}
