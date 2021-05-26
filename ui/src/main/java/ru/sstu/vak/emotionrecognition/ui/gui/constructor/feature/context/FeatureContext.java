package ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context;

import javafx.scene.control.Label;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.Feature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.FeatureAction;

public abstract class FeatureContext<T extends Feature> {

    private final T feature;

    private final AutoIncrementMap<AnalyzableModel> models;

    protected FeatureContext(T feature, AutoIncrementMap<AnalyzableModel> models) {
        this.feature = feature;
        this.models = models;
    }

    public T getFeature() {
        return feature;
    }

    public AutoIncrementMap<AnalyzableModel> getModels() {
        return models;
    }

    public abstract int putFeature(int modelId);

    public abstract String createSerialNumber(int id);

    public abstract FeatureAction getRemoveHandler(int modelId, int featureId);

    public abstract FeatureAction getLibraryFeatureSettingHandler(Label nameLabel);

    public abstract FeatureAction getModelFeatureSettingHandler(int modelId, int featureId);

    public abstract void createAndPutModel(String stateName);
}
