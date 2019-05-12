package ru.sstu.vak.emotionRecognition.graphicPrep.frameIterator;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.nio.file.Path;

public interface FrameIterator extends AutoCloseable {

    interface RecordFrameListener {
        Frame onNextFrame(Frame frame);
    }

    interface FrameListener {
        void onNextFrame(Frame frame);
    }

    interface StopListener {
        void onIteratorStopped();
    }


    void setOnStopListener(StopListener onStopListener);

    boolean isRun();

    void stop();


    void start(String readFrom, FrameListener frameListener) throws FrameGrabber.Exception;

    void start(String readFrom, Path writeTo, RecordFrameListener frameListener) throws FrameGrabber.Exception;

}
