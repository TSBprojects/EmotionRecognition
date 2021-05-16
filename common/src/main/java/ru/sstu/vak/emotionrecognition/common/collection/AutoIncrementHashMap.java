package ru.sstu.vak.emotionrecognition.common.collection;

import java.util.HashMap;
import java.util.Map;

public class AutoIncrementHashMap<E> extends ForwardingMap<Integer, E> implements AutoIncrementMap<E> {

    private int nextId = 0;

    public AutoIncrementHashMap() {
        super(new HashMap<>());
    }

    public AutoIncrementHashMap(Map<Integer, E> map) {
        super(map);
        nextId = map.size();
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
    public E put(Integer key, E value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends E> m) {
        throw new UnsupportedOperationException();
    }
}
