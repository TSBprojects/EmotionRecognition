package ru.sstu.vak.emotionrecognition.common.collection;

import java.util.Map;

public interface AutoIncrementMap<E> extends Map<Integer, E> {
    int put(E value);
    int getNextId();
}
