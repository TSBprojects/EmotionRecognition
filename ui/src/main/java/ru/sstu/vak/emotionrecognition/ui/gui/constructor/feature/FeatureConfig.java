package ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature;

import lombok.Builder;
import lombok.Getter;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.HasChildren;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context.FeatureContext;

@Getter
@Builder
public class FeatureConfig {
    private final int modelId;
    private final String featureName;
    private final int featureNumberInModel;
    private final HasChildren<?> featureHolder;
    private final FeatureContext<?> featureContext;

    public String getFeatureName() {
        if (featureName != null) {
            return featureName;
        }
        return featureContext.getFeature().getName();
    }
}
