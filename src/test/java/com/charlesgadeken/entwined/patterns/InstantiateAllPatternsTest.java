package com.charlesgadeken.entwined.patterns;

import com.charlesgadeken.entwined.EntwinedGui;
import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.stream.Stream;

import heronarts.lx.pattern.LXPattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;

@TestInstance(Lifecycle.PER_CLASS)
public class InstantiateAllPatternsTest {
    Stream<Class<? extends EntwinedBasePattern>> findPatterns() {
        Reflections reflection = new Reflections("com.charlesgadeken");
        return reflection.getSubTypesOf(EntwinedBasePattern.class).stream()
                .filter(p -> !Modifier.isAbstract(p.getModifiers()))
                .sorted(Comparator.comparing(Class::getName, String::compareTo))
                .filter(Utilities::isConcrete);
    }

    @ParameterizedTest
    @MethodSource("findPatterns")
    public <T extends EntwinedBasePattern> void  validatePatternsFromBaseInstantiate(Class<T> cls)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException,
                    InvocationTargetException {
        Model model = Model.fromConfigs();
        cls.getConstructor(LX.class).newInstance(new LX(model));
    }


//    @ParameterizedTest
//    @MethodSource("findPatterns")
    public <T extends EntwinedBasePattern> void validatePatternsRunWithKnownDeltas(Class<T> cls)
        throws IllegalAccessException, InstantiationException, NoSuchMethodException,
        InvocationTargetException {
        Model model = Model.fromConfigs();
        LX lx = new LX(model);

        model.runTransforms();
        try {
            Thread.sleep(100);
        } catch (Exception e){}

        T pattern = cls.getConstructor(LX.class).newInstance(lx);
        lx.engine.mixer.addChannel(new LXPattern[] {pattern});

        pattern.run(100);
    }
}
