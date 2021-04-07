package ru.sstu.vak.emotionrecognition.identifyemotion.media.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

public class VideoInfo implements MediaInfo {

    @JsonProperty("frames")
    private List<VideoFrame> frames;


    public VideoInfo(VideoInfo videoInfo) {
        this.frames = videoInfo.getFrames().stream()
                .map(VideoFrame::new)
                .collect(Collectors.toList());
    }

    public VideoInfo(List<VideoFrame> frames) {
        this.frames = frames;
    }

    public List<VideoFrame> getFrames() {
        return frames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoInfo)) return false;

        VideoInfo videoInfo = (VideoInfo) o;

        return getFrames().equals(videoInfo.getFrames());
    }

    @Override
    public int hashCode() {
        return getFrames().hashCode();
    }

    @Override
    public String toString() {
        return "VideoInfo{" +
                "frames=" + frames +
                '}';
    }
}
