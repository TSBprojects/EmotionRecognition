package ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.context;

import java.io.IOException;
import java.util.Collections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.var;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.MetaFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.SimpleAnalyzableModel;
import static ru.sstu.vak.emotionrecognition.ui.Main.TITLE_IMAGE_PATH;
import ru.sstu.vak.emotionrecognition.ui.gui.MainController;
import ru.sstu.vak.emotionrecognition.ui.gui.MetaFeatureSettingsController;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature.FeatureAction;

public class MetaFeatureContext extends FeatureContext<MetaFeature> {

    public MetaFeatureContext(MetaFeature feature, AutoIncrementMap<AnalyzableModel> models) {
        super(feature, models);
    }

    @Override
    public int putFeature(int modelId) {
        return getModels().get(modelId).getMetaFeatures().put(getFeature().copy());
    }

    @Override
    public String createSerialNumber(int id) {
        return id + MainController.META_FEATURE_ID_SUFFIX;
    }

    @Override
    public FeatureAction getRemoveHandler(int modelId, int featureId) {
        return (button, holder, feature) -> event -> {
            holder.getChildren().remove(feature.value());
            getModels().get(modelId).getMetaFeatures().remove(featureId);
        };
    }

    @Override
    public FeatureAction getLibraryFeatureSettingHandler(Label nameLabel) {
        return (button, ignore2, ignore3) -> {
            button.setVisible(false);
            button.setDisable(true);
            return e -> {
            };
        };
    }

    @Override
    public FeatureAction getModelFeatureSettingHandler(int modelId, int featureId) {
        return (ignore1, ignore2, ignore3) -> event -> {

            var model = getModels().get(modelId);

            MetaFeature feature = model.getMetaFeatures().get(featureId);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/metaFeatureSettings.fxml"));

            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            MetaFeatureSettingsController progressController = loader.getController();
            progressController.setData(feature, model.getFeatures());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(feature.getName() + " - конфигурация");
            stage.setScene(new Scene(root, 629, 496));
            stage.getIcons().add(new Image(TITLE_IMAGE_PATH));
            stage.showAndWait();
        };
    }

    @Override
    public void createAndPutModel(String stateName) {
        getModels().put(new SimpleAnalyzableModel(
            stateName,
            true,
            Collections.emptyMap(),
            Collections.singletonMap(0, getFeature().copy())
        ));
    }
}
