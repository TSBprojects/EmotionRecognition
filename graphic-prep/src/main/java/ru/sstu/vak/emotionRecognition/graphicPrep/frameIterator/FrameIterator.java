package ru.sstu.vak.emotionRecognition.graphicPrep.frameIterator;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.nio.file.Path;

public interface FrameIterator extends AutoCloseable {

    @FunctionalInterface
    interface ExceptionListener {
        void onException(Throwable e);
    }

    @FunctionalInterface
    interface RecordFrameListener {
        Frame onNextFrame(Frame frame);
    }

    @FunctionalInterface
    interface FrameListener {
        void onNextFrame(Frame frame);
    }

    @FunctionalInterface
    interface StopListener {
        void onIteratorStopped();
    }


    void setDeviceFrameRate(Integer deviceFrameRate);

    void setFileFrameRate(Integer videoFileFrameRate);

    void setOnExceptionListener(ExceptionListener onExceptionListener);

    void setOnStopListener(StopListener onStopListener);

    boolean isRun();

    void stop();


    void start(String readFrom, FrameListener frameListener) throws FrameGrabber.Exception;

    void start(String readFrom, Path writeTo, RecordFrameListener frameListener) throws FrameGrabber.Exception;

}
