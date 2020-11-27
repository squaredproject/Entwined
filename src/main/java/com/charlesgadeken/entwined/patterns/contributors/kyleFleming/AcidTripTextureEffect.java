package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedBaseEffect;
import com.charlesgadeken.entwined.model.Cube;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;

public class AcidTripTextureEffect extends EntwinedBaseEffect {

    public final BoundedParameter amount = new BoundedParameter("ACID");

    final SawLFO trails = new SawLFO(360, 0, 7000);

    public AcidTripTextureEffect(LX lx) {
        super(lx);
        addModulator(trails).start();
    }

    public void run(double deltaMs, double unused) {
        if (amount.getValue() > 0) {
            for (int i = 0; i < colors.length; i++) {
                int oldColor = colors[i];
                Cube cube = model.cubes.get(i);
                // TODO ashley modify the rest of the file for shrubCubes
                // ShrubCube shrubCube = model.shrubCubes.get(i);

                float newHue =
                        Utilities.abs(model.cy - cube.transformedY)
                                + Utilities.abs(model.cy - cube.transformedTheta)
                                + trails.getValuef() % 360;
                int newColor = lx.hsb(newHue, 100, 100);
                int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
                colors[i] =
                        lx.hsb(
                                LXColor.h(blendedColor),
                                LXColor.s(blendedColor),
                                LXColor.b(oldColor));
            }
        }
    }
}
