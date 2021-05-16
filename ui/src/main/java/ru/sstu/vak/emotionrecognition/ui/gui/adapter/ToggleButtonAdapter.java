package ru.sstu.vak.emotionrecognition.ui.gui.adapter;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ToggleButton;
import lombok.var;

public class ToggleButtonAdapter implements ActionableNode<ToggleButton> {

    private final ToggleButton toggleButton;

    private final List<EventHandler<ActionEvent>> actionHandlers = new ArrayList<>();

    public ToggleButtonAdapter(ToggleButton toggleButton) {
        this.toggleButton = toggleButton;
        var action = toggleButton.getOnAction();
        if (action != null) {
            this.actionHandlers.add(action);
        }
        this.toggleButton.setOnAction(event -> actionHandlers.forEach(h -> h.handle(event)));
    }

    @Override
    public ToggleButton getNode() {
        return toggleButton;
    }

    @Override
    public String getValue() {
        return toggleButton.getText();
    }

    @Override
    public void addOnAction(EventHandler<ActionEvent> handler) {
        actionHandlers.add(handler);
    }
}
