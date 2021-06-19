package ru.sstu.vak.emotionrecognition.ui.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;
import ru.sstu.vak.emotionrecognition.timeseries.TimeSeries;

@Getter
@ToString
public class AnalysisHolder {

    private final Set<String> emotionalStates;

    private final List<VideoFrame> frames;

    protected AnalysisHolder(Set<String> emotionalStates, List<VideoFrame> frames) {
        this.emotionalStates = emotionalStates;
        this.frames = frames;
    }

    public static AnalysisHolder from(Set<String> emotionalStates, TimeSeries timeSeries) {
        return new AnalysisHolder(emotionalStates, new ArrayList<>(timeSeries.getRaw().values()));
    }
}
