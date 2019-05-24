package ru.sstu.vak.emotionRecognition.graphicPrep.iterators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class PixelIterator {

    private static final Logger log = LogManager.getLogger(PixelIterator.class.getName());

    private PixelIterator() {
    }


    @FunctionalInterface
    public interface PixelReader {
        /**
         * @param pixel representing pixel bytes in ABGR format
         */
        void onNextPixel(int x, int y, int[] pixel);
    }

    @FunctionalInterface
    public interface PixelWriter {
        /**
         * @param pixel representing pixel bytes in ABGR format
         */
        int[] onNextPixel(int x, int y, int[] pixel);
    }


    /**
     * @param image       image to walk
     * @param pixelReader callback for image pixels, parameter -
     *                    int array representing pixel bytes in ABGR format
     */
    public static void walk(BufferedImage image, PixelReader pixelReader) {
        log.info("Walking through image pixels...");
        PixelGrabber pixelGrabber = new PixelGrabber(image);
        for (int i = 0; i < pixelGrabber.getWidth(); i++) {
            for (int j = 0; j < pixelGrabber.getHeight(); j++) {
                pixelReader.onNextPixel(i, j, pixelGrabber.grab(i, j));
            }
        }
    }

    /**
     * @param image       image to change
     * @param pixelWriter callback for image pixels, parameter -
     *                    int array representing pixel bytes in ABGR format
     * @return changed image
     */
    public static BufferedImage change(BufferedImage image, PixelWriter pixelWriter) {
        log.info("Walking through image pixels and apply returned pixels to new image...");

        PixelGrabber pixelGrabber = new PixelGrabber(image);

        if (pixelGrabber.isGrayScale()) {
            int[] res = new int[pixelGrabber.getWidth() * pixelGrabber.getHeight()];
            for (int i = 0; i < pixelGrabber.getWidth(); i++) {
                for (int j = 0; j < pixelGrabber.getHeight(); j++) {
                    res[(j * pixelGrabber.getWidth()) + i] = pixelWriter.onNextPixel(i, j, pixelGrabber.grab(i, j))[0];
                }
            }
            getImageGRAY(res, image);
            return image;
        } else {
            for (int i = 0; i < pixelGrabber.getWidth(); i++) {
                for (int j = 0; j < pixelGrabber.getHeight(); j++) {

                    int[] pixel = pixelWriter.onNextPixel(i, j, pixelGrabber.grab(i, j));

                    int alpha = 0;
                    if (pixelGrabber.hasAlphaChannel()) {
                        alpha = pixel[0];
                    }
                    int blue = pixel[1];
                    int green = pixel[2];
                    int red = pixel[3];

                    int newPixel = colorToRGB(alpha, red, green, blue);

                    image.setRGB(i, j, newPixel);
                }
            }
            return image;
        }

    }


    private static void getImageGRAY(int[] image, BufferedImage originImage) {
        WritableRaster raster = originImage.getRaster();
        raster.setSamples(0, 0, originImage.getWidth(), originImage.getHeight(), 0, image);
    }

    private static int colorToRGB(int alpha, int red, int green, int blue) {

        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;

    }

}