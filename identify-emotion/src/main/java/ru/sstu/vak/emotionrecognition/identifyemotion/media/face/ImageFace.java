package ru.sstu.vak.emotionrecognition.identifyemotion.media.face;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.image.BufferedImage;
import ru.sstu.vak.emotionrecognition.common.Prediction;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageCorrector;

public class ImageFace extends MediaFace {

    @JsonIgnore
    private final BufferedImage faceImage;

    public ImageFace(ImageFace imageFace) {
        super(imageFace.getPrediction(), new Location(imageFace.getLocation()));
        this.faceImage = ImageCorrector.copyBufferedImage(imageFace.getFaceImage());
    }

    public ImageFace(Prediction prediction, Location location, BufferedImage faceImage) {
        super(prediction, location);
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
