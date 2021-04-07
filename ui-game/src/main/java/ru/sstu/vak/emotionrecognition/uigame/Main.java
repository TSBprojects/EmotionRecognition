package ru.sstu.vak.emotionrecognition.uigame;

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
import ru.sstu.vak.emotionrecognition.uigame.gui.GameController;

public class Main extends Application {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL gameControllerUrl = getClass().getResource("/main.fxml");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(gameControllerUrl);
        Parent root = loader.load();
        GameController gameController = loader.getController();
        List<String> parameters = getParameters().getRaw();
        gameController.setParameters(parameters);

        primaryStage.getIcons().add(new Image("image/face-ico.png"));
        primaryStage.setTitle("Игра");
        primaryStage.setScene(new Scene(root, 1290, 914));
        primaryStage.show();
    }

    public static void main(String[] args) {
        log.info("Launching an application...");
        launch(args);
    }
}
