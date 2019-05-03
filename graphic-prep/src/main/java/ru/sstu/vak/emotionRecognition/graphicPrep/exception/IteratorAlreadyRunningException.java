package ru.sstu.vak.emotionRecognition.graphicPrep.exception;

public class IteratorAlreadyRunningException extends RuntimeException {
    public IteratorAlreadyRunningException() {
        super("Iterator is already running!");
    }

    public IteratorAlreadyRunningException(String message) {
        super(message);
    }
}
