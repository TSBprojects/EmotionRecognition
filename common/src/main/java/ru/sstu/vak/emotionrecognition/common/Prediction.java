package ru.sstu.vak.emotionrecognition.common;

public class Prediction {

    private final Emotion emotion;

    private final double probability;

    public Prediction(Emotion emotion, double probability) {
        this.emotion = emotion;
        this.probability = probability;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public double getProbability() {
        return probability;
    }
}
