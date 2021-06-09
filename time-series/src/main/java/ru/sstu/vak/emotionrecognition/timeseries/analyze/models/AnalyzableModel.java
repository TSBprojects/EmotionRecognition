package ru.sstu.vak.emotionrecognition.timeseries.analyze.models;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Map;
import ru.sstu.vak.emotionrecognition.common.Satisfiable;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.endpoint.Endpoint;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.EmotionFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.MetaFeature;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "type")
public interface AnalyzableModel extends Satisfiable {

    String getName();

    void setName(String name);

    boolean isStrictly();

    void setStrictly(boolean strictly);

    AutoIncrementMap<MetaFeature> getMetaFeatures();

    AutoIncrementMap<EmotionFeature> getFeatures();

    Map<Integer, Endpoint> getEndpoints();
}
