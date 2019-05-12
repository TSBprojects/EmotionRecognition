package ru.sstu.vak.emotionRecognition.cnnTrain.preProcessing;

import org.apache.commons.io.FilenameUtils;
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
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class DataSetPreProcessor {

    private static final Logger log = LogManager.getLogger(DataSetPreProcessor.class.getName());

    private DataSetPreProcessor() {
    }


    public static void rotateDataSet(Path dataSet) throws IOException {
        processDataSet(dataSet, "_cr_rot", DataSetPreProcessor::transformImage);
    }

    public static void horFlipDataSet(Path dataSet) throws IOException {
        processDataSet(dataSet, "_flipped", DataSetPreProcessor::flipHorizontally);
    }

    public static void transformRawDataSet(Path readFrom, Path writeTo) throws IOException {
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
                    matFaceImage = ImageConverter.toGrayScale(matFaceImage);
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


    private static void processDataSet(Path dataSet, String prefix, ImageProcessor imageProcessor) throws IOException {
        Files.walk(dataSet).filter(Files::isRegularFile).forEach(imagePath -> {
            try {
                final String imageParentName = imagePath.getParent().getFileName().toString();
                final String fileName = FilenameUtils.removeExtension(imagePath.getFileName().toString());
                ImageIO.write(
                        imageProcessor.process(ImageIO.read(imagePath.toFile())),
                        "png",
                        new File(dataSet + "\\" + imageParentName + "\\" + fileName + prefix + ".png")
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static BufferedImage transformImage(BufferedImage image) {

        int x = ThreadLocalRandom.current().nextInt(3, 11);
        int y = ThreadLocalRandom.current().nextInt(3, 11);
        double theta = ThreadLocalRandom.current().nextDouble(-0.25, 0.25);

        AffineTransform transform = new AffineTransform();
        transform.rotate(theta, image.getWidth() / 2, image.getHeight() / 2);
        AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        image = transformOp.filter(image, null);

        transform.setToScale(1.25, 1.25);
        transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage scaledImage = transformOp.filter(image, null);

        BufferedImage croppedImage = cropImage(scaledImage, new Rectangle(x, y, 48, 48));

        Mat matImage = ImageConverter.toMat(croppedImage);
        if (matImage.channels() > 1) {
            matImage = ImageConverter.toGrayScale(matImage);
        }
        return ImageConverter.toBufferedImage(ImageConverter.eqHist(matImage));
    }

    private static BufferedImage cropImage(BufferedImage src, Rectangle rect) {
        return src.getSubimage(rect.x, rect.y, rect.width, rect.height);
    }

    private static BufferedImage flipHorizontally(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
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

    private interface ImageProcessor {
        BufferedImage process(BufferedImage image);
    }
}
