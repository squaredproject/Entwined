package com.charlesgadeken.entwined.patterns;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.Model;
import com.charlesgadeken.entwined.triggers.ParameterTriggerableAdapter;
import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;
import org.reflections.Reflections;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class EntwinedBasePattern extends LXPattern {
    ParameterTriggerableAdapter parameterTriggerableAdapter;
    public String readableName;

    protected final Model model;

    protected EntwinedBasePattern(LX lx) {
        super(lx);
        model = (Model) lx.getModel();
    }

    public static List<EntwinedBasePattern> getAllPatterns(LX lx) {
        return getAllPatternClasses().stream()
                .map(
                        cls -> {
                            try {
                                return cls.getConstructor(LX.class).newInstance(lx);
                            } catch (Exception e) {
                                System.err.printf("Can't init: %s\n", cls.getName());
                                return null;
                            }
                        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static List<Class<? extends EntwinedBasePattern>> getAllPatternClasses() {
        Reflections reflections = new Reflections("com.charlesgadeken");
        return reflections.getSubTypesOf(EntwinedBasePattern.class).stream()
                .filter(Utilities::isConcrete)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.toList());
    }

    protected void setCallRun(boolean callRun) {
        getChannel().enabled.setValue(callRun);
    }

    public String getReadableName() {
        return readableName == null ? getClass().getSimpleName() : readableName;
    }
}
