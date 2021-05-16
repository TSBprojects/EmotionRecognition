package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import ru.sstu.vak.emotionrecognition.common.Descriptable;
import ru.sstu.vak.emotionrecognition.common.Invalidable;
import ru.sstu.vak.emotionrecognition.common.Satisfiable;

public interface Feature extends Descriptable, Satisfiable, Invalidable {

    void clear();

    Feature copy();
}
