package ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context;

import java.io.IOException;
import java.util.Collections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.EmotionFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.SimpleAnalyzableModel;
import static ru.sstu.vak.emotionrecognition.ui.Main.TITLE_IMAGE_PATH;
import ru.sstu.vak.emotionrecognition.ui.gui.FeatureSettingsController;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.FeatureAction;

public class EmotionFeatureContext extends FeatureContext<EmotionFeature> {

    public EmotionFeatureContext(EmotionFeature feature, AutoIncrementMap<AnalyzableModel> models) {
        super(feature, models);
    }

    @Override
    public int putFeature(int modelId) {
        return getModels().get(modelId).getFeatures().put(getFeature().copy());
    }

    @Override
    public String createSerialNumber(int id) {
        return Integer.toString(id);
    }

    @Override
    public FeatureAction getRemoveHandler(int modelId, int featureId) {
        return (ignore1, holder, feature) -> event -> {
            holder.getChildren().remove(feature.value());
            getModels().get(modelId).getFeatures().remove(featureId);
        };
    }

    @Override
    public FeatureAction getLibraryFeatureSettingHandler(Label nameLabel) {
        return (ignore1, ignore2, ignore3) -> getFeatureSettingHandler(getFeature(), nameLabel);
    }

    @Override
    public FeatureAction getModelFeatureSettingHandler(int modelId, int featureId) {
        return (ignore1, holder, feature) ->
            getFeatureSettingHandler(getModels().get(modelId).getFeatures().get(featureId), feature.getName());
    }

    private EventHandler<ActionEvent> getFeatureSettingHandler(EmotionFeature feature, Label nameLabel) {
        return event -> {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/featureSettings.fxml"));

            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FeatureSettingsController progressController = loader.getController();
            progressController.setFeature(feature);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(feature.getName() + " - конфигурация");
            stage.setScene(new Scene(root, 423, 317));
            stage.setOnCloseRequest(e -> {
                nameLabel.setText(feature.getName());
                nameLabel.getTooltip().setText(feature.getName());
            });
            stage.getIcons().add(new Image(TITLE_IMAGE_PATH));
            stage.showAndWait();
        };
    }

    @Override
    public void createAndPutModel(String stateName) {
        getModels().put(new SimpleAnalyzableModel(
            stateName,
            true,
            Collections.singletonMap(0, getFeature().copy()),
            Collections.emptyMap()
        ));
    }
}
