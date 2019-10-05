package ru.sstu.vak.emotionRecognition.ui.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.FrameGrabber;
import org.springframework.web.client.RestTemplate;
import ru.sstu.vak.emotionRecognition.common.Emotion;
import ru.sstu.vak.emotionRecognition.graphicPrep.imageProcessing.ImageConverter;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataFace.impl.VideoFace;
import ru.sstu.vak.emotionRecognition.identifyEmotion.emotionRecognizer.EmotionRecognizer;
import ru.sstu.vak.emotionRecognition.identifyEmotion.emotionRecognizer.impl.EmotionRecognizerGame;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {

    private static final Logger log = LogManager.getLogger(MainController.class.getName());

    private final static Image VIDEO_PLACE_HOLDER = new Image("image/videoPlaceHolder.png");

    private final static Image VIDEO_PLACE_HOLDER_FOR_FACE = new Image("image/videoPlaceHolderForFace.png");

    private final static String SERVER_API = "http://antonv-pc.myddns.me:8090/emotionStatus";

    @FXML
    private ImageView videoImageView;

    @FXML
    private Button startRcognVideoBtn;

    @FXML
    private Button stopVideoBtn;

    @FXML
    private ProgressBar startVideoProgressBar;

    @FXML
    private TextField videoPath;

    @FXML
    private TextField serverAddressPath;


    private Timer requestTimer;

    TimerTask timerTask;

    private Emotion currentEmotion;

    private Stage currentStage;


    private EmotionRecognizer emotionRecognizer;

    private RestTemplate restTemplate;


    private String modelName = "cnnModel.bin";

    public void setParameters(List<String> parameters) {
        if (parameters.size() == 1) {
            this.modelName = parameters.get(0);
        }
        tryIt(() -> {
            emotionRecognizer = new EmotionRecognizerGame(modelName);
            emotionRecognizer.setOnStopListener(videoInfo -> {
                onStopAction();
            });

            emotionRecognizer.setOnExceptionListener(e -> {
                Platform.runLater(() -> {
                    showError(e.getMessage());
                });
            });
        });
    }

    private void onStopAction() {
        startVidProgressBarOff();
        videoImageView.setImage(VIDEO_PLACE_HOLDER);
    }


    @FXML
    public void initialize() {
        log.info("Initialize main components...");
        restTemplate = new RestTemplate();

        initExit();
    }

    @FXML
    void startRecognVideo(ActionEvent event) {
        if (emotionRecognizer.isRun()) return;

        requestTimer = new Timer(false);
        requestTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentEmotion != null) {
                    restTemplate.postForObject(
                            SERVER_API,
                            currentEmotion.getStatus(),
                            Boolean.class
                    );
                }
            }
        }, 0, 3 * 1000);

        tryIt(() -> {
            startVidProgressBarOn();
            String videoPath = this.videoPath.getText();
            emotionRecognizer.processVideo(videoPath, frameInfo -> {
                startVidProgressBarOff();
                List<VideoFace> videoFaces = frameInfo.getVideoFaces();
                if (videoFaces.size() > 0) {
                    currentEmotion = frameInfo.getVideoFaces().get(0).getEmotion();
                } else {
                    currentEmotion = null;
                }
                Image image = ImageConverter.toJavaFXImage(frameInfo.getProcessedImage());
                videoImageView.setImage(image);
            });
        });
    }

    @FXML
    void stopVideo(ActionEvent event) throws FrameGrabber.Exception {
        tryIt(() -> {
            if (emotionRecognizer.isRun()) {
                startVidProgressBarOn();
                currentEmotion = null;
                requestTimer.cancel();
                emotionRecognizer.stop();
            }
        });
    }

    private void startVidProgressBarOn() {
        if (!startVideoProgressBar.isVisible()) {
            startRcognVideoBtn.setVisible(false);
            stopVideoBtn.setVisible(false);
            startVideoProgressBar.setVisible(true);
        }
    }

    private void startVidProgressBarOff() {
        if (startVideoProgressBar.isVisible()) {
            startRcognVideoBtn.setVisible(true);
            stopVideoBtn.setVisible(true);
            startVideoProgressBar.setVisible(false);
        }
    }


    private void initExit() {
        Platform.runLater(() -> {
            tryIt(() -> {
                currentStage = (Stage) videoImageView.getScene().getWindow();
                currentStage.setOnCloseRequest(event -> {
                    if (emotionRecognizer != null && emotionRecognizer.isRun()) {
                        emotionRecognizer.stop();
                        while (emotionRecognizer.isRun()) ;
                    }
                    System.exit(0);
                });
            });
        });
    }


    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message + " \nSee logs: 'logs.log'");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/image/face-ico.png"));
        alert.showAndWait();
    }

    private static void showMessage(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void tryIt(TryItCallback tryCode) {
        try {
            tryCode.executableCode();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Platform.runLater(() -> showError(e.getMessage()));
            if (emotionRecognizer != null) {
                emotionRecognizer.stop();
            }
        }
    }

    private interface TryItCallback {
        void executableCode() throws Exception;
    }
}

