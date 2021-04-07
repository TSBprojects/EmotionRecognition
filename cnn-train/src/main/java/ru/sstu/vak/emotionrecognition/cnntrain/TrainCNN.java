package ru.sstu.vak.emotionrecognition.cnntrain;

import java.io.File;
import java.io.IOException;
import static java.lang.Math.toIntExact;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LocalResponseNormalization;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.schedule.ScheduleType;
import org.nd4j.linalg.schedule.StepSchedule;
import ru.sstu.vak.emotionrecognition.cnn.ConvNetwork;
import ru.sstu.vak.emotionrecognition.cnn.MyModelSerializer;

public class TrainCNN {

    private static final Logger log = LogManager.getLogger(TrainCNN.class.getName());

    private static final int EXEC_SERVICE_THREADS = 10;

    private static final int HEIGHT = 48;
    private static final int WIDTH = 48;
    private static final int CHANNELS = 1;
    private static final int BATCH_SIZE = 100;
    private static final int MAX_PATHS_PER_LABEL = 0; // 0 == max
    private static final long SEED = System.currentTimeMillis();

    private double maxModelScore = 0;

    private int epoch;
    private int numLabels;
    private double splitTrainSet;
    private String dataSetPath;
    private ConvNetwork model;

    private EpochListener epochListener;
    private ScoreListener scoreListener;

    private ExecutorService executorService;

    /**
     * @param dataSetPath   Path to the folder with dataset.
     *                      This folder should contain subfolders with the
     *                      names of the classes for training.
     *                      In the subfolders should be placed
     *                      48x48 images in grayscale format.
     * @param epochCount    number of epochs for train
     * @param splitTrainSet Part of the dataset to test the quality of training
     */
    public TrainCNN(String dataSetPath, int epochCount, double splitTrainSet) {
        this.epoch = epochCount;
        this.dataSetPath = dataSetPath;
        this.splitTrainSet = splitTrainSet;
    }


    public interface EpochListener {
        boolean onNextEpoch(String status);
    }

    public interface ScoreListener {
        void onTotalScore(int epoch, double totalScore);
    }


    public void setOnEpochListener(EpochListener epochListener) {
        this.epochListener = epochListener;
    }

    public void setOnScoreListener(ScoreListener scoreListener) {
        this.scoreListener = scoreListener;
    }


    public void train() throws Exception {
        train(Paths.get(""), -1);
    }

    public void train(int checkAfterEveryIter) throws Exception {
        train(Paths.get(""), checkAfterEveryIter);
    }

    public void train(Path saveModelTo) throws Exception {
        train(saveModelTo, -1);
    }

    public void train(Path saveModelTo, int checkAfterEveryIter) throws Exception {
        this.executorService = Executors.newFixedThreadPool(EXEC_SERVICE_THREADS);
        Random rand = new Random(SEED);

        log.info("Load data....");
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        File mainPath = new File(dataSetPath);
        FileSplit fileSplit = new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, rand);
        int numExamples = toIntExact(fileSplit.length());
        numLabels = fileSplit.getRootDir().listFiles(File::isDirectory).length; //This only works if your root is clean: only label subdirs.
        BalancedPathFilter pathFilter = new BalancedPathFilter(rand, labelMaker, numExamples, numLabels, MAX_PATHS_PER_LABEL);

        model = new ConvNetwork(modifiedAlexNetArchitecture());
        model.init();

        InputSplit[] inputSplit = fileSplit.sample(pathFilter, 1 - splitTrainSet, splitTrainSet);
        InputSplit trainData = inputSplit[0];
        InputSplit testData = inputSplit[1];

        ImageRecordReader recordReader = initImageRecordReader(trainData, labelMaker);
        DataSetIterator dataIter = initDataSetIterator(recordReader);

        ImageRecordReader testRecordReader = initImageRecordReader(testData, labelMaker);
        DataSetIterator testDataIter = initDataSetIterator(testRecordReader);


        executorService.submit(() -> {
            int counter = checkAfterEveryIter;
            log.info("Train model....");
            for (int i = 0; i < epoch; i++) {
                model.fit(dataIter);

                final String epochInfo = "*** Completed epoch " + i + " ***";
                log.info(epochInfo);

                if (epochListener != null && !epochListener.onNextEpoch(epochInfo)) {
                    break;
                }
                if (i == counter) {
                    counter += checkAfterEveryIter;
                    if (scoreListener != null) {
                        scoreListener.onTotalScore(i, validateModel(saveModelTo, testDataIter));
                    }
                }
            }
            log.info("****************Train finished********************");
            executorService.shutdown();
        });

    }


    @SuppressWarnings("all")
    private double validateModel(Path saveModelTo, DataSetIterator testDataIter) {
        log.info("Validate model....");
        Evaluation eval = model.evaluate(testDataIter);
        String stats = eval.stats();

        double totalScore = getTotalScore(stats);

        if (totalScore > maxModelScore) {
            maxModelScore = totalScore;
            executorService.submit(() -> {
                saveModel(saveModelTo);
            });
        }

        return totalScore;
    }

    private void saveModel(Path saveModelTo) {
        log.info("Save model...");
        try {
            MyModelSerializer.writeModel(model, saveModelTo + "trainedModel.bin", true);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private double getTotalScore(String stats) {
        Pattern p = Pattern.compile("(F1 Score:\\s+)(\\d{1},\\d{4})");
        Matcher matcher = p.matcher(stats);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group(2).replace(",", "."));
        }

        throw new PatternSyntaxException("Can't find F1 Score", p.toString(), -1);
    }


    private ImageRecordReader initImageRecordReader(InputSplit inputSplit, ParentPathLabelGenerator labelMaker) throws IOException {
        ImageRecordReader recordReader = new ImageRecordReader(HEIGHT, WIDTH, CHANNELS, labelMaker);
        recordReader.initialize(inputSplit, null);
        return recordReader;
    }

    private DataSetIterator initDataSetIterator(ImageRecordReader recordReader) {
        return new RecordReaderDataSetIterator(recordReader, BATCH_SIZE, 1, numLabels);
    }


    private MultiLayerConfiguration modifiedAlexNetArchitecture() {
        /*
           Construct the neural network
        */
        log.info("Build model....");

        double nonZeroBias = 1;
        double dropOut = 0.5;

        return new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .activation(Activation.RELU)
                .updater(new Nesterovs(new StepSchedule(ScheduleType.ITERATION, 1e-2, 0.1, 100000), 0.9))
                .biasUpdater(new Nesterovs(new StepSchedule(ScheduleType.ITERATION, 2e-2, 0.1, 100000), 0.9))
                .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // normalize to prevent vanishing or exploding gradients
                .l2(5 * 1e-4)
                .list()
                .layer(0, convInit("cnn1", CHANNELS, 96, new int[]{5, 5}, new int[]{1, 1}, new int[]{1, 1}, nonZeroBias))
                .layer(1, new LocalResponseNormalization.Builder().name("lrn1").build())
                .layer(2, maxPool("maxpool1", new int[]{2, 2}))
                .layer(3, conv5x5("cnn2", 256, new int[]{1, 1}, new int[]{2, 2}, nonZeroBias))
                .layer(4, new LocalResponseNormalization.Builder().name("lrn2").build())
                .layer(5, maxPool("maxpool2", new int[]{2, 2}))
                .layer(6, conv3x3("cnn3", 384, nonZeroBias))
                .layer(7, conv3x3("cnn4", 384, nonZeroBias))
                .layer(8, new LocalResponseNormalization.Builder().name("lrn3").build())
                .layer(9, maxPool("maxpool3", new int[]{3, 3}))
                .layer(10, fullyConnected("ffn1", 2048, nonZeroBias, dropOut))
                .layer(11, fullyConnected("ffn2", 2048, nonZeroBias, dropOut))
                .layer(12, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .name("output")
                        .nOut(numLabels)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutional(HEIGHT, WIDTH, CHANNELS))
                .build();
    }

    private ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
    }

    private ConvolutionLayer conv3x3(String name, int out, double bias) {
        return new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1}).name(name).nOut(out).biasInit(bias).build();
    }

    private ConvolutionLayer conv5x5(String name, int out, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(new int[]{5, 5}, stride, pad).name(name).nOut(out).biasInit(bias).build();
    }

    private SubsamplingLayer maxPool(String name, int[] kernel) {
        return new SubsamplingLayer.Builder(kernel, new int[]{2, 2}).name(name).build();
    }

    private DenseLayer fullyConnected(String name, int out, double bias, double dropOut) {
        return new DenseLayer.Builder().name(name).nOut(out).biasInit(bias).dropOut(dropOut).build();
    }

}
