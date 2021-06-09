package ru.sstu.vak.emotionrecognition.ui.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.endpoint.Endpoint;

public class EndpointSettingsController {

    private boolean ok = false;

    private Stage currentStage;

    private Endpoint endpoint;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField ipTextField;

    @FXML
    private TextField portTextField;

    @FXML
    void onOk(ActionEvent event) {
        endpoint.setName(nameTextField.getText());
        endpoint.setIp(ipTextField.getText());
        endpoint.setPort(portTextField.getText());
        ok = true;
        closeForm();
    }

    @FXML
    void onCancel(ActionEvent event) {
        ok = false;
        closeForm();
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
        nameTextField.setText(endpoint.getName());
        ipTextField.setText(endpoint.getIp());
        portTextField.setText(endpoint.getPort());
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public boolean isOk() {
        return ok;
    }

    private void closeForm() {
        currentStage.fireEvent(new WindowEvent(currentStage, WINDOW_CLOSE_REQUEST));
    }
}
