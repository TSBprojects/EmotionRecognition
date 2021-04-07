package ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import static ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageCorrector.eqBrightness;

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

        if (face.channels() > 1) {
            ImageCorrector.toGrayScale(face);
        }

        IplImage iplFace = ImageConverter.toIplImage(face);
        eqBrightness(iplFace, iplFace);

        return ImageCorrector.eqHist(ImageConverter.toMat(iplFace));
    }

}
