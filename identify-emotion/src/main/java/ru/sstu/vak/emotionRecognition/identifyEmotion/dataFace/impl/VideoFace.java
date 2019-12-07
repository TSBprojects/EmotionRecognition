package ru.sstu.vak.emotionRecognition.identifyEmotion.dataFace.impl;

import ru.sstu.vak.emotionRecognition.common.Emotion;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataFace.DataFace;

public class VideoFace extends DataFace {
    public VideoFace(VideoFace videoFace) {
        super(videoFace);
    }

    public VideoFace(Emotion emotion, Location location) {
        super(emotion, location);
    }
}
