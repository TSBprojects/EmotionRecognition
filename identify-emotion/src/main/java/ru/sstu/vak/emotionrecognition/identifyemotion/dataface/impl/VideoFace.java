package ru.sstu.vak.emotionrecognition.identifyemotion.dataface.impl;

import ru.sstu.vak.emotionrecognition.common.Emotion;
import ru.sstu.vak.emotionrecognition.identifyemotion.dataface.DataFace;

public class VideoFace extends DataFace {
    public VideoFace(VideoFace videoFace) {
        super(videoFace);
    }

    public VideoFace(Emotion emotion, Location location) {
        super(emotion, location);
    }
}
