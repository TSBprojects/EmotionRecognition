package ru.sstu.vak.emotionrecognition.ui.gui.constructor.model;

import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import lombok.Builder;

@Builder
public class SimpleModelPane implements ModelPane {
    private final SplitPane value;
    private final TextField state;
    private final RadioButton stringency;
    private final Button remove;
    private final FlowPane featureHolder;

    @Override
    public SplitPane value() {
        return value;
    }

    @Override
    public TextField getState() {
        return state;
    }

    @Override
    public RadioButton getStringency() {
        return stringency;
    }

    @Override
    public Button getRemoveBtn() {
        return remove;
    }

    @Override
    public FlowPane getFeatureHolder() {
        return featureHolder;
    }
}
