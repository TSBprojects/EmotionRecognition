package ru.sstu.vak.emotionrecognition.ui.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static javafx.application.Platform.runLater;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import lombok.var;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.Frame;
import org.controlsfx.control.RangeSlider;
import ru.sstu.vak.emotionrecognition.common.Emotion;
import static ru.sstu.vak.emotionrecognition.common.Emotion.ANGER;
import static ru.sstu.vak.emotionrecognition.common.Emotion.DISGUST;
import static ru.sstu.vak.emotionrecognition.common.Emotion.FEAR;
import static ru.sstu.vak.emotionrecognition.common.Emotion.HAPPY;
import static ru.sstu.vak.emotionrecognition.common.Emotion.NEUTRAL;
import static ru.sstu.vak.emotionrecognition.common.Emotion.SAD;
import static ru.sstu.vak.emotionrecognition.common.Emotion.SURPRISE;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageConverter;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageCorrector;
import ru.sstu.vak.emotionrecognition.graphicprep.iterators.frameiterator.FrameIterator;
import ru.sstu.vak.emotionrecognition.graphicprep.iterators.frameiterator.impl.FrameIteratorBase;
import ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.ClosestFaceEmotionRecognizer;
import ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.EmotionRecognizer;
import ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.timeseries.TimeSeries;
import ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.timeseries.TimeSeriesCollector;
import static ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.timeseries.TimelineState.PARTIAL_FROM_END;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.FrameInfo;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.ImageInfo;

public class MainController {

    private static final Logger log = LogManager.getLogger(MainController.class.getName());

    private static final Image VIDEO_PLACE_HOLDER = new Image("image/videoPlaceHolder.png");

    private static final Image VIDEO_PLACE_HOLDER_FOR_FACE = new Image("image/videoPlaceHolderForFace.png");

    private static final String NO_EMOTION = "NONE";

    private static final String CHART_RANGE_NAME = "chart";

    private static final String ANALYZE_RANGE_NAME = "analyze";

    private static final int COMFORT_CHART_FACTOR = 34;

    private static final float FIXED_RANGE_TRIGGER_FACTOR = 0.02f;

    private static final float FREE_RANGE_TRIGGER_FACTOR = 0.04f;

    private static final Duration TARGET_FIXED_RANGE = Duration.ofSeconds(6);

    private static final long MIN_RANGE_SIZE_MS = 6000;

    @FXML
    private ImageView videoImageView;

    @FXML
    private Button startVideoBtn;

    @FXML
    private Button startRcognVideoBtn;

    @FXML
    private AnchorPane videoPane;

    @FXML
    private Button screenshotBtn;

    @FXML
    private Button stopVideoBtn;

    @FXML
    private ImageView faceFromVideo;

    @FXML
    private ProgressBar startVideoProgressBar;

    @FXML
    private TextField videoPath;

    @FXML
    private LineChart<String, String> emotionChart;

    @FXML
    public CategoryAxis emotionChartX;

    @FXML
    public CategoryAxis emotionChartY;

    private XYChart.Series<String, String> chartLine;


    @FXML
    private final RangeSlider chartRangeSlider = new RangeSlider(0, 6000, 0, 6000);

    @FXML
    private final RangeSlider analyzeRangeSlider = new RangeSlider(0, 6000, 0, 6000);

    @FXML
    private Pane chartRangeHolder;

    @FXML
    private Text chartHighSliderLabel;

    @FXML
    private Text chartLowSliderLabel;

    @FXML
    private Text chartRangeTotalLabel;

    @FXML
    private Pane analyzeRangeHolder;

    @FXML
    private Text analyzeHighSliderLabel;

    @FXML
    private Text analyzeLowSliderLabel;

    @FXML
    private Text analyzeRangeTotalLabel;


    @FXML
    private ImageView screenImageView;

    @FXML
    private Button recognScreenBtn;

    @FXML
    private ImageView faceFromScreen;

    @FXML
    private TextField imagePath;

    @FXML
    private Button browseImageBtn;

    @FXML
    private Button saveImageBtn;

    @FXML
    private ProgressBar recognImageProgressBar;

    @FXML
    private Button openImageBtn;


    private final EventHandler<? super MouseEvent> mouseConsumeEvent = MouseEvent::consume;

    private final ChangeListener<? super Number> chartRangeHighValueChange = (ChangeListener<Number>) (o, oldV, newV) -> {
        if (getChartSliderRange() < MIN_RANGE_SIZE_MS) {
            chartRangeSlider.highValueProperty().removeListener(this.chartRangeHighValueChange);
            chartRangeSlider.setHighValue(oldV.doubleValue());
            chartRangeSlider.highValueProperty().addListener(this.chartRangeHighValueChange);
            return;
        }
        chartTargetRangeChange(o, oldV, newV);
    };

    private final ChangeListener<? super Number> chartRangeLowValueChange = (ChangeListener<Number>) (o, oldV, newV) -> {
        if (getChartSliderRange() < MIN_RANGE_SIZE_MS) {
            chartRangeSlider.lowValueProperty().removeListener(this.chartRangeLowValueChange);
            chartRangeSlider.setLowValue(oldV.doubleValue());
            chartRangeSlider.lowValueProperty().addListener(this.chartRangeLowValueChange);
            return;
        }
        chartTargetRangeChange(o, oldV, newV);
    };

    private final ChangeListener<? super Number> analyzeRangeHighValueChange = (ChangeListener<Number>) (o, oldV, newV) -> {
        if (getAnalyzeSliderRange() < MIN_RANGE_SIZE_MS) {
            chartRangeSlider.highValueProperty().removeListener(this.analyzeRangeHighValueChange);
            analyzeRangeSlider.setHighValue(oldV.doubleValue());
            chartRangeSlider.highValueProperty().addListener(this.analyzeRangeHighValueChange);
            return;
        }
        analyzeTargetRangeChange(o, oldV, newV);
    };

    private final ChangeListener<? super Number> analyzeRangeLowValueChange = (ChangeListener<Number>) (o, oldV, newV) -> {
        if (getAnalyzeSliderRange() < MIN_RANGE_SIZE_MS) {
            chartRangeSlider.lowValueProperty().removeListener(this.analyzeRangeLowValueChange);
            analyzeRangeSlider.setLowValue(oldV.doubleValue());
            chartRangeSlider.lowValueProperty().addListener(this.analyzeRangeLowValueChange);
            return;
        }
        analyzeTargetRangeChange(o, oldV, newV);
    };

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);


    private Stage currentStage;

    private Frame currentFrame;

    private BufferedImage screenshotFrame;

    private BufferedImage originalScreenShot;

    private ImageInfo imageInfo;


    private TimeSeries chartTimeSeries;

    private TimeSeries analyzeTimeSeries;

    private TimeSeriesCollector timeSeriesCollector;

    private EmotionRecognizer emotionRecognizer;

    private FrameIterator frameIterator;


    private String modelName = "cnnModel.bin";

    public void setParameters(List<String> parameters) {
        if (parameters.size() == 1) {
            this.modelName = parameters.get(0);
        }
        tryIt(() -> {
            emotionRecognizer = new ClosestFaceEmotionRecognizer(modelName);
            emotionRecognizer.setOnStopListener(videoInfo -> onStopAction());
            emotionRecognizer.setFrameListener(frame -> currentFrame = frame);
            emotionRecognizer.setImageNetInputListener(face -> runLater(() ->
                faceFromScreen.setImage(ImageConverter.toJavaFXImage(face))
            ));
            emotionRecognizer.setVideoNetInputListener(face -> runLater(() ->
                faceFromVideo.setImage(ImageConverter.toJavaFXImage(face))
            ));
            emotionRecognizer.setOnExceptionListener(e -> runLater(() -> showError(e.getMessage())));

            timeSeriesCollector = new TimeSeriesCollector(emotionRecognizer);
            chartTimeSeries = timeSeriesCollector.addTargetTimeSeries(CHART_RANGE_NAME);
            analyzeTimeSeries = timeSeriesCollector.addTargetTimeSeries(ANALYZE_RANGE_NAME);
        });
    }

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
            initEmotionChart();
            initRangeSlider();

            frameIterator = new FrameIteratorBase();
            frameIterator.setOnStopListener(this::onStopAction);
            frameIterator.setOnExceptionListener(e -> runLater(() -> showError(e.getMessage())));
            addChangeListeners();
        });
    }

    private void addChangeListeners() {
        addChartChangeListeners();
        addAnalyzeChangeListeners();
    }

    private void removeChangeListeners() {
        removeChartChangeListeners();
        removeAnalyzeChangeListeners();
    }

    private void addChartChangeListeners() {
        chartRangeSlider.highValueProperty().addListener(chartRangeHighValueChange);
        chartRangeSlider.lowValueProperty().addListener(chartRangeLowValueChange);
    }

    private void removeChartChangeListeners() {
        chartRangeSlider.highValueProperty().removeListener(chartRangeHighValueChange);
        chartRangeSlider.lowValueProperty().removeListener(chartRangeLowValueChange);
    }

    private void addAnalyzeChangeListeners() {
        analyzeRangeSlider.highValueProperty().addListener(analyzeRangeHighValueChange);
        analyzeRangeSlider.lowValueProperty().addListener(analyzeRangeLowValueChange);
    }

    private void removeAnalyzeChangeListeners() {
        analyzeRangeSlider.highValueProperty().removeListener(analyzeRangeHighValueChange);
        analyzeRangeSlider.lowValueProperty().removeListener(analyzeRangeLowValueChange);
    }

    @FXML
    void analyzeTargetRangeChange(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (Math.round(oldValue.doubleValue()) == Math.round(newValue.doubleValue())) {
            return;
        }

        long highValueMs = Math.round(analyzeRangeSlider.getHighValue());
        long lowValueMs = Math.round(analyzeRangeSlider.getLowValue());
        analyzeTimeSeries.mutateToRelativeRange(lowValueMs, highValueMs);
        String fullRange = analyzeHighSliderLabel.getText().split("/")[1];
        analyzeHighSliderLabel.setText(prettyDuration(highValueMs) + "/" + fullRange);
        analyzeLowSliderLabel.setText(prettyDuration(lowValueMs) + "/" + fullRange);
        analyzeRangeTotalLabel.setText(prettyDuration(highValueMs - lowValueMs));
    }

    @FXML
    void chartTargetRangeChange(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (Math.round(oldValue.doubleValue()) == Math.round(newValue.doubleValue())) {
            return;
        }

        long chartCurrentMax = Math.round(chartRangeSlider.getMax());
        long highValueMs = Math.round(chartRangeSlider.getHighValue());
        long lowValueMs = Math.round(chartRangeSlider.getLowValue());

        Duration currentRange = Duration.ofMillis(getChartSliderRange());
        if (chartTimeSeries.getState().isReachedToEnd() && currentRange.compareTo(TARGET_FIXED_RANGE) > 0) {
            long untiedValue = chartCurrentMax - (long) (chartCurrentMax * FREE_RANGE_TRIGGER_FACTOR);
            chartRangeSlider.highValueProperty().removeListener(this.chartRangeHighValueChange);
            chartRangeSlider.setHighValue(untiedValue);
            chartRangeSlider.highValueProperty().addListener(this.chartRangeHighValueChange);
            highValueMs = untiedValue;
        }

        chartTimeSeries.mutateToRelativeRange(lowValueMs, highValueMs);

        long fixedRangeStart = chartCurrentMax - (long) (chartCurrentMax * FIXED_RANGE_TRIGGER_FACTOR);
        if (fixedRangeStart <= highValueMs && highValueMs <= chartCurrentMax) {
            chartTimeSeries.setState(PARTIAL_FROM_END);
            chartRangeSlider.addEventFilter(MouseEvent.ANY, mouseConsumeEvent);
        }

        String fullRange = chartHighSliderLabel.getText().split("/")[1];
        chartHighSliderLabel.setText(prettyDuration(highValueMs) + "/" + fullRange);
        chartLowSliderLabel.setText(prettyDuration(lowValueMs) + "/" + fullRange);
        chartRangeTotalLabel.setText(prettyDuration(highValueMs - lowValueMs));

        if (!chartTimeSeries.getState().isReachedToEnd()) {
            analyzeRangeSlider.setHighValue(highValueMs);
            analyzeRangeSlider.setLowValue(lowValueMs);
        }
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
                    log.error("Cannot load root scene graph operations handler", e);
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
                emotionRecognizer.processVideo(videoPath, this::processedFrameHandler);
            } else {
                String videoPath = this.videoPath.getText();
                File videoFile = saveFile("Сохранить медиаданные", "Video", "*.mp4");
                if (videoFile != null) {
                    emotionRecognizer.processVideo(videoPath, videoFile.toPath(), frameInfo -> {
                        startVidProgressBarOff();
                        videoImageView.setImage(ImageConverter.toJavaFXImage(frameInfo.getProcessedImage()));
                    });
                } else {
                    startVidProgressBarOff();
                }
            }
        });
    }

    private void processedFrameHandler(FrameInfo frameInfo) {
        startVidProgressBarOff();
        videoImageView.setImage(ImageConverter.toJavaFXImage(frameInfo.getProcessedImage()));

        runLater(() -> {
            if (chartTimeSeries.getRaw().isEmpty()) return;

            Instant now = Instant.now();
            if (chartTimeSeries.getState().isReachedToEnd()) {
                drawFixedRange(now, frameInfo);
            } else {
                drawTargetRange(chartTimeSeries);
            }

            long firstFrameTimestamp = timeSeriesCollector.getFullTimeline().firstKey();
            Duration fullRange = Duration.ofMillis(now.toEpochMilli() - firstFrameTimestamp);
            refreshSliderLabels(fullRange, chartRangeSlider, chartHighSliderLabel, chartLowSliderLabel, chartRangeTotalLabel);
            refreshSliderLabels(fullRange, analyzeRangeSlider, analyzeHighSliderLabel, analyzeLowSliderLabel, analyzeRangeTotalLabel);
        });
    }

    private void drawFixedRange(Instant now, FrameInfo frameInfo) {
        long start = now.minus(TARGET_FIXED_RANGE).toEpochMilli();
        if (chartTimeSeries.getRaw().firstKey() < start) {
            chartLine.dataProperty().get().remove(0);

            long greatStart = timeSeriesCollector.getFullTimeline().firstKey();
            long startMs = (start - greatStart);
            long endMs = (now.toEpochMilli() - greatStart);

            withoutTriggers(() -> {
                chartRangeSlider.setHighValue(endMs);
                chartRangeSlider.setLowValue(startMs);
                analyzeRangeSlider.setHighValue(endMs);

                chartTimeSeries.mutateToRelativeRange(startMs, endMs);
                analyzeTimeSeries.mutateToRelativeRange(Math.round(analyzeRangeSlider.getLowValue()), endMs);
            });
        }

        List<VideoFace> faces = frameInfo.getVideoFaces();
        if (faces.isEmpty()) {
            addPointToChart(frameInfo.getFrameIndex(), NO_EMOTION);
            return;
        }
        Emotion emotion = faces.get(0).getPrediction().getEmotion();
        addPointToChart(frameInfo.getFrameIndex(), emotion.getName());
    }

    private void drawTargetRange(TimeSeries timeSeries) {
        chartLine.getData().clear();
        var timeline = new ArrayList<>(timeSeries.getRaw().values());
        int lineSize = timeline.size();
        int step = Math.max(lineSize / COMFORT_CHART_FACTOR, 1);
        for (int i = 0; i < lineSize; i += step) {
            var videoFrame = timeline.get(i);
            List<VideoFace> faces = videoFrame.getVideoFaces();
            if (faces.isEmpty()) {
                addPointToChart(videoFrame.getFrameIndex(), NO_EMOTION);
                continue;
            }
            Emotion emotion = faces.get(0).getPrediction().getEmotion();
            addPointToChart(videoFrame.getFrameIndex(), emotion.getName());
        }
    }

    private void refreshSliderLabels(
        Duration fullRange,
        RangeSlider slider,
        Text highSliderLabel,
        Text lowSliderLabel,
        Text rangeTotalLabel
    ) {
        if (fullRange.toMillis() >= slider.getMax()) {
            slider.setMax(fullRange.toMillis());
        }
        String prettyDuration = prettyDuration(fullRange);
        long highValueMs = Math.round(slider.getHighValue());
        long lowValueMs = Math.round(slider.getLowValue());
        highSliderLabel.setText(prettyDuration(highValueMs) + "/" + prettyDuration);
        lowSliderLabel.setText(prettyDuration(lowValueMs) + "/" + prettyDuration);
        rangeTotalLabel.setText(prettyDuration(highValueMs - lowValueMs));
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
    void stopVideo(ActionEvent event) {
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
                originalScreenShot = ImageCorrector.copyBufferedImage(screenshotFrame);
                screenImageView.setImage(ImageConverter.toJavaFXImage(screenshotFrame));
            });
        }
    }

    @FXML
    void recognizeScreenshot(ActionEvent event) {
        if (screenshotFrame != null) {
            recognScreenProgressBarToggle();
            executorService.submit(() ->
                tryIt(() -> {
                    imageInfo = emotionRecognizer.processImage(ImageCorrector.copyBufferedImage(originalScreenShot));
                    screenshotFrame = imageInfo.getProcessedImage();
                    screenImageView.setImage(ImageConverter.toJavaFXImage(screenshotFrame));
                    recognScreenProgressBarToggle();
                })
            );
        }
    }

    @FXML
    void browseImage(ActionEvent event) {
        log.info("Browse for image...");

        tryIt(() -> {
            File image = selectFile("Select image for recognize emotions!",
                "Image", "*.png", "*.jpeg", "*.jpg", "*.bmp"
            );
            if (image != null) {
                imagePath.setText(image.getPath());
                screenshotFrame = ImageIO.read(image);
                originalScreenShot = ImageCorrector.copyBufferedImage(screenshotFrame);
                screenImageView.setImage(ImageConverter.toJavaFXImage(screenshotFrame));
            }
        });
    }

    @FXML
    void saveScreen(ActionEvent event) {
        log.info("Save image...");

        if (screenshotFrame != null) {
            tryIt(() -> {
                File image = saveFile("Saving image",
                    "Image", "*.png", "*.jpg", "*.jpeg", "*.bmp"
                );
                if (image != null) {
                    if (imageInfo != null) {
                        emotionRecognizer.writeImageInfo(imageInfo, image.toPath(), true);
                    } else {
                        ImageIO.write(screenshotFrame, "png", image);
                    }
                }
            });
        }
    }

    private void initEmotionChart() {
        emotionChartX.setAnimated(false);
        emotionChartX.setLabel("Целевые кадры");
        emotionChartY.setAnimated(false);
        emotionChartY.setLabel("Эмоции");
        chartLine = new XYChart.Series<>();
        chartLine.setName("Текущая эмоция");
        ObservableList<String> emotions = FXCollections.observableArrayList(
            FEAR.getName(),
            ANGER.getName(),
            DISGUST.getName(),
            SAD.getName(),
            NO_EMOTION,
            NEUTRAL.getName(),
            SURPRISE.getName(),
            HAPPY.getName()
        );
        emotionChartY.setCategories(emotions);
        emotionChart.setCreateSymbols(false);
        emotionChart.setAnimated(false);
        emotionChart.getData().add(chartLine);
        emotionChart.setLegendVisible(false);
    }

    private void initRangeSlider() {
        runLater(() -> {
            chartRangeSlider.setLayoutX(115);
            chartRangeSlider.setLayoutY(9);
            chartRangeSlider.setMinWidth(239);
            chartRangeSlider.setMinHeight(38);
            chartRangeHolder.getChildren().add(chartRangeSlider);
            chartRangeHolder.addEventFilter(MouseEvent.MOUSE_RELEASED, event ->
                chartRangeSlider.removeEventFilter(MouseEvent.ANY, mouseConsumeEvent)
            );

            analyzeRangeSlider.setLayoutX(115);
            analyzeRangeSlider.setLayoutY(9);
            analyzeRangeSlider.setMinWidth(239);
            analyzeRangeSlider.setMinHeight(38);
            analyzeRangeHolder.getChildren().add(analyzeRangeSlider);
        });
    }

    private void addPointToChart(long value, String emotion) {
        chartLine.getData().add(new XYChart.Data<>(Long.toString(value), emotion));
    }

    private long getChartSliderRange() {
        return Math.round(chartRangeSlider.getHighValue() - chartRangeSlider.getLowValue());
    }

    private long getAnalyzeSliderRange() {
        return Math.round(analyzeRangeSlider.getHighValue() - analyzeRangeSlider.getLowValue());
    }

    private String prettyDuration(Duration duration) {
        return prettyDuration(duration.toMillis());
    }

    private String prettyDuration(Number ms) {
        long lSeconds = ms.longValue() / 1000;
        return String.format("%02d:%02d:%02d", lSeconds / 3600, (lSeconds % 3600) / 60, (lSeconds % 60));
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
        runLater(() ->
            tryIt(() -> {
                currentStage = (Stage) videoImageView.getScene().getWindow();
                currentStage.setOnCloseRequest(event -> {
                    if (emotionRecognizer != null && emotionRecognizer.isRun()) {
                        emotionRecognizer.stop();
                        while (emotionRecognizer.isRun()) ;
                    } else if (frameIterator != null && frameIterator.isRun()) {
                        frameIterator.stop();
                        while (frameIterator.isRun()) ;
                    }
                    System.exit(0);
                });
            })
        );
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

        return option.get() == ButtonType.OK;
    }

    private void withoutTriggers(Runnable block) {
        removeChangeListeners();
        block.run();
        addChangeListeners();
    }

    private void tryIt(TryItCallback tryCode) {
        try {
            tryCode.executableCode();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            runLater(() -> showError(e.getMessage()));
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
