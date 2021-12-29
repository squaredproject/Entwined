package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;

public class Palette extends EntwinedBasePattern {
    public Palette(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            colors[cube.index] = lx.hsb(cube.index % 360, 100, 100);
        }
    }
}
