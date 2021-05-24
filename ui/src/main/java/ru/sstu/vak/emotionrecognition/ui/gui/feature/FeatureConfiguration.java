package ru.sstu.vak.emotionrecognition.ui.gui.feature;

import lombok.Builder;
import lombok.Getter;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.HasChildren;

@Getter
@Builder
public class FeatureConfiguration {
    private final int modelId;
    private final String label;
    private final String serialNumber;
    private final ActionHandler removeHandler;
    private final ActionHandler settingHandler;
    private final HasChildren<?> featureHolder;
}
