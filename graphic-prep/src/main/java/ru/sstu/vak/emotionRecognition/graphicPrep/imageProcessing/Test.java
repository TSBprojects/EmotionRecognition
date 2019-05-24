package ru.sstu.vak.emotionRecognition.graphicPrep.imageProcessing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

@Deprecated
public class Test {

    private static final Logger log = LogManager.getLogger(Test.class.getName());

    private Test() {
    }


    public static BufferedImage go(BufferedImage image) {
        log.debug("Smoothing image...");

        byte[] bytes = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        int[] res = new int[bytes.length];


        for (int i = 0; i < bytes.length; i++) {
            int originByte = Byte.toUnsignedInt(bytes[i]);

            if (originByte < 100) {
                originByte = (int) (100 + originByte / 255.0 * 155);
            }

            res[i] = originByte;
        }

        return getImage(res);
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
