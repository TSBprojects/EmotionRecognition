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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import ru.sstu.vak.emotionRecognition.graphicPrep.ImageConverter;
import ru.sstu.vak.emotionRecognition.identifyEmotion.EmotionRecognizer;
import ru.sstu.vak.emotionRecognition.identifyEmotion.image.ImageInfo;
import ru.sstu.vak.emotionRecognition.identifyEmotion.video.VideoInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    private static final Logger log = LogManager.getLogger(MainController.class.getName());


    private final static String MODEL_NAME = "cnnModel.bin";

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


    @FXML
    public void initialize() {
        log.info("Initialize main components...");
        tryIt(() -> {
            initExit();

            emotionRecognizer = new EmotionRecognizer(MODEL_NAME);

            emotionRecognizer.setOnExceptionListener(e -> {
                Platform.runLater(() -> {
                    showError(e.getMessage());
                    startVidProgressBarOff();
                });
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
    void startRecognVideo(ActionEvent event) {
        if (emotionRecognizer.isRun()) return;

        tryIt(() -> {
            EmotionRecognizer.ProcessedFrameListener callBack =
                    new EmotionRecognizer.ProcessedFrameListener() {
                        @Override
                        public void onNextFrame(BufferedImage frame) {
                            startVidProgressBarOff();
                            videoImageView.setImage(ImageConverter.toJavaFXImage(frame));
                        }

                        @Override
                        public void onStop(VideoInfo videoInfo) {
                            // do something
                        }
                    };

            String videoPath = this.videoPath.getText();
            startVidProgressBarOn();
            if (isDeviceId(videoPath)) {
                emotionRecognizer.processedVideo(Integer.parseInt(videoPath), callBack);
            } else {
                emotionRecognizer.processedVideo(videoPath, callBack);
            }
        });

//        tryIt(() -> {
//            startVidProgressBarOn();
//            emotionRecognizer.processedRecordVideo(
//                    DEVICE_INDEX,
//                    Paths.get("C:\\Users\\Антон\\Desktop\\2\\f.mp4"),
//                    new EmotionRecognizer.ProcessedFrameListener() {
//                        @Override
//                        public void onNextFrame(BufferedImage frame) {
//                            Platform.runLater(() -> {
//                                startVidProgressBarOff();
//                                videoImageView.setImage(ImageConverter.toJavaFXImage(frame));
//                            });
//                        }
//
//                        @Override
//                        public void onStop(VideoInfo videoInfo) {
//                            try {
//                                emotionRecognizer.writeVideoInfo(videoInfo, Paths.get("C:\\Users\\Антон\\Desktop\\2\\f.mp4"));
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//        });
    }


    @FXML
    void startVideo(ActionEvent event) {
        if (emotionRecognizer.isRun()) return;
        tryIt(() -> {
            EmotionRecognizer.FrameListener callBack = frame -> Platform.runLater(() -> {
                startVidProgressBarOff();
                videoImageView.setImage(ImageConverter.toJavaFXImage(frame));
            });

            String videoPath = this.videoPath.getText();
            startVidProgressBarOn();
            if (isDeviceId(videoPath)) {
                emotionRecognizer.video(Integer.parseInt(videoPath), callBack);
            } else {
                emotionRecognizer.video(videoPath, callBack);
            }
        });
    }

    @FXML
    void stopVideo(ActionEvent event) throws FrameGrabber.Exception {
        if (!emotionRecognizer.isRun()) return;

        startVidProgressBarOn();
        emotionRecognizer.stop(() -> {
            startVidProgressBarOff();
            videoImageView.setImage(VIDEO_PLACE_HOLDER);
            faceFromVideo.setImage(VIDEO_PLACE_HOLDER_FOR_FACE);
            currentFrame = null;
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
            recognScreenProgressBar();
            runTask(() -> {
                tryIt(() -> {
                    imageInfo = emotionRecognizer.processImage(ImageConverter.copyBufferedImage(originalScreenShot));
                    screenshotFrame = imageInfo.getProcessedImage();
                    Platform.runLater(() -> {
                        screenImageView.setImage(ImageConverter.toJavaFXImage(screenshotFrame));
                        recognScreenProgressBar();
                    });
                });
            });
        }
    }


    @FXML
    void browseImage(ActionEvent event) throws IOException {
        log.info("Browse for image...");
        File image = selectFile("Select image for recognize emotions!",
                "Image", "*.png", "*.jpeg", "*.jpg", "*.bmp"
        );
        if (image != null) {
            screenshotFrame = ImageIO.read(image);
            originalScreenShot = ImageConverter.copyBufferedImage(screenshotFrame);
            screenImageView.setImage(ImageConverter.toJavaFXImage(screenshotFrame));
        }
    }

    @FXML
    void saveScreen(ActionEvent event) throws IOException {
        log.info("Save image...");
        if (screenshotFrame != null) {
            File image = saveFile("Saving image",
                    "Image", "*.png", "*.jpg", "*.jpeg", "*.bmp"
            );
            if (image != null) {
                emotionRecognizer.writeImageInfo(imageInfo, image.toPath(), true);
            }
        }
    }


    private boolean isDeviceId(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private void recognScreenProgressBar() {
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


    private void runTask(Runnable task) {
        executorService.submit(task);
    }


    private String getModelPath(String path) throws URISyntaxException {
        return new File(
                MainController.class.getResource(path).toURI()
        ).getPath();
    }

    private void initExit() {
        Platform.runLater(() -> {
            tryIt(() -> {
                currentStage = (Stage) videoImageView.getScene().getWindow();
                currentStage.setOnCloseRequest(event -> {
                    if (emotionRecognizer != null && emotionRecognizer.isRun()) {
                        emotionRecognizer.stop(() -> {
                            System.exit(0);
                        });
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

    private File selectFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(null);
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

    private File selectFolder(String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        return directoryChooser.showDialog(null);
    }


    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
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
            Platform.runLater(() -> {
                showError(e.getMessage() + " \nSee logs: 'logs.log'");
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

