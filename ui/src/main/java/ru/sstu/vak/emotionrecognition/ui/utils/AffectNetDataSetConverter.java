package ru.sstu.vak.emotionrecognition.ui.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static ru.sstu.vak.emotionrecognition.cnn.FeedForwardCNN.INPUT_HEIGHT;
import static ru.sstu.vak.emotionrecognition.cnn.FeedForwardCNN.INPUT_WIDTH;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageCorrector;

public class AffectNetDataSetConverter {

    private static int imageCount;

    private static int faceCount;

    private AffectNetDataSetConverter() {
    }

    public static void convert(Path csvFile, Path imagesPath, Path writeTo) throws IOException {
        imageCount = 0;
        faceCount = 0;

        int rowsCount = (int) Files.lines(csvFile).count();

        Path preparedData = mkDir(Paths.get(writeTo.getParent() + "\\" + writeTo.getFileName() + "_processed"));

        Files.lines(csvFile).forEach(line -> {
            final char firstLineSymbol = line.charAt(0);
            if (firstLineSymbol == '0' || firstLineSymbol == '1' || firstLineSymbol == '2'
                    || firstLineSymbol == '3' || firstLineSymbol == '4' || firstLineSymbol == '7'
                    || firstLineSymbol == '5' || firstLineSymbol == '6'
                    || firstLineSymbol == '8' || firstLineSymbol == '9') {

                imageCount++;

                final String[] params = line.split(",");
                final String imgPath = params[0].replace("/", "\\");
                final int faceX = Integer.parseInt(params[1]);
                final int faceY = Integer.parseInt(params[2]);
                final int faceWidth = Integer.parseInt(params[3]);
                final int faceHeight = Integer.parseInt(params[4]);
                final int emotionId = Integer.parseInt(params[6]);
                final Rect faceRect = new Rect(faceX, faceY, faceWidth, faceHeight);

                Mat face = null;
                try {
                    face = imread(imagesPath + "\\" + imgPath).apply(faceRect);
                } catch (Exception e) {
                    System.out.println(imagesPath + "\\" + imgPath);
                    e.printStackTrace();
                }

                if (face != null) {

                    face = ImageCorrector.resize(face, INPUT_WIDTH, INPUT_HEIGHT);

                    if (face.channels() > 1) {
                        ImageCorrector.toGrayScale(face);
                    }

                    Mat preparedFace = ImageCorrector.eqHist(face.clone());

                    Path classFolderPathProcessed = null;
                    Path classFolderPath = null;
                    try {
                        classFolderPathProcessed = getClassPath(emotionId, preparedData);
                        classFolderPath = getClassPath(emotionId, writeTo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String imageName = FilenameUtils.removeExtension(Paths.get(imgPath).getFileName().toString());

                    if (classFolderPath != null) {
                        imwrite(classFolderPathProcessed + "\\" + imageName + ".png", preparedFace);
                        imwrite(classFolderPath + "\\" + imageName + ".png", face);
                        faceCount++;
                    }

                    System.out.printf("image count - %1$s --- face count - %2$s --- done - %3$s\n",
                            imageCount, faceCount, getPercentage(rowsCount, imageCount));


                }
            }
        });
    }


    private static Path mkDir(Path dirPath) throws IOException {
        if (!Files.exists(dirPath)) {
            return Files.createDirectory(dirPath);
        }
        return dirPath;
    }

    private static Path getClassPath(int emotionId, Path writeTo) throws IOException {
        switch (emotionId) {
            case 0: {
                Path classPath = Paths.get(writeTo.toString() + "\\NEUTRAL");
                if (Files.exists(classPath)) {
                    return classPath;
                } else {
                    return Files.createDirectory(classPath);
                }
            }
            case 1: {
                Path classPath = Paths.get(writeTo.toString() + "\\HAPPY");
                if (Files.exists(classPath)) {
                    return classPath;
                } else {
                    return Files.createDirectory(classPath);
                }
            }
            case 2: {
                Path classPath = Paths.get(writeTo.toString() + "\\SAD");
                if (Files.exists(classPath)) {
                    return classPath;
                } else {
                    return Files.createDirectory(classPath);
                }
            }
            case 3: {
                Path classPath = Paths.get(writeTo.toString() + "\\SURPRISE");
                if (Files.exists(classPath)) {
                    return classPath;
                } else {
                    return Files.createDirectory(classPath);
                }
            }
            case 4: {
                Path classPath = Paths.get(writeTo.toString() + "\\FEAR");
                if (Files.exists(classPath)) {
                    return classPath;
                } else {
                    return Files.createDirectory(classPath);
                }
            }
            case 5: {
                Path classPath = Paths.get(writeTo.toString() + "\\DISGUST");
                if (Files.exists(classPath)) {
                    return classPath;
                } else {
                    return Files.createDirectory(classPath);
                }
            }
            case 6: {
                Path classPath = Paths.get(writeTo.toString() + "\\ANGER");
                if (Files.exists(classPath)) {
                    return classPath;
                } else {
                    return Files.createDirectory(classPath);
                }
            }
            default: {
                return null;
            }
        }
    }

    private static double getPercentage(int all, int current) {
        return ((int) (1000 * current / all)) / 1000.0;
    }

}
