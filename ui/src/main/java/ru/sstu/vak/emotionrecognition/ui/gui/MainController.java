package ru.sstu.vak.emotionrecognition.ui.gui;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import com.google.common.collect.Lists;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javafx.application.Platform.runLater;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import static javafx.scene.input.TransferMode.ANY;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import static javafx.scene.paint.Color.GRAY;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.ORANGE;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import lombok.var;
import static org.apache.commons.lang3.StringUtils.isBlank;
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
import ru.sstu.vak.emotionrecognition.common.Invalidable;
import ru.sstu.vak.emotionrecognition.common.Satisfiable;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementHashMap;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageConverter;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageCorrector;
import ru.sstu.vak.emotionrecognition.graphicprep.iterators.frameiterator.FrameIterator;
import ru.sstu.vak.emotionrecognition.graphicprep.iterators.frameiterator.impl.FrameIteratorBase;
import ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.ClosestFaceEmotionRecognizer;
import ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.EmotionRecognizer;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.FrameInfo;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.ImageInfo;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeries;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeriesCollector;
import static ru.sstu.vak.emotionrecognition.timeseries.TimelineState.PARTIAL_FROM_END;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.AnalyzeEngine;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.EmotionFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.Feature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.MetaFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;
import static ru.sstu.vak.emotionrecognition.ui.Main.TITLE_IMAGE_PATH;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.HasChildren;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.PaneAdapter;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.SplitPaneAdapter;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.FeatureConfig;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.FeatureFactory;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context.EmotionFeatureContext;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context.FeatureContext;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context.MetaFeatureContext;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.ModelPane;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.SimpleModelPane;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.io.ModelsHolder;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildFeatureNameLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildFeatureSettingsButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyFlowPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyPlaceHolderInner;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyPlaceHolderLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyPlaceHolderOuter;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelHeaderAnchorPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelScrollPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelSplitPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildRemoveModelButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildSelectFeatureAnchorPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildStateNameTextField;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildStringencyRadioButton;
import static ru.sstu.vak.emotionrecognition.ui.util.NodeDecorator.shadow;

public class MainController {

    private static final Logger log = LogManager.getLogger(MainController.class.getName());

    private static final Image VIDEO_PLACE_HOLDER = new Image("image/videoPlaceHolder.png");

    private static final Image VIDEO_PLACE_HOLDER_FOR_FACE = new Image("image/videoPlaceHolderForFace.png");

    private static final String NO_EMOTION = "NONE";

    private static final String CHART_RANGE_NAME = "chart";

    private static final String ANALYZE_RANGE_NAME = "analyze";

    private static final int COMFORT_CHART_FACTOR = 40;

    private static final float FIXED_RANGE_TRIGGER_FACTOR = 0.02f;

    private static final float FREE_RANGE_TRIGGER_FACTOR = 0.04f;

    private static final Duration TARGET_FIXED_RANGE = Duration.ofSeconds(6);

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
    private final RangeSlider chartRangeSlider = initChartRangeSlider();

    @FXML
    private final RangeSlider analyzeRangeSlider = initAnalyzeRangeSlider();

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
    private ListView<String> stateListView;

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

    @FXML
    private VBox constructorVbox;


    private final EventHandler<? super MouseEvent> mouseConsumeEvent = MouseEvent::consume;

    private final ChangeListener<? super Number> chartRangeHighValueChange = (ChangeListener<Number>) (o, oldV, newV) -> {
        if (getChartSliderRange() < TARGET_FIXED_RANGE.toMillis()) {
            chartRangeSlider.highValueProperty().removeListener(this.chartRangeHighValueChange);
            chartRangeSlider.setHighValue(oldV.doubleValue());
            chartRangeSlider.highValueProperty().addListener(this.chartRangeHighValueChange);
            return;
        }
        chartTargetRangeChange(o, oldV, newV);
    };

    private final ChangeListener<? super Number> chartRangeLowValueChange = (ChangeListener<Number>) (o, oldV, newV) -> {
        if (getChartSliderRange() < TARGET_FIXED_RANGE.toMillis()) {
            chartRangeSlider.lowValueProperty().removeListener(this.chartRangeLowValueChange);
            chartRangeSlider.setLowValue(oldV.doubleValue());
            chartRangeSlider.lowValueProperty().addListener(this.chartRangeLowValueChange);
            return;
        }
        chartTargetRangeChange(o, oldV, newV);
    };

    private final ChangeListener<? super Number> analyzeRangeHighValueChange = (ChangeListener<Number>) (o, oldV, newV) -> {
        if (getAnalyzeSliderRange() < TARGET_FIXED_RANGE.toMillis()) {
            chartRangeSlider.highValueProperty().removeListener(this.analyzeRangeHighValueChange);
            analyzeRangeSlider.setHighValue(oldV.doubleValue());
            chartRangeSlider.highValueProperty().addListener(this.analyzeRangeHighValueChange);
            return;
        }
        analyzeTargetRangeChange(o, oldV, newV);
    };

    private final ChangeListener<? super Number> analyzeRangeLowValueChange = (ChangeListener<Number>) (o, oldV, newV) -> {
        if (getAnalyzeSliderRange() < TARGET_FIXED_RANGE.toMillis()) {
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
        tryGracefully(() -> {
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
        tryGracefully(() -> {
            initExit();
            initEmotionChart();
            initStateListView();
            addConstructorFeatures();
            initAddModelDragAndDropHandlers(addModelPlaceHolder);

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
            long diffToSub = (long) (chartCurrentMax * FREE_RANGE_TRIGGER_FACTOR);
            long untiedLowValue = lowValueMs - diffToSub;
            long untiedHighValue = highValueMs - diffToSub;
            removeChartChangeListeners();
            chartRangeSlider.setLowValue(untiedLowValue);
            chartRangeSlider.setHighValue(untiedHighValue);
            addChartChangeListeners();
            lowValueMs = untiedLowValue;
            highValueMs = untiedHighValue;
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
            tryGracefully(() -> {
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
                stage.setScene(new Scene(root, winSize.width - 150.0, winSize.height - 150.0));
                stage.setResizable(true);
                stage.getIcons().add(new Image(TITLE_IMAGE_PATH));
                stage.showAndWait();
            });
        }
    }

    @FXML
    void startRecognVideo(ActionEvent event) {
        if (emotionRecognizer.isRun() || frameIterator.isRun()) return;

        tryGracefully(() -> {
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


            var modelsList = stateListView.getItems();

            if (currentModels.isEmpty()) {
                if (modelsList.get(0).equals(STATE_MODEL_NOT_SET)) return;
                shadow(stateListView, ORANGE);
                modelsList.clear();
                modelsList.add(STATE_MODEL_NOT_SET);
                return;
            }

            var r = AnalyzeEngine.analyze(analyzeTimeSeries, currentModels);

            applyHighlighting(currentModels);

            if (r.isEmpty()) {
                if (modelsList.get(0).equals(STATE_NO_MATCH)) return;
                shadow(stateListView, GRAY);
                stateListView.getItems().clear();
                stateListView.getItems().add(STATE_NO_MATCH);
                return;
            }

            if (modelsList.equals(new ArrayList<>(r))) return;
            shadow(stateListView, GREEN);
            stateListView.getItems().clear();
            stateListView.getItems().addAll(r);
        });
    }

    private void applyHighlighting(AutoIncrementMap<AnalyzableModel> models) {
        if (models.isEmpty()) return;

        var scene = mainAnchorPane.getScene();

        for (var modelEntry : models.entrySet()) {
            int modelId = modelEntry.getKey();
            var model = modelEntry.getValue();
            var splitPane = (SplitPane) scene.lookup("#" + modelId + MODEL_ID_SUFFIX);
            highlightNode(model, new SplitPaneAdapter(splitPane));

            for (var entry : model.getFeatures().entrySet()) {
                int featureId = entry.getKey();
                var feature = entry.getValue();
                var selector = "#" + modelId + "_" + featureId + FEATURE_ID_SUFFIX;
                var anchorPane = (AnchorPane) scene.lookup(selector);
                highlightNode(feature, new PaneAdapter(anchorPane));
            }
            for (var entry : model.getMetaFeatures().entrySet()) {
                int featureId = entry.getKey();
                var feature = entry.getValue();
                var selector = "#" + modelId + "_" + featureId + META_FEATURE_ID_SUFFIX + FEATURE_ID_SUFFIX;
                var anchorPane = (AnchorPane) scene.lookup(selector);
                highlightNode(feature, new PaneAdapter(anchorPane));
            }
        }
    }

    private void highlightNode(Satisfiable object, HasChildren<?> hasChildren) {
        var node = hasChildren.getNode();

        if (node == null) return;

        var nodeChildren = hasChildren.getChildren();

        final String styleDelimiter = ";";
        var styles = new ArrayList<>(Arrays.asList(node.getStyle().split(styleDelimiter)));
        var lastStyle = styles.get(styles.size() - 1);
        if (lastStyle.contains("-fx-effect: dropshadow")) {
            styles.remove(lastStyle);
            lastStyle = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);";
        } else {
            lastStyle = "";
        }

        var warnLabelOpt = nodeChildren.stream()
            .filter(n -> "warn".equals(n.getId()))
            .findFirst();

        warnLabelOpt.ifPresent(value -> value.setVisible(false));

        var nodeStyle = String.join(styleDelimiter, styles) + styleDelimiter;
        if (Invalidable.class.isAssignableFrom(object.getClass()) && ((Invalidable) object).isInvalid()) {
            var style = "-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0);";
            node.setStyle(nodeStyle + style);
            warnLabelOpt.ifPresent(value -> value.setVisible(true));
        } else if (object.isSatisfied()) {
            var style = "-fx-effect: dropshadow(three-pass-box, green, 10, 0, 0, 0);";
            node.setStyle(nodeStyle + style);
        } else {
            node.setStyle(nodeStyle + lastStyle);
        }
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

        tryGracefully(() -> {
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
        tryGracefully(() -> {
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
            tryGracefully(() -> {
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
                tryGracefully(() -> {
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

        tryGracefully(() -> {
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
            tryGracefully(() -> {
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

    private void initStateListView() {
        stateListView.getItems().add(STATE_MODEL_NOT_SET);
        stateListView.setFixedCellSize(23);
    }

    private RangeSlider initAnalyzeRangeSlider() {
        long rangeMs = TARGET_FIXED_RANGE.toMillis();
        RangeSlider rangeSlider = new RangeSlider(0, rangeMs, 0, rangeMs);
        runLater(() -> {
            rangeSlider.setLayoutX(115);
            rangeSlider.setLayoutY(9);
            rangeSlider.setMinWidth(239);
            rangeSlider.setMinHeight(38);
            analyzeRangeHolder.getChildren().add(analyzeRangeSlider);
        });
        return rangeSlider;
    }

    private RangeSlider initChartRangeSlider() {
        long rangeMs = TARGET_FIXED_RANGE.toMillis();
        RangeSlider rangeSlider = new RangeSlider(0, rangeMs, 0, rangeMs);
        runLater(() -> {
            rangeSlider.setLayoutX(115);
            rangeSlider.setLayoutY(9);
            rangeSlider.setMinWidth(239);
            rangeSlider.setMinHeight(38);
            chartRangeHolder.getChildren().add(rangeSlider);
            chartRangeHolder.addEventFilter(MouseEvent.MOUSE_RELEASED, event ->
                rangeSlider.removeEventFilter(MouseEvent.ANY, mouseConsumeEvent)
            );
        });
        return rangeSlider;
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
            tryGracefully(() -> {
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

    private void tryGracefully(TryItCallback tryCode) {
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

    private void tryIt(TryItCallback tryCode) {
        try {
            tryCode.executableCode();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            runLater(() -> showError(e.getMessage()));
        }
    }

    private interface TryItCallback {
        void executableCode() throws Exception;
    }

    ///////////////////////////////////////////////
    //////////////// CONSTRUCTOR 2.0 //////////////
    ///////////////////////////////////////////////

    public static final String STATE_NO_MATCH = "НЕТ СОВПАДЕНИЙ";

    public static final String STATE_MODEL_NOT_SET = "НЕ ЗАДАНО";

    public static final String MODEL_ID_SUFFIX = "_model";

    public static final String FEATURE_ID_SUFFIX = "_feature";

    public static final String META_FEATURE_ID_SUFFIX = "_meta";

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private AnchorPane addModelPlaceHolder;

    @FXML
    private FlowPane selectFeaturePane;

    @FXML
    private VBox selectModelVBox;

    private static final ObjectMapper mapper = initMapperForModels();

    private static final AutoIncrementMap<AnalyzableModel> currentModels =
        new AutoIncrementHashMap<>(new ConcurrentHashMap<>());

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    private static final Map<Integer, FeatureContext<?>> BASE_FEATURE_CONTEXTS = initFeatures();

    private static Map<Integer, FeatureContext<?>> initFeatures() {
        Map<Integer, FeatureContext<?>> features = new HashMap<>();

        for (var feature : Lists.newArrayList(ServiceLoader.load(MetaFeature.class))) {
            features.put(feature.getId(), new MetaFeatureContext(feature, currentModels));
        }

        for (var feature : Lists.newArrayList(ServiceLoader.load(EmotionFeature.class))) {
            features.put(feature.getId(), new EmotionFeatureContext(feature, currentModels));
        }

        return Collections.unmodifiableMap(features);
    }

    private static ObjectMapper initMapperForModels() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(INDENT_OUTPUT);
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private void initAddModelDragAndDropHandlers(Node node) {
        node.setOnDragOver(event -> {
            /* accept it only if it is  not dragged from the same node
             * and if it has a string data */
            if (event.getGestureSource() != node && event.getDragboard().hasString()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(ANY);
            }

            event.consume();
        });

//        addModelPlaceHolder.setOnDragEntered(new EventHandler <DragEvent>() {
//            public void handle(DragEvent event) {
//                /* the drag-and-drop gesture entered the target */
//                System.out.println("onDragEntered");
//                /* show to the user that it is an actual gesture target */
//                if (event.getGestureSource() != target &&
//                    event.getDragboard().hasString()) {
//                    target.setFill(Color.GREEN);
//                }
//
//                event.consume();
//            }
//        });

//        addModelPlaceHolder.setOnDragExited(new EventHandler <DragEvent>() {
//            public void handle(DragEvent event) {
//                /* mouse moved away, remove the graphical cues */
//                target.setFill(Color.BLACK);
//
//                event.consume();
//            }
//        });

        node.setOnDragDropped(event -> {
            boolean success = false;

            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                int featureNumberInModel = 0;
                int modelId = currentModels.getNextId();
                String stateName = "Модель " + modelId;
                int featureId = Integer.parseInt(db.getString());
                var featureContext = BASE_FEATURE_CONTEXTS.get(featureId);
                featureContext.createAndPutModel(stateName);

                ModelPane modelPane = createModelPane(modelId, stateName);

                FlowPane featuresHolder = modelPane.getFeatureHolder();

                FeatureConfig featureConfig = FeatureConfig.builder()
                    .modelId(modelId)
                    .featureNumberInModel(featureNumberInModel)
                    .featureHolder(new PaneAdapter(featuresHolder))
                    .featureContext(featureContext)
                    .build();

                AnchorPane firstFeature = FeatureFactory.createFeature(featureConfig).value();

                featuresHolder.getChildren().add(0, firstFeature);

                var paneModels = selectModelVBox.getChildren();
                paneModels.add(paneModels.size() - 1, modelPane.value());

                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private ModelPane createModelPane(int modelId, String stateName) {
        FlowPane featuresHolder = buildModelBodyFlowPane();

        AnchorPane addFeaturePlaceHolder = buildModelBodyPlaceHolderOuter(
            buildModelBodyPlaceHolderInner(buildModelBodyPlaceHolderLabel())
        );

        featuresHolder.getChildren().add(addFeaturePlaceHolder);

        TextField state = buildStateNameTextField(stateName);

        RadioButton stringency = buildStringencyRadioButton();

        Button removeModelButton = buildRemoveModelButton();

        SplitPane modelPane = buildModelSplitPane(
            buildModelHeaderAnchorPane(
                state,
                stringency,
                removeModelButton
            ),
            buildModelScrollPane(featuresHolder)
        );

        modelPane.setId(modelId + MODEL_ID_SUFFIX);

        initStateNameChangeHandler(modelId, state);

        initStringencyChangeHandler(modelId, stringency);

        initAddFeatureToModelHandlers(modelId, featuresHolder, featuresHolder);

        initRemoveModelHandler(modelId, selectModelVBox, modelPane, removeModelButton);

        return SimpleModelPane.builder()
            .value(modelPane)
            .state(state)
            .stringency(stringency)
            .remove(removeModelButton)
            .featureHolder(featuresHolder)
            .build();
    }

    private void initStateNameChangeHandler(int modelId, TextField stateTextField) {
        stateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            var model = currentModels.get(modelId);
            String newName = stateTextField.getText();
            if (isBlank(newName)) {
                String oldName = model.getName();
                model.setName(oldName);
                stateTextField.setText(oldName);
                stateTextField.setStyle("-fx-border-color:red");
                SCHEDULER.schedule(() -> runLater(() -> stateTextField.setStyle("-fx-border-color:none")), 1, SECONDS);
            } else {
                model.setName(newName);
                stateTextField.setStyle("-fx-border-color:none");
            }
        });
    }

    private void initStringencyChangeHandler(int modelId, RadioButton stringency) {
        stringency.setOnAction(event -> currentModels.get(modelId).setStrictly(stringency.isSelected()));
    }

    private void initRemoveModelHandler(int modelId, Pane holder, Node target, Button removeBtn) {
        removeBtn.setOnAction(event -> {
            holder.getChildren().remove(target);
            currentModels.remove(modelId);
        });
    }

    private void initAddFeatureToModelHandlers(int modelId, Pane target, Pane addTo) {

        target.setOnDragOver(event -> {
            /* accept it only if it is  not dragged from the same node
             * and if it has a string data */
            if (event.getGestureSource() != target && event.getDragboard().hasString()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(ANY);
            }

            event.consume();
        });

//        addModelPlaceHolder.setOnDragEntered(new EventHandler <DragEvent>() {
//            public void handle(DragEvent event) {
//                /* the drag-and-drop gesture entered the target */
//                System.out.println("onDragEntered");
//                /* show to the user that it is an actual gesture target */
//                if (event.getGestureSource() != target &&
//                    event.getDragboard().hasString()) {
//                    target.setFill(Color.GREEN);
//                }
//
//                event.consume();
//            }
//        });

//        addModelPlaceHolder.setOnDragExited(new EventHandler <DragEvent>() {
//            public void handle(DragEvent event) {
//                /* mouse moved away, remove the graphical cues */
//                target.setFill(Color.BLACK);
//
//                event.consume();
//            }
//        });

        target.setOnDragDropped(event -> {

            boolean success = false;

            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                int featureId = Integer.parseInt(db.getString());

                var featureContext = BASE_FEATURE_CONTEXTS.get(featureId);

                int featureNumberInModel = featureContext.putFeature(modelId);

                FeatureConfig featureConfig = FeatureConfig.builder()
                    .modelId(modelId)
                    .featureNumberInModel(featureNumberInModel)
                    .featureHolder(new PaneAdapter(addTo))
                    .featureContext(featureContext)
                    .build();

                AnchorPane featurePane = FeatureFactory.createFeature(featureConfig).value();

                var addToChildren = addTo.getChildren();
                addToChildren.add(addToChildren.size() - 1, featurePane);

                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void addConstructorFeatures() {
        for (var entry : BASE_FEATURE_CONTEXTS.entrySet()) {
            final int id = entry.getKey();
            final var featureContext = entry.getValue();

            Button settingsButton = buildFeatureSettingsButton();

            Label labelName = buildFeatureNameLabel(featureContext.getFeature().getName());

            AnchorPane featurePane = buildSelectFeatureAnchorPane(labelName, settingsButton);

            settingsButton.setOnAction(featureContext.getLibraryFeatureSettingHandler(labelName).apply(
                settingsButton, null, null
            ));

            featurePane.setOnDragDetected(event -> {
                //greenShadow(featurePane);

                /* allow any transfer mode */
                Dragboard db = featurePane.startDragAndDrop(ANY);

                /* put a string on dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString(Integer.toString(id));
                db.setContent(content);

                event.consume();
            });

            featurePane.setOnDragDone(event -> {
                //blackShadow(featurePane);
                event.consume();
            });

            selectFeaturePane.getChildren().add(featurePane);
        }
    }

    @FXML
    void onModelsSave(ActionEvent event) {
        tryIt(() -> {
            File configFile = saveFile(
                "Сохранить конфигурацию",
                "Emotion recognition constructor configuration",
                "*.ercc"
            );
            if (configFile != null) {
                byte[] json = mapper.writeValueAsBytes(ModelsHolder.from(currentModels));
                Files.write(configFile.toPath(), Base64.getEncoder().encode(json));
            }
        });
    }

    @FXML
    void onModelsLoad(ActionEvent event) {
        tryIt(() -> {
            File configFile = selectFile(
                "Загрузить конфигурацию",
                "Emotion recognition constructor configuration",
                "*.ercc"
            );
            if (configFile != null) {
                byte[] encodedJson = Files.readAllBytes(configFile.toPath());
                ModelsHolder holder = mapper.readValue(Base64.getDecoder().decode(encodedJson), ModelsHolder.class);
                addLoadedModels(holder.getModels());
            }
        });
    }

    private void addLoadedModels(List<AnalyzableModel> models) {
        for (var model : models) {
            int modelId = currentModels.getNextId();
            String stateName = model.getName();
            currentModels.put(model);

            ModelPane modelPane = createModelPane(modelId, stateName);
            FlowPane featuresHolder = modelPane.getFeatureHolder();

            addLoadedFeatures(modelId, model.getFeatures(), featuresHolder);
            addLoadedFeatures(modelId, model.getMetaFeatures(), featuresHolder);

            var paneModels = selectModelVBox.getChildren();
            paneModels.add(paneModels.size() - 1, modelPane.value());
        }
    }

    private void addLoadedFeatures(int modelId, AutoIncrementMap<? extends Feature> features, FlowPane featuresHolder) {
        for (var entry : features.entrySet()) {
            var featureNumberInModel = entry.getKey();
            var feature = entry.getValue();
            var featureContext = BASE_FEATURE_CONTEXTS.get(feature.getId());

            FeatureConfig featureConfig = FeatureConfig.builder()
                .modelId(modelId)
                .featureNumberInModel(featureNumberInModel)
                .featureHolder(new PaneAdapter(featuresHolder))
                .featureContext(featureContext)
                .build();

            AnchorPane featurePane = FeatureFactory.createFeature(featureConfig).value();

            var addToChildren = featuresHolder.getChildren();
            addToChildren.add(addToChildren.size() - 1, featurePane);
        }
    }
}
