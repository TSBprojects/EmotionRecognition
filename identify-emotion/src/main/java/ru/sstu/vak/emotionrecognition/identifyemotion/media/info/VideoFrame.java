package ru.sstu.vak.emotionrecognition.identifyemotion.media.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;

public class VideoFrame implements MediaInfo {

    @JsonProperty("frameIndex")
    protected int frameIndex;

    @JsonProperty("faces")
    protected List<VideoFace> videoFaces;

    public VideoFrame(VideoFrame videoFrame) {
        this.frameIndex = videoFrame.getFrameIndex();
        this.videoFaces = videoFrame.getVideoFaces().stream()
                .map(VideoFace::new)
                .collect(Collectors.toList());
    }

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
