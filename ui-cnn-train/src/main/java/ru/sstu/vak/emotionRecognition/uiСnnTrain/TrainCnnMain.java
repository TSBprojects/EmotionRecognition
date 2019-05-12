package ru.sstu.vak.emotionRecognition.uiСnnTrain;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.bytedeco.javacpp.opencv_core;
import ru.sstu.vak.emotionRecognition.cnnTrain.preProcessing.DataSetPreProcessor;
import ru.sstu.vak.emotionRecognition.graphicPrep.ImageConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;

public class TrainCnnMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        primaryStage.setTitle("Emotion recognizer");
        primaryStage.getIcons().add(new Image("image/face-ico.png"));
        primaryStage.setScene(new Scene(root, 931, 460));
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException {
        launch(args);

//
//        String readFrom = "G:\\Main things\\Study\\DIPLOMA\\My\\datasets\\gray\\emotion\\new_dataset";
//        String writeTo = "C:\\Users\\Антон\\Downloads\\done_sm";
//
//        DataSetPreProcessor.rotateDataSet(
//                Paths.get(readFrom)
//        );
//
//
//        System.exit(0);
    }



}

