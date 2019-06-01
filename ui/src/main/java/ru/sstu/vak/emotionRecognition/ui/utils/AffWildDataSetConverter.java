package ru.sstu.vak.emotionRecognition.ui.utils;

import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import ru.sstu.vak.emotionRecognition.graphicPrep.imageProcessing.ImageConverter;
import ru.sstu.vak.emotionRecognition.graphicPrep.iterators.frameIterator.FrameIterator;
import ru.sstu.vak.emotionRecognition.graphicPrep.iterators.frameIterator.impl.FrameIteratorBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;


public class AffWildDataSetConverter {

    private static final double MIN_ACCURACY = 0.999;

    //    private static int highAccuracyFaceCount;
    private static int faceCount;
    private static int frameCount;
    private static int videoCount;

    private static final int GRAB_EVERY_N_FACE = 55;

    private AffWildDataSetConverter() {
    }

    public static void convert(Path videosFolder, Path pointsFolder, Path writeTo) throws IOException {
        frameCount = 1;
        videoCount = 1;
        faceCount = 1;
//        highAccuracyFaceCount = 1;

//        FeedForwardCNN cnn = new FeedForwardCNN("G:\\Main things\\Study\\DIPLOMA\\My\\" +
//                "EmotionRecognition\\сompiled application\\bestModel(0.75).bin");
        FrameIterator frameIterator = new FrameIteratorBase();

        AtomicInteger grabEvery = new AtomicInteger(GRAB_EVERY_N_FACE);
        Files.walk(videosFolder)
                .filter(Files::isRegularFile)
                .forEach(videoPath -> {
                    try {

                        System.out.println("--------- NEW VIDEO ---------");

                        String videoName = FilenameUtils.removeExtension(videoPath.getFileName().toString());
                        frameIterator.start(videoPath.toString(), frame -> {

                            if (frameCount == grabEvery.get()) {
                                grabEvery.addAndGet(GRAB_EVERY_N_FACE);

                                Path ptsFile = Paths.get(pointsFolder + "\\" + videoName + "\\" + frameCount + ".pts");
                                Rect faceBox = parsePtsFile(ptsFile, frame.imageWidth, frame.imageHeight);

                                if (faceBox != null) {
                                    Mat croppedFace = ImageConverter.toMat(frame).apply(faceBox);
                                    imwrite(writeTo + "\\" + faceCount + ".jpg", croppedFace);

                                    System.out.println("video '" + videoName + "' " + videoCount + " -- frame " + frameCount +
                                            " -- all faces " + faceCount);

                                    faceCount++;
                                }

                            }

                            frameCount++;
                        });

                        while (frameIterator.isRun());

                        grabEvery.set(GRAB_EVERY_N_FACE);
                        frameCount = 0;
                        videoCount++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        System.out.println("--- ALL DONE ---");
    }

    private static Rect parsePtsFile(Path ptsFile, int maxWidth, int maxHeight) {
        List<String> lines;
        try {
            lines = Files.readAllLines(ptsFile);
        } catch (IOException e) {
            return null;
        }

        String[] coords = lines.get(3).split(" ");

        int x = (int) Double.parseDouble(coords[0]);
        int y = (int) Double.parseDouble(coords[1]);
        int size = (int) Double.parseDouble(lines.get(4).split(" ")[1]) - y;

        int boundX = x + size;
        int boundY = y + size;

        if (boundX > maxWidth) {
            size -= boundX - maxWidth;
        }
        if (boundY > maxHeight) {
            size -= boundY - maxHeight;
        }
        return new Rect(x, y, size, size);
    }

    private static Path mkDir(Path dirPath) throws IOException {
        if (!Files.exists(dirPath)) {
            return Files.createDirectory(dirPath);
        }
        return dirPath;
    }

    private static long filesCount(Path folderPath) throws IOException {
        return Files.walk(folderPath)
                .filter(path -> Files.isRegularFile(path))
                .count();
    }

}


//                    try {
//                            System.out.println("--------- NEW VIDEO ---------");
//
//                            String videoName = FilenameUtils.removeExtension(videoPath.getFileName().toString());
//                            frameIterator.start(videoPath.toString(), frame -> {
//                            Path ptsFile = Paths.get(pointsFolder + "\\" + videoName + "\\" + frameCount + ".pts");
//                            Rect faceBox = parsePtsFile(ptsFile, frame.imageWidth, frame.imageHeight);
//
//                            if (faceBox != null) {
//                            Mat croppedFace = ImageConverter.toMat(frame).apply(faceBox);
//
//                            if (croppedFace.channels() > 1) {
//                            ImageConverter.toGrayScale(croppedFace);
//                            }
//
//                            Mat preparedFace = FacePreProcessing.process(croppedFace, INPUT_WIDTH, INPUT_HEIGHT, true);
//
//                            try {
//                            Emotion emotion = cnn.predict(preparedFace);
//                            double emotionProbability = emotion.getProbability();
//
//                            if (emotionProbability >= MIN_ACCURACY) {
//                            Path emDirPath = Paths.get(writeTo + "\\" + emotion.getValue());
//                            imwrite(mkDir(emDirPath) + "\\" + highAccuracyFaceCount + ".png", preparedFace);
//                            highAccuracyFaceCount++;
//                            } else {
//                            Path emDirPath = Paths.get(
//                            mkDir(Paths.get(writeTo + "\\LESS_THAN_" + MIN_ACCURACY)) +
//                            "\\" + emotion.getValue()
//                            );
//                            imwrite(mkDir(emDirPath) + "\\" + faceCount + ".png", preparedFace);
//                            faceCount++;
//                            }
//                            System.out.println("video '" + videoName + "' " + videoCount + " -- frame " + frameCount +
//                            " -- high accuracy faces " + highAccuracyFaceCount +
//                            " -- all faces " + faceCount +
//                            " -- " + emotion.getValue() + " " + emotionProbability);
//                            } catch (Exception e) {
//                            e.printStackTrace();
//                            }
//                            }
//
//                            frameCount++;
//                            });
//
//                            while (frameIterator.isRun()) ;
//
//                            frameCount = 0;
//                            videoCount++;
//                            } catch (Exception e) {
//                            e.printStackTrace();
//                            }
