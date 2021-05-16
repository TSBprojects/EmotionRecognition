package ru.sstu.vak.emotionrecognition.ui.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import static java.util.Arrays.stream;
import java.util.Collections;
import static java.util.Collections.singletonList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import lombok.var;
import ru.sstu.vak.emotionrecognition.common.Nameable;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.EmotionFeature;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.configuration.ConfigurableProperty;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.feature.configuration.PairProperty;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.Settings.buildPropertyHBox;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.Settings.buildPropertyNameLabel;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.Settings.buildPropertyValueComboBox;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.Settings.buildPropertyValueTextField;

public class FeatureSettingsController {

    @FXML
    private VBox settingsVBox;

    @FXML
    private TextArea featureDescriptionTextArea;

    private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_MAP;

    static {
        WRAPPER_TYPE_MAP = new HashMap<>(6);
        WRAPPER_TYPE_MAP.put(int.class, Integer.class);
        WRAPPER_TYPE_MAP.put(byte.class, Byte.class);
        WRAPPER_TYPE_MAP.put(double.class, Double.class);
        WRAPPER_TYPE_MAP.put(float.class, Float.class);
        WRAPPER_TYPE_MAP.put(long.class, Long.class);
        WRAPPER_TYPE_MAP.put(short.class, Short.class);
    }

    private final ObjectMapper mapper = initMapper();

    private EmotionFeature feature;

    public void setFeature(EmotionFeature feature) {
        this.feature = feature;
        initSettings();
    }

    @SneakyThrows
    private void initSettings() {
        featureDescriptionTextArea.setText(feature.getDescription());

        var featureClass = feature.getClass();
        for (var entry : getAllFields(featureClass).entrySet()) {
            var field = entry.getKey();
            var currentOwner = entry.getValue();
            var configurable = field.getAnnotation(ConfigurableProperty.class);
            if (configurable != null && (configurable.inheritable() || currentOwner == featureClass)) {
                var pair = field.getAnnotation(PairProperty.class);

                List<Node> inputNodes = new ArrayList<>(singletonList(buildPropertyNameLabel(configurable.alias())));
                inputNodes.add(createInputNode(feature, field));
                if (pair != null) {
                    inputNodes.add(createInputNode(feature, currentOwner.getDeclaredField(pair.mate())));
                }

                HBox propertyLayout = buildPropertyHBox();
                propertyLayout.getChildren().addAll(inputNodes);
                settingsVBox.getChildren().add(propertyLayout);
            }
        }
    }

    private Node createInputNode(Object o, Field field) throws IllegalAccessException {
        field.setAccessible(true);
        var fieldType = field.getType();
        if (fieldType.isEnum()) {
            String fieldValue;
            List<String> options;
            if (Nameable.class.isAssignableFrom(fieldType)) {
                @SuppressWarnings("unchecked")
                var e = (Class<Nameable>) field.getType();

                options = stream(e.getEnumConstants())
                    .map(Nameable::getName)
                    .collect(Collectors.toList());

                var value = (Nameable) field.get(o);
                fieldValue = value != null ? value.getName() : "";
            } else {
                @SuppressWarnings("unchecked")
                var e = (Class<Enum<?>>) field.getType();

                options = stream(e.getEnumConstants())
                    .map(Enum::name)
                    .collect(Collectors.toList());

                var value = (Enum<?>) field.get(o);
                fieldValue = value != null ? value.name() : "";
            }

            ComboBox<String> node = buildPropertyValueComboBox(options);
            node.setValue(fieldValue);
            initEditAction(field.getName(), node);
            return node;
        }

        var value = field.get(o);
        TextField node = buildPropertyValueTextField(value == null ? "" : value.toString());
        initEditAction(field.getName(), fieldType, node);
        return node;
    }

    private void initEditAction(String fieldName, ComboBox<String> control) {
        control.setOnAction(event -> {
            try {
                control.setStyle("-fx-border-color:none");
                Map<String, String> map = Collections.singletonMap(fieldName, control.getValue());
                mapper.readerForUpdating(feature).readValue(mapper.writeValueAsString(map));
            } catch (Exception e) {
                control.setStyle("-fx-border-color:red");
            }
        });
    }

    private void initEditAction(String fieldName, Class<?> type, TextField control) {
        control.textProperty().addListener((observable, oldValue, newValue) -> {
            Class<?> wrappedType = WRAPPER_TYPE_MAP.get(type);
            Class<?> clarifiedType = wrappedType != null ? wrappedType : type;
            if (Number.class.isAssignableFrom(clarifiedType) && !newValue.matches("\\d*")) {
                control.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }
            try {
                control.setStyle("-fx-border-color:none");
                Map<String, String> map = Collections.singletonMap(fieldName, control.getText());
                mapper.readerForUpdating(feature).readValue(mapper.writeValueAsString(map));
            } catch (Exception e) {
                control.setStyle("-fx-border-color:red");
            }
        });
    }

    private Map<Field, Class<?>> getAllFields(Class<? extends EmotionFeature> featureClass) {
        List<Class<?>> parents = new ArrayList<>();
        for (
            Class<?> parent = featureClass.getSuperclass();
            parent != null && parent != EmotionFeature.class;
            parent = parent.getSuperclass()
        ) {
            parents.add(parent);
        }

        Collections.reverse(parents);

        Map<Field, Class<?>> fields = new LinkedHashMap<>();
        for (var parent : parents) {
            fields.putAll(getMapOfFields(parent));
        }
        fields.putAll(getMapOfFields(featureClass));
        return fields;
    }

    private Map<Field, Class<?>> getMapOfFields(Class<?> cls){
        Map<Field, Class<?>> fields = new LinkedHashMap<>();
        for (var field: cls.getDeclaredFields()) {
            fields.put(field, cls);
        }
        return fields;
    }

    private static ObjectMapper initMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(FIELD, ANY);
        return mapper;
    }
}
