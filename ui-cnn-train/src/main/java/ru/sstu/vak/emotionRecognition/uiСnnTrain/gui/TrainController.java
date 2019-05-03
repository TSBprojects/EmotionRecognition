package ru.sstu.vak.emotionRecognition.uiСnnTrain.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import ru.sstu.vak.emotionRecognition.cnnTrain.TrainCNN;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

public class TrainController {

    private final static int CHECK_EVERY_EPOCH = 0;

    private final static int EPOCH_COUNT = 320;

    private final static double SPLIT_TRAIN_SET = 0.2;


    private final static String DATA_SET_PATH = "G:\\Main things\\Study\\DIPLOMA\\My\\datasets\\gray\\emotion_dataset3+2_smoothed_pixels";

    @FXML
    private TextArea trainInfo;

    @FXML
    private Button startTrainBtn;

    @FXML
    private Button stopTrainBtn;

    @FXML
    private AnchorPane chartPane;


    private TrainCNN emotionRec;

    private volatile boolean trainRunning = false;

    private Timer timer = new Timer();

    private XYChart.Series chartLine;

    @FXML
    public void initialize() throws IOException {

        initStatusChart();

        emotionRec = new TrainCNN(DATA_SET_PATH, EPOCH_COUNT, SPLIT_TRAIN_SET);

        emotionRec.setOnEpochListener(status -> {
            printMessage(status);
            return trainRunning;
        });

        emotionRec.setOnScoreListener((epoch, totalScore) -> {
            Platform.runLater(() -> {
                chartLine.getData().add(new XYChart.Data(String.valueOf(epoch), totalScore));
            });
        });

    }

    @FXML
    void startTrain(ActionEvent event) throws Exception {
        trainRunning = true;
        emotionRec.train(CHECK_EVERY_EPOCH);
    }

    @FXML
    void stopTrain(ActionEvent event) {
        trainRunning = false;
    }


    private void printMessage(String mess) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Platform.runLater(() -> trainInfo.appendText("[" + dateFormat.format(new Date()) + "] - " + mess + "\n"));
    }

    private void initStatusChart() {
        CategoryAxis epochs = new CategoryAxis();
        NumberAxis score = new NumberAxis();
        epochs.setLabel("Epoch");
        score.setLabel("Net score");
        LineChart<String, Number> scoreChart = new LineChart<String, Number>(
                epochs,
                score
        );
        scoreChart.setCreateSymbols(false);
        scoreChart.setMinWidth(874);
        chartLine = new XYChart.Series();
        chartLine.setName("Score status");
        chartLine.getData().add(new XYChart.Data(String.valueOf(0), 0));
        scoreChart.getData().addAll(chartLine);
        chartPane.getChildren().add(scoreChart);
    }

}
