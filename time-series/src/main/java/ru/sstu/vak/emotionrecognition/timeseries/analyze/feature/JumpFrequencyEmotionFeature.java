package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import com.google.auto.service.AutoService;
import java.util.List;
import lombok.ToString;
import ru.sstu.vak.emotionrecognition.common.Emotion;
import static ru.sstu.vak.emotionrecognition.common.Emotion.HAPPY;
import static ru.sstu.vak.emotionrecognition.common.Emotion.SAD;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeries;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.configuration.ConfigurableProperty;

@ToString(callSuper = true)
@AutoService(EmotionFeature.class)
public class JumpFrequencyEmotionFeature extends AbstractEmotionFeature {

    private static final int ID = 2;

    private static final String DESCRIPTION =
        "Частота перехода с одной эмоции на другую - переход с исходной эмоции"
            + " на конечную эмоцию – увеличивает частоту на единицу";

    private int frequency = 0;

    private Emotion prevEm = null;

    @ConfigurableProperty(alias = "Исходная эмоция")
    private Emotion emotionFrom = HAPPY;

    @ConfigurableProperty(alias = "Конечная эмоция")
    private Emotion emotionTo = SAD;

    public JumpFrequencyEmotionFeature() {
        this("Частота перехода");
    }

    public JumpFrequencyEmotionFeature(String name) {
        super(name);
    }

    public JumpFrequencyEmotionFeature(JumpFrequencyEmotionFeature feature) {
        super(feature);
        this.emotionFrom = feature.emotionFrom;
        this.emotionTo = feature.emotionTo;
    }

    @Override
    public int getValue() {
        return frequency;
    }

    @Override
    public int getId() {
        return ID;
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
            if (emotionFrom.equals(prevEm) && emotionTo.equals(curEm)) {
                frequency++;
            }
            prevEm = curEm;
        }
    }

    @Override
    public void clear() {
        frequency = 0;
        prevEm = null;
    }

    @Override
    public EmotionFeature copy() {
        return new JumpFrequencyEmotionFeature(this);
    }
}
