package ru.sstu.vak.emotionrecognition.graphicprep.exception;

public class IteratorAlreadyRunningException extends RuntimeException {
    public IteratorAlreadyRunningException() {
        super("Iterator is already running!");
    }

    public IteratorAlreadyRunningException(String message) {
        super(message);
    }
}
