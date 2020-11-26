package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;

public class RandomColor extends EntwinedBasePattern {

    final BoundedParameter speed = new BoundedParameter("Speed", 1, 1, 10);

    int frameCount = 0;

    public RandomColor(LX lx) {
        super(lx);
        addParameter(speed);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        frameCount++;
        if (frameCount >= speed.getValuef()) {
            for (BaseCube cube : model.baseCubes) {
                colors[cube.index] = lx.hsb(Utilities.random(360), 100, 100);
            }
            frameCount = 0;
        }
    }
}
