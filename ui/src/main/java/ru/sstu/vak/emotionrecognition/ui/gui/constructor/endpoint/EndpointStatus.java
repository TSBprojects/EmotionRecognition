package ru.sstu.vak.emotionrecognition.ui.gui.constructor.endpoint;

import lombok.Getter;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.ENDPOINT_ERROR_STATUS_CLASS;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.ENDPOINT_OK_STATUS_CLASS;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.ENDPOINT_PROGRESS_STATUS_CLASS;
import static ru.sstu.vak.emotionrecognition.ui.util.ConstructorV2.ENDPOINT_WARN_STATUS_CLASS;

@Getter
public enum EndpointStatus {
    OK(ENDPOINT_OK_STATUS_CLASS),
    WARN(ENDPOINT_WARN_STATUS_CLASS),
    ERROR(ENDPOINT_ERROR_STATUS_CLASS),
    PROGRESS(ENDPOINT_PROGRESS_STATUS_CLASS);

    private final String styleClass;

    EndpointStatus(String styleClass) {
        this.styleClass = styleClass;
    }
}
