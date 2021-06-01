package ru.sstu.vak.emotionrecognition.identifyemotion.emotionrecognizer;

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
import static ru.sstu.vak.emotionrecognition.cnn.FeedForwardCNN.INPUT_HEIGHT;
import static ru.sstu.vak.emotionrecognition.cnn.FeedForwardCNN.INPUT_WIDTH;
import ru.sstu.vak.emotionrecognition.common.Prediction;
import ru.sstu.vak.emotionrecognition.facedetector.BoundingBox;
import ru.sstu.vak.emotionrecognition.graphicprep.imageprocessing.FacePreProcessing;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.MediaFace;
import ru.sstu.vak.emotionrecognition.identifyemotion.media.face.VideoFace;

public class ClosestFaceEmotionRecognizer extends SimpleEmotionRecognizer {

    private static final Logger log = LogManager.getLogger(ClosestFaceEmotionRecognizer.class.getName());

    public ClosestFaceEmotionRecognizer(String modelPath) throws IOException {
        super(modelPath);
    }

    @Override
    protected List<VideoFace> processFaces(BufferedImage buffFrame, Map<Rect, Mat> faces) {
        List<VideoFace> videoFacesList = new ArrayList<>();

        Size maxSize = new Size(0, 0);
        Prediction closestFacePrediction = null;
        MediaFace.Location maxLocation = null;

        for (Map.Entry<Rect, Mat> entry : faces.entrySet()) {
            try {
                Rect rect = entry.getKey();
                Mat face = entry.getValue();
                MediaFace.Location videoLocation = new MediaFace.Location(rect.x(), rect.y(), rect.width(), rect.height());
                Mat preparedFace = FacePreProcessing.process(face, INPUT_WIDTH, INPUT_HEIGHT);
                if (videoNetInputListener != null) {
                    videoNetInputListener.onNextFace(preparedFace.clone());
                }
                Prediction prediction = feedForwardCNN.predict(preparedFace);
                BoundingBox.draw(buffFrame, rect, prediction);

                if (rect.size().area() > maxSize.area()) {
                    maxSize = rect.size();
                    closestFacePrediction = prediction;
                    maxLocation = videoLocation;
                }

            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throwException(e);
            }
        }

        maxSize.close();

        if (closestFacePrediction != null) {
            videoFacesList.add(new VideoFace(closestFacePrediction, maxLocation));
        }

        return videoFacesList;
    }
}
