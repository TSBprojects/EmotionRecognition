package ru.sstu.vak.emotionRecognition.ui.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import ru.sstu.vak.emotionRecognition.graphicPrep.ImageConverter;
import ru.sstu.vak.emotionRecognition.graphicPrep.frameIterator.FrameIterator;
import ru.sstu.vak.emotionRecognition.graphicPrep.frameIterator.impl.FrameIteratorBase;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataInfo.impl.ImageInfo;
import ru.sstu.vak.emotionRecognition.identifyEmotion.emotionRecognizer.EmotionRecognizer;
import ru.sstu.vak.emotionRecognition.identifyEmotion.emotionRecognizer.impl.EmotionRecognizerBase;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    private static final Logger log = LogManager.getLogger(MainController.class.getName());


    private final static String MODEL_NAME = "bestModel(0.75).bin";

    private final static Image VIDEO_PLACE_HOLDER = new Image("image/videoPlaceHolder.png");

    private final static Image VIDEO_PLACE_HOLDER_FOR_FACE = new Image("image/videoPlaceHolderForFace.png");


    @FXML
    private Button saveImageBtn;

    @FXML
    private ImageView videoImageView;

    @FXML
    private Button startVideoBtn;

    @FXML
    private Button startRcognVideoBtn;

    @FXML
    private Button screenshotBtn;

    @FXML
    private ImageView screenImageView;

    @FXML
    private Button recognScreenBtn;

    @FXML
    private Button stopVideoBtn;

    @FXML
    private ImageView faceFromVideo;

    @FXML
    private ImageView faceFromScreen;

    @FXML
    private TextField imagePath;

    @FXML
    private TextField videoPath;

    @FXML
    private Button browseImageBtn;

    @FXML
    private ProgressBar recognImageProgressBar;

    @FXML
    private ProgressBar startVideoProgressBar;


    private ExecutorService executorService = Executors.newFixedThreadPool(10);


    private Stage currentStage;

    private Frame currentFrame;

    private BufferedImage screenshotFrame;

    private BufferedImage originalScreenShot;

    private ImageInfo imageInfo;


    private EmotionRecognizer emotionRecognizer;

    private FrameIterator frameIterator;

    private void onStopAction() {
        startVidProgressBarOff();
        videoImageView.setImage(VIDEO_PLACE_HOLDER);
        faceFromVideo.setImage(VIDEO_PLACE_HOLDER_FOR_FACE);
        currentFrame = null;
    }

    @FXML
    public void initialize() {
        log.info("Initialize main components...");
        tryIt(() -> {
            initExit();

            frameIterator = new FrameIteratorBase();
            frameIterator.setOnStopListener(this::onStopAction);
            frameIterator.setOnExceptionListener(e -> {
                Platform.runLater(() -> {
                    showError(e.getMessage());
                });
            });

            emotionRecognizer = new EmotionRecognizerBase(MODEL_NAME);
            emotionRecognizer.setOnStopListener(videoInfo -> {
                onStopAction();
            });
            emotionRecognizer.setFrameListener(frame -> {
                currentFrame = frame;
            });
            emotionRecognizer.setImageNetInputListener(face -> {
                Platform.runLater(() -> {
                    faceFromScreen.setImage(ImageConverter.toJavaFXImage(face));
                });
            });
            emotionRecognizer.setVideoNetInputListener(face -> {
                Platform.runLater(() -> {
                    faceFromVideo.setImage(ImageConverter.toJavaFXImage(face));
                });
            });
        });
    }

    @FXML
    void openScreen(ActionEvent event) {
        if (screenshotFrame != null) {
            tryIt(() -> {
                Dimension winSize = Toolkit.getDefaultToolkit().getScreenSize();

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/showImage.fxml"));

                Parent root = null;
                try {
                    root = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ShowImageController progressController = loader.getController();
                progressController.setImage(ImageConverter.toJavaFXImage(screenshotFrame));

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Изображение");
                stage.setScene(new Scene(root, winSize.width - 150, winSize.height - 150));
                stage.setResizable(true);
                stage.getIcons().add(new Image("image/face-ico.png"));
                stage.showAndWait();
            });
        }
    }

    @FXML
    void startRecognVideo(ActionEvent event) {
        if (emotionRecognizer.isRun() || frameIterator.isRun()) return;

        tryIt(() -> {
            boolean ok = showConfirm(
                    "Что делать с данными?",
                    "Сохранить обработанные данные и информацию о них?",
                    "Нажмите ОК для сохранения и Cancel для отмены"
            );
            startVidProgressBarOn();
            if (!ok) {
                String videoPath = this.videoPath.getText();
                emotionRecognizer.processVideo(videoPath, frame -> {
                    startVidProgressBarOff();
                    videoImageView.setImage(ImageConverter.toJavaFXImage(frame));
                });
            } else {
                String videoPath = this.videoPath.getText();
                File videoFile = saveFile("Сохранить медиаданные", "Video", "*.mp4");
                emotionRecognizer.processVideo(videoPath, videoFile.toPath(), frame -> {
                    startVidProgressBarOff();
                    videoImageView.setImage(ImageConverter.toJavaFXImage(frame));
                });
            }
        });
    }

    @FXML
    void startVideo(ActionEvent event) {
        if (emotionRecognizer.isRun() || frameIterator.isRun()) return;
        tryIt(() -> {
            startVidProgressBarOn();
            frameIterator.start(videoPath.getText(), frame -> {
                startVidProgressBarOff();
                currentFrame = frame;
                videoImageView.setImage(ImageConverter.toJavaFXImage(frame));
            });
        });
    }

    @FXML
    void stopVideo(ActionEvent event) throws FrameGrabber.Exception {
        tryIt(() -> {
            if (emotionRecognizer.isRun()) {
                startVidProgressBarOn();
                emotionRecognizer.stop();
            } else if (frameIterator.isRun()) {
                startVidProgressBarOn();
                frameIterator.stop();
            }
        });
    }

    @FXML
    void takeScreenshot(ActionEvent event) {
        if (currentFrame != null) {
            log.info("Take a screenshot...");
            tryIt(() -> {
                screenshotFrame = ImageConverter.toBufferedImage(currentFrame.clone());
                originalScreenShot = ImageConverter.copyBufferedImage(screenshotFrame);
                screenImageView.setImage(ImageConverter.toJavaFXImage(screenshotFrame));
            });
        }
    }

    @FXML
    void recognizeScreenshot(ActionEvent event) {
        if (screenshotFrame != null) {
            recognScreenProgressBarToggle();
            executorService.submit(() -> {
                tryIt(() -> {
                    imageInfo = emotionRecognizer.processImage(ImageConverter.copyBufferedImage(originalScreenShot));
                    screenshotFrame = imageInfo.getProcessedImage();
                    screenImageView.setImage(ImageConverter.toJavaFXImage(screenshotFrame));
                    recognScreenProgressBarToggle();
                });
            });
        }
    }

    @FXML
    void browseImage(ActionEvent event) throws IOException {
        log.info("Browse for image...");

        tryIt(() -> {
            File image = selectFile("Select image for recognize emotions!",
                    "Image", "*.png", "*.jpeg", "*.jpg", "*.bmp"
            );
            if (image != null) {
                screenshotFrame = ImageIO.read(image);
                originalScreenShot = ImageConverter.copyBufferedImage(screenshotFrame);
                screenImageView.setImage(ImageConverter.toJavaFXImage(screenshotFrame));
            }
        });
    }

    @FXML
    void saveScreen(ActionEvent event) throws IOException {
        log.info("Save image...");

        if (screenshotFrame != null) {
            tryIt(() -> {
                File image = saveFile("Saving image",
                        "Image", "*.png", "*.jpg", "*.jpeg", "*.bmp"
                );
                if (image != null) {
                    emotionRecognizer.writeImageInfo(imageInfo, image.toPath(), true);
                }
            });
        }
    }


    private void recognScreenProgressBarToggle() {
        if (recognScreenBtn.isVisible()) {
            recognScreenBtn.setVisible(false);
            recognImageProgressBar.setVisible(true);
        } else {
            recognScreenBtn.setVisible(true);
            recognImageProgressBar.setVisible(false);
        }
    }

    private void startVidProgressBarOn() {
        if (!startVideoProgressBar.isVisible()) {
            startVideoBtn.setVisible(false);
            startRcognVideoBtn.setVisible(false);
            stopVideoBtn.setVisible(false);
            startVideoProgressBar.setVisible(true);
        }
    }

    private void startVidProgressBarOff() {
        if (startVideoProgressBar.isVisible()) {
            startVideoBtn.setVisible(true);
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
                        emotionRecognizer.setOnStopListener(videoInfo -> {
                            System.exit(0);
                        });
                        emotionRecognizer.stop();
                    } else if (frameIterator != null && frameIterator.isRun()) {
                        frameIterator.setOnStopListener(() -> {
                            System.exit(0);
                        });
                        frameIterator.stop();
                    } else {
                        System.exit(0);
                    }
                });
            });
        });
    }


    private File selectFile(String title, String extDesc, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                extDesc,
                extensions
        );
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser.showOpenDialog(null);
    }

    private File selectFolder(String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        return directoryChooser.showDialog(null);
    }

    private File saveFile(String title, String extDesc, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                extDesc,
                extensions
        );
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser.showSaveDialog(null);
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
            if (frameIterator != null) {
                frameIterator.stop();
            }
        }
    }

    private interface TryItCallback {
        void executableCode() throws Exception;
    }
}

