package ru.sstu.vak.emotionrecognition.ui.gui.constructor.feature;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import ru.sstu.vak.emotionrecognition.ui.gui.adapter.HasChildren;

public interface FeatureAction {

    EventHandler<ActionEvent> apply(Button target, HasChildren<?> featureHolder, FeaturePane feature);
}
