package ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer.impl;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import static ru.sstu.vak.emotionrecognition.cnn.FeedForwardCNN.INPUT_HEIGHT;
import static ru.sstu.vak.emotionrecognition.cnn.FeedForwardCNN.INPUT_WIDTH;
import ru.sstu.vak.emotionrecognition.common.Emotion;
import ru.sstu.vak.emotionrecognition.facedetector.BoundingBox;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.FacePreProcessing;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.ImageConverter;
import ru.sstu.vak.emotionrecognition.identifyemotion.dataface.impl.VideoFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.datainfo.FrameInfo;
import ru.sstu.vak.emotionrecognition.identifyemotion.datainfo.VideoFrame;

public class EmotionRecognizerGame extends EmotionRecognizerBase {

    private static final Logger log = LogManager.getLogger(EmotionRecognizerGame.class.getName());

    public EmotionRecognizerGame(String modelPath) throws IOException {
        super(modelPath);
    }


    @Override
    public synchronized void processVideo(String readFrom, ProcessedFrameListener listener) throws FrameGrabber.Exception {
        log.info("Starting video with emotion recognition...");
        frameIterator.start(readFrom, frame -> listener.onNextFrame(new FrameInfo(processedFrame(frame))));
    }


    private FrameInfo processedFrame(Frame frame) {
        if (frameListener != null) {
            frameListener.onNextFrame(frame);
        }
        List<VideoFace> videoFacesList = new ArrayList<>();


        Size maxSize = new Size(0, 0);
        Emotion maxEmotion = null;
        VideoFace.Location maxLocation = null;

        Mat matImage = ImageConverter.toMat(frame);
        BufferedImage buffFrame = ImageConverter.toBufferedImage(frame);
        Map<Rect, Mat> faces = haarFaceDetector.detect(matImage, false);

        for (Map.Entry<Rect, Mat> entry : faces.entrySet()) {
            Rect rect = entry.getKey();

            try {
                VideoFace.Location videoLocation = new VideoFace.Location(rect.x(), rect.y(), rect.width(), rect.height());
                Mat preparedFace = FacePreProcessing.process(matImage.apply(rect), INPUT_WIDTH, INPUT_HEIGHT);
                if (videoNetInputListener != null) {
                    videoNetInputListener.onNextFace(preparedFace.clone());
                }
                Emotion emotion = feedForwardCNN.predict(preparedFace);
                BoundingBox.draw(buffFrame, rect, emotion);

                if (rect.size().area() > maxSize.area()) {
                    maxSize = rect.size();
                    maxEmotion = emotion;
                    maxLocation = videoLocation;
                }

            } catch (IOException e) {
                log.error(e.getMessage(), e);
                e.printStackTrace();
                throwException(e);
            }
        }
        videoFacesList.add(new VideoFace(maxEmotion, maxLocation));

        FrameInfo frameInfo;
        if (maxEmotion == null) {
            frameInfo = new FrameInfo(frames.size(), buffFrame, null);
        } else {
            frameInfo = new FrameInfo(frames.size(), buffFrame, videoFacesList);
        }

        VideoFrame videoFrame = new VideoFrame(frames.size(), videoFacesList);
        frames.add(videoFrame);
        return frameInfo;
    }

}
