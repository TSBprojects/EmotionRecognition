package ru.sstu.vak.emotionRecognition.graphicPrep.frameIterator.impl;

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.*;
import ru.sstu.vak.emotionRecognition.graphicPrep.exception.IteratorAlreadyRunningException;
import ru.sstu.vak.emotionRecognition.graphicPrep.frameIterator.FrameIterator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FrameIteratorBase implements FrameIterator {

    private static final Logger log = LogManager.getLogger(FrameIteratorBase.class.getName());


    private static final int DEVICE_FRAME_RATE = 0; // unlimited

    private static final int VIDEO_FILE_FRAME_RATE = 30;


    private ExecutorService executorService;

    private FrameGrabber frameGrabber;

    private OpenCVFrameRecorder frameRecorder;


    private volatile boolean run = false;

    private StopListener onStopListener;

    private ExceptionListener onExceptionListener;


    private Stopwatch stopwatch;

    private final int fpsSamplingLimit = 50;

    private List<Frame> framesToRec = new ArrayList<>(fpsSamplingLimit);


    public FrameIteratorBase() {
        this.executorService = Executors.newSingleThreadExecutor();
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
    public boolean isRun() {
        return run;
    }

    @Override
    public void stop() {
        log.info("Stopping FrameIterator...");
        run = false;
    }

    @Override
    public void close() throws Exception {
        log.debug("Shutdown executor service...");
        this.executorService.shutdown();
    }


    @Override
    public void start(String readFrom, FrameListener frameListener) throws FrameGrabber.Exception {
        if (isDeviceId(readFrom)) {
            int deviceId = Integer.parseInt(readFrom);
            log.info("Starting OpenCVFrameGrabber with deviceIndex '{}'", deviceId);
            core(OpenCVFrameGrabber.createDefault(deviceId), frameListener::onNextFrame, DEVICE_FRAME_RATE);
        } else {
            log.info("Starting OpenCVFrameGrabber with fileName '{}'", readFrom);
            core(new FFmpegFrameGrabber(readFrom), frameListener::onNextFrame, VIDEO_FILE_FRAME_RATE);
        }
    }

    @Override
    public void start(String readFrom, Path writeTo, RecordFrameListener frameListener) throws FrameGrabber.Exception {
        if (isDeviceId(readFrom)) {
            int deviceId = Integer.parseInt(readFrom);
            log.info("Starting OpenCVFrameGrabber with deviceIndex '{}'", deviceId);

            stopwatch = Stopwatch.createStarted();
            core(OpenCVFrameGrabber.createDefault(deviceId), frame -> {
                recordCore(frameListener.onNextFrame(frame), writeTo);
            }, DEVICE_FRAME_RATE);
        } else {
            log.info("Starting OpenCVFrameGrabber with fileName '{}'", readFrom);

            stopwatch = Stopwatch.createStarted();
            core(new FFmpegFrameGrabber(readFrom), frame -> {
                recordCore(frameListener.onNextFrame(frame), writeTo);
            }, VIDEO_FILE_FRAME_RATE);
        }
    }


    private void core(FrameGrabber grabber, CoreFrameListener listener, int frameRate) {
        if (frameGrabber == null) {
            frameGrabber = grabber;
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
            if (framesToRec.size() >= fpsSamplingLimit) {
                if (frameRecorder == null) {
                    stopwatch.stop();
                    int frameRate = (int) (fpsSamplingLimit / stopwatch.elapsed(SECONDS)) + 3;

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
                }

                frameRecorder.record(videoFrame);

            } else {
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
            framesToRec.clear();
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
