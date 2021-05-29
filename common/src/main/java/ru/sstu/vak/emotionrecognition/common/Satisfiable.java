package ru.sstu.vak.emotionrecognition.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Satisfiable {
    @JsonIgnore
    boolean isSatisfied();
}
