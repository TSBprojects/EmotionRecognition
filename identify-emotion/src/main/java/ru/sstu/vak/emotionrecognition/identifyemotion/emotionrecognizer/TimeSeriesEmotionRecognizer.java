package ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer;

import com.google.common.primitives.Longs;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;

public class TimeSeriesEmotionRecognizer extends ClosestFaceEmotionRecognizer {

    private final SortedMap<Long, VideoFrame> timeline = new TreeMap<>();

    private SortedMap<Long, VideoFrame> targetTimeline = timeline;

    private TimelineState targetState = TimelineState.FULL_COVERAGE;

    @Getter
    @Setter
    private boolean isFixedRange = true;

    private Duration fixedTargetRange;

    public TimeSeriesEmotionRecognizer(String modelPath) throws IOException {
        this(modelPath, Duration.ofMinutes(30));
    }

    public TimeSeriesEmotionRecognizer(String modelPath, Duration fixedTargetRange) throws IOException {
        super(modelPath);
        Objects.requireNonNull(fixedTargetRange, "Fixed target range cannot be null!");
        this.fixedTargetRange = fixedTargetRange;
    }

    public void applyTimeRange(Instant start, Instant end) {
        Objects.requireNonNull(start, "Start instant cannot be null!");
        Objects.requireNonNull(end, "End instant cannot be null!");

        TargetTimeLine target = internalApplyTimeRange(timeline, start.toEpochMilli(), end.toEpochMilli());
        targetTimeline = target.getTimeline();
        targetState = target.getState();
    }

    public void setFixedTargetRange(Duration fixedTargetRange) {
        Objects.requireNonNull(fixedTargetRange, "Fixed target range cannot be null!");
        this.fixedTargetRange = fixedTargetRange;
    }

    public SortedMap<Long, VideoFrame> getFullTimeline() {
        return Collections.unmodifiableSortedMap(timeline);
    }

    public SortedMap<Long, VideoFrame> getTargetTimeline() {
        return Collections.unmodifiableSortedMap(targetTimeline);
    }

    @Override
    protected VideoFrame createVideoFrame(int frameIndex, List<VideoFace> videoFacesList) {
        Instant now = Instant.now();
        long timestamp = now.toEpochMilli();
        VideoFrame videoFrame = new VideoFrame(frameIndex, videoFacesList);

        if (!timeline.isEmpty()) {

            if (isFixedRange) {

                long start = now.minus(fixedTargetRange).toEpochMilli();

                if (targetState.isFullCoverage() && timeline.firstKey() < start) {
                    targetTimeline = new TreeMap<>(timeline);
                    targetState = TimelineState.PARTIAL_FROM_END;
                    trimTimelineLeft(targetTimeline, start);
                }

                if (targetState.isPartialFromEnd() && targetTimeline.firstKey() < start) {
                    trimTimelineLeft(targetTimeline, start);
                }
            }

            if (timeline != targetTimeline && targetState.isReachedToEnd()) {
                targetTimeline.put(timestamp, videoFrame);
            }
        }

        timeline.put(timestamp, videoFrame);

        return videoFrame;
    }

    private void trimTimelineLeft(SortedMap<Long, VideoFrame> timeline, long startTimestamp) {
        Iterator<Map.Entry<Long, VideoFrame>> iterator = timeline.entrySet().iterator();
        while (iterator.hasNext() && iterator.next().getKey() < startTimestamp) {
            iterator.remove();
        }
    }

    private TargetTimeLine internalApplyTimeRange(SortedMap<Long, VideoFrame> timeline, long start, long end) {
        if (timeline.isEmpty()) return new TargetTimeLine(TimelineState.FULL_COVERAGE, timeline);

        long[] timestamps = Longs.toArray(timeline.keySet());
        int startIndex = binarySearchClosestMatch(timestamps, start);
        int endIndex = binarySearchClosestMatch(timestamps, end);

        if (startIndex == 0 && endIndex == timeline.size() - 1) {
            return new TargetTimeLine(TimelineState.FULL_COVERAGE, timeline);
        }

        TreeMap<Long, VideoFrame> target = new TreeMap<>(timeline.subMap(
            timestamps[startIndex],
            timestamps[endIndex]
        ));

        if (startIndex > 0 && endIndex == timeline.size() - 1) {
            return new TargetTimeLine(TimelineState.PARTIAL_FROM_END, target);
        }

        if (startIndex == 0 && endIndex < timeline.size() - 1) {
            return new TargetTimeLine(TimelineState.PARTIAL_FROM_START, target);
        }

        return new TargetTimeLine(TimelineState.PARTIAL, target);
    }

    private static int binarySearchClosestMatch(long[] a, long key) {
        int low = 0;
        int high = a.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;

            Comparable<Long> midVal = a[mid];
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }

        if (low >= a.length) {
            return a.length - 1;
        }

        if (high <= 0) {
            return 0;
        }

        return Math.abs(a[low] - key) < Math.abs(a[high] - key) ? low : high;
    }

    @Getter
    @RequiredArgsConstructor
    private static class TargetTimeLine {
        private final TimelineState state;
        private final SortedMap<Long, VideoFrame> timeline;
    }

    private enum TimelineState {
        FULL_COVERAGE,
        PARTIAL_FROM_START,
        PARTIAL_FROM_END,
        PARTIAL;

        public boolean isFullCoverage() {
            return this == FULL_COVERAGE;
        }

        public boolean isPartialFromEnd() {
            return this == PARTIAL_FROM_END;
        }

        public boolean isReachedToEnd() {
            return isFullCoverage() || isPartialFromEnd();
        }
    }
}
