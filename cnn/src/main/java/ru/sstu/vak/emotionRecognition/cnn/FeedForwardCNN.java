package ru.sstu.vak.emotionRecognition.cnn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.nd4j.linalg.api.ndarray.INDArray;
import ru.sstu.vak.emotionRecognition.common.Emotion;
import ru.sstu.vak.emotionRecognition.graphicPrep.imageProcessing.ImageConverter;

import java.io.File;
import java.io.IOException;

public class FeedForwardCNN {

    private static final Logger log = LogManager.getLogger(FeedForwardCNN.class.getName());

    private ConvNetwork model;

    public static final int INPUT_HEIGHT = 48;
    public static final int INPUT_WIDTH = 48;

    public FeedForwardCNN(String modelPath) throws IOException {
        log.info("Restoring CNN model from file...");
        this.model = (ConvNetwork) MyModelSerializer.restoreMultiLayerNetwork(new File(modelPath));
        log.info("Initialize network...");
        this.model.init();
    }

    public synchronized Emotion predict(Mat face) throws IOException {
        INDArray array = ImageConverter.toNDArray(face);

        log.debug("Predict emotion...");
        int[] predictedClasses = model.predict(array);

        return getEmotion(predictedClasses);
    }

    private Emotion getEmotion(int[] predictedClasses) {
        int maxClass = -1;
        double maxProbability = -1;
        for (int i = 0; i < predictedClasses.length; i++) {
            if (predictedClasses[i] > maxProbability) {
                maxProbability = predictedClasses[i];
                maxClass = i;
            }
        }
        Emotion emotion = Emotion.valueOf(maxClass);
        emotion.setProbability(maxProbability / ConvNetwork.PARSE_FACTOR);

        return emotion;
    }

}