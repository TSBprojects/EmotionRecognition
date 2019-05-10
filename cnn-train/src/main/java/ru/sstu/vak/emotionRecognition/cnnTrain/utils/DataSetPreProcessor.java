package ru.sstu.vak.emotionRecognition.cnnTrain.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import ru.sstu.vak.emotionRecognition.cnn.FeedForwardCNN;
import ru.sstu.vak.emotionRecognition.faceDetector.HaarFaceDetector;
import ru.sstu.vak.emotionRecognition.graphicPrep.FacePreProcessing;
import ru.sstu.vak.emotionRecognition.graphicPrep.ImageConverter;
import ru.sstu.vak.emotionRecognition.graphicPrep.PixelSmoother;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGRA2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

public class DataSetPreProcessor {

    private static final Logger log = LogManager.getLogger(DataSetPreProcessor.class.getName());

    private DataSetPreProcessor() {
    }

    public static void process(Path readFrom, Path writeTo) throws IOException {
        Path preparedData = createDir(Paths.get(writeTo.getParent() + "\\" + writeTo.getFileName() + "_processed"));

        HaarFaceDetector haarFaceDetector = new HaarFaceDetector();

        List<Path> imagesPath = Files.walk(readFrom)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());


        for (Path imagePath : imagesPath) {
            String imageParentName = imagePath.getParent().getFileName().toString();
            BufferedImage image = ImageIO.read(imagePath.toFile());

            Mat matImage = ImageConverter.toMat(image);
            Map<Rect, Mat> faces = haarFaceDetector.detect(matImage);

            Path classPathOriginal = createDir(Paths.get(writeTo + "\\" + imageParentName));
            Path classPathProcessed = createDir(Paths.get(preparedData + "\\" + imageParentName));

            for (Map.Entry<Rect, Mat> entry : faces.entrySet()) {
                Rect rect = entry.getKey();
                Mat face = entry.getValue();


                Mat matFaceImage = matImage.apply(rect);
                if (matFaceImage.channels() > 1) {
                    log.debug("Convert Mat image to grayscale format...");
                    cvtColor(matFaceImage, matFaceImage, COLOR_BGRA2GRAY);
                }

                Mat resizedFaceImage = ImageConverter.resize(matFaceImage, FeedForwardCNN.WIDTH, FeedForwardCNN.HEIGHT);
                Mat resizedProcessedFaceImage = ImageConverter.resize(face, FeedForwardCNN.WIDTH, FeedForwardCNN.HEIGHT);

                BufferedImage faceImage = ImageConverter.toBufferedImage(resizedFaceImage);
                writeImage(faceImage, classPathOriginal, imagePath);

                BufferedImage processedImage = PixelSmoother.smoothImage(
                        ImageConverter.toBufferedImage(resizedProcessedFaceImage),
                        FacePreProcessing.DATA_SET_IMAGE_INDEX,
                        true
                );
                writeImage(processedImage, classPathProcessed, imagePath);
            }
        }

    }

    private static void writeImage(BufferedImage image, Path dirPath, Path imagePath) throws IOException {
        ImageIO.write(
                image,
                "png",
                new File(dirPath + "\\" + String.valueOf(imagePath.getFileName()))
        );
    }

    private static Path createDir(Path dirPath) throws IOException {
        if (!Files.exists(dirPath)) {
            return Files.createDirectory(dirPath);
        }
        return dirPath;
    }
}
