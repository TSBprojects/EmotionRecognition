package ru.sstu.vak.emotionRecognition.identifyEmotion.dataInfo.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.sstu.vak.emotionRecognition.graphicPrep.imageProcessing.ImageCorrector;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataFace.impl.VideoFace;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

public class FrameInfo extends VideoFrame {

    @JsonIgnore
    private BufferedImage processedImage;

    public FrameInfo(FrameInfo frameInfo) {
        super(frameInfo);
        this.processedImage = ImageCorrector.copyBufferedImage(frameInfo.getProcessedImage());
    }

    public FrameInfo(int frameIndex, BufferedImage processedImage, List<VideoFace> videoFaces) {
        super(frameIndex, videoFaces);
        this.processedImage = processedImage;
    }

    public BufferedImage getProcessedImage() {
        return processedImage;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FrameInfo)) return false;
        if (!super.equals(o)) return false;
        FrameInfo frameInfo = (FrameInfo) o;
        return getProcessedImage().equals(frameInfo.getProcessedImage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getProcessedImage());
    }

    @Override
    public String toString() {
        return "FrameInfo{" +
                "processedImage=" + processedImage +
                ", frameIndex=" + frameIndex +
                ", videoFaces=" + videoFaces +
                '}';
    }
}
