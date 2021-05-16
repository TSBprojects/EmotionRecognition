package ru.sstu.vak.emotionrecognition.timeseries.analyze.signs;

import ru.sstu.vak.emotionrecognition.common.Nameable;

public enum BracketSign implements Nameable {
    LEFT("("),
    RIGHT(")");

    private final String value;

    BracketSign(String value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return value;
    }
}
