package ru.sstu.vak.emotionrecognition.ui;

import java.net.URL;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sstu.vak.emotionrecognition.ui.gui.MainController;

public class Main extends Application {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL gameControllerUrl = getClass().getResource("/main.fxml");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(gameControllerUrl);
        Parent root = loader.load();
        MainController mainController = loader.getController();
        List<String> parameters = getParameters().getRaw();
        mainController.setParameters(parameters);

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
