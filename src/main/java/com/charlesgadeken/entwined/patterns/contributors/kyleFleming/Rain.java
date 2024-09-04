package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;

public class Rain extends MultiObjectPattern<RainDrop> {

    public Rain(LX lx) {
        super(lx);
        fadeTime = 500;
    }

    BoundedParameter getFrequencyParameter() {
        return new BoundedParameter("FREQ", 40, 1, 75);
    }

    RainDrop generateObject(float strength) {
        RainDrop rainDrop = new RainDrop(lx);

        rainDrop.runningTimerEnd = 180 + Utilities.random(20);
        rainDrop.theta = Utilities.random(360);
        rainDrop.startY = model.yMax + 20;
        rainDrop.endY = model.yMin - 20;
        rainDrop.hue = 200 + (int) Utilities.random(20);
        rainDrop.thickness = 10 * (1.5f + Utilities.random(.6f));

        return rainDrop;
    }
}
