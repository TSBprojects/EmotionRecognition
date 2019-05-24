package ru.sstu.vak.emotionRecognition.uiGame.gui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Rect;
import ru.sstu.vak.emotionRecognition.common.Emotion;
import ru.sstu.vak.emotionRecognition.graphicPrep.imageProcessing.ImageConverter;
import ru.sstu.vak.emotionRecognition.graphicPrep.iterators.frameIterator.FrameIterator;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataFace.DataFace;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataFace.impl.VideoFace;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataInfo.impl.FrameInfo;
import ru.sstu.vak.emotionRecognition.uiGame.EmotionRecognizerGame;
import ru.sstu.vak.emotionRecognition.uiGame.GameCore;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.FutureTask;

public class GameController {

    private final static Logger log = LogManager.getLogger(GameController.class.getName());

    private final static String MAIN_HEADER_TEXT = "У вас есть 1 минута чтобы выбить все эмоции!";

    private final static double FACE_MOVE_DURATION = 0.5;

    private final static double IMAGE_HIDDEN_DURATION = 0.5;

    private final static double PROGRESS_BAR_SHOW_DURATION = 0.5;

    private final static double CROPPED_FACES_START_Y = 110;

    private String modelName = "cnnModel.bin";

    private String pathToVid = "0";

    public void setParameters(List<String> parameters) {
        if (parameters.size() == 1) {
            this.pathToVid = parameters.get(0);
        }
        if (parameters.size() == 2) {
            this.pathToVid = parameters.get(0);
            this.modelName = parameters.get(1);
        }
        tryIt(() -> {
            emotionRecognizer = new EmotionRecognizerGame(modelName);
            emotionRecognizer.setOnExceptionListener(e -> Platform.runLater(() -> showError(e.getMessage())));
            gameCore = new GameCore(pathToVid, emotionRecognizer);
        });
    }


    @FXML
    private AnchorPane mainPain;

    @FXML
    private AnchorPane videoImageViewLayout;

    @FXML
    private ImageView videoImageView;

    @FXML
    private ImageView videoPlaceHolder;

    @FXML
    private Text ruleText;

    @FXML
    private AnchorPane playBtn;

    @FXML
    private ImageView cameraImage;

    @FXML
    private ImageView imageOnCamera;

    @FXML
    private ImageView catProgressBar;

    @FXML
    private ImageView stopBtn;

    @FXML
    private AnchorPane happyBackgr;

    @FXML
    private Label happyText;

    @FXML
    private AnchorPane sadBackgr;

    @FXML
    private Label sadText;

    @FXML
    private AnchorPane neutralBackgr;

    @FXML
    private Label neutralText;

    @FXML
    private AnchorPane surpriseBackgr;

    @FXML
    private Label surpriseText;

    @FXML
    private AnchorPane disgustBackgr;

    @FXML
    private Label disgustText;

    @FXML
    private AnchorPane angryBackgr;

    @FXML
    private Label angryText;

    @FXML
    private AnchorPane fearBackgr;

    @FXML
    private Label fearText;

    @FXML
    private Label timeLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label mainHeaderText;

    @FXML
    private ImageView happyFaceImage;

    @FXML
    private ImageView sadFaceImage;

    @FXML
    private ImageView neutralFaceImage;

    @FXML
    private ImageView surpriseFaceImage;

    @FXML
    private ImageView disgustFaceImage;

    @FXML
    private ImageView angryFaceImage;

    @FXML
    private ImageView fearFaceImage;

    @FXML
    private ImageView fireworkImage1;

    @FXML
    private ImageView fireworkImage2;


    private Stage currentStage;


    private EmotionRecognizerGame emotionRecognizer;

    private GameCore gameCore;

    private FadeTransition fadeTransition;

    private boolean clickStop = false;


    private AnimationTimer animationTimer;

    private double prevFaceImageLayoutY = CROPPED_FACES_START_Y;


    @FXML
    public void initialize() {
        log.info("Initialize main components...");
        tryIt(this::initExit);
    }

    @FXML
    void onStartGame(MouseEvent event) {
        if (emotionRecognizer.isRun()) return;
        clickStop = false;

        resetGame();
        showCatProgressBar();
        ruleText.setVisible(true);

        gameCore.start(new GameCore.Callback() {
            @Override
            public void onStart() {
                runLater(() -> {
                    EmotionCell emotionCell = getEmotionCell(gameCore.getAwaitEmotionId());

                    emotionCell.emotionBackgr.setStyle("-fx-background-color: yellow;");

                    showStopBtn();
                    hideVideoPlaceholder();
                    catProgressBar.setVisible(false);
                    catProgressBar.setOpacity(0);
                    ruleText.setVisible(false);
                });
            }

            @Override
            public void onFrameProcessed(BufferedImage processedImage) {
                runLater(() -> videoImageView.setImage(ImageConverter.toJavaFXImage(processedImage)));
            }

            @Override
            public void onGameTick(int tick) {
                runLater(() -> timeLabel.setText(Integer.toString(tick)));
            }

            @Override
            public void onCorrectEmotion(Emotion emotion) {
                startTransition(1, 0, 0.5, true, getEmotionCell(emotion.getEmotionId()).emotionName);
            }

            @Override
            public void onEmotionAchieved(FrameInfo frameInfo) {
                runLater(() -> {
                    stopTransition(-1);

                    int emId = frameInfo.getVideoFaces().get(0).getEmotion().getEmotionId();

                    getEmotionCell(emId).emotionBackgr
                            .setStyle("-fx-background-color: #00FF26;");

                    if (emId < 6) {
                        emId++;
                        getEmotionCell(emId).emotionBackgr
                                .setStyle("-fx-background-color: yellow;");
                    }

                    faceImageFly(frameInfo);
                });
            }

            @Override
            public void onEmotionFailed() {
                EmotionCell emotionCell = getEmotionCell(gameCore.getAwaitEmotionId());

                double duration = 0.1;

                stopTransition(0.5);
                startTransition(1, 0, duration, true, emotionCell.emotionName);
                runLater(() -> emotionCell.emotionName.setStyle("-fx-text-fill: red"));

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runLater(() -> {
                            stopTransition(duration);
                            emotionCell.emotionName.setStyle("-fx-text-fill: black");
                        });
                    }
                }, 600);

            }

            @Override
            public void onGameOver() {
                final int emId = gameCore.getAwaitEmotionId();
                runLater(() -> {
                    if (!clickStop) {
                        resetVideoView();
                        String message;
                        String style;
                        if (emId == 7) {
                            message = "Вы выиграли!";
                            style = "-fx-text-fill: #2DD81E; -fx-alignment: center;";
                            fireworkImage1.setVisible(true);
                            fireworkImage2.setVisible(true);
                        } else {
                            message = "Время вышло!";
                            style = "-fx-text-fill: red; -fx-alignment: center;";
                        }
                        mainHeaderText.setText(message);
                        mainHeaderText.setStyle(style);
                        startTransition(1, 0, 0.4, true, mainHeaderText);
                    } else {
                        resetGame();
                    }
                });
            }
        });
    }

    @FXML
    void onStopGame(MouseEvent event) {
        tryIt(() -> {
            clickStop = true;
            if (gameCore.isRun()) {
                gameCore.stop();
            }
        });
    }


    private void showCatProgressBar() {

        catProgressBar.setOpacity(0);
        catProgressBar.setVisible(true);

        FadeTransition catProgressBarFadeTransition =
                createDisposableFaceTransition(PROGRESS_BAR_SHOW_DURATION, 0, 1, catProgressBar, null);

        FadeTransition cameraImageFadeTransition =
                createDisposableFaceTransition(
                        PROGRESS_BAR_SHOW_DURATION, 1, 0,
                        cameraImage, event -> cameraImage.setVisible(false)
                );

        FadeTransition imageOnCameraFadeTransition =
                createDisposableFaceTransition(
                        PROGRESS_BAR_SHOW_DURATION, 1, 0,
                        imageOnCamera, event -> imageOnCamera.setVisible(false)
                );

        cameraImageFadeTransition.play();
        imageOnCameraFadeTransition.play();
        catProgressBarFadeTransition.play();
    }

    private void hideVideoPlaceholder() {

        FadeTransition cameraImageFadeTransition =
                createDisposableFaceTransition(
                        PROGRESS_BAR_SHOW_DURATION, 1, 0,
                        videoPlaceHolder, null
                );
        cameraImageFadeTransition.play();
    }

    private void hideStopBtn() {

        DoubleProperty imageX = new SimpleDoubleProperty(stopBtn.getLayoutX());
        DoubleProperty imageY = new SimpleDoubleProperty(stopBtn.getLayoutY());
        DoubleProperty imageRotate = new SimpleDoubleProperty(0);
        DoubleProperty imageWidth = new SimpleDoubleProperty(stopBtn.getFitWidth());
        DoubleProperty imageHeight = new SimpleDoubleProperty(stopBtn.getFitHeight());

        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                stopBtn.setLayoutX(imageX.get());
                stopBtn.setLayoutY(imageY.get());
                stopBtn.setRotate(imageRotate.get());
                stopBtn.setFitWidth(imageWidth.get());
                stopBtn.setFitHeight(imageHeight.get());
            }
        };

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(IMAGE_HIDDEN_DURATION),
                        new KeyValue(imageX, stopBtn.getLayoutX() + stopBtn.getFitWidth() / 2)
                ),
                new KeyFrame(Duration.seconds(IMAGE_HIDDEN_DURATION),
                        new KeyValue(imageY, stopBtn.getLayoutY() + stopBtn.getFitHeight() / 2)
                ),
                new KeyFrame(Duration.seconds(IMAGE_HIDDEN_DURATION),
                        new KeyValue(imageRotate, 270)
                ),
                new KeyFrame(Duration.seconds(IMAGE_HIDDEN_DURATION),
                        new KeyValue(imageWidth, 0)
                ),
                new KeyFrame(Duration.seconds(IMAGE_HIDDEN_DURATION),
                        new KeyValue(imageHeight, 0)
                )
        );
        timeline.setOnFinished(event -> {
            stopBtn.setVisible(false);
            animationTimer.stop();
        });
        animationTimer.start();
        timeline.play();
    }

    private void showStopBtn() {
        final double x = 727;
        final double y = 24;
        final double w = 65;
        final double h = 65;

        stopBtn.setVisible(true);

        DoubleProperty imageX = new SimpleDoubleProperty(stopBtn.getLayoutX());
        DoubleProperty imageY = new SimpleDoubleProperty(stopBtn.getLayoutY());
        DoubleProperty imageRotate = new SimpleDoubleProperty(stopBtn.getRotate());
        DoubleProperty imageWidth = new SimpleDoubleProperty(stopBtn.getFitWidth());
        DoubleProperty imageHeight = new SimpleDoubleProperty(stopBtn.getFitHeight());

        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                stopBtn.setLayoutX(imageX.get());
                stopBtn.setLayoutY(imageY.get());
                stopBtn.setRotate(imageRotate.get());
                stopBtn.setFitWidth(imageWidth.get());
                stopBtn.setFitHeight(imageHeight.get());
            }
        };

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(IMAGE_HIDDEN_DURATION),
                        new KeyValue(imageX, stopBtn.getLayoutX() - w / 2)
                ),
                new KeyFrame(Duration.seconds(IMAGE_HIDDEN_DURATION),
                        new KeyValue(imageY, stopBtn.getLayoutY() - h / 2)
                ),
                new KeyFrame(Duration.seconds(IMAGE_HIDDEN_DURATION),
                        new KeyValue(imageRotate, 0)
                ),
                new KeyFrame(Duration.seconds(IMAGE_HIDDEN_DURATION),
                        new KeyValue(imageWidth, w)
                ),
                new KeyFrame(Duration.seconds(IMAGE_HIDDEN_DURATION),
                        new KeyValue(imageHeight, h)
                )
        );
        timeline.setOnFinished(event -> animationTimer.stop());
        animationTimer.start();
        timeline.play();
    }

    private void faceImageFly(FrameInfo frameInfo) {
        VideoFace videoFace = frameInfo.getVideoFaces().get(0);
        DataFace.Location faceLocation = videoFace.getLocation();
        Emotion faceEmotion = videoFace.getEmotion();
        Image faceImage = ImageConverter.toJavaFXImage(
                ImageConverter.toMat(frameInfo.getProcessedImage())
                        .apply(new Rect(faceLocation.x, faceLocation.y, faceLocation.width, faceLocation.height))
        );

        final double currentWidth = FrameIterator.FRAME_WIDTH;
        final double currentHeight = FrameIterator.FRAME_HEIGHT;
        final double originWidth = videoImageView.getFitWidth();
        final double originHeight = videoImageView.getFitHeight();

        double faceX = videoImageViewLayout.getLayoutX() + 10 + calcDimension(faceLocation.x, currentWidth, originWidth);
        double faceY = videoImageViewLayout.getLayoutY() + 10 + calcDimension(faceLocation.y, currentHeight, originHeight);

        ImageView faceImageView = getEmotionCell(faceEmotion.getEmotionId()).faceImageView;
        faceImageView.setVisible(true);
        faceImageView.setFitWidth(calcDimension(faceLocation.width, currentWidth, originWidth));
        faceImageView.setFitHeight(calcDimension(faceLocation.height, currentHeight, originHeight));
        faceImageView.setLayoutX(faceX);
        faceImageView.setLayoutY(faceY);
        faceImageView.setImage(faceImage);

        DoubleProperty facePropertyX = new SimpleDoubleProperty(faceX);
        DoubleProperty facePropertyY = new SimpleDoubleProperty(faceY);
        DoubleProperty facePropertyRotate = new SimpleDoubleProperty(0);
        DoubleProperty facePropertyWidth = new SimpleDoubleProperty(faceImageView.getFitWidth());
        DoubleProperty facePropertyHeight = new SimpleDoubleProperty(faceImageView.getFitHeight());

        if (animationTimer != null) {
            animationTimer.stop();
        }
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                faceImageView.setLayoutX(facePropertyX.doubleValue());
                faceImageView.setLayoutY(facePropertyY.doubleValue());
                faceImageView.setRotate(facePropertyRotate.doubleValue());
                faceImageView.setFitWidth(facePropertyWidth.doubleValue());
                faceImageView.setFitHeight(facePropertyHeight.doubleValue());
            }
        };

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(FACE_MOVE_DURATION),
                        new KeyValue(facePropertyX, 1174)
                ),
                new KeyFrame(Duration.seconds(FACE_MOVE_DURATION),
                        new KeyValue(facePropertyY, prevFaceImageLayoutY)
                ),
                new KeyFrame(Duration.seconds(FACE_MOVE_DURATION),
                        new KeyValue(facePropertyRotate, -26)
                ),
                new KeyFrame(Duration.seconds(FACE_MOVE_DURATION),
                        new KeyValue(facePropertyWidth, 80)
                ),
                new KeyFrame(Duration.seconds(FACE_MOVE_DURATION),
                        new KeyValue(facePropertyHeight, 80)
                )
        );
        timeline.setOnFinished(event -> prevFaceImageLayoutY += 107);

        animationTimer.start();
        timeline.play();

    }

    private double calcDimension(double value, double currentDim, double originDim) {
        return value / currentDim * originDim;
    }


    private void startTransition(
            double from, double to, double duration, boolean loop, Node node
    ) {
        fadeTransition = new FadeTransition(Duration.seconds(duration), node);
        fadeTransition.setFromValue(from);
        fadeTransition.setToValue(to);
        fadeTransition.setCycleCount(2);
        if (loop) {
            fadeTransition.setAutoReverse(true);
            fadeTransition.setOnFinished((ActionEvent event) -> fadeTransition.play());
        } else {
            fadeTransition.setAutoReverse(false);
            fadeTransition.setOnFinished(null);
        }
        fadeTransition.play();
    }

    private FadeTransition createDisposableFaceTransition(
            double duration, double from, double to, Node node, EventHandler<ActionEvent> value
    ) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(duration), node);
        fadeTransition.setFromValue(from);
        fadeTransition.setToValue(to);
        fadeTransition.setCycleCount(1);
        fadeTransition.setOnFinished(value);
        return fadeTransition;
    }


    private void stopTransition(double startDuration) {
        if (fadeTransition != null && (fadeTransition.getDuration().toSeconds() == startDuration || startDuration == -1)) {
            fadeTransition.setOnFinished(null);
        }
    }


    private void resetGame() {
        resetVideoView();
        fireworkImage1.setVisible(false);
        fireworkImage2.setVisible(false);
        catProgressBar.setVisible(false);
        catProgressBar.setOpacity(0);
        mainHeaderText.setStyle("-fx-text-fill: black; -fx-alignment: center;");
        mainHeaderText.setText(MAIN_HEADER_TEXT);
        timeLabel.setText("...");
        for (int i = 0; i < 7; i++) {
            EmotionCell emotionCell = getEmotionCell(i);
            emotionCell.emotionBackgr.setStyle("-fx-background-color: E5E5E5;");
            emotionCell.faceImageView.setImage(null);
            emotionCell.faceImageView.setVisible(false);
        }
        prevFaceImageLayoutY = CROPPED_FACES_START_Y;
    }

    public void resetVideoView() {
        videoPlaceHolder.setOpacity(1);
        cameraImage.setVisible(true);
        cameraImage.setOpacity(1);
        imageOnCamera.setVisible(true);
        imageOnCamera.setOpacity(1);
        hideStopBtn();
        stopTransition(-1);
    }


    private EmotionCell getEmotionCell(int id) {
        switch (id) {
            case 5:
                return new EmotionCell(angryBackgr, angryText, angryFaceImage);
            case 4:
                return new EmotionCell(disgustBackgr, disgustText, disgustFaceImage);
            case 6:
                return new EmotionCell(fearBackgr, fearText, fearFaceImage);
            case 0:
                return new EmotionCell(happyBackgr, happyText, happyFaceImage);
            case 1:
                return new EmotionCell(sadBackgr, sadText, sadFaceImage);
            case 3:
                return new EmotionCell(surpriseBackgr, surpriseText, surpriseFaceImage);
            case 2:
                return new EmotionCell(neutralBackgr, neutralText, neutralFaceImage);

            default:
                throw new UnsupportedOperationException("Unknown or not supported emotion with id: " + id);
        }
    }

    private class EmotionCell {
        public AnchorPane emotionBackgr;
        public Label emotionName;
        public ImageView faceImageView;

        public EmotionCell(AnchorPane emotionBackgr, Label emotionName, ImageView faceImageView) {
            this.emotionBackgr = emotionBackgr;
            this.emotionName = emotionName;
            this.faceImageView = faceImageView;
        }
    }


    private void initExit() {
        Platform.runLater(() -> {
            tryIt(() -> {
                currentStage = (Stage) mainPain.getScene().getWindow();
                currentStage.setOnCloseRequest(event -> {
                    if (emotionRecognizer != null && emotionRecognizer.isRun()) {
                        emotionRecognizer.setOnStopListener(videoInfo -> {
                            System.exit(0);
                        });
                        emotionRecognizer.stop();
                    } else {
                        System.exit(0);
                    }
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
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/image/face-ico.png"));
        alert.showAndWait();
    }

    private static boolean showConfirm(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/image/face-ico.png"));

        Optional<ButtonType> option = alert.showAndWait();

        if (option.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }

    private void runLater(RunLaterCode runLaterCode) {
        tryIt(() -> {
            final FutureTask<Object> task = new FutureTask<>(() -> {
                runLaterCode.exec();
                return null;
            });
            Platform.runLater(task);
            task.get();
        });
    }

    private interface RunLaterCode {
        void exec();
    }

    private void tryIt(TryItCallback tryCode) {
        try {
            tryCode.executableCode();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Platform.runLater(() -> {
                showError(e.getMessage());
            });
            if (emotionRecognizer != null) {
                emotionRecognizer.stop();
            }
        }
    }

    private interface TryItCallback {
        void executableCode() throws Exception;
    }

}

