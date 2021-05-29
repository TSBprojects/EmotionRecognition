package ru.sstu.vak.emotionrecognition.ui.util;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.geometry.Pos.CENTER;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import static javafx.scene.paint.Color.RED;
import javafx.scene.text.Font;
import static ru.sstu.vak.emotionrecognition.ui.util.NodeDecorator.tile;

public final class ConstructorV2 {

    private ConstructorV2() {
        throw new AssertionError();
    }

    public static SplitPane buildModelSplitPane(AnchorPane header, ScrollPane body) {
        SplitPane model = new SplitPane();

        model.setMaxHeight(236);
        model.setMinHeight(236);
        model.setOrientation(VERTICAL);
        VBox.setMargin(model, new Insets(20, 20, 20, 20));

        model.getItems().addAll(header, body);

        model.setDividerPosition(0, 0.1923);
        model.setDividerPosition(1, 0.7991);

        tile(model);

        return model;
    }

    public static Label buildModelBodyPlaceHolderLabel() {
        Label label = new Label("Перетащите фактор");
        label.setOpacity(0.34);
        label.setLayoutX(88);
        label.setLayoutY(8);
        return label;
    }

    public static AnchorPane buildModelBodyPlaceHolderInner(Label text) {
        AnchorPane placeHolder = new AnchorPane();
        placeHolder.setStyle("-fx-border-style: dashed;");
        AnchorPane.setBottomAnchor(placeHolder, 5D);
        AnchorPane.setLeftAnchor(placeHolder, 5D);
        AnchorPane.setTopAnchor(placeHolder, 5D);
        AnchorPane.setRightAnchor(placeHolder, 5D);
        placeHolder.getChildren().add(text);
        return placeHolder;
    }

    public static AnchorPane buildModelBodyPlaceHolderOuter(AnchorPane inner) {
        AnchorPane placeHolder = new AnchorPane();
        placeHolder.setStyle("-fx-border-style: dashed;");
        placeHolder.setMaxWidth(305);
        placeHolder.setMinWidth(305);
        placeHolder.setMaxHeight(45);
        placeHolder.setMinHeight(45);
        placeHolder.getChildren().add(inner);
        FlowPane.setMargin(placeHolder, new Insets(0, 0, 15, 15));
        return placeHolder;
    }

    public static AnchorPane buildModelBodyFeatureAnchorPane(
        Label serialNumber,
        Label warnMsg,
        Label name,
        Button settings,
        Button remove
    ) {
        AnchorPane feature = buildClearFeatureAnchorPane();
        feature.setMaxWidth(305);
        feature.setMinWidth(305);

        HBox hBox = (HBox) feature.getChildren().get(0);
        HBox.setMargin(name, new Insets(0, 0, 0, 10));
        HBox.setMargin(settings, new Insets(0, 0, 0, 10));
        HBox.setMargin(remove, new Insets(0, 10, 0, 10));
        hBox.getChildren().addAll(name, settings, remove);

        feature.getChildren().addAll(serialNumber, warnMsg);

        return feature;
    }

    public static Label buildModelBodyFeatureSerialNumberLabel(int id) {
        return buildModelBodyFeatureSerialNumberLabel(Integer.toString(id));
    }

    public static Label buildModelBodyFeatureSerialNumberLabel(String id) {
        Label serialNumber = new Label(id);
        serialNumber.setFont(new Font(8));
        serialNumber.setPadding(new Insets(0, 0, 0, 2));
        AnchorPane.setLeftAnchor(serialNumber, 0D);
        AnchorPane.setTopAnchor(serialNumber, 0D);
        return serialNumber;
    }

    public static Label buildModelBodyFeatureWarnLabel() {
        Label warn = new Label("Некорректен!");
        warn.setId("warn");
        warn.setVisible(false);
        warn.setFont(new Font(9.5));
        warn.setPadding(new Insets(0, 0, 0, 2));
        warn.setTextFill(RED);
        AnchorPane.setLeftAnchor(warn, 0D);
        AnchorPane.setBottomAnchor(warn, 0D);
        return warn;
    }

    public static FlowPane buildModelBodyFlowPane(AnchorPane... features) {
        FlowPane body = new FlowPane();
        body.setPadding(new Insets(15, 15, 0, 0));
        body.getChildren().addAll(features);
        return body;
    }

    public static ScrollPane buildModelScrollPane(Node content) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(content);
        return scrollPane;
    }

    public static AnchorPane buildModelHeaderAnchorPane(TextField state, RadioButton stringency, Button remove) {
        AnchorPane header = new AnchorPane();

        AnchorPane.setLeftAnchor(state, 10D);
        AnchorPane.setTopAnchor(state, 10D);

        AnchorPane.setLeftAnchor(stringency, 193D);
        AnchorPane.setTopAnchor(stringency, 14D);

        AnchorPane.setTopAnchor(remove, 10D);
        AnchorPane.setRightAnchor(remove, 10D);

        header.getChildren().addAll(state, stringency, remove);

        return header;
    }

    public static Button buildRemoveModelButton() {
        Button remove = new Button("Удалить");
        remove.setMaxWidth(62);
        remove.setMinWidth(62);
        remove.setMaxHeight(25);
        remove.setMinHeight(25);
        return remove;
    }

    public static RadioButton buildStringencyRadioButton(boolean value) {
        RadioButton stringency = new RadioButton();
        stringency.setText("строго");
        stringency.setSelected(value);
        stringency.setTooltip(new Tooltip("Строго - все факторы должны быть истины, иначе достаточно одного"));
        return stringency;
    }

    public static TextField buildStateNameTextField(String stateName) {
        TextField name = new TextField();
        name.setPromptText("Эмоциональное состояние");
        name.setText(stateName);
        name.setMaxWidth(164);
        name.setMinWidth(164);
        name.setMaxHeight(25);
        name.setMinHeight(25);
        return name;
    }

    public static AnchorPane buildSelectFeatureAnchorPane(Label name) {
        AnchorPane feature = buildClearFeatureAnchorPane();
        feature.setMaxWidth(232);
        feature.setMinWidth(232);

        HBox hBox = (HBox) feature.getChildren().get(0);
        HBox.setMargin(name, new Insets(0, 0, 0, 10));
        hBox.getChildren().addAll(name);

        return feature;
    }

    public static AnchorPane buildSelectFeatureAnchorPane(Label name, Button settings) {
        return buildSelectFeatureAnchorPane(buildSelectFeatureAnchorPane(name), settings);
    }

    public static AnchorPane buildSelectFeatureAnchorPane(AnchorPane feature, Button settings) {
        HBox hBox = (HBox) feature.getChildren().get(0);
        HBox.setMargin(settings, new Insets(0, 10, 0, 10));
        hBox.getChildren().addAll(settings);
        return feature;
    }

    public static Label buildFeatureNameLabel(String name) {
        Label nameLabel = new Label(name);
        nameLabel.setMaxWidth(112);
        nameLabel.setMinWidth(112);
        return nameLabel;
    }

    public static Button buildFeatureSettingsButton() {
        Button settings = new Button("настройки");
        settings.setMaxWidth(82);
        settings.setMinWidth(82);
        settings.setMaxHeight(25);
        settings.setMinHeight(25);
        return settings;
    }


    public static AnchorPane buildClearFeatureAnchorPane() {
        AnchorPane feature = new AnchorPane();

        feature.setMaxHeight(45);
        feature.setMinHeight(45);
        FlowPane.setMargin(feature, new Insets(0, 0, 15, 15));

        HBox hBox = new HBox();
        hBox.setAlignment(CENTER);
        AnchorPane.setBottomAnchor(hBox, 0D);
        AnchorPane.setLeftAnchor(hBox, 0D);
        AnchorPane.setTopAnchor(hBox, 0D);
        AnchorPane.setRightAnchor(hBox, 0D);

        feature.getChildren().add(hBox);

        tile(feature);

        return feature;
    }

    public static class Settings {
        private Settings() {
            throw new AssertionError();
        }

        public static HBox buildPropertyHBox() {
            HBox property = new HBox();
            property.setPadding(new Insets(20, 20, 0, 0));
            return property;
        }

        public static Label buildPropertyNameLabel(String name) {
            Label propName = new Label(name);
            propName.setPrefWidth(1000);
            propName.setMaxWidth(1000);
            propName.setMinWidth(150);
            propName.setMaxHeight(25);
            propName.setMinHeight(25);
            propName.setFont(new Font(14));
            HBox.setMargin(propName, new Insets(0, 0, 0, 20));
            return propName;
        }

        public static TextField buildPropertyValueTextField(String value) {
            TextField input = new TextField();
            input.setText(value);
            input.setPrefWidth(1000);
            input.setMaxWidth(1000);
            input.setMinWidth(150);
            input.setMaxHeight(25);
            input.setMinHeight(25);
            HBox.setMargin(input, new Insets(0, 0, 0, 15));
            return input;
        }

        public static ComboBox<String> buildPropertyValueComboBox(List<String> options) {
            ComboBox<String> input = new ComboBox<>(FXCollections.observableArrayList(options));
            input.setPrefWidth(1000);
            input.setMaxWidth(1000);
            input.setMinWidth(51);
            input.setMaxHeight(25);
            input.setMinHeight(25);
            HBox.setMargin(input, new Insets(0, 0, 0, 15));
            return input;
        }
    }
}
