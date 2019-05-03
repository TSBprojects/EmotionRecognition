package ru.sstu.vak.emotionRecognition.faceDetector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_imgproc.*;


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
     * @param frame the frame captured by the {@link org.bytedeco.javacv.FrameGrabber}
     * @return A map of faces along with their coordinates in the frame
     */
    public synchronized Map<Rect, Mat> detect(Frame frame) {
        log.debug("Detecting faces on frame...");

        Map<Rect, Mat> detectedFaces = new HashMap<>();

        log.debug("Convert Frame to Mat...");
        Mat matFrame = converterToMat.convert(frame);
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

            Mat croppedFaceMat = matFrameGrayEqualizedHist.apply(face);

            detectedFaces.put(face, croppedFaceMat);
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

//    /**
//     * Detects and returns a map of cropped faces from a given captured frame
//     *
//     * @param frame the frame captured by the {@link org.bytedeco.javacv.FrameGrabber}
//     * @return A map of faces along with their coordinates in the frame
//     */
//    public synchronized Map<Rect, Mat> detect(Frame frame) {
//        log.info("Detecting faces on frame...");
//
//        Map<Rect, Mat> detectedFaces = new HashMap<>();
//
//        Mat matFrame = converterToMat.convert(frame);
//        Mat matFrameGrayEqualizedHist = new Mat();
//        //Mat matFrameGray;
//
//        if (matFrame.channels() > 1) {
//            cvtColor(matFrame, matFrameGrayEqualizedHist, COLOR_BGRA2GRAY);
//        } else {
//            matFrameGrayEqualizedHist = matFrame;
//        }
//
//        //matFrameGray = matFrameGrayEqualizedHist.clone();
//        equalizeHist(matFrameGrayEqualizedHist, matFrameGrayEqualizedHist);
//
//        RectVector faces = new RectVector();
//        cascadeClassifier.detectMultiScale(matFrameGrayEqualizedHist, faces);
////        cascadeClassifier.detectMultiScale(matFrameGrayEqualizedHist, faces,1.1,3,0,new Size(),new Size());
//
//        for (int i = 0; i < faces.size(); i++) {
//            Rect face = faces.get(i);
//
////            Mat croppedFaceMat = matFrameGray.apply(face);
//
//            Mat croppedFaceMat = matFrameGrayEqualizedHist.apply(face);
//
//            detectedFaces.put(face, croppedFaceMat);
//        }
//
//        return detectedFaces;
//    }
}


