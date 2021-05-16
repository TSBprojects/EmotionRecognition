package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import com.google.auto.service.AutoService;
import java.util.List;
import ru.sstu.vak.emotionrecognition.common.Emotion;
import static ru.sstu.vak.emotionrecognition.common.Emotion.HAPPY;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeries;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.configuration.ConfigurableProperty;

@AutoService(EmotionFeature.class)
public class FrequencyEmotionFeature extends AbstractEmotionFeature {

    private static final String DESCRIPTION =
        "Частота проявления эмоции – количество отрезков времени на которых эмоция была непрерывна. "
            + "Т.е. переход с любой эмоции на целевую – увеличивает частоту на единицу";

    private int frequency = 0;

    private Emotion prevEm = null;

    @ConfigurableProperty(alias = "Эмоция")
    private Emotion emotion = HAPPY;

    public FrequencyEmotionFeature() {
        this("Частота проявления");
    }

    public FrequencyEmotionFeature(String name) {
        super(name);
    }

    public FrequencyEmotionFeature(FrequencyEmotionFeature feature) {
        super(feature);
        this.emotion = feature.emotion;
    }

    @Override
    public int getValue() {
        return frequency;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void apply(TimeSeries ignore, long timestamp, VideoFrame videoFrame) {
        List<VideoFace> faces = videoFrame.getVideoFaces();

        Emotion curEm = null;
        if (!faces.isEmpty()) {
            VideoFace face = faces.get(0);
            curEm = face.getPrediction().getEmotion();
        }

        if (prevEm != curEm) {
            prevEm = curEm;
            if (emotion.equals(curEm)) {
                frequency++;
            }
        }
    }

    @Override
    public void clear() {
        frequency = 0;
        prevEm = null;
    }

    @Override
    public EmotionFeature copy() {
        return new FrequencyEmotionFeature(this);
    }
}
