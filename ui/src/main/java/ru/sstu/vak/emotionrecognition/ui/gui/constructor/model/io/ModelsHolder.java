package ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.io;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;
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

    public static ModelsHolder from(AutoIncrementMap<AnalyzableModel> models) {
        return new ModelsHolder(new ArrayList<>(models.values()));
    }
}
