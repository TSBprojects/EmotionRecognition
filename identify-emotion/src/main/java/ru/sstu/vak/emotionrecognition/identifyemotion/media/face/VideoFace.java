package ru.sstu.vak.emotionrecognition.identifyemotion.media.face;

import ru.sstu.vak.emotionrecognition.common.Prediction;

public class VideoFace extends MediaFace {
    public VideoFace(VideoFace videoFace) {
        super(videoFace);
    }

    public VideoFace(Prediction prediction, Location location) {
        super(prediction, location);
    }
}
