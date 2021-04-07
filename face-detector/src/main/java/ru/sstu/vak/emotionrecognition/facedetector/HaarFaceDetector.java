package ru.sstu.vak.emotionrecognition.facedetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGRA2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

/**
 * Face detector using haar classifier cascades
 */
public class HaarFaceDetector {

    private static final Logger log = LogManager.getLogger(HaarFaceDetector.class.getName());

    private final static String CLASSIFIER_PATH = "/haarcascadeModel/model_alt.xml";

    private CascadeClassifier cascadeClassifier;

    private OpenCVFrameConverter.ToMat converterToMat;


    public HaarFaceDetector() throws IOException {
        log.info("Initialize Haar's cascade classifier...");
        this.cascadeClassifier = new CascadeClassifier(getClassifierPath(CLASSIFIER_PATH));
        this.converterToMat = new OpenCVFrameConverter.ToMat();
    }


    /**
     * Detects and returns a map of cropped faces from a given captured frame
     *
     * @param frame       the frame captured by the {@link org.bytedeco.javacv.FrameGrabber}
     * @param originFaces if true return cropped origin faces, else return processed faces
     * @return A map of faces along with their coordinates in the frame
     */
    public synchronized Map<Rect, Mat> detect(Frame frame, boolean originFaces) {
        log.debug("Convert Frame to Mat...");
        return detect(converterToMat.convert(frame), originFaces);
    }

    /**
     * Detects and returns a map of cropped faces from a given captured frame
     *
     * @param frame       the frame captured by the {@link org.bytedeco.javacv.FrameGrabber}
     *                    and converted to {@link org.bytedeco.javacpp.opencv_core.Mat}
     * @param originFaces if true return cropped origin faces, else return processed faces
     * @return A map of faces along with their coordinates in the frame
     */
    public synchronized Map<Rect, Mat> detect(Mat frame, boolean originFaces) {
        Mat matFrame = frame.clone();

        log.debug("Detecting faces on frame...");

        Map<Rect, Mat> detectedFaces = new HashMap<>();

        Mat matFrameGrayEqualizedHist = new Mat();

        if (matFrame.channels() > 1) {
            log.debug("Convert Mat image to grayscale format...");
            cvtColor(matFrame, matFrameGrayEqualizedHist, COLOR_BGRA2GRAY);
        } else {
            matFrameGrayEqualizedHist = matFrame;
        }

        log.debug("Do histogram equalization on Mat image...");
        equalizeHist(matFrameGrayEqualizedHist, matFrameGrayEqualizedHist);

        log.debug("Detecting faces on processed Mat image...");
        RectVector faces = new RectVector();
        cascadeClassifier.detectMultiScale(matFrameGrayEqualizedHist, faces);

        log.debug("Crop faces from the Mat image...");
        for (int i = 0; i < faces.size(); i++) {
            Rect face = faces.get(i);

            Mat croppedFaceMat;
            if (originFaces) {
                croppedFaceMat = matFrame.apply(face);
            } else {
                croppedFaceMat = matFrameGrayEqualizedHist.apply(face);
            }

            detectedFaces.put(new Rect(face), croppedFaceMat);
        }

        return detectedFaces;
    }

    private String getClassifierPath(String classifierPath) throws IOException {

        File file;
        URL res = getClass().getResource(classifierPath);
        if (res.getProtocol().equals("jar")) {

            InputStream input = getClass().getResourceAsStream(classifierPath);
            file = File.createTempFile("temp", ".tmp");
            OutputStream out = new FileOutputStream(file);
            int read;
            byte[] bytes = new byte[1024];

            while ((read = input.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.close();
            file.deleteOnExit();

            return file.getPath();
        } else {
            return res.getPath()
                    .replace("%20", " ")
                    .substring(1);
        }
    }

}


