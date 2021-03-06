package ru.sstu.vak.emotionrecognition.common.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AutoIncrementHashMap<E> extends ForwardingMap<Integer, E> implements AutoIncrementMap<E> {

    private int nextId = 0;

    public AutoIncrementHashMap() {
        super(new HashMap<>());
    }

    public AutoIncrementHashMap(Map<Integer, E> map) {
        super(map);
        if (!map.isEmpty()) {
            nextId = Collections.max(map.keySet()) + 1;
        }
    }

    @Override
    public int put(E value) {
        super.put(nextId, value);
        return nextId++;
    }

    @Override
    public int getNextId() {
        return nextId;
    }

    @Override
    public E putIfAbsent(Integer key, E value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E computeIfAbsent(Integer key, Function<? super Integer, ? extends E> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E computeIfPresent(Integer key, BiFunction<? super Integer, ? super E, ? extends E> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E compute(Integer key, BiFunction<? super Integer, ? super E, ? extends E> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E put(Integer key, E value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends E> m) {
        throw new UnsupportedOperationException();
    }
}
