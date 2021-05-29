package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.configuration.ConfigurableProperty;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.configuration.PairProperty;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.signs.EquivalenceSign;
import static ru.sstu.vak.emotionrecognition.timeseries.analyze.signs.EquivalenceSign.MORE;

@Getter
@Setter
@ToString
public abstract class AbstractEmotionFeature implements EmotionFeature {

    @ConfigurableProperty(alias = "Имя", inheritable = true)
    private String name;

    @PairProperty(mate = "threshold")
    @ConfigurableProperty(alias = "Пороговое значение", inheritable = true)
    private EquivalenceSign thresholdSign = MORE;

    @JsonProperty
    private long threshold = 0;

    protected AbstractEmotionFeature(AbstractEmotionFeature feature) {
        this.name = feature.name;
        this.threshold = feature.threshold;
        this.thresholdSign = feature.thresholdSign;
    }

    protected AbstractEmotionFeature(String name) {
        this.name = name;
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public boolean isSatisfied() {
        return thresholdSign.apply(getValue(), threshold);
    }
}
