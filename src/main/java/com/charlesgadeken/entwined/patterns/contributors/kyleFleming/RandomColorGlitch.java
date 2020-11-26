package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.model.BaseCube;
import heronarts.lx.LX;

public class RandomColorGlitch extends EntwinedTriggerablePattern {

    public RandomColorGlitch(LX lx) {
        super(lx);
    }

    final int brokenCubeIndex = (int) Utilities.random(model.baseCubes.size());

    final int cubeColor = (int) Utilities.random(360);

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            if (cube.index == brokenCubeIndex) {
                colors[cube.index] = lx.hsb(Utilities.random(360), 100, 100);
            } else {
                colors[cube.index] = lx.hsb(cubeColor, 100, 100);
            }
        }
    }
}
