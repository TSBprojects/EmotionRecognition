package ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer;

import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;

public interface Listenable {

    void addVideoFrameListener(VideoFrameListener videoFrameListener);

    void removeVideoFrameListener(VideoFrameListener videoFrameListener);

    @FunctionalInterface
    interface VideoFrameListener {
        void onNextFrame(VideoFrame videoFrame);
    }
}
