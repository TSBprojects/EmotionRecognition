package ru.sstu.vak.emotionrecognition.ui.gui.constructor.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.endpoint.EventRequestContext;

public class SimpleEventRequestContext implements EventRequestContext {

    private final Map<Integer, Map<Integer, Future<?>>> endpointsInProgress = new HashMap<>(new ConcurrentHashMap<>());

    @Override
    public boolean contains(int modelId, int endpointId) {
        Map<Integer, Future<?>> endpointRequests = endpointsInProgress.get(modelId);
        return endpointRequests != null && endpointRequests.containsKey(endpointId);
    }

    @Override
    public void add(int modelId, int endpointId, Future<?> requestFuture) {
        endpointsInProgress.computeIfAbsent(modelId, ignore -> new ConcurrentHashMap<>()).put(endpointId, requestFuture);
    }


    @Override
    public void remove(int modelId, int endpointId) {
        Map<Integer, Future<?>> endpointIds = endpointsInProgress.get(modelId);
        if (endpointIds != null) {
            Future<?> requestFuture = endpointIds.get(endpointId);
            if (requestFuture != null) {
                endpointIds.remove(endpointId);
                requestFuture.cancel(true);
            }
        }
    }

    void removeModel(int modelId) {
        Map<Integer, Future<?>> endpointIds = endpointsInProgress.get(modelId);
        if (endpointIds != null) {
            for (int endpointId : endpointIds.keySet()) {
                remove(modelId, endpointId);
            }
            endpointsInProgress.remove(modelId);
        }
    }
}
