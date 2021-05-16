package ru.sstu.vak.emotionrecognition.timeseries.analyze.signs;

import com.fasterxml.jackson.annotation.JsonCreator;
import static java.util.Arrays.stream;
import java.util.Map;
import static java.util.stream.Collectors.toMap;
import ru.sstu.vak.emotionrecognition.common.Nameable;

public enum LogicalSign implements Nameable {
    AND("и", "&&"),
    OR("или", "||");

    private static final Map<String, LogicalSign> MAP = stream(values()).collect(toMap(LogicalSign::getName, e -> e));

    private final String humanReadable;

    private final String programReadable;

    LogicalSign(String humanReadable, String programReadable) {
        this.humanReadable = humanReadable;
        this.programReadable = programReadable;
    }

    @JsonCreator
    public static LogicalSign of(String name) {
        LogicalSign sign = MAP.get(name);

        if (sign == null) {
            throw new UnsupportedOperationException("Unknown or not supported sign: " + name);
        }

        return sign;
    }

    @Override
    public String getName() {
        return humanReadable;
    }

    public static String toProgramReadable(String str) {
        String result = str;
        for (LogicalSign sign : values()) {
            result = result.replace(surroundWithSpaces(sign.humanReadable), surroundWithSpaces(sign.programReadable));
        }
        return result;
    }

    private static String surroundWithSpaces(String value) {
        return " " + value + " ";
    }
}
