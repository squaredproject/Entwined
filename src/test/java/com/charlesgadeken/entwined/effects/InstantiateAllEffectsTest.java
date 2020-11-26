package com.charlesgadeken.entwined.effects;

import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;

@TestInstance(Lifecycle.PER_CLASS)
public class InstantiateAllEffectsTest {

    Model model;

    @BeforeAll
    void init() {
        this.model = Model.fromConfigs();
    }

    Stream<Class<? extends EntwinedBaseEffect>> findEffects() {
        Reflections reflection = new Reflections("com.charlesgadeken");
        return reflection.getSubTypesOf(EntwinedBaseEffect.class).stream().filter(e-> !Modifier.isAbstract(e.getModifiers()));
    }

    @ParameterizedTest
    @MethodSource("findEffects")
    void validateEffectsFromBaseInstantiate(Class<?> cls)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException,
                    InvocationTargetException {
        cls.getConstructor(LX.class).newInstance(new LX(model));
    }
}
