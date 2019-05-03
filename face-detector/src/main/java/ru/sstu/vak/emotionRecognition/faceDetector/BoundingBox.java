package ru.sstu.vak.emotionRecognition.faceDetector;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.Rect;
import ru.sstu.vak.emotionRecognition.common.Emotion;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BoundingBox {

    private static final Logger log = LogManager.getLogger(BoundingBox.class.getName());

    private static final int FONT_STYLE = Font.BOLD;
    private static final String FONT_NAME = "Consolas";
    private static final double FONT_HEIGHT_COEFFICIENT = 0.33;
    private static final double FONT_WIDTH_COEFFICIENT = 0.27;
    private static final int EDGE_INDENT = 10;

    public static void draw(BufferedImage image, Rect rect, Emotion emotion, int borderThickness, int topPaneHeight) {
        log.debug("Draw rectangle around the face...");
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setColor(emotion.getColor());
        g2.setStroke(new BasicStroke(borderThickness));
        g2.fill(new Rectangle(
                rect.x() - borderThickness / 2,
                rect.y() - topPaneHeight,
                rect.width() + borderThickness,
                topPaneHeight
        ));
        g2.drawRect(rect.x(), rect.y(), rect.width(), rect.height());
        drawText(g2, emotion, rect, topPaneHeight);
        g2.dispose();
    }

    private static void drawText(Graphics2D g2, Emotion emotion, Rect rect, int emNameBackHeight) {
        log.debug("Prepare text with emotion to draw...");

        String emotionName = emotion.getValue();
        final int emotionLength = emotionName.length();
        final int halfBackHeight = emNameBackHeight / 2;
        final int textCenterAlignment = (int) (halfBackHeight * emotionLength * FONT_WIDTH_COEFFICIENT);
        final int textWidth = textCenterAlignment * 2;

        final int fullBackWidth = rect.width() - EDGE_INDENT * 2;
        if(fullBackWidth < 0) return;
        final int oneSymbolWidth = textWidth / emotionLength;
        final int fittingSymbolsCount = fullBackWidth / oneSymbolWidth;
        final int extraSymbolsCount = emotionLength - fittingSymbolsCount;
        final int extraNameWidth = Math.max(extraSymbolsCount * oneSymbolWidth, 0);

        log.debug("Prepared parameters: \n" +
                        "rect.width = {}\n" +
                        "rect.height = {}\n" +
                        "emotionName = {}\n" +
                        "emotionLength = {}\n" +
                        "halfBackHeight = {}\n" +
                        "textCenterAlignment = {}\n" +
                        "textWidth = {}\n" +
                        "fullBackWidth = {}\n" +
                        "oneSymbolWidth = {}\n" +
                        "fittingSymbolsCount = {}\n" +
                        "extraSymbolsCount = {}\n" +
                        "extraNameWidth = {}",
                rect.width(),  rect.height(),
                emotionName, emotionLength, halfBackHeight, textCenterAlignment, textWidth,
                fullBackWidth, oneSymbolWidth, fittingSymbolsCount, extraSymbolsCount, extraNameWidth
        );

        if (extraSymbolsCount > 0 && emotionLength > 1) {
            emotionName = emotionName.substring(0, emotionLength - extraSymbolsCount);
        }

        log.debug("Draw text with emotion...");
        g2.setColor(emotion.getTextColor());
        g2.setFont(new Font(FONT_NAME, FONT_STYLE, halfBackHeight));
        g2.drawString(
                emotionName,
                rect.x() - textCenterAlignment + extraNameWidth / 2 + fullBackWidth / 2 + EDGE_INDENT,
                rect.y() - (int) (emNameBackHeight * FONT_HEIGHT_COEFFICIENT)
        );
    }

}
