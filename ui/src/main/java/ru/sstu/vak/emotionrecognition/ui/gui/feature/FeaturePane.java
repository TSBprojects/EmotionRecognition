package ru.sstu.vak.emotionrecognition.ui.gui.feature;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public interface FeaturePane {

    AnchorPane value();

    Label getSerialNumberLabel();

    Label getWarnMsgLabel();

    Label getNameLabel();

    Button getSettingsBtn();

    Button getRemoveBtn();
}
