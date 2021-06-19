package ru.sstu.vak.emotionrecognition.ui.gui.dragdrop;

import javafx.util.Pair;

public final class DragDropData {

    private DragDropData() {
        throw new AssertionError();
    }

    public static String serialize(Type type, int id) {
        return type.name() + "_" + id;
    }

    public static Pair<Integer, Type> deserialize(String data) {
        String[] parsedData = data.split("_");
        return new Pair<>(Integer.parseInt(parsedData[1]), Type.valueOf(parsedData[0]));
    }

    public enum Type {
        ENDPOINT,
        FEATURE
    }
}
