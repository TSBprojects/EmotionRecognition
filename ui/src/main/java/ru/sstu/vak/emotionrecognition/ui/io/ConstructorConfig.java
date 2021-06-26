package ru.sstu.vak.emotionrecognition.ui.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.endpoint.Endpoint;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;

@Getter
@ToString
public class ConstructorConfig {

    private final List<AnalyzableModel> models;

    private final Map<Integer, Endpoint> endpoints;

    protected ConstructorConfig() {
        models = new ArrayList<>();
        endpoints = new HashMap<>();
    }

    public ConstructorConfig(Map<Integer, Endpoint> endpoints, List<AnalyzableModel> models) {
        this.models = models;
        this.endpoints = endpoints;
    }

    public static ConstructorConfig from(Map<Integer, Endpoint> endpoints, Map<Integer, AnalyzableModel> models) {
        return new ConstructorConfig(new HashMap<>(endpoints), new ArrayList<>(models.values()));
    }
}
