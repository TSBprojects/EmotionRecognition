package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeries;

public interface EmotionFeature extends Feature {

    int getValue();

    void apply(TimeSeries wholeTarget, long timestamp, VideoFrame videoFrame);

    EmotionFeature copy();
}
