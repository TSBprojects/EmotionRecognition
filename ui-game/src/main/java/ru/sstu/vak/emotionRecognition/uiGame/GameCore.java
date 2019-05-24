package ru.sstu.vak.emotionRecognition.uiGame;

import org.bytedeco.javacv.FrameGrabber;
import ru.sstu.vak.emotionRecognition.common.Emotion;
import ru.sstu.vak.emotionRecognition.identifyEmotion.dataInfo.impl.FrameInfo;

import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class GameCore {

    private int gameTime = 60; // sec

    private int beforeStartTime = 1500; // ms

    private int emotionAchievedTime = 1000; // ms


    private int awaitEmotion = 0;

    private boolean isStart = false;

    private AtomicInteger gameTickCounter = new AtomicInteger(0);


    private Timer emotionTimer;

    private Timer gameOverTimer;

    private Timer beforeStartTimer;


    private String pathToVid;

    private EmotionRecognizerGame emotionRecognizer;


    private Callback callback;

    private FrameInfo currentFrameInfo;


    public GameCore(String pathToVid, EmotionRecognizerGame emotionRecognizer) {
        this.pathToVid = pathToVid;
        this.emotionRecognizer = emotionRecognizer;
    }

    public interface Callback {
        void onStart();

        void onFrameProcessed(BufferedImage frame);

        void onGameTick(int tick);

        void onCorrectEmotion(Emotion emotion);

        void onEmotionAchieved(FrameInfo frameInfo);

        void onEmotionFailed();

        void onGameOver();
    }


    public void setGameTime(int sec) {
        this.gameTime = sec;
    }

    public void setBeforeStartTime(int ms) {
        this.beforeStartTime = ms;
    }

    public void setEmotionAchievedTime(int ms) {
        this.emotionAchievedTime = ms;
    }

    public int getAwaitEmotionId() {
        return awaitEmotion;
    }

    public void start(Callback callback) {
        this.callback = callback;

        beforeStartTimer = new Timer();
        beforeStartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    emotionRecognizer.processVideo(pathToVid, frameInfo -> {
                        currentFrameInfo = frameInfo;
                        if (!isStart) {
                            callback.onStart();
                            callback.onGameTick(gameTickCounter.getAndIncrement());
                            gameOverTimer = new Timer();
                            gameOverTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (gameTickCounter.get() >= gameTime) {
                                        stop();
                                    }
                                    callback.onGameTick(gameTickCounter.getAndIncrement());
                                }
                            }, 1000, 1000);
                            isStart = true;
                        }

                        callback.onFrameProcessed(frameInfo.getProcessedImage());

                        if (frameInfo.getVideoFaces() != null && emotionTimer == null &&
                                frameInfo.getVideoFaces().get(0).getEmotion().getEmotionId() == awaitEmotion) {

                            callback.onCorrectEmotion(frameInfo.getVideoFaces().get(0).getEmotion());
                            emotionTimer = new Timer();
                            emotionTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    callback.onEmotionAchieved(currentFrameInfo);
                                    emotionTimer.cancel();
                                    emotionTimer = null;

                                    if (awaitEmotion == 6) {
                                        stop();
                                    }
                                    awaitEmotion++;
                                }
                            }, emotionAchievedTime);
                        }

                        if (emotionTimer != null && (frameInfo.getVideoFaces() == null ||
                                frameInfo.getVideoFaces().get(0).getEmotion().getEmotionId() != awaitEmotion)) {
                            callback.onEmotionFailed();
                            emotionTimer.cancel();
                            emotionTimer = null;
                        }

                    });
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            }
        }, beforeStartTime);
    }

    public void stop() {
        emotionRecognizer.setOnStopListener(videoInfo -> {
            if (beforeStartTimer != null) {
                beforeStartTimer.cancel();
                beforeStartTimer = null;
            }
            if (gameOverTimer != null) {
                gameOverTimer.cancel();
                gameOverTimer = null;
            }
            if (emotionTimer != null) {
                emotionTimer.cancel();
                emotionTimer = null;
            }
            callback.onGameOver();
            gameTickCounter.set(0);
            awaitEmotion = 0;
            isStart = false;
        });
        emotionRecognizer.stop();
    }

    public boolean isRun() {
        return emotionRecognizer.isRun();
    }


}
