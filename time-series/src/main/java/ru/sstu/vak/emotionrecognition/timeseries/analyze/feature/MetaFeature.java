package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import java.util.SortedMap;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;

public interface MetaFeature extends Feature {

    void apply(AutoIncrementMap<EmotionFeature> features);

    SortedMap<Integer, String> getRule();

    MetaFeature copy();
}
