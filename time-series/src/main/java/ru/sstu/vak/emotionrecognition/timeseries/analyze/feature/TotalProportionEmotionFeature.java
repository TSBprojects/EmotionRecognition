package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import com.google.auto.service.AutoService;
import java.util.List;
import lombok.var;
import ru.sstu.vak.emotionrecognition.common.Emotion;
import static ru.sstu.vak.emotionrecognition.common.Emotion.HAPPY;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeries;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.configuration.ConfigurableProperty;

@AutoService(EmotionFeature.class)
public class TotalProportionEmotionFeature extends AbstractEmotionFeature {

    private static final String DESCRIPTION =
        "Общая доля эмоции за отрезок времени – сумма всех отрезков времени на которых эмоция"
            + " была непрерывна, выраженная в процентах относительно всего отрезка времени";

    private int proportion = 0;

    private long totalTime = 0;

    private long prevEmEnd = 0;

    private Emotion prevEm = null;

    @ConfigurableProperty(alias = "Эмоция")
    private Emotion emotion = HAPPY;

    public TotalProportionEmotionFeature() {
        this("Общая доля");
    }

    public TotalProportionEmotionFeature(String name) {
        super(name);
    }

    public TotalProportionEmotionFeature(TotalProportionEmotionFeature feature) {
        super(feature);
        this.emotion = feature.emotion;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public int getValue() {
        return proportion;
    }

    @Override
    public void apply(TimeSeries wholeTarget, long timestamp, VideoFrame videoFrame) {
        List<VideoFace> faces = videoFrame.getVideoFaces();

        Emotion curEm = null;
        if (!faces.isEmpty()) {
            VideoFace face = faces.get(0);
            curEm = face.getPrediction().getEmotion();
        }

        if (prevEm != curEm) {
            if (emotion.equals(prevEm)) {
                totalTime += timestamp - prevEmEnd;
                proportion = calculateProportion(totalTime, wholeTarget);
            }
            prevEm = curEm;
        } else {
            if (emotion.equals(curEm)) {
                totalTime += timestamp - prevEmEnd;
                proportion = calculateProportion(totalTime, wholeTarget);
            }
        }

        prevEmEnd = timestamp;
    }

    private int calculateProportion(long totalTime, TimeSeries wholeTarget) {
        var rawTS = wholeTarget.getRaw();
        var wholeDuration = rawTS.lastKey() - rawTS.firstKey();
        return (int) Math.round((totalTime * 100.0 / wholeDuration));
    }

    @Override
    public void clear() {
        proportion = 0;
        totalTime = 0;
        prevEmEnd = 0;
        prevEm = null;
    }

    @Override
    public EmotionFeature copy() {
        return new TotalProportionEmotionFeature(this);
    }
}
