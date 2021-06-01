package ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer;

public enum ErFeature {
    COLLECT_FRAMES(true),
    GENERATE_JSON_OUTPUT(true);

    final boolean defaultState;

    ErFeature(boolean defaultState) {
        this.defaultState = defaultState;
    }
}
