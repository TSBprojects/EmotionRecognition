package ru.sstu.vak.emotionrecognition.timeseries.analyze.models;

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

    public SimpleAnalyzableModel(
        String name,
        boolean strictly,
        Map<Integer, EmotionFeature> features,
        Map<Integer, MetaFeature> metaFeatures
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
