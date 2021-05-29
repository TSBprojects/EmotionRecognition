package ru.sstu.vak.emotionrecognition.timeseries.analyze.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.var;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementHashMap;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.EmotionFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.Feature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.MetaFeature;

@Data
public class SimpleAnalyzableModel implements AnalyzableModel {

    private String name;

    private boolean strictly;

    private final AutoIncrementMap<MetaFeature> metaFeatures;

    private final AutoIncrementMap<EmotionFeature> features;

    @JsonCreator
    public SimpleAnalyzableModel(
        @JsonProperty("name") String name,
        @JsonProperty("strictly") boolean strictly,
        @JsonProperty("features") Map<Integer, EmotionFeature> features,
        @JsonProperty("metaFeatures") Map<Integer, MetaFeature> metaFeatures
    ) {
        this.name = name;
        this.strictly = strictly;
        this.features = new AutoIncrementHashMap<>(new ConcurrentHashMap<>(features));
        this.metaFeatures = new AutoIncrementHashMap<>(new ConcurrentHashMap<>(metaFeatures));
    }

    @Override
    public boolean isSatisfied() {
        List<Feature> allFeatures = new ArrayList<>(metaFeatures.values());
        allFeatures.addAll(features.values());

        for (var feature : allFeatures) {
            if (feature.isSatisfied() && !strictly) {
                return true;
            } else if (!feature.isSatisfied() && strictly) {
                return false;
            }
        }

        return strictly && !allFeatures.isEmpty();
    }
}
