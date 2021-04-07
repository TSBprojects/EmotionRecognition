package ru.sstu.vak.emotionrecognition.cnntrain.preprocessing;

import java.awt.Rectangle;
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
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static ru.sstu.vak.emotionrecognition.cnn.FeedForwardCNN.INPUT_HEIGHT;
import static ru.sstu.vak.emotionrecognition.cnn.FeedForwardCNN.INPUT_WIDTH;
import ru.sstu.vak.emotionrecognition.facedetector.HaarFaceDetector;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.FacePreProcessing;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageConverter;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageCorrector;

public class DataSetPreProcessor {

    private static final Logger log = LogManager.getLogger(DataSetPreProcessor.class.getName());

    private DataSetPreProcessor() {
    }


    public static void transformRawDataSet(Path readFrom, Path writeTo, boolean detectFaces) throws IOException {
        log.info("Transform raw dataset by folder path '" + readFrom + "' and write to '" + writeTo + "'");

        Path preparedData = createDir(Paths.get(writeTo.getParent() + "\\" + writeTo.getFileName() + "_processed"));

        List<Path> imagesPath = Files.walk(readFrom)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        if (detectFaces) {
            HaarFaceDetector haarFaceDetector = new HaarFaceDetector();

            for (Path imagePath : imagesPath) {
                String imageParentName = imagePath.getParent().getFileName().toString();
                BufferedImage image = ImageIO.read(imagePath.toFile());

                Mat matImage = ImageConverter.toMat(image);
                Map<Rect, Mat> faces = haarFaceDetector.detect(matImage,false);

                Path classPathOriginal = createDir(Paths.get(writeTo + "\\" + imageParentName));
                Path classPathProcessed = createDir(Paths.get(preparedData + "\\" + imageParentName));

                for (Map.Entry<Rect, Mat> entry : faces.entrySet()) {
                    Rect rect = entry.getKey();

                    Mat matFaceImage = matImage.apply(rect);

                    Mat resizedFaceImage = ImageCorrector.resize(matFaceImage, INPUT_WIDTH, INPUT_HEIGHT);
                    imwrite(classPathOriginal + "\\" + imagePath.getFileName(), resizedFaceImage);

                    Mat resizedProcessedFaceImage = FacePreProcessing.process(matImage.apply(rect), INPUT_WIDTH, INPUT_HEIGHT);
                    imwrite(classPathProcessed + "\\" + imagePath.getFileName(), resizedProcessedFaceImage);
                }
            }
        } else {
            for (Path imagePath : imagesPath) {
                String imageParentName = imagePath.getParent().getFileName().toString();

                Mat matFaceImage = imread(imagePath.toString());

                Path classPathOriginal = createDir(Paths.get(writeTo + "\\" + imageParentName));
                Path classPathProcessed = createDir(Paths.get(preparedData + "\\" + imageParentName));

                Mat resizedFaceImage = ImageCorrector.resize(matFaceImage, INPUT_WIDTH, INPUT_HEIGHT);
                imwrite(classPathOriginal + "\\" + imagePath.getFileName(), resizedFaceImage);

                Mat resizedProcessedFaceImage = FacePreProcessing.process(matFaceImage, INPUT_WIDTH, INPUT_HEIGHT);
                imwrite(classPathProcessed + "\\" + imagePath.getFileName(), resizedProcessedFaceImage);
            }
        }

    }

    public static void dataSetAugmentation(Path readFrom, Path writeTo) throws IOException {
        log.info("Start augmentation of dataset by folder path '" + readFrom + "' and write to '" + writeTo + "'");

        List<Path> imagesPath = Files.walk(readFrom)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        int count = 0;
        for (Path imagePath : imagesPath) {
            final String imageParentName = imagePath.getParent().getFileName().toString();
            final Path classPathOriginal = createDir(Paths.get(writeTo + "\\" + imageParentName));

            Mat matFaceImage = imread(imagePath.toString());

            if (matFaceImage.channels() > 1) {
                ImageCorrector.toGrayScale(matFaceImage);
            }

            Mat matFaceImageEqHist = ImageCorrector.eqHist(matFaceImage);
            imwrite(classPathOriginal + "\\" + count + "_eqHist.png", matFaceImageEqHist);
            ImageIO.write(
                    flipHorizontally(ImageConverter.toBufferedImage(matFaceImageEqHist)),
                    "png", new File(classPathOriginal + "\\" + count + "_eqHist_hor_flip.png")
            );

            BufferedImage bfFaceImageEqHistLowGamma = ImageCorrector.gammaCorrection(ImageConverter.toBufferedImage(matFaceImageEqHist), 0.5);
            ImageIO.write(bfFaceImageEqHistLowGamma, "png", new File(classPathOriginal + "\\" + count + "_low_gamma.png"));
            ImageIO.write(flipHorizontally(bfFaceImageEqHistLowGamma), "png", new File(classPathOriginal + "\\" + count + "_low_gamma_hor_flip.png"));

            BufferedImage bfFaceImageEqHistHighGamma = ImageCorrector.gammaCorrection(ImageConverter.toBufferedImage(matFaceImageEqHist), 3);
            ImageIO.write(bfFaceImageEqHistHighGamma, "png", new File(classPathOriginal + "\\" + count + "_high_gamma.png"));
            ImageIO.write(flipHorizontally(bfFaceImageEqHistHighGamma), "png", new File(classPathOriginal + "\\" + count + "_high_gamma_hor_flip.png"));

            count++;
        }
    }

    @Deprecated
    public static void rotateDataSet(Path dataSet) throws IOException {
        processDataSet(dataSet, "_cr_rot", DataSetPreProcessor::transformImage);
    }

    public static void horFlipDataSet(Path dataSet) throws IOException {
        log.info("Horizontal flipping dataset by folder path '" + dataSet + "' and write to the same folder");
        processDataSet(dataSet, "_flipped", DataSetPreProcessor::flipHorizontally);
    }


    private static void processDataSet(Path dataSet, String postfix, ImageProcessor imageProcessor) throws IOException {
        Files.walk(dataSet).filter(Files::isRegularFile).forEach(imagePath -> {
            try {
                final String imageParentName = imagePath.getParent().getFileName().toString();
                final String fileName = FilenameUtils.removeExtension(imagePath.getFileName().toString());
                ImageIO.write(
                        imageProcessor.process(ImageIO.read(imagePath.toFile())),
                        "png",
                        new File(dataSet + "\\" + imageParentName + "\\" + fileName + postfix + ".png")
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
            ImageCorrector.toGrayScale(matImage);
        }
        return ImageConverter.toBufferedImage(ImageCorrector.eqHist(matImage));
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
