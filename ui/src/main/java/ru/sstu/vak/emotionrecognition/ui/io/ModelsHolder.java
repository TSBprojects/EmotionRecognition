package ru.sstu.vak.emotionrecognition.ui.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;

@Getter
@ToString
public class ModelsHolder {

    private final List<AnalyzableModel> models;

    protected ModelsHolder() {
        models = new ArrayList<>();
    }

    protected ModelsHolder(List<AnalyzableModel> models) {
        this.models = models;
    }

    public static ModelsHolder from(Map<Integer, AnalyzableModel> models) {
        return new ModelsHolder(new ArrayList<>(models.values()));
    }
}
