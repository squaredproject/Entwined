package com.charlesgadeken.entwined.patterns;

import com.charlesgadeken.entwined.Utilities;
import heronarts.lx.LX;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.reflections.Reflections;

public class Patterns {
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
}
