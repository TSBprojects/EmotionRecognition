package ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Deprecated
public class PixelSmoother {

    private static final Logger log = LogManager.getLogger(PixelSmoother.class.getName());

    private PixelSmoother() {
    }

    public static void smooth(Path readFrom, Path writeTo) throws IOException {
        log.info("Smoothing all images from '{}' to '{}' ...", readFrom.toString(), writeTo.toString());
        List<Path> imagePaths = Files.walk(readFrom)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        double avgIndex = getAvgIndex(imagePaths);

        int count = 0;
        for (Path imagePath : imagePaths) {

            Path classPath = Paths.get(writeTo + "\\" + imagePath.getParent().getFileName());
            if (!Files.exists(classPath)) {
                Files.createDirectory(classPath);
            }

            Path newImagePath = Paths.get(classPath + "\\" + imagePath.getFileName());
            BufferedImage smoothedImage = smoothImage(ImageCorrector.eqHist(ImageIO.read(imagePath.toFile())), avgIndex);
            ImageIO.write(smoothedImage, "png", newImagePath.toFile());
            System.out.println(++count);
        }
    }

    public static BufferedImage smoothImage(BufferedImage image, double index) {
        log.debug("Smoothing image...");

        byte[] bytes = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        int[] res = new int[bytes.length];

        double imageIndex = getImageIndex(bytes);
        double extraScore = index - imageIndex;

        for (int i = 0; i < bytes.length; i++) {
            int originByte = Byte.toUnsignedInt(bytes[i]);

            int newByte = originByte + (int) (255 * extraScore);
            if (newByte > 255) {
                newByte = 255;
            } else if (newByte < 0) {
                newByte = 0;
            }
            res[i] = newByte;
        }

        return getImage(res);
    }

    public static double getAvgIndex(List<Path> imagePaths) throws IOException {
        log.debug("Getting avg index of all the pictures...");

        double sum = 0;
        for (Path imagePath : imagePaths) {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            sum += getImageIndex(image);
        }
        return sum / imagePaths.size();
    }

    public static double getImageIndex(BufferedImage image) {
        byte[] bytes = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        return getImageIndex(bytes);
    }


    private static BufferedImage getImage(int[] image) {
        log.debug("Getting BufferedImage from int[] array of bytes");
        BufferedImage outputImage = new BufferedImage(48, 48, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = outputImage.getRaster();
        raster.setSamples(0, 0, 48, 48, 0, image);
        return outputImage;
    }

    private static double getImageIndex(byte[] bytes) {
        log.debug("Getting image pixel index");
        int sum = 0;
        for (byte aByte : bytes) {
            sum += Byte.toUnsignedInt(aByte);
        }
        return (sum / (double) bytes.length) / 255.0;
    }
}
