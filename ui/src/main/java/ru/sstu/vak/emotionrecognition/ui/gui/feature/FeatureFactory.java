package ru.sstu.vak.emotionrecognition.ui.gui.feature;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import static ru.sstu.vak.emotionrecognition.ui.gui.MainController.FEATURE_ID_SUFFIX;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildFeatureNameLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildFeatureSettingsButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyFeatureAnchorPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyFeatureSerialNumberLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyFeatureWarnLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildRemoveModelButton;

public final class FeatureFactory {

    private FeatureFactory() {
        throw new AssertionError();
    }

    public static AnchorPane createFeature(FeatureConfiguration config) {

        String serialNumber = config.getSerialNumber();

        Label labelName = buildFeatureNameLabel(config.getLabel());

        Button configureFeatureButton = buildFeatureSettingsButton();

        Button removeModelFeatureButton = buildRemoveModelButton();

        Label serialNumberLabel = buildModelBodyFeatureSerialNumberLabel(serialNumber);

        Label warnLabel = buildModelBodyFeatureWarnLabel();

        AnchorPane feature = buildModelBodyFeatureAnchorPane(
            serialNumberLabel,
            warnLabel,
            labelName,
            configureFeatureButton,
            removeModelFeatureButton
        );

        feature.setId(config.getModelId() + "_" + serialNumber + FEATURE_ID_SUFFIX);

        FeaturePane featureWrapper = SimpleFeaturePane.builder()
            .name(labelName)
            .serialNumber(serialNumberLabel)
            .warnMsg(warnLabel)
            .remove(removeModelFeatureButton)
            .settings(configureFeatureButton)
            .pane(feature)
            .build();

        configureFeatureButton.setOnAction(config.getSettingHandler().apply(
            configureFeatureButton,
            config.getFeatureHolder(),
            featureWrapper
        ));

        removeModelFeatureButton.setOnAction(config.getRemoveHandler().apply(
            removeModelFeatureButton,
            config.getFeatureHolder(),
            featureWrapper
        ));

        return feature;
    }
}
