package ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public interface FeaturePane {

    AnchorPane value();

    Label getSerialNumber();

    Label getWarnMsg();

    Label getName();

    Button getSettingsBtn();

    Button getRemoveBtn();
}
