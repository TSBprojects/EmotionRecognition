package ru.sstu.vak.emotionrecognition.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Descriptable extends Nameable {
    @JsonIgnore
    String getDescription();
}
