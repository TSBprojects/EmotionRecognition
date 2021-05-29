package ru.sstu.vak.emotionrecognition.timeseries.analyze.signs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import static java.util.Arrays.stream;
import java.util.Map;
import java.util.function.BiPredicate;
import static java.util.stream.Collectors.toMap;
import ru.sstu.vak.emotionrecognition.common.Nameable;

public enum EquivalenceSign implements Nameable {
    MORE(">", ">", (x, y) -> x > y),
    LESS("<", "<", (x, y) -> x < y),
    EQUAL("=", "==", (x, y) -> x.compareTo(y) == 0);

    private static final Map<String, EquivalenceSign> MAP = stream(values()).collect(toMap(EquivalenceSign::getName, e -> e));

    @JsonValue
    private final String humanReadable;

    private final String programReadable;

    private final BiPredicate<Long, Long> expression;

    EquivalenceSign(String humanReadable, String programReadable, BiPredicate<Long, Long> expression) {
        this.humanReadable = humanReadable;
        this.programReadable = programReadable;
        this.expression = expression;
    }

    @JsonCreator
    public static EquivalenceSign of(String name) {
        EquivalenceSign sign = MAP.get(name);

        if (sign == null) {
            throw new UnsupportedOperationException("Unknown or not supported sign: " + name);
        }

        return sign;
    }

    @Override
    public String getName() {
        return humanReadable;
    }

    public boolean apply(long x, long y) {
        return this.expression.test(x, y);
    }

    public static String toProgramReadable(String str) {
        String result = str;
        for (EquivalenceSign sign : values()) {
            result = result.replace(surroundWithSpaces(sign.humanReadable), surroundWithSpaces(sign.programReadable));
        }
        return result;
    }

    private static String surroundWithSpaces(String value) {
        return " " + value + " ";
    }
}
