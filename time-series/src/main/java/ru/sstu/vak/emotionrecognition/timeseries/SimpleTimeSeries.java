package ru.sstu.vak.emotionrecognition.timeseries;

import com.google.common.primitives.Longs;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;
import static ru.sstu.vak.emotionrecognition.timeseries.TimelineState.FULL_COVERAGE;
import static ru.sstu.vak.emotionrecognition.timeseries.TimelineState.PARTIAL;
import static ru.sstu.vak.emotionrecognition.timeseries.TimelineState.PARTIAL_FROM_END;
import static ru.sstu.vak.emotionrecognition.timeseries.TimelineState.PARTIAL_FROM_START;

public class SimpleTimeSeries implements TimeSeries {

    private final SortedMap<Long, VideoFrame> wholeTimeline;

    private SortedMap<Long, VideoFrame> targetTimeline;

    private TimelineState targetState = FULL_COVERAGE;

    SimpleTimeSeries(SortedMap<Long, VideoFrame> wholeTimeline) {
        this.wholeTimeline = wholeTimeline;
        this.targetTimeline = new ConcurrentSkipListMap<>(wholeTimeline);
    }

    @Override
    public TimelineState getState() {
        return targetState;
    }

    @Override
    public void setState(TimelineState state) {
        this.targetState = state;
    }

    @Override
    public SortedMap<Long, VideoFrame> getRaw() {
        return targetTimeline;
    }

    @Override
    public void mutateToRelativeRange(long startMs, long endMs) {
        if (!wholeTimeline.isEmpty()) {
            long startPoint = wholeTimeline.firstKey();
            ApplyResult result = internalApplyTimeRange(wholeTimeline, startPoint + startMs, startPoint + endMs);
            targetTimeline = result.getTimeline();
            targetState = result.getState();
        }
    }

    @Override
    public void mutateToDirectRange(long startMs, long endMs) {
        ApplyResult result = internalApplyTimeRange(wholeTimeline, startMs, endMs);
        targetTimeline = result.getTimeline();
        targetState = result.getState();
    }

    private static ApplyResult internalApplyTimeRange(SortedMap<Long, VideoFrame> timeline, long start, long end) {
        if (timeline.isEmpty()) return new ApplyResult(FULL_COVERAGE, new ConcurrentSkipListMap<>(timeline));

        long[] timestamps = Longs.toArray(timeline.keySet());
        int startIndex = binarySearchClosestMatch(timestamps, start);
        int endIndex = binarySearchClosestMatch(timestamps, end);

        if (startIndex == 0 && endIndex == timeline.size() - 1) {
            return new ApplyResult(FULL_COVERAGE, new ConcurrentSkipListMap<>(timeline));
        }

        ConcurrentSkipListMap<Long, VideoFrame> target = new ConcurrentSkipListMap<>(timeline.subMap(
            timestamps[startIndex],
            timestamps[endIndex]
        ));

        if (startIndex > 0 && endIndex == timeline.size() - 1) {
            return new ApplyResult(PARTIAL_FROM_END, target);
        }

        if (startIndex == 0 && endIndex < timeline.size() - 1) {
            return new ApplyResult(PARTIAL_FROM_START, target);
        }

        return new ApplyResult(PARTIAL, target);
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
    private static class ApplyResult {
        private final TimelineState state;
        private final SortedMap<Long, VideoFrame> timeline;
    }
}
