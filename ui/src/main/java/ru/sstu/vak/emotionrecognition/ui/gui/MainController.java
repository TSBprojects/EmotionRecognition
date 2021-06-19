package ru.sstu.vak.emotionrecognition.ui.gui;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import static java.time.ZoneOffset.UTC;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import static javafx.scene.input.TransferMode.ANY;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import static javafx.scene.paint.Color.GRAY;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.ORANGE;
import static javafx.scene.paint.Color.rgb;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.var;
import static org.apache.commons.lang3.StringUtils.isBlank;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import ru.sstu.vak.emotionrecognition.graphicprep.iterators.frameiterator.FrameIterator;
import ru.sstu.vak.emotionrecognition.graphicprep.iterators.frameiterator.impl.FrameIteratorBase;
import ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.ClosestFaceEmotionRecognizer;
import ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.EmotionRecognizer;
import static ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.ErFeature.COLLECT_FRAMES;
import static ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.ErFeature.GENERATE_JSON_OUTPUT;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.FrameInfo;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeries;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeriesCollector;
import static ru.sstu.vak.emotionrecognition.timeseries.TimelineState.PARTIAL_FROM_END;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.AnalyzeEngine;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.endpoint.Endpoint;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.EmotionFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.Feature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.MetaFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;
import static ru.sstu.vak.emotionrecognition.ui.Main.TITLE_IMAGE_PATH;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.HasChildren;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.PaneAdapter;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.SplitPaneAdapter;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.endpoint.EndpointStatus;
import static ru.sstu.vak.emotionrecognition.ui.gui.constructor.endpoint.EndpointStatus.ERROR;
import static ru.sstu.vak.emotionrecognition.ui.gui.constructor.endpoint.EndpointStatus.OK;
import static ru.sstu.vak.emotionrecognition.ui.gui.constructor.endpoint.EndpointStatus.PROGRESS;
import static ru.sstu.vak.emotionrecognition.ui.gui.constructor.endpoint.EndpointStatus.WARN;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.endpoint.EventRequestContext;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.FeatureConfig;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.FeatureFactory;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context.EmotionFeatureContext;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context.FeatureContext;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context.MetaFeatureContext;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.ModelContext;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.ModelPane;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.SimpleModelContext;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.SimpleModelPane;
import ru.sstu.vak.emotionrecognition.ui.gui.dragdrop.DragDropData;
import static ru.sstu.vak.emotionrecognition.ui.gui.dragdrop.DragDropData.Type.ENDPOINT;
import static ru.sstu.vak.emotionrecognition.ui.gui.dragdrop.DragDropData.Type.FEATURE;
import ru.sstu.vak.emotionrecognition.ui.io.AnalysisHolder;
import ru.sstu.vak.emotionrecognition.ui.io.ModelsHolder;
import ru.sstu.vak.emotionrecognition.ui.io.TrueModelEvent;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.ENDPOINT_STATUS_CLASS;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildEndpointAnchorPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildEndpointErrorButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildEndpointHBoxWithLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildEndpointIdleButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildEndpointLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildEndpointOkButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildEndpointProgressIndicator;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildEndpointToolbar;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildEndpointWarnButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildFeatureNameLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyEndpointPlaceHolderOuter;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyFeaturePlaceHolderOuter;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyFlowPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyPlaceHolderInner;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyPlaceHolderLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodySplitPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelEndpointAnchorPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelHeaderAnchorPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelScrollPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelSplitPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildRemoveButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildSelectFeatureAnchorPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildSettingsButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildStartListenAllButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildStateNameTextField;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildStopListenAllButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildStringencyCheckBox;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.newTooltipBuilder;
import static ru.sstu.vak.emotionrecognition.ui.util.NodeDecorator.shadow;

public class MainController {

    private static final Logger log = LogManager.getLogger(MainController.class.getName());

    private static final Image VIDEO_PLACE_HOLDER = new Image("image/videoPlaceHolder.png");

    private static final Image VIDEO_PLACE_HOLDER_FOR_FACE = new Image("image/videoPlaceHolderForFace.png");

    private static final String CONSTRUCTOR_CONFIGURATION_NAME = "constructor.ercc";

    private static final String NO_EMOTION = "NONE";

    private static final String CHART_RANGE_NAME = "chart";

    private static final String ANALYZE_RANGE_NAME = "analyze";

    private static final int COMFORT_CHART_FACTOR = 40;

    private static final float FIXED_RANGE_TRIGGER_FACTOR = 0.02f;

    private static final float FREE_RANGE_TRIGGER_FACTOR = 0.04f;

    private static final Duration TARGET_FIXED_RANGE = Duration.ofSeconds(6);

    private static final long GRAPHIC_TOOLTIP_VISIBLE_DELAY = 60_000;

    @FXML
    private ImageView videoImageView;

    @FXML
    private Button startRcognVideoBtn;

    @FXML
    private AnchorPane videoPane;

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
    private Label chartHighSliderLabel;

    @FXML
    private Label chartLowSliderLabel;

    @FXML
    private Label chartRangeTotalLabel;

    @FXML
    private Pane analyzeRangeHolder;

    @FXML
    private Label analyzeHighSliderLabel;

    @FXML
    private Label analyzeLowSliderLabel;

    @FXML
    private Label analyzeRangeTotalLabel;

    @FXML
    private Button graphicRangeInfoFirst;

    @FXML
    private Button graphicRangeInfoSecond;

    @FXML
    private ListView<String> stateListView;

    private final OkHttpClient client = new OkHttpClient();

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
            emotionRecognizer.disable(COLLECT_FRAMES);
            emotionRecognizer.disable(GENERATE_JSON_OUTPUT);
            emotionRecognizer.setOnStopListener(videoInfo -> onStopAction());
            emotionRecognizer.setVideoNetInputListener(face -> runLater(() ->
                faceFromVideo.setImage(ImageConverter.toJavaFXImage(face))
            ));
            emotionRecognizer.setOnExceptionListener(e -> runLater(() -> showError(e.getMessage())));

            timeSeriesCollector = new TimeSeriesCollector(emotionRecognizer);
            chartTimeSeries = timeSeriesCollector.addTargetTimeSeries(CHART_RANGE_NAME);
            analyzeTimeSeries = timeSeriesCollector.addTargetTimeSeries(ANALYZE_RANGE_NAME);
            loadConfiguration(new File(CONSTRUCTOR_CONFIGURATION_NAME));
        });
    }

    private void onStopAction() {
        freezeStatesList();
        startVidProgressBarOff();
        videoImageView.setImage(VIDEO_PLACE_HOLDER);
        faceFromVideo.setImage(VIDEO_PLACE_HOLDER_FOR_FACE);
    }


    @FXML
    public void initialize() {
        log.info("Initialize main components...");
        tryGracefully(() -> {
            initExit();
            initTooltips();
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
    void startRecognVideo(ActionEvent event) {
        if (emotionRecognizer.isRun() || frameIterator.isRun()) return;

        tryGracefully(() -> {
            boolean ok = showConfirm(
                "Что делать с видео?",
                "Сохранить обработанное видео?",
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
                    emotionRecognizer.processVideo(videoPath, videoFile.toPath(), this::processedFrameHandler);
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

            var statesList = stateListView.getItems();
            var statesListCopy = new ArrayList<>(statesList);

            var models = modelContext.getModels();

            if (models.isEmpty()) {
                if (statesList.get(0).equals(STATE_MODEL_NOT_SET)) return;
                shadow(stateListView, ORANGE);
                statesList.clear();
                statesList.add(STATE_MODEL_NOT_SET);
                return;
            }

            currentEmotionalStates = AnalyzeEngine.analyze(analyzeTimeSeries, models);

            applyHighlighting(models);

            if (currentEmotionalStates.isEmpty()) {
                if (statesList.get(0).equals(STATE_NO_MATCH)) return;
                shadow(stateListView, GRAY);
                statesList.clear();
                statesList.add(STATE_NO_MATCH);
                return;
            }

            List<String> newStates = new ArrayList<>(currentEmotionalStates);
            if (statesList.equals(newStates)) return;
            shadow(stateListView, GREEN);
            statesList.clear();
            statesList.addAll(currentEmotionalStates);

            newStates.removeAll(statesListCopy);
            if (!newStates.isEmpty()) {
                for (var state : newStates) {
                    for (var entry : models.entrySet()) {
                        var modelId = entry.getKey();
                        var model = entry.getValue();
                        if (model.getName().equals(state)) {
                            notifyEventListeners(modelId);
                        }
                    }
                }
            }
        });
    }

    private void freezeStatesList() {
        var states = stateListView.getItems();
        if (states.isEmpty() || !states.get(0).equals(STATE_MODEL_STOPPED)) {
            runLater(() -> {
                states.clear();
                shadow(stateListView, rgb(143, 235, 204));
                states.add(STATE_MODEL_STOPPED);
            });
        }
    }

    private void applyHighlighting(Map<Integer, AnalyzableModel> models) {
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

    private void notifyEventListeners(int modelId) {
        AnalyzableModel model = modelContext.getModel(modelId);
        if (!model.isSatisfied()) return;

        var mName = model.getName();
        var mEndpoints = model.getEndpoints();

        log.info("Sending {} notifications because state configuration '{}' is true", mEndpoints.size(), mName);

        for (var endpointId : mEndpoints.keySet()) {
            notifyEventListener(modelId, endpointId);
        }
    }

    private void notifyEventListener(int modelId, int endpointId) {
        AnalyzableModel model = modelContext.getModel(modelId);
        EventRequestContext requestContext = modelContext.getEventRequestContext();
        if (!model.isSatisfied() || requestContext.contains(modelId, endpointId)) return;

        Future<?> requestFuture = executorService.submit(() -> {
            applyEndpointStatus(modelId, endpointId, PROGRESS, "Отправка запроса ...");
            Endpoint endpoint = endpoints.get(endpointId);
            var event = new TrueModelEvent(model.getName(), LocalDateTime.now(UTC));
            try {
                Request request = new Request.Builder()
                    .url(endpoint.getUrl())
                    .post(RequestBody.create(MediaType.parse("application/json"), mapper.writeValueAsString(event)))
                    .build();

                Response response = client.newCall(request).execute();
                int responseCode = response.code();
                response.body().close();
                if (responseCode != 200) {
                    log.warn("Endpoint '{}' responded with failure code {}", endpoint, responseCode);
                    applyEndpointStatus(modelId, endpointId, WARN, "Слушатель вернул код: " + responseCode);
                    return;
                }

                applyEndpointStatus(modelId, endpointId, OK, "Слушатель успешно обработал запрос");
            } catch (Exception e) {
                log.error("Sending notification to {} is failed", endpoint.getUrl(), e);
                applyEndpointStatus(modelId, endpointId, ERROR, "Ошибка при отправке запроса: " + e.getMessage());
            }

            requestContext.remove(modelId, endpointId);
        });

        requestContext.add(modelId, endpointId, requestFuture);
    }

    private void applyEndpointStatus(int modelId, int endpointId, EndpointStatus status, String msg) {
        runLater(() -> {
            if (!modelContext.containsKey(modelId)) return;
            FlowPane endpointHolder = modelContext.getModelPane(modelId).getEndpointHolder();
            String labelSelector = "#" + endpointId + ENDPOINT_ID_SUFFIX;
            var endpointPane = (AnchorPane) endpointHolder.lookup(labelSelector);
            if (endpointPane != null) {
                var endpointHBox = (HBox) endpointPane.getChildren().get(0);
                for (var child : endpointHBox.getChildren()) {
                    var childClasses = child.getStyleClass();
                    if (childClasses.contains(status.getStyleClass())) {
                        child.setVisible(true);
                        child.setManaged(true);
                        ((Control) child).setTooltip(newTooltipBuilder(msg).build());
                        continue;
                    }
                    if (childClasses.contains(ENDPOINT_STATUS_CLASS)) {
                        child.setVisible(false);
                        child.setManaged(false);
                    }
                }
            }
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
        Label highSliderLabel,
        Label lowSliderLabel,
        Label rangeTotalLabel
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
    void saveTargetTimeSeries(ActionEvent event) {
        tryIt(() -> {
            File analysis = saveFile(
                "Сохранить описание целевых кадров и результат анализа",
                "Json",
                "*.json"
            );
            if (analysis != null) {
                mapper.writeValue(analysis, AnalysisHolder.from(currentEmotionalStates, analyzeTimeSeries));
            }
        });
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
        freezeStatesList();
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

    public static final String STATE_MODEL_STOPPED = "ОСТАНОВЛЕНО";

    public static final String STATE_MODEL_NOT_SET = "НЕ ЗАДАНО";

    public static final String MODEL_ID_SUFFIX = "_model";

    public static final String ENDPOINT_ID_SUFFIX = "_endpoint";

    public static final String ENDPOINT_PROGRESS_ID_SUFFIX = "_endpoint_progress";

    public static final String ENDPOINT_OK_STATUS_ID_SUFFIX = "_endpoint_ok";

    public static final String ENDPOINT_WARN_STATUS_ID_SUFFIX = "_endpoint_warn";

    public static final String ENDPOINT_ERROR_STATUS_ID_SUFFIX = "_endpoint_error";

    public static final String ENDPOINT_IDLE_STATUS_ID_SUFFIX = "_endpoint_idle";

    public static final String ENDPOINT_LABEL_ID_SUFFIX = "_endpoint_label";

    public static final String FEATURE_ID_SUFFIX = "_feature";

    public static final String META_FEATURE_ID_SUFFIX = "_meta";

    @FXML
    private Button addEndpoint;

    @FXML
    private Button showTreeViewBtn;

    @FXML
    private Button loadConstructorBtn;

    @FXML
    private Button saveConstructorBtn;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private AnchorPane addModelPlaceHolder;

    @FXML
    private FlowPane selectEndpointPane;

    @FXML
    private FlowPane selectFeaturePane;

    @FXML
    private VBox selectModelVBox;

    private Set<String> currentEmotionalStates = new HashSet<>();

    private static final ObjectMapper mapper = initMapper();

    private static final AutoIncrementMap<Endpoint> endpoints = new AutoIncrementHashMap<>(new ConcurrentHashMap<>());

    private static final ModelContext modelContext = new SimpleModelContext();

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    private static final Map<Integer, FeatureContext<?>> BASE_FEATURE_CONTEXTS = initFeatures();

    private static Map<Integer, FeatureContext<?>> initFeatures() {
        Map<Integer, FeatureContext<?>> features = new HashMap<>();

        for (var feature : Lists.newArrayList(ServiceLoader.load(MetaFeature.class))) {
            features.put(feature.getId(), new MetaFeatureContext(feature, modelContext));
        }

        for (var feature : Lists.newArrayList(ServiceLoader.load(EmotionFeature.class))) {
            features.put(feature.getId(), new EmotionFeatureContext(feature, modelContext));
        }

        return Collections.unmodifiableMap(features);
    }

    private static ObjectMapper initMapper() {
        return new ObjectMapper()
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(INDENT_OUTPUT)
            .registerModule(new JavaTimeModule());
    }

    private void initTooltips() {
        addEndpoint.setTooltip(newTooltipBuilder("Добавить слушателя").build());
        showTreeViewBtn.setTooltip(newTooltipBuilder("Показать дерево слушателей").build());
        loadConstructorBtn.setTooltip(newTooltipBuilder("Загрузить конфигурацию конструктора из файла").build());
        saveConstructorBtn.setTooltip(newTooltipBuilder("Сохранить конфигурацию конструктора в файл").build());
        Tooltip leftTimeLabel = newTooltipBuilder(
            "Время от старта в соответствии с текущим положением левого ползунка / Общее время от старта"
        ).build();
        chartLowSliderLabel.setTooltip(leftTimeLabel);
        analyzeLowSliderLabel.setTooltip(leftTimeLabel);
        Tooltip rightTimeLabel = newTooltipBuilder(
            "Время от старта в соответствии с текущим положением правого ползунка / Общее время от старта"
        ).build();
        chartHighSliderLabel.setTooltip(rightTimeLabel);
        analyzeHighSliderLabel.setTooltip(rightTimeLabel);
        chartRangeSlider.setTooltip(newTooltipBuilder(
            "Целевой отрезок времени отображаемый на усредненном(отезок > 5сек) графике эмоций"
        ).build());
        analyzeRangeSlider.setTooltip(newTooltipBuilder(
            "Целевой отрезок времени анализируемый на основе конфигурации конструктора"
        ).build());
        graphicRangeInfoFirst.setTooltip(newTooltipBuilder(
            "Ползунки анализируемого отрезка синхронизированы с ползунками отрезка отображаемого на графике. "
                + "Т.е. при изменении положения ползунков отрезка отвечающего за график - "
                + "соответсвенно изменится положение ползунков анализируемого отрезка."
        ).visibleDuration(GRAPHIC_TOOLTIP_VISIBLE_DELAY).build());
        graphicRangeInfoSecond.setTooltip(newTooltipBuilder(
            "Для комфортного отображения не усредненного графика в реальном времени "
                + "используется отрезок равный 5 секундам от текущего момента времени.\n\n"
                + "В связи с этим, при приведении правого ползунка в крайнее правое положение до упора - "
                + "он приклеится (как и правый ползунок анализируемого отрезка), "
                + "что соответсвует положению \"в реальном времени\". Левый ползунок в свою очередь "
                + "будет занимать положение на 5 секунд ранее относительно правого.\n"
                + "Левый ползунок анализируемого отрезка заблокирован НЕ будет.\n\n"
                + "Чтобы отклеить правый ползунок достаточно переместить левый ползунок влево."
        ).visibleDuration(GRAPHIC_TOOLTIP_VISIBLE_DELAY).build());
    }

    private void initAddModelDragAndDropHandlers(Node node) {
        node.setOnDragOver(event -> {
            /* accept it only if it is  not dragged from the same node
             * and if it has a string data */
            Dragboard db = event.getDragboard();
            if (event.getGestureSource() != node
                && db.hasString()
                && DragDropData.deserialize(db.getString()).getValue() == FEATURE) {
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
                int modelId = modelContext.getNextId();
                String stateName = "Показатель состояния " + modelId;
                int featureId = DragDropData.deserialize(db.getString()).getKey();
                var featureContext = BASE_FEATURE_CONTEXTS.get(featureId);

                ModelPane modelPane = createModelPane(modelId, stateName);

                featureContext.createAndPutModel(stateName, modelPane);

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
        return createModelPane(modelId, stateName, true);
    }

    private ModelPane createModelPane(int modelId, String stateName, boolean stringency) {
        FlowPane endpointHolder = buildModelBodyFlowPane();

        AnchorPane addEndpointPlaceHolder = buildModelBodyEndpointPlaceHolderOuter(
            buildModelBodyPlaceHolderInner(buildModelBodyPlaceHolderLabel("Перетащите слушателя"))
        );

        endpointHolder.getChildren().add(addEndpointPlaceHolder);

        FlowPane featuresHolder = buildModelBodyFlowPane();

        AnchorPane addFeaturePlaceHolder = buildModelBodyFeaturePlaceHolderOuter(
            buildModelBodyPlaceHolderInner(buildModelBodyPlaceHolderLabel("Перетащите фактор"))
        );

        featuresHolder.getChildren().add(addFeaturePlaceHolder);

        TextField state = buildStateNameTextField(stateName);

        CheckBox stringencyCheckBox = buildStringencyCheckBox(stringency);

        Button removeModelButton = buildRemoveButton();

        SplitPane modelPane = buildModelSplitPane(
            buildModelHeaderAnchorPane(
                state,
                stringencyCheckBox,
                removeModelButton
            ),
            buildModelBodySplitPane(
                buildModelScrollPane(endpointHolder),
                buildModelScrollPane(featuresHolder)
            )
        );

        modelPane.setId(modelId + MODEL_ID_SUFFIX);

        initStateNameChangeHandler(modelId, state);

        initStringencyChangeHandler(modelId, stringencyCheckBox);

        initAddFeatureToModelHandlers(modelId, featuresHolder, featuresHolder);

        initAddEndpointToModelHandlers(modelId, endpointHolder, endpointHolder);

        initRemoveModelHandler(modelId, selectModelVBox, modelPane, removeModelButton);

        return SimpleModelPane.builder()
            .value(modelPane)
            .state(state)
            .stringency(stringencyCheckBox)
            .remove(removeModelButton)
            .featureHolder(featuresHolder)
            .endpointHolder(endpointHolder)
            .build();
    }

    private void initStateNameChangeHandler(int modelId, TextField stateTextField) {
        stateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            var model = modelContext.getModel(modelId);
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

    private void initStringencyChangeHandler(int modelId, CheckBox stringency) {
        stringency.setOnAction(event -> modelContext.getModel(modelId).setStrictly(stringency.isSelected()));
    }

    private void initRemoveModelHandler(int modelId, Pane holder, Node target, Button removeBtn) {
        removeBtn.setOnAction(event -> {
            holder.getChildren().remove(target);
            modelContext.remove(modelId);
        });
    }

    private void initAddFeatureToModelHandlers(int modelId, Pane target, Pane addTo) {

        target.setOnDragOver(event -> {
            /* accept it only if it is  not dragged from the same node
             * and if it has a string data */
            Dragboard db = event.getDragboard();
            if (event.getGestureSource() != target
                && db.hasString()
                && DragDropData.deserialize(db.getString()).getValue() == FEATURE) {
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
                int featureId = DragDropData.deserialize(db.getString()).getKey();

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

    private void initAddEndpointToModelHandlers(int modelId, Pane target, Pane addTo) {
        target.setOnDragOver(event -> {
            /* accept it only if it is  not dragged from the same node
             * and if it has a string data */
            Dragboard db = event.getDragboard();
            if (event.getGestureSource() != target && db.hasString()) {
                var model = modelContext.getModel(modelId);
                var data = DragDropData.deserialize(db.getString());

                if (data.getValue() == ENDPOINT && !model.getEndpoints().containsKey(data.getKey())) {
                    /* allow for both copying and moving, whatever user chooses */
                    event.acceptTransferModes(ANY);
                }
            }

            event.consume();
        });

        target.setOnDragDropped(event -> {
            boolean success = false;

            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                int endpointId = DragDropData.deserialize(db.getString()).getKey();
                Endpoint endpoint = endpoints.get(endpointId);
                modelContext.getModel(modelId).getEndpoints().put(endpointId, endpoint);
                notifyEventListener(modelId, endpointId);

                AnchorPane endpointPain = createModelEndpointPane(modelId, endpointId);
                var addToChildren = addTo.getChildren();
                addToChildren.add(addToChildren.size() - 1, endpointPain);

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

            Button settingsButton = buildSettingsButton();

            Label labelName = buildFeatureNameLabel(featureContext.getFeature().getName());

            //TODO Need to fix UI bug with broken scroll bar.
            // Height of panes seems to be larger than it looks like.
            // Scroll bar doesn't appear for some reason
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
                content.putString(DragDropData.serialize(FEATURE, id));
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
                byte[] json = mapper.writeValueAsBytes(ModelsHolder.from(modelContext.getModels()));
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
            loadConfiguration(configFile);
        });
    }

    private void loadConfiguration(File configFile) throws IOException {
        if (configFile != null && configFile.exists()) {
            byte[] encodedJson = Files.readAllBytes(configFile.toPath());
            ModelsHolder holder = mapper.readValue(Base64.getDecoder().decode(encodedJson), ModelsHolder.class);
            addLoadedModels(holder.getModels());
        }
    }

    private void addLoadedModels(List<AnalyzableModel> models) {
        int newEndpointsStartId = -1;
        Map<Integer, Endpoint> uniqueEndpoints = new HashMap<>();
        List<Runnable> delayedEndpointIdsUpdates = new ArrayList<>();

        for (var model : models) {
            int modelId = modelContext.getNextId();

            ModelPane modelPane = createModelPane(modelId, model.getName(), model.isStrictly());
            modelContext.put(model, modelPane);

            FlowPane featuresHolder = modelPane.getFeatureHolder();
            addLoadedFeatures(modelId, model.getFeatures(), featuresHolder);
            addLoadedFeatures(modelId, model.getMetaFeatures(), featuresHolder);

            var modelEndpoints = model.getEndpoints();
            for (var endpointEntry : modelEndpoints.entrySet()) {
                int newEndpointId = endpoints.getNextId();
                int oldEndpointId = endpointEntry.getKey();
                Endpoint endpoint = endpointEntry.getValue();

                if (newEndpointsStartId == -1) {
                    newEndpointsStartId = newEndpointId;
                }

                var prevEndpoint = uniqueEndpoints.putIfAbsent(oldEndpointId, endpoint);
                if (prevEndpoint == null) {
                    delayedEndpointIdsUpdates.add(() -> {
                        modelEndpoints.remove(oldEndpointId);
                        modelEndpoints.put(newEndpointId, endpoint);
                        addLoadedEndpoint(modelId, newEndpointId, modelPane.getEndpointHolder());
                    });
                    endpoints.put(endpoint);
                    selectEndpointPane.getChildren().add(createSelectEndpointPane(newEndpointId));
                }
            }

            var paneModels = selectModelVBox.getChildren();
            paneModels.add(paneModels.size() - 1, modelPane.value());
        }

        var maxOldIdOpt = uniqueEndpoints.keySet().stream().max(Integer::compareTo);
        if (maxOldIdOpt.isPresent()) {
            int maxOldId = maxOldIdOpt.get();
            int newEndpointsEndId = newEndpointsStartId + uniqueEndpoints.size() - 1;
            if (newEndpointsStartId <= maxOldId && maxOldId < newEndpointsEndId) {
                Collections.reverse(delayedEndpointIdsUpdates);
            }
            delayedEndpointIdsUpdates.forEach(Runnable::run);
        }
    }

    private void addLoadedFeatures(int modelId, AutoIncrementMap<? extends Feature> features, FlowPane featuresHolder) {
        for (var entry : features.entrySet()) {
            var featureNumberInModel = entry.getKey();
            var feature = entry.getValue();
            var featureContext = BASE_FEATURE_CONTEXTS.get(feature.getId());

            FeatureConfig featureConfig = FeatureConfig.builder()
                .modelId(modelId)
                .featureName(feature.getName())
                .featureNumberInModel(featureNumberInModel)
                .featureHolder(new PaneAdapter(featuresHolder))
                .featureContext(featureContext)
                .build();

            AnchorPane featurePane = FeatureFactory.createFeature(featureConfig).value();

            var addToChildren = featuresHolder.getChildren();
            addToChildren.add(addToChildren.size() - 1, featurePane);
        }
    }

    private void addLoadedEndpoint(int modelId, int endpointId, FlowPane endpointHolder) {
        AnchorPane endpointPane = createModelEndpointPane(modelId, endpointId);
        var addToChildren = endpointHolder.getChildren();
        addToChildren.add(addToChildren.size() - 1, endpointPane);
    }

    @FXML
    void onAddEndpoint(ActionEvent event) {
        int id = endpoints.getNextId();
        int port = 8080 + id;
        String endpointName = "Слушатель " + id;
        Endpoint newEndpoint = Endpoint.of(endpointName, "http://localhost:" + port + "/event");

        showEndpointSettingsForm(newEndpoint, () -> {
            endpoints.put(newEndpoint);
            selectEndpointPane.getChildren().add(createSelectEndpointPane(id));
        });
    }

    private void showEndpointSettingsForm(Endpoint endpoint, Runnable onOk) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/endpointSettings.fxml"));

        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        EndpointSettingsController endpointController = loader.getController();
        endpointController.setEndpoint(endpoint);

        Stage stage = new Stage();
        endpointController.setCurrentStage(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(endpoint.getName() + " - конфигурация");
        stage.setScene(new Scene(root, 311, 274));
        stage.setOnCloseRequest(e -> {
            if (endpointController.isOk()) {
                onOk.run();
            }
        });
        stage.getIcons().add(new Image(TITLE_IMAGE_PATH));
        stage.showAndWait();
    }

    private AnchorPane createSelectEndpointPane(int endpointId) {
        Button startListen = buildStartListenAllButton();
        Button stopListen = buildStopListenAllButton();
        Button settings = buildSettingsButton();
        Button remove = buildRemoveButton();
        ToolBar header = buildEndpointToolbar(startListen, stopListen, settings, remove);
        Label nameLabel = buildEndpointLabel(endpoints.get(endpointId));
        HBox labelHolder = buildEndpointHBoxWithLabel(nameLabel);
        AnchorPane endpoint = buildEndpointAnchorPane(header, labelHolder);

        initStartListenAllEndpointHandler(endpointId, startListen);
        initStopListenAllEndpointHandler(endpointId, stopListen);
        initRemoveEndpointHandler(endpointId, remove, endpoint);
        initEditEndpointHandler(endpointId, settings, nameLabel);

        endpoint.setOnDragDetected(event -> {
            /* allow any transfer mode */
            Dragboard db = endpoint.startDragAndDrop(ANY);

            /* put a string on dragboard */
            ClipboardContent content = new ClipboardContent();
            content.putString(DragDropData.serialize(ENDPOINT, endpointId));
            db.setContent(content);

            event.consume();
        });

        return endpoint;
    }

    @FXML
    void onShowEndpointsTreeView(ActionEvent event) {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/endpointsTreeView.fxml"));

        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        EndpointsTreeViewController endpointsTreeView = loader.getController();
        endpointsTreeView.setEndpointsAndModels(endpoints, modelContext);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Структура отношений");
        stage.setScene(new Scene(root, 311, 320));
        stage.getIcons().add(new Image(TITLE_IMAGE_PATH));
        stage.showAndWait();
    }

    private AnchorPane createModelEndpointPane(int modelId, int endpointId) {
        Button remove = buildRemoveButton();
        Label nameLabel = buildEndpointLabel(endpoints.get(endpointId));
        AnchorPane endpoint = buildModelEndpointAnchorPane(
            buildEndpointProgressIndicator(endpointId + ENDPOINT_PROGRESS_ID_SUFFIX),
            buildEndpointOkButton(endpointId + ENDPOINT_OK_STATUS_ID_SUFFIX),
            buildEndpointWarnButton(endpointId + ENDPOINT_WARN_STATUS_ID_SUFFIX),
            buildEndpointErrorButton(endpointId + ENDPOINT_ERROR_STATUS_ID_SUFFIX),
            buildEndpointIdleButton(endpointId + ENDPOINT_IDLE_STATUS_ID_SUFFIX),
            nameLabel,
            remove
        );

        nameLabel.setId(endpointId + ENDPOINT_LABEL_ID_SUFFIX);
        endpoint.setId(endpointId + ENDPOINT_ID_SUFFIX);

        initRemoveModelEndpointHandler(modelId, endpointId, remove, endpoint);

        return endpoint;
    }

    private void initEditEndpointHandler(int endpointId, Button settings, Label label) {
        Endpoint endpoint = endpoints.get(endpointId);
        settings.setOnAction(e -> showEndpointSettingsForm(endpoint, () -> {
            label.setText(endpoint.getName());
            label.setTooltip(newTooltipBuilder(endpoint).build());
            updateEndpointLabelInAllModels(endpointId);
        }));
    }

    private void initRemoveEndpointHandler(int endpointId, Button remove, AnchorPane target) {
        remove.setOnAction(e -> {
            endpoints.remove(endpointId);
            removeEndpointFromModels(endpointId);
            selectEndpointPane.getChildren().remove(target);
        });
    }

    private void initRemoveModelEndpointHandler(int modelId, int endpointId, Button remove, AnchorPane target) {
        remove.setOnAction(e -> {
            modelContext.getModel(modelId).getEndpoints().remove(endpointId);
            modelContext.getModelPane(modelId).getEndpointHolder().getChildren().remove(target);
            modelContext.getEventRequestContext().remove(modelId, endpointId);
        });
    }

    private void initStartListenAllEndpointHandler(int endpointId, Button startListen) {
        startListen.setOnAction(e -> {
            Endpoint endpoint = endpoints.get(endpointId);
            var modelsIterator = modelContext.getModels().values().iterator();
            var panesIterator = modelContext.getPanes().entrySet().iterator();
            while (modelsIterator.hasNext() && panesIterator.hasNext()) {
                var model = modelsIterator.next();
                var panesEntry = panesIterator.next();
                var modelId = panesEntry.getKey();
                var modelPane = panesEntry.getValue();

                var prevEndpoint = model.getEndpoints().putIfAbsent(endpointId, endpoint);
                if (prevEndpoint == null) {
                    var endpChildren = modelPane.getEndpointHolder().getChildren();
                    endpChildren.add(endpChildren.size() - 1, createModelEndpointPane(modelId, endpointId));
                    notifyEventListener(modelId, endpointId);
                }
            }
        });
    }

    private void initStopListenAllEndpointHandler(int endpointId, Button stopListen) {
        stopListen.setOnAction(e -> removeEndpointFromModels(endpointId));
    }

    private void removeEndpointFromModels(int endpointId) {
        var modelsEntryIterator = modelContext.getModels().entrySet().iterator();
        var panesIterator = modelContext.getPanes().values().iterator();
        while (modelsEntryIterator.hasNext() && panesIterator.hasNext()) {
            var modelEntry = modelsEntryIterator.next();
            var modelId = modelEntry.getKey();
            var model = modelEntry.getValue();
            var modelPane = panesIterator.next();

            model.getEndpoints().remove(endpointId);
            var endpointPane = modelPane.getEndpointHolder().lookup("#" + endpointId + ENDPOINT_ID_SUFFIX);
            modelPane.getEndpointHolder().getChildren().remove(endpointPane);
            modelContext.getEventRequestContext().remove(modelId, endpointId);
        }
    }

    private void updateEndpointLabelInAllModels(int endpointId) {
        var endpoint = endpoints.get(endpointId);
        for (ModelPane modelPane : modelContext.getPanes().values()) {
            FlowPane endpointHolder = modelPane.getEndpointHolder();
            if (!endpointHolder.getChildren().isEmpty()) {
                String labelSelector = "#" + endpointId + ENDPOINT_LABEL_ID_SUFFIX;
                var endpointLabel = (Label) endpointHolder.lookup(labelSelector);
                if (endpointLabel != null) {
                    endpointLabel.setText(endpoint.getName());
                    endpointLabel.setTooltip(newTooltipBuilder(endpoint).build());
                }
            }
        }
    }
}
