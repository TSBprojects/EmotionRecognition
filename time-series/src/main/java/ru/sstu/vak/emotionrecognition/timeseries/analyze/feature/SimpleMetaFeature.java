package ru.sstu.vak.emotionrecognition.timeseries.analyze.feature;

import com.google.auto.service.AutoService;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import lombok.ToString;
import lombok.var;
import ru.sstu.vak.emotionrecognition.common.collection.AutoIncrementMap;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.signs.EquivalenceSign;
import ru.sstu.vak.emotionrecognition.timeseries.analyze.signs.LogicalSign;

@ToString
@AutoService(MetaFeature.class)
public class SimpleMetaFeature implements MetaFeature {

    private static final int ID = 6;

    private static final String DESCRIPTION =
        "Мета фактор. выражение, описывающее отношение (>,<,=) между факторами, указанными в текущей конфигурации";

    private final String name;

    private final SortedMap<Integer, String> rule;

    @ToString.Exclude
    private final ScriptEngine evalEngine = new ScriptEngineManager().getEngineByName("JavaScript");

    private boolean invalid = false;

    private boolean satisfied = false;

    public SimpleMetaFeature() {
        this("Мета фактор", new TreeMap<>());
    }

    public SimpleMetaFeature(SimpleMetaFeature feature) {
        this(feature.name, new TreeMap<>(feature.rule));
        this.invalid = feature.invalid;
        this.satisfied = feature.satisfied;
    }

    public SimpleMetaFeature(String name, SortedMap<Integer, String> rule) {
        this.name = name;
        this.rule = rule;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void apply(AutoIncrementMap<EmotionFeature> features) {
        if (rule.isEmpty()) {
            invalid = false;
            satisfied = false;
            return;
        }

        String template = rule.values().stream().map(this::removeSpaces).collect(Collectors.joining(" "));
        template = LogicalSign.toProgramReadable(EquivalenceSign.toProgramReadable(template));

        try {
            for (var entry : features.entrySet()) {
                var id = entry.getKey();
                var feature = entry.getValue();
                String nameToReplace = removeSpaces(id + "." + feature.getName());
                template = template.replace(nameToReplace, Integer.toString(feature.getValue()));
            }

            boolean result = (boolean) evalEngine.eval(template);
            invalid = false;
            satisfied = result;
        } catch (Exception e) {
            invalid = true;
            satisfied = false;
        }
    }

    @Override
    public SortedMap<Integer, String> getRule() {
        return rule;
    }

    @Override
    public void clear() {
        invalid = false;
        satisfied = false;
    }

    @Override
    public SimpleMetaFeature copy() {
        return new SimpleMetaFeature(this);
    }

    @Override
    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public boolean isSatisfied() {
        return satisfied;
    }

    private String removeSpaces(String value) {
        return value.replaceAll("\\s", "");
    }
}
