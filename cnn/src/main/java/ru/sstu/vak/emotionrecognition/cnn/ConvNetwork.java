package ru.sstu.vak.emotionrecognition.cnn;

import java.util.Arrays;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

public class ConvNetwork extends MultiLayerNetwork {

    public final static int PARSE_FACTOR = 10000;

    public ConvNetwork(MultiLayerConfiguration conf) {
        super(conf);
    }

    public ConvNetwork(String conf, INDArray params) {
        super(conf, params);
    }

    public ConvNetwork(MultiLayerConfiguration conf, INDArray params) {
        super(conf, params);
    }

    @Override
    public int[] predict(INDArray d) {
        INDArray output = this.output(d, TrainingMode.TEST);
        return Arrays.stream(output.getRow(0).data().asDouble())
                .mapToInt(operand -> (int) (operand * PARSE_FACTOR))
                .toArray();
    }

}
