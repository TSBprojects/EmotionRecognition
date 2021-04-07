package ru.sstu.vak.emotionrecognition.identifyemotion.datainfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageCorrector;
import ru.sstu.vak.emotionrecognition.identifyemotion.dataface.impl.ImageFace;

public class ImageInfo implements MediaInfo {

    @JsonIgnore
    private BufferedImage processedImage;

    @JsonProperty("faces")
    private List<ImageFace> imageFaces;


    public ImageInfo(ImageInfo imageInfo) {
        this.processedImage = ImageCorrector.copyBufferedImage(imageInfo.getProcessedImage());
        this.imageFaces = imageInfo.getImageFaces().stream()
                .map(ImageFace::new)
                .collect(Collectors.toList());
    }

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
