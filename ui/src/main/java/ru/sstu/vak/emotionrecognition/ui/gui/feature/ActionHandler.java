package ru.sstu.vak.emotionrecognition.ui.gui.feature;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.HasChildren;

public interface ActionHandler {

    EventHandler<ActionEvent> apply(Button target, HasChildren<?> featureHolder, FeaturePane feature);
}
