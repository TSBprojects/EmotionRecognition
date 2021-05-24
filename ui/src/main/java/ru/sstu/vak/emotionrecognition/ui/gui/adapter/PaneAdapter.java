package ru.sstu.vak.emotionrecognition.ui.gui.adapter;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class PaneAdapter implements HasChildren<Pane> {

    private final Pane pane;

    public PaneAdapter(Pane pane) {
        this.pane = pane;
    }

    @Override
    public Pane getNode() {
        return pane;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return pane.getChildren();
    }
}
