package ru.sstu.vak.emotionrecognition.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.Color;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.util.Arrays.stream;
import java.util.Map;
import static java.util.stream.Collectors.toMap;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Emotion {

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

    private static final Map<Integer, Emotion> map = stream(values()).collect(toMap(Emotion::getEmotionId, e -> e));

    Emotion(int emotionId, String name, Color color, Color textColor) {
        this.emotionId = emotionId;
        this.name = name;
        this.color = color;
        this.textColor = textColor;
    }

    public static Emotion valueOf(int index) {
        Emotion emotion = map.get(index);

        if (emotion == null) {
            throw new UnsupportedOperationException("Unknown or not supported emotion with index: " + index);
        }

        return emotion;
    }

    @JsonIgnore
    public int getEmotionId() {
        return emotionId;
    }

    @JsonProperty("name")
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
