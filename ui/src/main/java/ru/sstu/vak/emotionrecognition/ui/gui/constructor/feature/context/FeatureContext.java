package ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context;

import javafx.scene.control.Label;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.Feature;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.FeatureAction;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.ModelContext;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.ModelPane;

public abstract class FeatureContext<T extends Feature> {

    private final T feature;

    private final ModelContext modelContext;

    protected FeatureContext(T feature, ModelContext modelContext) {
        this.feature = feature;
        this.modelContext = modelContext;
    }

    public T getFeature() {
        return feature;
    }

    public ModelContext getModelContext() {
        return modelContext;
    }

    public abstract int putFeature(int modelId);

    public abstract String createSerialNumber(int id);

    public abstract FeatureAction getRemoveHandler(int modelId, int featureId);

    public abstract FeatureAction getLibraryFeatureSettingHandler(Label nameLabel);

    public abstract FeatureAction getModelFeatureSettingHandler(int modelId, int featureId);

    public abstract void createAndPutModel(String stateName, ModelPane modelPane);
}
