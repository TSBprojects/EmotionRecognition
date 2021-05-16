package ru.sstu.vak.emotionrecognition.ui.util;

import javafx.scene.Node;
import javafx.scene.paint.Color;

public final class NodeDecorator {

    private NodeDecorator() {
        throw new AssertionError();
    }

    public static void tile(Node node) {
        node.setStyle(
            "-fx-background-color: white;"
                + "-fx-background-radius: 5px;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
        );
    }

    public static void shadow(Node node, Color color) {
        node.setStyle(
            "-fx-effect: dropshadow(three-pass-box, rgba("
                + color.getRed() * 255
                + ","
                + color.getGreen() * 255
                + ","
                + color.getBlue() * 255
                + ", 0.8"
                + "), 10, 0, 0, 0);"
        );
    }

    public static void clearStyle(Node node) {
        node.setStyle("");
    }
}
