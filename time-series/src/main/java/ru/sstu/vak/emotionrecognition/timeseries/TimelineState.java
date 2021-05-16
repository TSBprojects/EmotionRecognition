package ru.sstu.vak.emotionrecognition.timeseries;

public enum TimelineState {
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

    public boolean isPartial() {
        return this == PARTIAL_FROM_START || this == PARTIAL || isPartialFromEnd();
    }

    public boolean isReachedToEnd() {
        return isFullCoverage() || isPartialFromEnd();
    }
}
