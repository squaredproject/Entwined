package com.charlesgadeken.entwined.patterns.examples;

import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

@LXCategory("Example")
public class ExamplePattern extends EntwinedBasePattern {

    public ExamplePattern(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.hsb(240, 100, 100);
        }
    }
}
