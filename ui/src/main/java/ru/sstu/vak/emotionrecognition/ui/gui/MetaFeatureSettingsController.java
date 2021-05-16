package ru.sstu.vak.emotionrecognition.ui.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import lombok.var;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.EmotionFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.MetaFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.signs.BracketSign;
import static ru.sstu.vak.emotionrecognition.timeseries.analyze.signs.BracketSign.LEFT;
import static ru.sstu.vak.emotionrecognition.timeseries.analyze.signs.BracketSign.RIGHT;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.signs.EquivalenceSign;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.signs.LogicalSign;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.ActionableNode;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV1.buildAddFeatureButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV1.buildBracketButton;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV1.buildEquivalenceSignCombobox;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV1.buildFeatureCombobox;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV1.buildRemoveFeatureButton;

public class MetaFeatureSettingsController {

    @FXML
    private VBox settingsVBox;

    @FXML
    private TextArea featureDescriptionTextArea;

    @FXML
    private FlowPane ruleHolderFlowPane;

    private final List<String> eqSigns = Arrays.stream(EquivalenceSign.values())
        .map(EquivalenceSign::getName)
        .collect(Collectors.toList());

    private final List<String> logicalSigns = Arrays.stream(LogicalSign.values())
        .map(LogicalSign::getName)
        .collect(Collectors.toList());

    private final List<String> bracketSigns = Arrays.stream(BracketSign.values())
        .map(BracketSign::getName)
        .collect(Collectors.toList());

    private boolean signsFlag = true;

    private MetaFeature metaFeature;

    private List<String> nameFeatures;

    public void setData(MetaFeature metaFeature, AutoIncrementMap<EmotionFeature> features) {
        this.metaFeature = metaFeature;
        this.nameFeatures = features.entrySet().stream()
            .map(e -> e.getKey() + ". " + e.getValue().getName())
            .collect(Collectors.toList());

        List<String> ignoreSymbols = new ArrayList<>(eqSigns);
        ignoreSymbols.addAll(logicalSigns);
        ignoreSymbols.addAll(bracketSigns);

        var ruleCopy = new TreeMap<>(metaFeature.getRule());
        ruleCopy.forEach((key, value) -> {
            if (!nameFeatures.contains(value) && !ignoreSymbols.contains(value)) {
                metaFeature.getRule().remove(key);
            }
        });

        this.featureDescriptionTextArea.setText(metaFeature.getDescription());
        addConstructorModelLine();
    }

    private void addConstructorModelLine() {
        var childrenHolder = ruleHolderFlowPane.getChildren();

        Button addBtn = buildAddFeatureButton();
        Button removeBtn = buildRemoveFeatureButton();

        addNodeAndSetActionHandler(0, buildBracketButton(getBool(0), LEFT));
        addNodeAndSetActionHandler(1, buildFeatureCombobox(getString(1), nameFeatures));
        addNodeAndSetActionHandler(2, buildEquivalenceSignCombobox(getString(2), eqSigns));
        addNodeAndSetActionHandler(3, buildBracketButton(getBool(3), LEFT));
        addNodeAndSetActionHandler(4, buildFeatureCombobox(getString(4), nameFeatures));
        addNodeAndSetActionHandler(5, buildBracketButton(getBool(5), RIGHT));

        childrenHolder.add(addBtn);
        childrenHolder.add(removeBtn);

        var rule = metaFeature.getRule();
        while (!rule.isEmpty() && childrenHolder.size() < rule.lastKey()) {
            addNextFeatureInputs();
        }

        addBtn.setOnAction(event -> addNextFeatureInputs());

        removeBtn.setOnAction(event -> {
            if (childrenHolder.size() > 10) {
                int removeIndex = childrenHolder.size() - 3;
                for (int i = 0; i < 2; i++) {
                    childrenHolder.remove(removeIndex);
                    remove(removeIndex--);
                    childrenHolder.remove(removeIndex);
                    remove(removeIndex--);
                    childrenHolder.remove(removeIndex);
                    remove(removeIndex--);
                    childrenHolder.remove(removeIndex);
                    remove(removeIndex--);
                }
            }
        });
    }

    private void addNextFeatureInputs() {
        var childrenHolder = ruleHolderFlowPane.getChildren();
        int insertIndex = childrenHolder.size() - 2;
        for (int i = 0; i < 2; i++) {
            addNodeAndSetActionHandler(insertIndex, buildEquivalenceSignCombobox(getString(insertIndex++), getSigns()));
            addNodeAndSetActionHandler(insertIndex, buildBracketButton(getBool(insertIndex++), LEFT));
            addNodeAndSetActionHandler(insertIndex, buildFeatureCombobox(getString(insertIndex++), nameFeatures));
            addNodeAndSetActionHandler(insertIndex, buildBracketButton(getBool(insertIndex++), RIGHT));
        }
    }

    private List<String> getSigns() {
        if (signsFlag) {
            signsFlag = false;
            return logicalSigns;
        }
        signsFlag = true;
        return eqSigns;
    }

    private boolean getBool(int index) {
        return isValueExist(index);
    }

    private String getString(int index) {
        var rule = metaFeature.getRule();
        if (isValueExist(index)) {
            return rule.get(index);
        }
        return "";
    }

    private void remove(int index) {
        metaFeature.getRule().remove(index);
    }

    private boolean isValueExist(int index) {
        return metaFeature.getRule().get(index) != null;
    }

    private void addNodeAndSetActionHandler(int index, ActionableNode<?> node) {
        ruleHolderFlowPane.getChildren().add(index, node.getNode());
        node.addOnAction(event -> {
            String value = node.getValue();
            if (bracketSigns.contains(value)) {
                ToggleButton b = (ToggleButton) node.getNode();
                if (!b.isSelected()) {
                    metaFeature.getRule().remove(index);
                    return;
                }
            }
            metaFeature.getRule().put(index, value);
        });
    }
}
