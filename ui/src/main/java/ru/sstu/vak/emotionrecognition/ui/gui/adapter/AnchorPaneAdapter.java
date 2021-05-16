package ru.sstu.vak.emotionrecognition.ui.gui.adapter;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class AnchorPaneAdapter implements HasChildren<AnchorPane> {

    private final AnchorPane anchorPane;

    public AnchorPaneAdapter(AnchorPane anchorPane) {
        this.anchorPane = anchorPane;
    }

    @Override
    public AnchorPane getNode() {
        return anchorPane;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return anchorPane.getChildren();
    }
}
