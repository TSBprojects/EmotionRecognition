package ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.timeseries;

import java.util.SortedMap;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;

public interface TimeSeries {

    TimelineState getState();

    void setState(TimelineState state);

    SortedMap<Long, VideoFrame> getRaw();

    void mutateToRelativeRange(long startMs, long endMs);

    void mutateToDirectRange(long startMs, long endMs);
}
