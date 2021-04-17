package ru.sstu.vak.emotionrecognition.uigame;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.FrameGrabber;
import ru.sstu.vak.emotionrecognition.common.Emotion;
import ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.EmotionRecognizer;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.info.FrameInfo;

public class GameCore {

    private static final Logger log = LogManager.getLogger(GameCore.class.getName());

    private int gameTime = 60; // sec

    private int beforeStartTime = 1500; // ms

    private int emotionAchievedTime = 1000; // ms


    private int[] emotionOrder = new int[]{3, 5, 4, 6, 1, 0, 2};

    private int achievedEmotions = 0;

    private int awaitEmotion = 0;

    private boolean isStart = false;

    private AtomicInteger gameTickCounter = new AtomicInteger(0);


    private Timer emotionTimer;

    private Timer gameOverTimer;

    private Timer beforeStartTimer;


    private String pathToVid;

    private EmotionRecognizer emotionRecognizerGame;


    private Callback callback;

    private FrameInfo currentFrameInfo;


    public GameCore(String pathToVid, EmotionRecognizer emotionRecognizerGame) {
        this.pathToVid = pathToVid;
        this.emotionRecognizerGame = emotionRecognizerGame;
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


    public int getFirstAwaitEmotion() {
        return emotionOrder[0];
    }

    public int getLastAwaitEmotion() {
        return emotionOrder[emotionOrder.length - 1];
    }

    public int getNextAwaitEmotion() {
        return emotionOrder[awaitEmotion + 1];
    }

    public int getAwaitEmotionId() {
        return emotionOrder[awaitEmotion];
    }

    public boolean allEmotionsAchieved() {
        return achievedEmotions == emotionOrder.length;
    }

    public void start(Callback callback) {
        this.callback = callback;

        beforeStartTimer = new Timer();
        beforeStartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    emotionRecognizerGame.processVideo(pathToVid, frameInfo -> {
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

                        List<VideoFace> faces = frameInfo.getVideoFaces();

                        if (!faces.isEmpty() && emotionTimer == null &&
                            faces.get(0).getPrediction().getEmotion().getEmotionId() == emotionOrder[awaitEmotion]) {

                            callback.onCorrectEmotion(faces.get(0).getPrediction().getEmotion());
                            emotionTimer = new Timer();
                            emotionTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    callback.onEmotionAchieved(currentFrameInfo);
                                    emotionTimer.cancel();
                                    emotionTimer = null;

                                    achievedEmotions++;

                                    if (awaitEmotion == 6) {
                                        stop();
                                    } else {
                                        awaitEmotion++;
                                    }
                                }
                            }, emotionAchievedTime);
                        }

                        if (emotionTimer != null && (faces.isEmpty() ||
                            faces.get(0).getPrediction().getEmotion().getEmotionId() != emotionOrder[awaitEmotion])) {
                            callback.onEmotionFailed();
                            emotionTimer.cancel();
                            emotionTimer = null;
                        }

                    });
                } catch (FrameGrabber.Exception e) {
                    log.error("Some error occurred", e);
                }
            }
        }, beforeStartTime);
    }

    public void stop() {
        emotionRecognizerGame.setOnStopListener(videoInfo -> {
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
            achievedEmotions = 0;
            isStart = false;
        });
        emotionRecognizerGame.stop();
    }

    public boolean isRun() {
        return emotionRecognizerGame.isRun();
    }


}
