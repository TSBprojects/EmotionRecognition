package ru.sstu.vak.emotionRecognition.identifyEmotion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import ru.sstu.vak.emotionRecognition.cnn.FeedForwardCNN;
import ru.sstu.vak.emotionRecognition.common.Emotion;
import ru.sstu.vak.emotionRecognition.faceDetector.BoundingBox;
import ru.sstu.vak.emotionRecognition.faceDetector.HaarFaceDetector;
import ru.sstu.vak.emotionRecognition.graphicPrep.FacePreProcessing;
import ru.sstu.vak.emotionRecognition.graphicPrep.FrameIterator;
import ru.sstu.vak.emotionRecognition.graphicPrep.ImageConverter;
import ru.sstu.vak.emotionRecognition.identifyEmotion.image.ImageFace;
import ru.sstu.vak.emotionRecognition.identifyEmotion.image.ImageInfo;
import ru.sstu.vak.emotionRecognition.identifyEmotion.video.VideoFace;
import ru.sstu.vak.emotionRecognition.identifyEmotion.video.VideoFrame;
import ru.sstu.vak.emotionRecognition.identifyEmotion.video.VideoInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.sstu.vak.emotionRecognition.cnn.FeedForwardCNN.HEIGHT;
import static ru.sstu.vak.emotionRecognition.cnn.FeedForwardCNN.WIDTH;

public class EmotionRecognizer {

    private static final Logger log = LogManager.getLogger(EmotionRecognizer.class.getName());

    private FrameIterator frameIterator;
    private HaarFaceDetector haarFaceDetector;
    private FeedForwardCNN feedForwardCNN;

    private int boundingBoxBorderThickness = 5;
    private int boundingBoxTopPaneHeight = 40;

    private ExceptionListener exceptionListener;
    private FrameListener frameListener;
    private NetInputListener videoNetInputListener;
    private NetInputListener imageNetInputListener;
    private ProcessedFrameListener processedFrameListener;

    private List<VideoFrame> frames;


    public EmotionRecognizer(String modelPath) throws IOException {
        this.frameIterator = new FrameIterator();
        this.haarFaceDetector = new HaarFaceDetector();
        this.feedForwardCNN = new FeedForwardCNN(modelPath);
        this.frames = new ArrayList<>();

        this.frameIterator.setOnExceptionListener(e -> {
            if (exceptionListener != null) {
                exceptionListener.onException(e);
            }
        });
    }


    public interface ExceptionListener {
        void onException(Throwable e);
    }

    public interface FrameListener {
        void onNextFrame(Frame frame);
    }

    public interface ProcessedFrameListener {
        void onNextFrame(BufferedImage frame);

        void onStop(VideoInfo videoInfo);
    }

    public interface NetInputListener {
        void onNextFace(Mat frame);
    }


    public void setBoundingBoxBorderThickness(int boundingBoxBorderThickness) {
        this.boundingBoxBorderThickness = boundingBoxBorderThickness;
    }

    public void setBoundingBoxTopPaneHeight(int boundingBoxTopPaneHeight) {
        this.boundingBoxTopPaneHeight = boundingBoxTopPaneHeight;
    }


    public void setOnExceptionListener(ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
    }

    public void setFrameListener(FrameListener frameListener) {
        this.frameListener = frameListener;
    }

    public void setImageNetInputListener(NetInputListener imageNetInputListener) {
        this.imageNetInputListener = imageNetInputListener;
    }

    public void setVideoNetInputListener(NetInputListener videoNetInputListener) {
        this.videoNetInputListener = videoNetInputListener;
    }


    public void writeVideoInfo(VideoInfo videoInfo, Path videoPath) throws IOException {
        log.info("Write video info...");

        final String fileName = FilenameUtils.removeExtension(videoPath.getFileName().toString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(videoPath.getParent() + "\\" + fileName + "-videoInfo.json"), videoInfo);
    }

    public void writeImageInfo(ImageInfo imageInfo, Path imagePath, boolean saveFaces) throws IOException {
        log.info("Write image info...");

        final String fileName = FilenameUtils.removeExtension(imagePath.getFileName().toString());

        if (saveFaces) {
            List<ImageFace> imageFaces = imageInfo.getImageFaces();
            for (int i = 0; i < imageFaces.size(); i++) {
                ImageIO.write(
                        imageFaces.get(i).getFaceImage(),
                        "png",
                        new File(imagePath.getParent() + "\\" + fileName + "-face" + i + ".png"));
            }
        }
        ImageIO.write(
                imageInfo.getProcessedImage(),
                "png",
                new File(imagePath.getParent() + "\\" + fileName + "-processed.png"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(
                imagePath.getParent() + "\\" + fileName + "-imageInfo.json"), imageInfo
        );
    }


    public synchronized ImageInfo processImage(BufferedImage image) {
        log.info("Finding faces and recognize emotions on them in the image...");
        List<ImageFace> imageFaceList = new ArrayList<>();

        Mat matImage = ImageConverter.toMat(image);
        Map<Rect, Mat> faces = haarFaceDetector.detect(ImageConverter.toFrame(image));
        faces.forEach((rect, face) -> {
            try {
                BufferedImage faceImage = ImageConverter.toBufferedImage(matImage.apply(rect));
                ImageFace.Location faceLocation = new ImageFace.Location(rect.x(), rect.y(), rect.width(), rect.height());
                Mat preparedFace = FacePreProcessing.process(face, WIDTH, HEIGHT, false);
                if (imageNetInputListener != null) {
                    imageNetInputListener.onNextFace(preparedFace);
                }
                Emotion emotion = feedForwardCNN.predict(preparedFace);
                BoundingBox.draw(image, rect, emotion, boundingBoxBorderThickness, boundingBoxTopPaneHeight);
                imageFaceList.add(new ImageFace(emotion, faceLocation, faceImage));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                if (exceptionListener != null) {
                    exceptionListener.onException(e);
                } else {
                    e.printStackTrace();
                }
            }
        });
        return new ImageInfo(image, imageFaceList);
    }

    public synchronized void video(int deviceId, FrameListener listener) throws FrameGrabber.Exception {
        log.info("Starting video...");
        frameIterator.start(deviceId, frame -> {
            processedFrame(frame, listener);
        });
    }

    public synchronized void video(String fileName, FrameListener listener) throws FrameGrabber.Exception {
        log.info("Starting video...");
        frameIterator.start(fileName, frame -> {
            processedFrame(frame, listener);
        });
    }

    public synchronized void processedVideo(int deviceId, ProcessedFrameListener listener) throws FrameGrabber.Exception {
        log.info("Starting video with emotion recognition...");
        processedFrameListener = listener;
        frameIterator.start(deviceId, frame -> {
            BufferedImage procImage = processedFrame(frame);
            listener.onNextFrame(procImage);
        });
    }

    public synchronized void processedVideo(String fileName, ProcessedFrameListener listener) throws FrameGrabber.Exception {
        log.info("Starting video with emotion recognition...");
        processedFrameListener = listener;
        frameIterator.start(fileName, frame -> {
            BufferedImage procImage = processedFrame(frame);
            listener.onNextFrame(procImage);
        });
    }

    public synchronized void processedRecordVideo(int deviceId, Path videoFile, ProcessedFrameListener listener) throws FrameGrabber.Exception {
        log.info("Starting video with emotion recognition...");
        processedFrameListener = listener;
        frameIterator.startRecord(deviceId, videoFile.toString(), frame -> {
            BufferedImage procImage = processedFrame(frame);
            listener.onNextFrame(procImage);
            return ImageConverter.toFrame(procImage);
        });
    }

    public synchronized void processedRecordVideo(String fileName, Path videoFile, ProcessedFrameListener listener) throws FrameGrabber.Exception {
        log.info("Starting video with emotion recognition...");
        processedFrameListener = listener;
        frameIterator.startRecord(fileName, videoFile.toString(), frame -> {
            BufferedImage procImage = processedFrame(frame);
            listener.onNextFrame(procImage);
            return ImageConverter.toFrame(procImage);
        });
    }


    public boolean isRun() {
        return frameIterator.isRun();
    }

    public void stop() {
        frameIterator.stop(() -> {
            processedFrameListener.onStop(new VideoInfo(frames));
            frames.clear();
        });
    }

    public void stop(FrameIterator.StopListener onStopListener) {
        frameIterator.stop(() -> {
            onStopListener.onIteratorStopped();
            processedFrameListener.onStop(new VideoInfo(frames));
            frames.clear();
        });
    }


    private void processedFrame(Frame frame, FrameListener listener) {
        if (frameListener != null) {
            frameListener.onNextFrame(frame);
        }
        listener.onNextFrame(frame);
    }

    private BufferedImage processedFrame(Frame frame) {
        if (frameListener != null) {
            frameListener.onNextFrame(frame);
        }
        List<VideoFace> videoFacesList = new ArrayList<>();

        Map<Rect, Mat> faces = haarFaceDetector.detect(frame);
        BufferedImage image = ImageConverter.toBufferedImage(frame);
        faces.forEach((rect, face) -> {
            try {
                VideoFace.Location videoLocation = new VideoFace.Location(rect.x(), rect.y(), rect.width(), rect.height());
                Mat preparedFace = FacePreProcessing.process(face, WIDTH, HEIGHT, false);
                if (videoNetInputListener != null) {
                    videoNetInputListener.onNextFace(preparedFace);
                }
                Emotion emotion = feedForwardCNN.predict(preparedFace);
                BoundingBox.draw(image, rect, emotion, boundingBoxBorderThickness, boundingBoxTopPaneHeight);
                videoFacesList.add(new VideoFace(emotion, videoLocation));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                if (exceptionListener != null) {
                    exceptionListener.onException(e);
                } else {
                    e.printStackTrace();
                }
            }
        });
        frames.add(new VideoFrame(frames.size(), videoFacesList));
        return image;
    }

}
