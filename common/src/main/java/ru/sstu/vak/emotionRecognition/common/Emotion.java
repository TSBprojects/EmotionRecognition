package ru.sstu.vak.emotionRecognition.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.awt.*;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Emotion {

    ANGER(0),
    DISGUST(1),
    FEAR(2),
    HAPPY(3),
    NEUTRAL(4),
    SAD(5),
    SURPRISE(6);

    private int emotionId;
    private double probability;

    Emotion(int index) {
        this.emotionId = index;
    }

    public static Emotion valueOf(int index) {
        switch (index) {
            case 0:
                return ANGER;
            case 1:
                return DISGUST;
            case 2:
                return FEAR;
            case 3:
                return HAPPY;
            case 4:
                return NEUTRAL;
            case 5:
                return SAD;
            case 6:
                return SURPRISE;

            default:
                throw new UnsupportedOperationException("Unknown or not supported emotion with index: " + index);
        }
    }

    public boolean isPositive() {
        switch (this) {
            case ANGER:
            case DISGUST:
            case FEAR:
            case SAD:
                return false;
            case HAPPY:
            case SURPRISE:
            case NEUTRAL:
                return true;
            default:
                throw new UnsupportedOperationException("Unknown or not supported emotion: " + this);
        }

    }


    @JsonProperty("name")
    public String getValue() {
        switch (this) {
            case ANGER:
            case DISGUST:
            case FEAR:
            case SAD:
                return "NEGATIVE";
            case HAPPY:
            case SURPRISE:
            case NEUTRAL:
                return "ОК";

            default:
                throw new UnsupportedOperationException("Unknown or not supported emotion: " + this);
        }
    }

    @JsonProperty("status")
    public boolean getStatus() {
        return getValue().equals("ОК");
    }

    @JsonProperty("probability")
    public double getProbability() {
        return probability;
    }

    @JsonIgnore
    public int getEmotionId() {
        return emotionId;
    }

    @JsonIgnore
    public Color getColor() {
        switch (this) {
            case ANGER:
            case DISGUST:
            case FEAR:
            case SAD:
                return new Color(0, 103, 170);
            case HAPPY:
            case SURPRISE:
            case NEUTRAL:
                return new Color(214, 76, 0);

            default:
                throw new UnsupportedOperationException("Unknown or not supported emotion: " + this);
        }
    }

    @JsonIgnore
    public Color getTextColor() {
        switch (this) {
            case ANGER:
            case DISGUST:
            case FEAR:
            case SAD:
                return new Color(255, 255, 255);
            case HAPPY:
            case SURPRISE:
            case NEUTRAL:
                return new Color(255, 255, 255);

            default:
                throw new UnsupportedOperationException("Unknown or not supported emotion: " + this);
        }
    }


    public void setProbability(double probability) {
        this.probability = probability;
    }


    @Override
    public String toString() {
        return "Emotion{" +
                "emotionId=" + emotionId +
                ", emotionName=" + getValue() +
                ", probability=" + probability +
                '}';
    }
}
