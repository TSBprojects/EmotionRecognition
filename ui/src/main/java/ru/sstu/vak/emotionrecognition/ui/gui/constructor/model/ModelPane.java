package ru.sstu.vak.emotionrecognition.ui.gui.constructor.model;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public interface ModelPane {

    SplitPane value();

    TextField getState();

    CheckBox getStringency();

    Button getRemoveBtn();

    FlowPane getFeatureHolder();

    FlowPane getEndpointHolder();
}
