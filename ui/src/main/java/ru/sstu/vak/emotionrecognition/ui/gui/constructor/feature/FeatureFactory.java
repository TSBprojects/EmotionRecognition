package ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import static ru.sstu.vak.emotionrecognition.ui.gui.MainController.FEATURE_ID_SUFFIX;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context.FeatureContext;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildFeatureNameLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyFeatureAnchorPane;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyFeatureSerialNumberLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildModelBodyFeatureWarnLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildRemoveButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.buildSettingsButton;

public final class FeatureFactory {

    private FeatureFactory() {
        throw new AssertionError();
    }

    public static FeaturePane createFeature(FeatureConfig config) {

        int modelId = config.getModelId();

        int featureNumberInModel = config.getFeatureNumberInModel();

        FeatureContext<?> featureContext = config.getFeatureContext();

        String serialNumber = featureContext.createSerialNumber(featureNumberInModel);

        Label labelName = buildFeatureNameLabel(config.getFeatureName());

        Button configureFeatureButton = buildSettingsButton();

        Button removeModelFeatureButton = buildRemoveButton();

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
            .value(feature)
            .build();

        FeatureAction settingsHandler = featureContext.getModelFeatureSettingHandler(modelId, featureNumberInModel);
        configureFeatureButton.setOnAction(settingsHandler.apply(
            configureFeatureButton,
            config.getFeatureHolder(),
            featureWrapper
        ));

        FeatureAction removeHandler = featureContext.getRemoveHandler(modelId, featureNumberInModel);
        removeModelFeatureButton.setOnAction(removeHandler.apply(
            removeModelFeatureButton,
            config.getFeatureHolder(),
            featureWrapper
        ));

        return featureWrapper;
    }
}
