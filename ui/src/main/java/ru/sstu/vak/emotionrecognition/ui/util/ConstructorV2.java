package ru.sstu.vak.emotionrecognition.ui.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.Cursor.HAND;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import static javafx.scene.layout.Priority.ALWAYS;
import javafx.scene.layout.VBox;
import static javafx.scene.paint.Color.RED;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.endpoint.Endpoint;
import static ru.sstu.vak.emotionrecognition.ui.util.NodeDecorator.tile;

public final class ConstructorV2 {

    private static final Logger log = LogManager.getLogger(ConstructorV2.class.getName());

    public static final long TOOLTIP_OPEN_DELAY_MS = 150;

    public static final long TOOLTIP_VISIBLE_DURATION_MS = 5000;

    public static final long TOOLTIP_CLOSE_DELAY_MS = 200;

    public static final String ENDPOINT_STATUS_CLASS = "endpoint-status";

    public static final String ENDPOINT_PROGRESS_STATUS_CLASS = "endpoint-progress";

    public static final String ENDPOINT_OK_STATUS_CLASS = "endpoint-ok";

    public static final String ENDPOINT_WARN_STATUS_CLASS = "endpoint-warn";

    public static final String ENDPOINT_ERROR_STATUS_CLASS = "endpoint-error";

    public static final String ENDPOINT_IDLE_STATUS_CLASS = "endpoint-idle";

    public static final String ENDPOINT_CLASS = "endpoint";

    public static final String START_LISTEN_ALL_CLASS = "start-listen-all";

    public static final String STOP_LISTEN_ALL_CLASS = "stop-listen-all";

    public static final String SETTINGS_CLASS = "settings-btn";

    public static final String REMOVE_CLASS = "remove-btn";

    private ConstructorV2() {
        throw new AssertionError();
    }

    public static SplitPane buildModelSplitPane(AnchorPane header, SplitPane body) {
        SplitPane model = new SplitPane();

        model.setMaxHeight(236);
        model.setMinHeight(236);
        model.setOrientation(VERTICAL);
        VBox.setMargin(model, new Insets(20, 20, 20, 20));

        model.getItems().addAll(header, body);
        model.setDividerPosition(0, 0.1923);
        tile(model);

        return model;
    }

    public static SplitPane buildModelBodySplitPane(ScrollPane endpoints, ScrollPane features) {
        SplitPane model = new SplitPane();
        model.setOrientation(HORIZONTAL);
        model.getItems().addAll(endpoints, features);

        model.setDividerPosition(0, 0.3692);

        return model;
    }

    public static Label buildModelBodyPlaceHolderLabel(String text) {
        Label label = new Label(text);
        label.setOpacity(0.34);
        label.setLayoutX(88);
        label.setLayoutY(8);
        return label;
    }

    public static VBox buildModelBodyPlaceHolderInner(Label text) {
        VBox placeHolder = new VBox();
        placeHolder.setStyle("-fx-border-style: dashed;");
        AnchorPane.setBottomAnchor(placeHolder, 5D);
        AnchorPane.setLeftAnchor(placeHolder, 5D);
        AnchorPane.setTopAnchor(placeHolder, 5D);
        AnchorPane.setRightAnchor(placeHolder, 5D);
        placeHolder.setAlignment(CENTER);
        placeHolder.getChildren().add(text);
        return placeHolder;
    }

    public static AnchorPane buildModelBodyFeaturePlaceHolderOuter(VBox inner) {
        AnchorPane placeHolder = new AnchorPane();
        placeHolder.setStyle("-fx-border-style: dashed;");
        placeHolder.setMaxWidth(210);
        placeHolder.setMinWidth(210);
        placeHolder.setMaxHeight(45);
        placeHolder.setMinHeight(45);
        placeHolder.getChildren().add(inner);
        FlowPane.setMargin(placeHolder, new Insets(0, 0, 15, 15));
        return placeHolder;
    }

    public static AnchorPane buildModelBodyEndpointPlaceHolderOuter(VBox inner) {
        AnchorPane placeHolder = new AnchorPane();
        placeHolder.setStyle("-fx-border-style: dashed;");
        placeHolder.setMaxWidth(180);
        placeHolder.setMinWidth(180);
        placeHolder.setMaxHeight(35);
        placeHolder.setMinHeight(35);
        placeHolder.getChildren().add(inner);
        FlowPane.setMargin(placeHolder, new Insets(0, 0, 15, 15));
        return placeHolder;
    }

    public static AnchorPane buildModelEndpointAnchorPane(
        ProgressIndicator progressIndicator,
        Node okIndicator,
        Node warnIndicator,
        Node errorIndicator,
        Node idleIndicator,
        Label name,
        Button remove
    ) {
        AnchorPane endpoint = new AnchorPane();

        endpoint.setMaxWidth(180);
        endpoint.setMinWidth(180);
        endpoint.setMaxHeight(30);
        endpoint.setMinHeight(30);
        endpoint.getStyleClass().add(ENDPOINT_CLASS);
        FlowPane.setMargin(endpoint, new Insets(0, 0, 15, 15));
        tile(endpoint);

        HBox hBox = new HBox();
        hBox.setAlignment(CENTER);
        AnchorPane.setBottomAnchor(hBox, 0D);
        AnchorPane.setLeftAnchor(hBox, 0D);
        AnchorPane.setTopAnchor(hBox, 0D);
        AnchorPane.setRightAnchor(hBox, 0D);
        hBox.setPadding(new Insets(0, 10, 0, 10));
        endpoint.getChildren().add(hBox);

        Insets rightIndent = new Insets(0, 7, 0, 0);
        Insets rightLeftIndent = new Insets(0, 10, 1, 2);

        HBox.setMargin(progressIndicator, rightIndent);
        HBox.setMargin(okIndicator, rightLeftIndent);
        HBox.setMargin(warnIndicator, rightLeftIndent);
        HBox.setMargin(errorIndicator, rightLeftIndent);
        HBox.setMargin(idleIndicator, rightLeftIndent);
        HBox.setMargin(name, rightIndent);

        name.setPrefWidth(110);
        name.setMaxWidth(110);
        name.setMinWidth(110);

        hBox.getChildren().addAll(
            progressIndicator,
            okIndicator,
            warnIndicator,
            errorIndicator,
            idleIndicator,
            name,
            remove
        );

        return endpoint;
    }

    public static AnchorPane buildEndpointAnchorPane(ToolBar header, HBox name) {
        AnchorPane endpoint = new AnchorPane();

        endpoint.setMaxWidth(200);
        endpoint.setMinWidth(200);
        endpoint.setMaxHeight(60);
        endpoint.setMinHeight(60);
        endpoint.getStyleClass().add(ENDPOINT_CLASS);
        FlowPane.setMargin(endpoint, new Insets(0, 0, 15, 15));
        tile(endpoint);

        VBox vBox = new VBox();
        vBox.setAlignment(CENTER);
        AnchorPane.setBottomAnchor(vBox, 0D);
        AnchorPane.setLeftAnchor(vBox, 0D);
        AnchorPane.setTopAnchor(vBox, 0D);
        AnchorPane.setRightAnchor(vBox, 0D);
        endpoint.getChildren().add(vBox);

        vBox.getChildren().addAll(header, name);

        return endpoint;
    }

    public static HBox buildEndpointHBoxWithLabel(Label nameLabel) {
        HBox hBox = new HBox();
        hBox.setMaxHeight(35);
        hBox.setMinHeight(35);
        hBox.setAlignment(CENTER);
        hBox.setCursor(HAND);

        nameLabel.setMaxWidth(180);
        nameLabel.setMinWidth(180);
        nameLabel.setAlignment(CENTER);
        hBox.getChildren().add(nameLabel);

        return hBox;
    }

    public static Label buildEndpointLabel(Endpoint endpoint) {
        Label label = new Label(endpoint.getName());
        label.setTooltip(newTooltipBuilder(endpoint).build());
        return label;
    }

    public static ProgressIndicator buildEndpointProgressIndicator(String id) {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setManaged(false);
        indicator.setVisible(false);
        indicator.setProgress(INDETERMINATE_PROGRESS);
        indicator.setMaxHeight(20);
        indicator.setMinHeight(20);
        indicator.setMaxWidth(20);
        indicator.setMinWidth(20);
        indicator.getStyleClass().addAll(ENDPOINT_PROGRESS_STATUS_CLASS, ENDPOINT_STATUS_CLASS);
        indicator.setId(id);
        return indicator;
    }

    public static Button buildEndpointOkButton(String id) {
        return buildEndpointStatusButton(id, ENDPOINT_OK_STATUS_CLASS);
    }

    public static Button buildEndpointWarnButton(String id) {
        return buildEndpointStatusButton(id, ENDPOINT_WARN_STATUS_CLASS);
    }

    public static Button buildEndpointErrorButton(String id) {
        return buildEndpointStatusButton(id, ENDPOINT_ERROR_STATUS_CLASS);
    }

    public static Button buildEndpointIdleButton(String id) {
        Button idle = buildEndpointStatusButton(id, ENDPOINT_IDLE_STATUS_CLASS);
        idle.setManaged(true);
        idle.setVisible(true);
        idle.setTooltip(newTooltipBuilder("В ожидании отправки запроса").build());
        return idle;
    }

    public static Button buildEndpointStatusButton(String id, String clazz) {
        Button button = new Button();
        button.setManaged(false);
        button.setVisible(false);
        button.setMaxHeight(15);
        button.setMinHeight(15);
        button.setMaxWidth(15);
        button.setMinWidth(15);
        button.getStyleClass().addAll(clazz, ENDPOINT_STATUS_CLASS);
        button.setId(id);
        return button;
    }

    public static ToolBar buildEndpointToolbar(
        Button startListenAll,
        Button stopListenAll,
        Button settings,
        Button remove
    ) {
        ToolBar toolBar = new ToolBar();
        toolBar.setMaxHeight(25);
        toolBar.setMinHeight(25);

        Pane separator = new Pane();
        HBox.setHgrow(separator, ALWAYS);

        toolBar.getItems().addAll(
            startListenAll,
            stopListenAll,
            separator,
            settings,
            remove
        );

        return toolBar;
    }

    public static Button buildStartListenAllButton() {
        Button start = new Button();
        start.getStyleClass().add(START_LISTEN_ALL_CLASS);
        start.setCursor(HAND);
        start.setTooltip(newTooltipBuilder("Слушать все конфигурации").build());
        return start;
    }

    public static Button buildStopListenAllButton() {
        Button stop = new Button();
        stop.getStyleClass().add(STOP_LISTEN_ALL_CLASS);
        stop.setCursor(HAND);
        stop.setTooltip(newTooltipBuilder("Перестать слушать все конфигурации").build());
        return stop;
    }

    public static AnchorPane buildModelBodyFeatureAnchorPane(
        Label serialNumber,
        Label warnMsg,
        Label name,
        Button settings,
        Button remove
    ) {
        AnchorPane feature = buildClearFeatureAnchorPane();
        feature.setMaxWidth(210);
        feature.setMinWidth(210);

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
        body.setPrefWrapLength(200);
        return body;
    }

    public static ScrollPane buildModelScrollPane(Node content) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(content);
        return scrollPane;
    }

    public static AnchorPane buildModelHeaderAnchorPane(TextField state, CheckBox stringency, Button remove) {
        AnchorPane header = new AnchorPane();

        AnchorPane.setLeftAnchor(state, 10D);
        AnchorPane.setTopAnchor(state, 10D);

        AnchorPane.setLeftAnchor(stringency, 193D);
        AnchorPane.setTopAnchor(stringency, 14D);

        AnchorPane.setTopAnchor(remove, 10D);
        AnchorPane.setRightAnchor(remove, 20D);

        header.getChildren().addAll(state, stringency, remove);

        return header;
    }

    public static Button buildRemoveButton() {
        Button remove = new Button();
        remove.setMaxWidth(62);
        remove.setMinWidth(62);
        remove.setMaxHeight(25);
        remove.setMinHeight(25);
        remove.getStyleClass().add(REMOVE_CLASS);
        remove.setCursor(HAND);
        return remove;
    }

    public static CheckBox buildStringencyCheckBox(boolean value) {
        String tooltipMsg = "Строго - все факторы должны быть истины, иначе достаточно одного";
        CheckBox stringency = new CheckBox();
        stringency.setText("строго");
        stringency.setSelected(value);
        stringency.setTooltip(newTooltipBuilder(tooltipMsg).build());
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
        feature.setMaxWidth(173);
        feature.setMinWidth(173);

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
        nameLabel.setTooltip(newTooltipBuilder(name).build());
        return nameLabel;
    }

    public static Button buildSettingsButton() {
        Button settings = new Button();
        settings.setMaxWidth(82);
        settings.setMinWidth(82);
        settings.setMaxHeight(25);
        settings.setMinHeight(25);
        settings.getStyleClass().add(SETTINGS_CLASS);
        settings.setCursor(HAND);
        return settings;
    }


    public static AnchorPane buildClearFeatureAnchorPane() {
        AnchorPane feature = new AnchorPane();

        feature.setMaxHeight(45);
        feature.setMinHeight(45);
        FlowPane.setMargin(feature, new Insets(0, 0, 15, 15));

        HBox hBox = new HBox();
        hBox.setCursor(HAND);
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
            propName.setTooltip(newTooltipBuilder(name).build());
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

    public static TooltipBuilder newTooltipBuilder(String text) {
        return new TooltipBuilder(text);
    }

    public static TooltipBuilder newTooltipBuilder(Endpoint endpoint) {
        return newTooltipBuilder("name: " + endpoint.getName() + "\n url: " + endpoint.getUrl());
    }

    public static class TooltipBuilder {
        private final String text;
        private long openDelay = TOOLTIP_OPEN_DELAY_MS;
        private long visibleDuration = TOOLTIP_VISIBLE_DURATION_MS;
        private long closeDelay = TOOLTIP_CLOSE_DELAY_MS;
        private boolean hideOnExit = false;

        private TooltipBuilder(String text) {
            this.text = text;
        }

        public TooltipBuilder openDelay(long openDelay) {
            this.openDelay = openDelay;
            return this;
        }

        public TooltipBuilder visibleDuration(long visibleDuration) {
            this.visibleDuration = visibleDuration;
            return this;
        }

        public TooltipBuilder closeDelay(long closeDelay) {
            this.closeDelay = closeDelay;
            return this;
        }

        public TooltipBuilder hideOnExit(boolean hideOnExit) {
            this.hideOnExit = hideOnExit;
            return this;
        }

        public Tooltip build() {
            Tooltip tooltip = new Tooltip(text);
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(250);

            try {
                Class<?> innerClass = Class.forName("javafx.scene.control.Tooltip$TooltipBehavior");
                Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                Object behavior = constructor.newInstance(
                    new Duration(openDelay),
                    new Duration(visibleDuration),
                    new Duration(closeDelay),
                    hideOnExit
                );

                Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
                fieldBehavior.setAccessible(true);
                fieldBehavior.set(tooltip, behavior);
            } catch (Exception e) {
                log.warn(
                    "Cannot set custom open/visible/close tooltip behavior. Default values will be used! Reason: {}",
                    e.getMessage()
                );
            }

            return tooltip;
        }
    }
}
