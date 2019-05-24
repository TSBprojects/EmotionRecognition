package ru.sstu.vak.emotionRecognition.graphicPrep.iterators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class PixelGrabber {

    private static final Logger log = LogManager.getLogger(PixelGrabber.class.getName());

    private BufferedImage image;
    private byte[] pixels;
    private int width;
    private int height;
    private boolean isGrayScale;
    private boolean hasAlphaChannel;

    public PixelGrabber(BufferedImage image) {
        this.image = image;
        this.pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.isGrayScale = width * height == pixels.length;
        this.hasAlphaChannel = image.getAlphaRaster() != null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isGrayScale() {
        return isGrayScale;
    }

    public boolean hasAlphaChannel() {
        return hasAlphaChannel;
    }

    public byte[] getData() {
        return pixels;
    }

    public int[] grab(int x, int y) {
        log.info("Grabbing pixel (" + x + "," + y + ")");
        if (isGrayScale) {
            return new int[]{Byte.toUnsignedInt(pixels[y * width + x])};
        } else {

            int[] pixel;
            if (hasAlphaChannel) {
                pixel = getPixel(x, y, pixels, 4, image.getWidth());
            } else {
                pixel = getPixel(x, y, pixels, 3, image.getWidth());
            }

            return pixel;
        }
    }


    private int[] getPixel(int x, int y, byte[] pixels, int pixelLength, int imageWidth) {
        int pos = (y * pixelLength * imageWidth) + (x * pixelLength);
        if (pixelLength == 3) {
            return new int[]
                    {
                            0,
                            Byte.toUnsignedInt(pixels[pos++]),
                            Byte.toUnsignedInt(pixels[pos++]),
                            Byte.toUnsignedInt(pixels[pos])
                    };
        } else {
            return new int[]
                    {
                            Byte.toUnsignedInt(pixels[pos++]),
                            Byte.toUnsignedInt(pixels[pos++]),
                            Byte.toUnsignedInt(pixels[pos++]),
                            Byte.toUnsignedInt(pixels[pos])
                    };
        }
    }

}
