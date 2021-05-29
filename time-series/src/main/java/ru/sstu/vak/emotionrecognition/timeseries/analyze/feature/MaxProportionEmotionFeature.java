package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import com.google.auto.service.AutoService;
import java.util.List;
import lombok.ToString;
import lombok.var;
import ru.sstu.vak.emotionrecognition.common.Emotion;
import static ru.sstu.vak.emotionrecognition.common.Emotion.HAPPY;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeries;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.configuration.ConfigurableProperty;

@ToString(callSuper = true)
@AutoService(EmotionFeature.class)
public class MaxProportionEmotionFeature extends AbstractEmotionFeature {

    private static final int ID = 3;

    private static final String DESCRIPTION =
        "Максимальная доля непрерывной эмоции за отрезок времени – максимальный из всех отрезков времени " +
            "на которых эмоция была непрерывна, выраженная в процентах относительно всего отрезка времени";

    private int maxProportion = 0;

    private long prevEmStart = 0;

    private Emotion prevEm = null;

    @ConfigurableProperty(alias = "Эмоция")
    private Emotion emotion = HAPPY;

    public MaxProportionEmotionFeature() {
        this("Макс. доля");
    }

    public MaxProportionEmotionFeature(String name) {
        super(name);
    }

    public MaxProportionEmotionFeature(MaxProportionEmotionFeature feature) {
        super(feature);
        this.emotion = feature.emotion;
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
    public int getValue() {
        return maxProportion;
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
                int rangeProportion = calculateProportion(timestamp - prevEmStart, wholeTarget);
                if (maxProportion < rangeProportion) {
                    maxProportion = rangeProportion;
                }
            }
            prevEm = curEm;
            prevEmStart = timestamp;
        } else {
            if (emotion.equals(curEm)) {
                int rangeProportion = calculateProportion(timestamp - prevEmStart, wholeTarget);
                if (maxProportion < rangeProportion) {
                    maxProportion = rangeProportion;
                }
            }
        }
    }

    private int calculateProportion(long totalTime, TimeSeries wholeTarget) {
        var rawTS = wholeTarget.getRaw();
        var wholeDuration = rawTS.lastKey() - rawTS.firstKey();
        return (int) Math.round((totalTime * 100.0 / wholeDuration));
    }

    @Override
    public void clear() {
        prevEm = null;
        maxProportion = 0;
        prevEmStart = 0;
    }

    @Override
    public EmotionFeature copy() {
        return new MaxProportionEmotionFeature(this);
    }
}
