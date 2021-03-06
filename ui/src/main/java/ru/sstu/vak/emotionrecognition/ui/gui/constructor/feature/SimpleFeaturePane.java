package ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import lombok.Builder;

@Builder
public class SimpleFeaturePane implements FeaturePane {
    private final AnchorPane value;
    private final Label serialNumber;
    private final Label warnMsg;
    private final Label name;
    private final Button settings;
    private final Button remove;

    @Override
    public AnchorPane value() {
        return value;
    }

    @Override
    public Label getSerialNumber() {
        return serialNumber;
    }

    @Override
    public Label getWarnMsg() {
        return warnMsg;
    }

    @Override
    public Label getName() {
        return name;
    }

    @Override
    public Button getSettingsBtn() {
        return settings;
    }

    @Override
    public Button getRemoveBtn() {
        return remove;
    }
}
