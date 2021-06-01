package ru.sstu.vak.emotionrecognition.common;

public interface Configurable<F> {

    void enable(F feature);

    void disable(F feature);
}
