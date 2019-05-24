package ru.sstu.vak.emotionRecognition.graphicPrep.iterators.frameIterator.impl;

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.*;
import ru.sstu.vak.emotionRecognition.graphicPrep.exception.IteratorAlreadyRunningException;
import ru.sstu.vak.emotionRecognition.graphicPrep.iterators.frameIterator.FrameIterator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FrameIteratorBase implements FrameIterator {

    private static final Logger log = LogManager.getLogger(FrameIteratorBase.class.getName());


    private ExecutorService executorService;

    private FrameGrabber frameGrabber;

    private OpenCVFrameRecorder frameRecorder;


    private volatile boolean run = false;

    private StopListener onStopListener;

    private ExceptionListener onExceptionListener;


    private int deviceFrameRate = 0; // 0 - unlimited

    private int videoFileFrameRate = 0; // 0 - unlimited


    private Stopwatch stopwatch;

    private final int SAMPLING_COUNT_FOR_FPS_MEASURE = 50;

    private int sampleFrameCount = 0;

    private List<Frame> framesToRec = new ArrayList<>(SAMPLING_COUNT_FOR_FPS_MEASURE);


    public FrameIteratorBase() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void setDeviceFrameRate(Integer deviceFrameRate) {
        if (deviceFrameRate != null && deviceFrameRate > 0) {
            this.deviceFrameRate = deviceFrameRate;
        }
    }

    @Override
    public void setFileFrameRate(Integer videoFileFrameRate) {
        if (videoFileFrameRate != null && videoFileFrameRate > 0) {
            this.videoFileFrameRate = videoFileFrameRate;
        }
    }

    @Override
    public void setOnExceptionListener(ExceptionListener onExceptionListener) {
        this.onExceptionListener = onExceptionListener;
    }

    @Override
    public void setOnStopListener(StopListener onStopListener) {
        this.onStopListener = onStopListener;
    }

    @Override
    public synchronized boolean isRun() {
        return frameGrabber != null || frameRecorder != null || run;
    }

    @Override
    public void stop() {
        log.info("Stopping FrameIterator...");
        run = false;
    }

    @Override
    public void close() {
        log.debug("Shutdown executor service...");
        this.executorService.shutdown();
    }


    @Override
    public void start(String readFrom, FrameListener frameListener) throws FrameGrabber.Exception {
        if (isDeviceId(readFrom)) {
            int deviceId = Integer.parseInt(readFrom);
            log.info("Starting OpenCVFrameGrabber with deviceIndex '{}'", deviceId);
            core(OpenCVFrameGrabber.createDefault(deviceId), frameListener::onNextFrame, deviceFrameRate);
        } else {
            log.info("Starting OpenCVFrameGrabber with fileName '{}'", readFrom);
            core(new FFmpegFrameGrabber(readFrom), frameListener::onNextFrame, videoFileFrameRate);
        }
    }

    @Override
    public void start(String readFrom, Path writeTo, RecordFrameListener frameListener) throws FrameGrabber.Exception {
        if (isDeviceId(readFrom)) {
            int deviceId = Integer.parseInt(readFrom);
            log.info("Starting OpenCVFrameGrabber with deviceIndex '{}'", deviceId);

            stopwatch = Stopwatch.createStarted();
            core(OpenCVFrameGrabber.createDefault(deviceId), frame -> recordCore(frameListener.onNextFrame(frame), writeTo), deviceFrameRate);
        } else {
            log.info("Starting OpenCVFrameGrabber with fileName '{}'", readFrom);

            stopwatch = Stopwatch.createStarted();
            core(new FFmpegFrameGrabber(readFrom), frame -> recordCore(frameListener.onNextFrame(frame), writeTo), videoFileFrameRate);
        }
    }

    private void core(FrameGrabber grabber, CoreFrameListener listener, int frameRate) {
        if (frameGrabber == null) {
            frameGrabber = grabber;
            frameGrabber.setImageWidth(FRAME_WIDTH);
            frameGrabber.setImageHeight(FRAME_HEIGHT);
        } else {
            IteratorAlreadyRunningException e = new IteratorAlreadyRunningException();
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }

        log.debug("Submit executor service task with frame grabber...");

        executorService.submit(() -> {

            try {
                log.debug("Start frame grabber");
                frameGrabber.start();
                run = true;

                while (run) {

                    log.debug("Grabbing frame...");
                    Frame videoFrame = null;
                    try {
                        videoFrame = frameGrabber.grab();
                    } catch (FrameGrabber.Exception e) {
                        log.error(e.getMessage(), e);
                        e.printStackTrace();
                        stopGrabber();
                        throwException(e);
                    }
                    if (videoFrame == null) {
                        run = false;
                        break;
                    }
                    if (videoFrame.image != null) {
                        videoFrame.opaque = null;
                        listener.onNextFrame(videoFrame.clone());
                    }
                    if (frameRate > 0) {
                        Thread.sleep(1000 / frameRate);
                    }
                }
                stopGrabber();

            } catch (FrameGrabber.Exception | InterruptedException e) {
                log.error(e.getMessage(), e);
                e.printStackTrace();
                stopGrabber();
                throwException(e);
            }
        });
    }


    private void recordCore(Frame videoFrame, Path videoFile) {
        try {
            if (sampleFrameCount >= SAMPLING_COUNT_FOR_FPS_MEASURE) {
                if (frameRecorder == null) {
                    stopwatch.stop();
                    int frameRate = (int) (SAMPLING_COUNT_FOR_FPS_MEASURE / stopwatch.elapsed(SECONDS)) + 3;

                    frameRecorder = new OpenCVFrameRecorder(
                            videoFile.toString(),
                            frameGrabber.getImageWidth(),
                            frameGrabber.getImageHeight()
                    );
                    frameRecorder.setFormat("mp4");
                    frameRecorder.setPixelFormat(1);
                    frameRecorder.setFrameRate(frameRate);
                    frameRecorder.start();

                    for (Frame frame : framesToRec) {
                        frameRecorder.record(frame);
                    }

                    framesToRec.clear();
                }

                frameRecorder.record(videoFrame);

            } else {
                sampleFrameCount++;
                framesToRec.add(videoFrame);
            }

        } catch (FrameRecorder.Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
            stopGrabber();
            throwException(e);
        }

//        try {
//            if (frameRecorder == null) {
//                frameRecorder = new OpenCVFrameRecorder(
//                        videoFile.toString(),
//                        frameGrabber.getImageWidth(),
//                        frameGrabber.getImageHeight()
//                );
//                frameRecorder.setFormat("mp4");
//                frameRecorder.setPixelFormat(1);
//                frameRecorder.setFrameRate(15);
//                frameRecorder.start();
//            }
//            frameRecorder.record(videoFrame);
//        } catch (FrameRecorder.Exception e) {
//            log.error(e.getMessage(), e);
//            e.printStackTrace();
//            stopGrabber();
//            throwException(e);
//        }
    }

    private void stopGrabber() {
        try {
            log.debug("Stopping frame grabber...");
            if (frameGrabber != null) {
                frameGrabber.release();
                frameGrabber.close();
                frameGrabber.stop();
                frameGrabber = null;
            }
            if (frameRecorder != null) {
                frameRecorder.release();
                frameRecorder.close();
                frameRecorder.stop();
                frameRecorder = null;
            }
            if (onStopListener != null) {
                onStopListener.onIteratorStopped();
            }
            sampleFrameCount = 0;
            log.info("FrameIterator stopped");
        } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
            throwException(e);
        }
    }

    private interface CoreFrameListener {
        void onNextFrame(Frame frame);
    }


    private void throwException(Throwable e) {
        if (onExceptionListener != null) {
            onExceptionListener.onException(e);
        }
    }

    private boolean isDeviceId(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
