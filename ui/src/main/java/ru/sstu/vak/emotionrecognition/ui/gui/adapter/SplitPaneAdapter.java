package ru.sstu.vak.emotionrecognition.ui.gui.adapter;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

public class SplitPaneAdapter implements HasChildren<SplitPane> {

    private final SplitPane splitPane;

    public SplitPaneAdapter(SplitPane splitPane) {
        this.splitPane = splitPane;
    }

    @Override
    public SplitPane getNode() {
        return splitPane;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return splitPane.getItems();
    }
}
