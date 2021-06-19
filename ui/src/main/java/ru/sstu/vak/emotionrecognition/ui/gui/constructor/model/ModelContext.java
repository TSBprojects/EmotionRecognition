package ru.sstu.vak.emotionrecognition.ui.gui.constructor.model;

import java.util.Map;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.endpoint.EventRequestContext;

public interface ModelContext {

    EventRequestContext getEventRequestContext();

    Map<Integer, AnalyzableModel> getModels();

    Map<Integer, ModelPane> getPanes();

    AnalyzableModel getModel(int id);

    ModelPane getModelPane(int id);

    void put(AnalyzableModel model, ModelPane pane);

    boolean containsKey(int modelId);

    void remove(int id);

    int getNextId();
}
