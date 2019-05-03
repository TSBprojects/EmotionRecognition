package ru.sstu.vak.emotionRecognition.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        primaryStage.getIcons().add(new Image("image/face-ico.png"));
        primaryStage.setTitle("Emotion recognizer");
        primaryStage.setScene(new Scene(root, 931, 490));
        primaryStage.show();
    }

    public static void main(String[] args) {
        log.info("Launching an application...");
        launch(args);
    }
}
