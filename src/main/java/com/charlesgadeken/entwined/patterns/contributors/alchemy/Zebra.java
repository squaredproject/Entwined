package com.charlesgadeken.entwined.patterns.contributors.alchemy;

import com.charlesgadeken.entwined.model.cube.Cube;
import com.charlesgadeken.entwined.model.shrub.ShrubCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("alchemy")
public class Zebra extends EntwinedBasePattern {

    BoundedParameter thickness = new BoundedParameter("THIC", 160, 0, 200);
    BoundedParameter period = new BoundedParameter("PERI", 500, 300, 3000);
    double timer = 0;

    SinLFO position = new SinLFO(0, 200, period);

    public Zebra(LX lx) {
        super(lx);
        addParameter("alchemy/zebra/thickness", thickness);
        addParameter("alchemy/zebra/period", period);
        addModulator(position).start();
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        timer = timer + deltaMs;
        for (Cube cube : model.cubes) {
            float hue = .4f;
            float saturation;
            float brightness = 1;

            if (((cube.transformedY + position.getValue() + cube.transformedTheta) % 200)
                    > thickness.getValue()) {
                saturation = 0;
                brightness = 1;
            } else {
                saturation = 1;
                brightness = 0;
            }

            colors[cube.index] = LX.hsb(360 * hue, 100 * saturation, 100 * brightness);
        }
        for (ShrubCube cube : model.shrubCubes) {
            float hue = .4f;
            float saturation;
            float brightness = 1;

            if (((cube.transformedY + position.getValue() + cube.transformedTheta) % 200)
                    > thickness.getValue()) {
                saturation = 0;
                brightness = 1;
            } else {
                saturation = 1;
                brightness = 0;
            }

            colors[cube.index] = LX.hsb(360 * hue, 100 * saturation, 100 * brightness);
        }
    }
}
