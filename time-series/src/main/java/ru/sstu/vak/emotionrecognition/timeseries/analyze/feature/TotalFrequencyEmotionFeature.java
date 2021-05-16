package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import com.google.auto.service.AutoService;
import java.util.List;
import ru.sstu.vak.emotionrecognition.common.Emotion;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeries;

@AutoService(EmotionFeature.class)
public class TotalFrequencyEmotionFeature extends FrequencyEmotionFeature {

    private static final String DESCRIPTION =
        "Общая частота смены всех эмоций за отрезок времени - переход с"
            + " любой эмоции на любую – увеличивает частоту на единицу";

    private int frequency = 0;

    private Emotion prevEm = null;

    public TotalFrequencyEmotionFeature() {
        super("Общая частота");
    }

    public TotalFrequencyEmotionFeature(String name) {
        super(name);
    }

    public TotalFrequencyEmotionFeature(TotalFrequencyEmotionFeature feature) {
        super(feature);
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
            frequency++;
        }
    }

    @Override
    public void clear() {
        frequency = 0;
        prevEm = null;
    }

    @Override
    public EmotionFeature copy() {
        return new TotalFrequencyEmotionFeature(this);
    }
}
