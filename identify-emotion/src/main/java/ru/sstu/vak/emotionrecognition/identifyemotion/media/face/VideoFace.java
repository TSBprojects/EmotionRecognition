package ru.sstu.vak.emotionrecognition.identifyemotion.media.face;

import ru.sstu.vak.emotionrecognition.common.Emotion;

public class VideoFace extends MediaFace {
    public VideoFace(VideoFace videoFace) {
        super(videoFace);
    }

    public VideoFace(Emotion emotion, Location location) {
        super(emotion, location);
    }
}
