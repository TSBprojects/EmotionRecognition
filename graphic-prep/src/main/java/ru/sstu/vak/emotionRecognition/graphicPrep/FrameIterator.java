package ru.sstu.vak.emotionRecognition.graphicPrep;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.*;
import ru.sstu.vak.emotionRecognition.graphicPrep.exception.IteratorAlreadyRunningException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrameIterator implements AutoCloseable {

    private static final Logger log = LogManager.getLogger(FrameIterator.class.getName());


    private ExecutorService executorService;

    private FrameGrabber frameGrabber;

    private FFmpegFrameRecorder frameRecorder;

    private volatile boolean run = false;


    private StopListener onStopListener;

    private ExceptionListener exceptionListener;


    public FrameIterator() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface ExceptionListener {
        void onException(Throwable e);
    }

    public interface RecordFrameListener {
        Frame onNextFrame(Frame frame);
    }

    public interface FrameListener {
        void onNextFrame(Frame frame);
    }

    public interface StopListener {
        void onIteratorStopped();
    }

    public void setOnExceptionListener(ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
    }


    public boolean isRun() {
        return run;
    }

    public void start(int deviceIndex, FrameListener frameListener) throws FrameGrabber.Exception {
        if (frameGrabber == null) {
            log.info("Starting OpenCVFrameGrabber with deviceIndex '{}'", deviceIndex);
            frameGrabber = OpenCVFrameGrabber.createDefault(deviceIndex);
            core(frameListener);
        } else {
            IteratorAlreadyRunningException e = new IteratorAlreadyRunningException();
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void start(String fileName, FrameListener frameListener) throws FrameGrabber.Exception {
        if (frameGrabber == null) {
            log.info("Starting OpenCVFrameGrabber with fileName '{}'", fileName);
            frameGrabber = new FFmpegFrameGrabber(fileName);
            core(frameListener);
        } else {
            IteratorAlreadyRunningException e = new IteratorAlreadyRunningException();
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private void core(FrameListener frameListener) {
        run = true;

        log.debug("Submit executor service task with frame grabber...");
        executorService.submit(() -> {
            try {
                log.debug("Start frame grabber");
                frameGrabber.start();

                while (run) {
                    log.debug("Grabbing frame...");
                    Frame videoFrame = frameGrabber.grab();
                    if (videoFrame == null) {
                        break;
                    }
                    if (videoFrame.image != null) {
                        frameListener.onNextFrame(videoFrame);
                        if (!run) {
                            stopGrabber();
                        }
                    }
                }
            } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
                log.error(e.getMessage(), e);
                frameGrabber = null;
                frameRecorder = null;
                if (exceptionListener != null) {
                    exceptionListener.onException(e);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }


    public void startRecord(int deviceIndex, String videoFile, RecordFrameListener listener) throws FrameGrabber.Exception {
        if (frameGrabber == null) {
            log.info("Starting OpenCVFrameGrabber with deviceIndex '{}'", deviceIndex);
            frameGrabber = OpenCVFrameGrabber.createDefault(deviceIndex);
            recordCore(videoFile, listener);
        } else {
            IteratorAlreadyRunningException e = new IteratorAlreadyRunningException();
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void startRecord(String fileName, String videoFile, RecordFrameListener listener) throws FrameGrabber.Exception {
        if (frameGrabber == null) {
            log.info("Starting FFmpegFrameGrabber with fileName '{}'", fileName);
            frameGrabber = new FFmpegFrameGrabber(fileName);
            recordCore(videoFile, listener);
        } else {
            IteratorAlreadyRunningException e = new IteratorAlreadyRunningException();
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private void recordCore(String videoFile, RecordFrameListener listener) {
        run = true;

        log.debug("Submit executor service task with frame grabber...");
        executorService.submit(() -> {
            try {
                log.debug("Start frame grabber");
                frameGrabber.start();

                while (run) {
                    log.debug("Grabbing frame...");
                    Frame videoFrame = frameGrabber.grab();
                    if (videoFrame == null) {
                        break;
                    }
                    if (videoFrame.image != null) {
                        if (frameRecorder == null) {
                            frameRecorder = new FFmpegFrameRecorder(videoFile, frameGrabber.getImageWidth(), frameGrabber.getImageHeight());
                            frameRecorder.start();
                            //FIXME recorder не работает
                        }
                        frameRecorder.record(listener.onNextFrame(videoFrame));
                    }
                }
                stopGrabber();
            } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
                log.error(e.getMessage(), e);
                if (exceptionListener != null) {
                    exceptionListener.onException(e);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }


    public void stop() {
        log.info("Stopping FrameIterator...");
        run = false;
    }

    public void stop(StopListener onStopListener) {
        log.info("Stopping FrameIterator...");
        this.onStopListener = onStopListener;
        this.run = false;
    }

    @Override
    public void close() throws Exception {
        log.debug("Shutdown executor service...");
        this.executorService.shutdown();
    }

    private void stopGrabber() throws FrameGrabber.Exception, FrameRecorder.Exception {
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
        log.info("FrameIterator stopped");
    }

}
