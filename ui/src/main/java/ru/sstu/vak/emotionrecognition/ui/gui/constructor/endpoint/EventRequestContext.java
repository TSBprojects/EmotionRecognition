package ru.sstu.vak.emotionrecognition.ui.gui.constructor.endpoint;

import java.util.concurrent.Future;

public interface EventRequestContext {

    boolean contains(int modelId, int endpointId);

    void add(int modelId, int endpointId, Future<?> requestFuture);

    void remove(int modelId, int endpointId);
}
