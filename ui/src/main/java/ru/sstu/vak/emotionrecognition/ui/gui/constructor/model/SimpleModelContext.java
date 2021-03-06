package ru.sstu.vak.emotionrecognition.ui.gui.constructor.model;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementHashMap;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.endpoint.EventRequestContext;

public class SimpleModelContext implements ModelContext {

    private final SimpleEventRequestContext endpointActionContext = new SimpleEventRequestContext();

    private final AutoIncrementMap<ModelPane> panes = new AutoIncrementHashMap<>(new ConcurrentHashMap<>());

    private final AutoIncrementMap<AnalyzableModel> models = new AutoIncrementHashMap<>(new ConcurrentHashMap<>());

    @Override
    public EventRequestContext getEventRequestContext() {
        return endpointActionContext;
    }

    @Override
    public Map<Integer, AnalyzableModel> getModels() {
        return Collections.unmodifiableMap(models);
    }

    @Override
    public Map<Integer, ModelPane> getPanes() {
        return Collections.unmodifiableMap(panes);
    }

    @Override
    public AnalyzableModel getModel(int id) {
        return models.get(id);
    }

    @Override
    public ModelPane getModelPane(int id) {
        return panes.get(id);
    }

    @Override
    public void put(AnalyzableModel model, ModelPane pane) {
        models.put(model);
        panes.put(pane);
    }

    @Override
    public boolean containsKey(int modelId) {
        return models.containsKey(modelId);
    }

    @Override
    public void remove(int id) {
        models.remove(id);
        panes.remove(id);
        endpointActionContext.removeModel(id);
    }

    @Override
    public int getNextId() {
        return models.getNextId();
    }
}
