package ru.sstu.vak.emotionrecognition.ui.gui;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ShowImageController {
    @FXML
    private ImageView imageView;

    @FXML
    public void initialize() {
        // FXML initializer
    }

    public void setImage(Image image) {
        imageView.setImage(image);
    }

}
