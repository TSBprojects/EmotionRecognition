package ru.sstu.vak.emotionrecognition.timeseries.analyze;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.var;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeries;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;

public final class AnalyzeEngine {

    private AnalyzeEngine() {
        throw new AssertionError();
    }

    public static Set<String> analyze(TimeSeries target, Map<Integer, AnalyzableModel> models) {
        if (models.isEmpty()) return Collections.emptySet();

        applyFramesToFeatures(target, models);

        Set<String> matchedStates = new HashSet<>();

        for (var model : models.values()) {
            applyFeaturesToMetaFeatures(model);

            if (model.isSatisfied()) {
                matchedStates.add(model.getName());
            }
        }

        return matchedStates;
    }

    private static void applyFramesToFeatures(TimeSeries target, Map<Integer, AnalyzableModel> models) {
        boolean isFirst = true;
        for (var tsEntry : target.getRaw().entrySet()) {
            for (var model : models.values()) {
                for (var feature : model.getFeatures().values()) {
                    if (isFirst) {
                        feature.clear();
                    }
                    feature.apply(target, tsEntry.getKey(), tsEntry.getValue());
                }
            }
            isFirst = false;
        }
    }

    private static void applyFeaturesToMetaFeatures(AnalyzableModel model) {
        for (var feature : model.getMetaFeatures().values()) {
            feature.clear();
            feature.apply(model.getFeatures());
        }
    }
}
