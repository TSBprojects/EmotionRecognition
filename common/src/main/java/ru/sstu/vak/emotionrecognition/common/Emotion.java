package ru.sstu.vak.emotionrecognition.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.awt.Color;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.util.Arrays.stream;
import java.util.Map;
import static java.util.stream.Collectors.toMap;

public enum Emotion implements Nameable {

    ANGER(0, "ЗЛОСТЬ", new Color(228, 48, 84), WHITE),
    DISGUST(1, "ОТВРАЩЕНИЕ", new Color(53, 164, 80), WHITE),
    FEAR(2, "СТРАХ", new Color(159, 120, 186), WHITE),
    HAPPY(3, "СЧАСТЬЕ", new Color(255, 114, 0), WHITE),
    NEUTRAL(4, "НЕЙТРАЛЬНЫЙ", new Color(0, 255, 255), BLACK),
    SAD(5, "ПЕЧАЛЬ", new Color(114, 157, 201), WHITE),
    SURPRISE(6, "УДИВЛЕНИЕ", new Color(255, 237, 43), BLACK);

    private final int emotionId;
    private final String name;
    private final Color color;
    private final Color textColor;

    private static final Map<Integer, Emotion> ID_MAP = stream(values()).collect(toMap(Emotion::getEmotionId, e -> e));

    private static final Map<String, Emotion> NAME_MAP = stream(values()).collect(toMap(Emotion::getName, e -> e));

    Emotion(int emotionId, String name, Color color, Color textColor) {
        this.emotionId = emotionId;
        this.name = name;
        this.color = color;
        this.textColor = textColor;
    }

    public static Emotion of(int index) {
        return of(ID_MAP, index);
    }

    @JsonCreator
    public static Emotion of(String name) {
        return of(NAME_MAP, name);
    }

    private static <T> Emotion of(Map<T, Emotion> map, T value) {
        Emotion emotion = map.get(value);

        if (emotion == null) {
            throw new UnsupportedOperationException("Unknown or not supported emotion with value: " + value);
        }

        return emotion;
    }

    @JsonIgnore
    public int getEmotionId() {
        return emotionId;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonIgnore
    public Color getColor() {
        return color;
    }

    @JsonIgnore
    public Color getTextColor() {
        return textColor;
    }

    @Override
    public String toString() {
        return "Emotion{" +
                "emotionId=" + emotionId +
                ", emotionName=" + name +
                '}';
    }
}
