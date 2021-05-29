package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.SortedMap;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "type")
public interface MetaFeature extends Feature {

    void apply(AutoIncrementMap<EmotionFeature> features);

    SortedMap<Integer, String> getRule();

    MetaFeature copy();
}
