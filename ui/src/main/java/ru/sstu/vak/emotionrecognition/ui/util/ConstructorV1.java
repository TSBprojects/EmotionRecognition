package ru.sstu.vak.emotionrecognition.ui.util;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import static javafx.scene.text.FontWeight.BOLD;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.signs.BracketSign;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.ActionableNode;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.ComboBoxAdapter;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.ToggleButtonAdapter;

public final class ConstructorV1 {

    private ConstructorV1() {
        throw new AssertionError();
    }

    public static HBox buildHBoxForModel() {
        HBox hBox = new HBox();
        VBox.setMargin(hBox, new Insets(0, 0, 20, 0));
        return hBox;
    }

    public static TextField buildTextFieldForStateName() {
        TextField textField = new TextField();
        textField.setPromptText("эмоциональное состояние");
        textField.setMaxWidth(162);
        textField.setMinWidth(162);
        FlowPane.setMargin(textField, new Insets(10, 0, 0, 0));
        return textField;
    }

    public static ActionableNode<ComboBox<String>> buildEquivalenceSignCombobox(List<String> signs) {
        return buildEquivalenceSignCombobox("", signs);
    }

    public static ActionableNode<ComboBox<String>> buildEquivalenceSignCombobox(String value, List<String> signs) {
        ComboBox<String> signBox = new ComboBox<>(FXCollections.observableArrayList(signs));
        signBox.setValue(value);
        signBox.setMaxWidth(63);
        signBox.setMinWidth(63);
        FlowPane.setMargin(signBox, new Insets(10, 10, 0, 0));
        return new ComboBoxAdapter(signBox);
    }

    public static ActionableNode<ComboBox<String>> buildFeatureCombobox(List<String> features) {
        return buildFeatureCombobox("", features);
    }

    public static ActionableNode<ComboBox<String>> buildFeatureCombobox(String value, List<String> features) {
        ObservableList<String> observableFeatures = FXCollections.observableArrayList(features);
        ComboBox<String> featureBox = new ComboBox<>(observableFeatures);
        featureBox.setValue(value);
        featureBox.setEditable(false);
        featureBox.setMaxWidth(150);
        featureBox.setMinWidth(150);
        FlowPane.setMargin(featureBox, new Insets(10, 10, 0, 0));
        return new ComboBoxAdapter(featureBox);
    }

    public static Label buildArrowLabel() {
        Label arrow = new Label();
        arrow.setText("→");
        arrow.setFont(Font.font("System", BOLD, 20));
        FlowPane.setMargin(arrow, new Insets(6, 10, 0, 0));
        return arrow;
    }

    public static Button buildRemoveFeatureButton() {
        return buildRemoveFeatureButton(null);
    }

    public static Button buildRemoveFeatureButton(EventHandler<ActionEvent> handler) {
        Button removeFeature = new Button("-");
        removeFeature.getStyleClass().add("remove-btn");
        removeFeature.setOnAction(handler);
        removeFeature.setMaxWidth(25);
        removeFeature.setMinWidth(25);
        FlowPane.setMargin(removeFeature, new Insets(10, 10, 0, 0));
        return removeFeature;
    }

    public static Button buildAddFeatureButton() {
        return buildAddFeatureButton(null);
    }

    public static Button buildAddFeatureButton(EventHandler<ActionEvent> handler) {
        Button addFeature = new Button("+");
        addFeature.setOnAction(handler);
        addFeature.setMaxWidth(25);
        addFeature.setMaxHeight(25);
        FlowPane.setMargin(addFeature, new Insets(10, 10, 0, 0));
        return addFeature;
    }

    public static ActionableNode<ToggleButton> buildBracketButton(BracketSign side) {
        return buildBracketButton(false, side);
    }

    public static ActionableNode<ToggleButton> buildBracketButton(boolean selected, BracketSign side) {
        ToggleButton bracket = new ToggleButton(side.getName());
        bracket.setSelected(selected);
        calculateBracketView(bracket);
        bracket.setOnAction(event -> calculateBracketView(bracket));
        return new ToggleButtonAdapter(bracket);
    }

    private static void calculateBracketView(ToggleButton bracket) {
        if (bracket.isSelected()) {
            bracket.setMaxWidth(25);
            bracket.setMaxHeight(25);
            bracket.setFont(new Font(12));
            FlowPane.setMargin(bracket, new Insets(10, 10, 0, 0));
        } else {
            bracket.setMaxWidth(15);
            bracket.setMaxHeight(15);
            bracket.setFont(new Font(7));
            FlowPane.setMargin(bracket, new Insets(0, 10, 0, 0));
        }
    }
}
