package ru.sstu.vak.emotionrecognition.ui.gui.adapter;

import javafx.collections.ObservableList;
import javafx.scene.Node;

public interface HasChildren<T extends Node> {

    T getNode();

    ObservableList<Node> getChildren();
}
