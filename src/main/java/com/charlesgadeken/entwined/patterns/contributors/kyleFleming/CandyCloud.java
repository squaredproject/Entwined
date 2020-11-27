package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;
import toxi.math.noise.SimplexNoise;

public class CandyCloud extends EntwinedBasePattern {

    final BoundedParameter darkness = new BoundedParameter("DARK", 8, 0, 12);

    final BoundedParameter scale = new BoundedParameter("SCAL", 2400, 600, 10000);
    final BoundedParameter speed = new BoundedParameter("SPD", 1, 1, 2);

    double time = 0;

    public CandyCloud(LX lx) {
        super(lx);

        addParameter(darkness);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        time += deltaMs;
        for (BaseCube cube : model.baseCubes) {
            double adjustedX = cube.x / scale.getValue();
            double adjustedY = cube.y / scale.getValue();
            double adjustedZ = cube.z / scale.getValue();
            double adjustedTime = time * speed.getValue() / 5000;

            float hue =
                    ((float) SimplexNoise.noise(adjustedX, adjustedY, adjustedZ, adjustedTime) + 1)
                            / 2
                            * 1080
                            % 360;

            float brightness =
                    Utilities.min(
                                    Utilities.max(
                                            (float)
                                                                    SimplexNoise.noise(
                                                                            cube.x / 250,
                                                                            cube.y / 250,
                                                                            cube.z / 250 + 10000,
                                                                            time / 5000)
                                                            * 8
                                                    + 8
                                                    - darkness.getValuef(),
                                            0),
                                    1)
                            * 100;

            colors[cube.index] = lx.hsb(hue, 100, brightness);
        }
    }
}
