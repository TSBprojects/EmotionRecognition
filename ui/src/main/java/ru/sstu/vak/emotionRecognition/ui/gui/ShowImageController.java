package ru.sstu.vak.emotionRecognition.ui.gui;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;

public class ShowImageController {
    @FXML
    private ImageView imageView;

    @FXML
    public void initialize() {
    }

    public void setImage(Image image) {
        imageView.setImage(image);
    }

}
