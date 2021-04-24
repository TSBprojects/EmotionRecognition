package ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.timeseries;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.Listenable;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;

public class TimeSeriesCollector {

    private final SortedMap<Long, VideoFrame> timeline = new TreeMap<>();

    private final Map<String, TimeSeries> targetTimeSequences = new HashMap<>();

    public TimeSeriesCollector(Listenable listenable) {
        listenable.addVideoFrameListener(this::onNextFrame);
    }

    public SortedMap<Long, VideoFrame> getFullTimeline() {
        return Collections.unmodifiableSortedMap(timeline);
    }

    public Map<String, TimeSeries> getTargetTimeSequences() {
        return Collections.unmodifiableMap(targetTimeSequences);
    }

    public TimeSeries addTargetTimeSeries(String name) {
        return targetTimeSequences.computeIfAbsent(name, n -> new SimpleTimeSeries(timeline));
    }

    public TimeSeries getTargetTimeSeries(String name) {
        return targetTimeSequences.get(name);
    }


    private void onNextFrame(VideoFrame videoFrame) {

        Instant now = Instant.now();
        long timestamp = now.toEpochMilli();

        if (!timeline.isEmpty()) {

            for (TimeSeries ts : targetTimeSequences.values()) {

                if (ts.getState().isReachedToEnd()) {
                    ts.getRaw().put(timestamp, videoFrame);
                }
            }
        }

        synchronized (timeline) {
            timeline.put(timestamp, videoFrame);
        }

//        if (!targetTimelines.isEmpty()) {
//            targetTimelines.forEach((s, timeSeries) -> System.out.println("!!!!!!!!!! "+s +" - "+timeSeries.getRaw().size()));
//        }
    }
}
