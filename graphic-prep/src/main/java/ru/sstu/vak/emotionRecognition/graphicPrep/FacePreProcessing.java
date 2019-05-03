package ru.sstu.vak.emotionRecognition.graphicPrep;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.awt.image.BufferedImage;

public class FacePreProcessing {

    private static final Logger log = LogManager.getLogger(FacePreProcessing.class.getName());

    private static final double DATA_SET_IMAGE_INDEX = 0.5059679381184801;

    private FacePreProcessing() {
    }

    public static Mat process(Mat face, int width, int height, double smoothIndex, boolean equalizeHist) {
        Mat scaledFace = ImageConverter.resize(face, width, height);
        return process(scaledFace, smoothIndex, equalizeHist);
    }

    public static Mat process(Mat face, int width, int height, boolean equalizeHist) {
        Mat scaledFace = ImageConverter.resize(face, width, height);
        return process(scaledFace, DATA_SET_IMAGE_INDEX, equalizeHist);
    }

    public static Mat process(Mat face, double smoothIndex, boolean equalizeHist) {
        log.debug("Pre-processing Mat face...");
        BufferedImage bfFace = ImageConverter.toBufferedImage(face);
        bfFace = PixelSmoother.smoothImage(bfFace, smoothIndex, equalizeHist);
        return ImageConverter.toMat(bfFace);
    }

    public static Mat process(Mat face, boolean equalizeHist) {
        return process(face, DATA_SET_IMAGE_INDEX, equalizeHist);
    }
}
