package ru.sstu.vak.emotionRecognition.identifyEmotion.dataFace.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.sstu.vak.emotionRecognition.common.Emotion;
import ru.sstu.vak.emotionRecognition.graphicPrep.imageProcessing.ImageCorrector;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataFace.DataFace;

import java.awt.image.BufferedImage;

public class ImageFace extends DataFace {

    @JsonIgnore
    private BufferedImage faceImage;

    public ImageFace(ImageFace imageFace) {
        super(imageFace.getEmotion(), new Location(imageFace.getLocation()));
        this.faceImage = ImageCorrector.copyBufferedImage(imageFace.getFaceImage());
    }

    public ImageFace(Emotion emotion, Location location, BufferedImage faceImage) {
        super(emotion, location);
        this.faceImage = faceImage;
    }

    public BufferedImage getFaceImage() {
        return faceImage;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageFace)) return false;
        if (!super.equals(o)) return false;

        ImageFace imageFace = (ImageFace) o;

        return getFaceImage().equals(imageFace.getFaceImage());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getFaceImage().hashCode();
        return result;
    }

}
