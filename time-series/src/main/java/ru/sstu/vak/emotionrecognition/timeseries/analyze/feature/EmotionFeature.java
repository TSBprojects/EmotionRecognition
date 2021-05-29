package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeries;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "type")
public interface EmotionFeature extends Feature {

    @JsonIgnore
    int getValue();

    void apply(TimeSeries wholeTarget, long timestamp, VideoFrame videoFrame);

    EmotionFeature copy();
}
