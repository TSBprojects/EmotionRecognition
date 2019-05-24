package ru.sstu.vak.emotionRecognition.identifyEmotion.dataInfo.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataFace.impl.ImageFace;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataInfo.DataInfo;

import java.awt.image.BufferedImage;
import java.util.List;

public class ImageInfo extends DataInfo {

    @JsonIgnore
    private BufferedImage processedImage;

    @JsonProperty("faces")
    private List<ImageFace> imageFaces;


    public ImageInfo(BufferedImage processedImage, List<ImageFace> imageFaces) {
        this.processedImage = processedImage;
        this.imageFaces = imageFaces;
    }


    public BufferedImage getProcessedImage() {
        return processedImage;
    }

    public List<ImageFace> getImageFaces() {
        return imageFaces;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageInfo)) return false;

        ImageInfo imageInfo = (ImageInfo) o;

        if (!getProcessedImage().equals(imageInfo.getProcessedImage())) return false;
        return getImageFaces().equals(imageInfo.getImageFaces());
    }

    @Override
    public int hashCode() {
        int result = getProcessedImage().hashCode();
        result = 31 * result + getImageFaces().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ImageInfo{" +
                "imageFaces=" + imageFaces +
                '}';
    }
}
