package ru.sstu.vak.emotionRecognition.identifyEmotion.emotionRecognizer;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.FrameGrabber;
import ru.sstu.vak.emotionRecognition.graphicPrep.frameIterator.FrameIterator;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataInfo.impl.ImageInfo;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataInfo.impl.VideoInfo;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public interface EmotionRecognizer {

    @FunctionalInterface
    interface StopListener {
        void onVideoStopped(VideoInfo videoInfo);
    }

    @FunctionalInterface
    interface ProcessedFrameListener {
        void onNextFrame(BufferedImage frame);
    }

    @FunctionalInterface
    interface NetInputListener {
        void onNextFace(Mat frame);
    }


    ImageInfo processImage(BufferedImage image);

    void processVideo(String readFrom, ProcessedFrameListener listener) throws FrameGrabber.Exception;

    void processVideo(String readFrom, Path writeTo, ProcessedFrameListener listener) throws FrameGrabber.Exception;

    void writeVideoInfo(VideoInfo videoInfo, Path writeTo) throws IOException;

    void writeImageInfo(ImageInfo imageInfo, Path writeTo, boolean saveFaces) throws IOException;

    boolean isRun();

    void stop();


    void setBBoxThickness(int boundingBoxBorderThickness);

    void setBBoxTopPaneHeight(int boundingBoxTopPaneHeight);

    void setOnExceptionListener(FrameIterator.ExceptionListener exceptionListener);

    void setOnStopListener(StopListener stopListener);

    void setFrameListener(FrameIterator.FrameListener frameListener);

    void setImageNetInputListener(NetInputListener imageNetInputListener);

    void setVideoNetInputListener(NetInputListener videoNetInputListener);

}
