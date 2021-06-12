package ru.sstu.vak.emotionrecognition.ui.gui;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.var;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.endpoint.Endpoint;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.models.AnalyzableModel;
import ru.sstu.vak.emotionrecognition.ui.gui.constructor.model.ModelContext;

public class EndpointsTreeViewController {

    @FXML
    private TreeView<String> endpointsTreeView;

    public void setEndpointsAndModels(AutoIncrementMap<Endpoint> endpoints, ModelContext modelContext) {

        List<EndpointView> endpointViews = new ArrayList<>();

        for (var entry : endpoints.entrySet()) {
            var endpointId = entry.getKey();
            var endpoint = entry.getValue();
            List<AnalyzableModel> models = new ArrayList<>();
            endpointViews.add(new EndpointView(endpoint, models));
            for (var model : modelContext.getModels().values()) {
                if (model.getEndpoints().containsKey(endpointId)) {
                    models.add(model);
                }
            }
        }

        initTreeView(endpointViews);
    }

    private void initTreeView(List<EndpointView> endpointViews) {

        TreeItem<String> rootItem = new TreeItem<>("Слушатели", createImageView("listener.png"));
        rootItem.setExpanded(true);

        for (var view : endpointViews) {
            Endpoint endpoint = view.getEndpoint();
            TreeItem<String> endpointItem = new TreeItem<>(endpoint.getName(), createImageView("sensor-ico.png"));
            for (var model : view.getModels()) {
                TreeItem<String> modelItem = new TreeItem<>(model.getName(), createImageView("screws-ico.png"));
                endpointItem.getChildren().add(modelItem);
            }
            rootItem.getChildren().add(endpointItem);
        }

        endpointsTreeView.setRoot(rootItem);
    }

    private ImageView createImageView(String imageName) {
        ImageView imageView = new ImageView(new Image("image/" + imageName));
        imageView.setFitHeight(25);
        imageView.setFitWidth(25);
        return imageView;
    }

    @Getter
    @RequiredArgsConstructor
    private static class EndpointView {
        private final Endpoint endpoint;
        private final List<AnalyzableModel> models;
    }
}
