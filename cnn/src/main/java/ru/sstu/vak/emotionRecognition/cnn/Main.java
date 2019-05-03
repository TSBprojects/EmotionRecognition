package ru.sstu.vak.emotionRecognition.cnn;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        MyModelSerializer.restoreMultiLayerNetwork(new File("G:\\Main things\\Study\\DIPLOMA\\My\\EmotionRecognition\\cnnModel.bin"));

        System.out.println("CNN module");
    }
}
