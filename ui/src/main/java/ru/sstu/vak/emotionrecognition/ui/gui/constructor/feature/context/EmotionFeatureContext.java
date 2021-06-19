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
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.EmotionFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.SimpleAnalyzableModel;
import static ru.sstu.vak.emotionrecognition.ui.Main.TITLE_IMAGE_PATH;
import ru.sstu.vak.emotionrecognition.ui.gui.FeatureSettingsController;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.FeatureAction;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.ModelContext;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.ModelPane;

public class EmotionFeatureContext extends FeatureContext<EmotionFeature> {

    public EmotionFeatureContext(EmotionFeature feature, ModelContext modelContext) {
        super(feature, modelContext);
    }

    @Override
    public int putFeature(int modelId) {
        return getModelContext().getModel(modelId).getFeatures().put(getFeature().copy());
    }

    @Override
    public String createSerialNumber(int id) {
        return Integer.toString(id);
    }

    @Override
    public FeatureAction getRemoveHandler(int modelId, int featureId) {
        return (ignore1, holder, feature) -> event -> {
            holder.getChildren().remove(feature.value());
            getModelContext().getModel(modelId).getFeatures().remove(featureId);
        };
    }

    @Override
    public FeatureAction getLibraryFeatureSettingHandler(Label nameLabel) {
        return (ignore1, ignore2, ignore3) -> getFeatureSettingHandler(getFeature(), nameLabel);
    }

    @Override
    public FeatureAction getModelFeatureSettingHandler(int modelId, int featureId) {
        return (ignore1, holder, feature) ->
            getFeatureSettingHandler(
                getModelContext().getModel(modelId).getFeatures().get(featureId),
                feature.getName()
            );
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

            FeatureSettingsController featureSettings = loader.getController();
            featureSettings.setFeature(feature);

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
    public void createAndPutModel(String stateName, ModelPane modelPane) {
        AnalyzableModel model = new SimpleAnalyzableModel(
            stateName,
            true,
            Collections.singletonMap(0, getFeature().copy()),
            Collections.emptyMap(),
            Collections.emptyMap()
        );
        getModelContext().put(model, modelPane);
    }
}
