package ru.sstu.vak.emotionRecognition.graphicPrep.imageProcessing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Mat;

import static ru.sstu.vak.emotionRecognition.graphicPrep.imageProcessing.ImageCorrector.eqBrightness;

public class FacePreProcessing {

    private static final Logger log = LogManager.getLogger(FacePreProcessing.class.getName());

    private FacePreProcessing() {
    }


    public static Mat process(Mat face, int width, int height) {
        Mat scaledFace = ImageCorrector.resize(face, width, height);
        return process(scaledFace);
    }

    public static Mat process(Mat face) {
        log.debug("Pre-processing Mat face...");
        return ImageConverter.toMat(eqBrightness(ImageConverter.toBufferedImage(face)));
    }

}
