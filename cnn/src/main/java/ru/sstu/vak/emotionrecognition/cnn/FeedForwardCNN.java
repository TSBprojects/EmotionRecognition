package ru.sstu.vak.emotionrecognition.cnn;

import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.nd4j.linalg.api.ndarray.INDArray;
import static ru.sstu.vak.emotionrecognition.cnn.ConvNetwork.PARSE_FACTOR;
import ru.sstu.vak.emotionrecognition.common.Emotion;
import ru.sstu.vak.emotionrecognition.common.Prediction;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageConverter;

public class FeedForwardCNN {

    private static final Logger log = LogManager.getLogger(FeedForwardCNN.class.getName());

    private final ConvNetwork model;

    public static final int INPUT_HEIGHT = 48;
    public static final int INPUT_WIDTH = 48;

    public FeedForwardCNN(String modelPath) throws IOException {
        log.info("Restoring CNN model from file...");
        this.model = (ConvNetwork) MyModelSerializer.restoreMultiLayerNetwork(new File(modelPath));
        log.info("Initialize network...");
        this.model.init();
    }

    public synchronized Prediction predict(Mat face) throws IOException {
        INDArray array = ImageConverter.toNDArray(face);

        log.debug("Predict emotion...");
        int[] predictedClasses = model.predict(array);

        int maxClass = -1;
        double maxProbability = -1;
        for (int i = 0; i < predictedClasses.length; i++) {
            if (predictedClasses[i] > maxProbability) {
                maxProbability = predictedClasses[i];
                maxClass = i;
            }
        }

        return new Prediction(Emotion.of(maxClass), maxProbability / PARSE_FACTOR);
    }
}
