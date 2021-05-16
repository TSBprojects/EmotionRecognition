package ru.sstu.vak.emotionrecognition.ui.gui.adapter;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import lombok.var;

public class ComboBoxAdapter implements ActionableNode<ComboBox<String>> {

    private final ComboBox<String> comboBox;

    private final List<EventHandler<ActionEvent>> actionHandlers = new ArrayList<>();

    public ComboBoxAdapter(ComboBox<String> comboBox) {
        this.comboBox = comboBox;
        var action = comboBox.getOnAction();
        if (action != null) {
            this.actionHandlers.add(action);
        }
        this.comboBox.setOnAction(event -> actionHandlers.forEach(h -> h.handle(event)));
    }

    @Override
    public ComboBox<String> getNode() {
        return comboBox;
    }

    @Override
    public String getValue() {
        return comboBox.getValue();
    }

    @Override
    public void addOnAction(EventHandler<ActionEvent> handler) {
        actionHandlers.add(handler);
    }
}
