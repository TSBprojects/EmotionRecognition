package ru.sstu.vak.emotionRecognition.identifyEmotion.video;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class VideoFrame {

    @JsonProperty("frameIndex")
    private int frameIndex;

    @JsonProperty("videoFaces")
    private List<VideoFace> videoFaces;

    public VideoFrame(int frameIndex, List<VideoFace> videoFaces) {
        this.frameIndex = frameIndex;
        this.videoFaces = videoFaces;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public List<VideoFace> getVideoFaces() {
        return videoFaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoFrame)) return false;

        VideoFrame that = (VideoFrame) o;

        if (getFrameIndex() != that.getFrameIndex()) return false;
        return getVideoFaces().equals(that.getVideoFaces());
    }

    @Override
    public int hashCode() {
        int result = getFrameIndex();
        result = 31 * result + getVideoFaces().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "VideoFrame{" +
                "frameIndex=" + frameIndex +
                ", videoFaces=" + videoFaces +
                '}';
    }
}
