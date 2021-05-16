package ru.sstu.vak.emotionrecognition.ui.gui.adapter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

public interface ActionableNode<T extends Node> {

    T getNode();

    String getValue();

    void addOnAction(EventHandler<ActionEvent> handler);
}
