package ru.sstu.vak.emotionRecognition.identifyEmotion.dataInfo.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataInfo.DataInfo;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataInfo.VideoFrame;

import java.util.List;

public class VideoInfo extends DataInfo {

    @JsonProperty("frames")
    private List<VideoFrame> frames;


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
