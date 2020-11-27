package com.charlesgadeken.entwined.patterns;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;

@TestInstance(Lifecycle.PER_CLASS)
public class InstantiateAllPatternsTest {
    Model model;

    @BeforeAll
    void init() {
        this.model = Model.fromConfigs();
    }

    Stream<Class<? extends EntwinedBasePattern>> findPatterns() {
        Reflections reflection = new Reflections("com.charlesgadeken");
        return reflection.getSubTypesOf(EntwinedBasePattern.class).stream()
                .filter(p -> !Modifier.isAbstract(p.getModifiers()))
                .sorted(Comparator.comparing(Class::getName, String::compareTo))
                .filter(Utilities::isConcrete);
    }

    @ParameterizedTest
    @MethodSource("findPatterns")
    void validatePatternsFromBaseInstantiate(Class<?> cls)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException,
                    InvocationTargetException {
        cls.getConstructor(LX.class).newInstance(new LX(model));
    }
}
