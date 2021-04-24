package ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.timeseries;

import com.google.common.primitives.Longs;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import static ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.timeseries.TimelineState.FULL_COVERAGE;
import static ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.timeseries.TimelineState.PARTIAL;
import static ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.timeseries.TimelineState.PARTIAL_FROM_END;
import static ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.timeseries.TimelineState.PARTIAL_FROM_START;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.VideoFrame;

public class SimpleTimeSeries implements TimeSeries {

    private final SortedMap<Long, VideoFrame> wholeTimeline;

    private SortedMap<Long, VideoFrame> targetTimeline;

    private TimelineState targetState = FULL_COVERAGE;

    SimpleTimeSeries(SortedMap<Long, VideoFrame> wholeTimeline) {
        this.wholeTimeline = wholeTimeline;
        this.targetTimeline = new TreeMap<>(wholeTimeline);
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
            synchronized (wholeTimeline) {
                long startPoint = wholeTimeline.firstKey();
                ApplyResult result = internalApplyTimeRange(wholeTimeline, startPoint + startMs, startPoint + endMs);
                targetTimeline = result.getTimeline();
                targetState = result.getState();
            }
        }
    }

    @Override
    public void mutateToDirectRange(long startMs, long endMs) {
        synchronized (wholeTimeline) {
            ApplyResult result = internalApplyTimeRange(wholeTimeline, startMs, endMs);
            targetTimeline = result.getTimeline();
            targetState = result.getState();
        }
    }

    private static ApplyResult internalApplyTimeRange(SortedMap<Long, VideoFrame> timeline, long start, long end) {
        if (timeline.isEmpty()) return new ApplyResult(FULL_COVERAGE, new TreeMap<>(timeline));

        long[] timestamps = Longs.toArray(timeline.keySet());
        int startIndex = binarySearchClosestMatch(timestamps, start);
        int endIndex = binarySearchClosestMatch(timestamps, end);

        if (startIndex == 0 && endIndex == timeline.size() - 1) {
            return new ApplyResult(FULL_COVERAGE, new TreeMap<>(timeline));
        }

        TreeMap<Long, VideoFrame> target = new TreeMap<>(timeline.subMap(
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


//    private List<EmotionSegment> applySegmentation(SortedMap<Long, VideoFrame> timeline) {
//        List<EmotionSegment> emoFrequency = new LinkedList<>();
//
//        double sumProb = 0;
//        int emCount = 0;
//        long start = 0;
//        long prevEmTime;
//        Emotion prevEm = null;
//        for (Map.Entry<Long, VideoFrame> entry : timeline.entrySet()) {
//            long curTime = entry.getKey();
//            VideoFrame frame = entry.getValue();
//            List<VideoFace> faces = frame.getVideoFaces();
//
//            Emotion curEm = null;
//            Prediction pr = new Prediction(curEm, 0);
//            if (!faces.isEmpty()) {
//                VideoFace face = faces.get(0);
//                pr = face.getPrediction();
//                curEm = pr.getEmotion();
//            }
//
//            if (emoFrequency.isEmpty() || prevEm == curEm) {
//                if (start == 0) {
//                    start = curTime;
//                }
//                prevEm = curEm;
//                prevEmTime = curTime;
//                sumProb += pr.getProbability();
//                emCount++;
//
//                if (emoFrequency.isEmpty()) {
//                    emoFrequency.add(new EmotionSegment(prevEm, sumProb / emCount, start, prevEmTime));
//                } else {
//                    EmotionSegment lastES = emoFrequency.get(emoFrequency.size() - 1);
//                    lastES.setAverageProbability(sumProb / emCount);
//                    lastES.setEndTimestamp(prevEmTime);
//                }
//            } else {
//                start = curTime;
//                prevEm = curEm;
//                prevEmTime = curTime;
//                sumProb = pr.getProbability();
//                emCount = 1;
//
//                emoFrequency.add(new EmotionSegment(prevEm, sumProb / emCount, start, prevEmTime));
//            }
//        }
//
//        return emoFrequency;
//    }
}
