package com.charlesgadeken.entwined.patterns.contributors.geoffSchmidt;

import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;

public class Wedges extends EntwinedBasePattern {
    final BoundedParameter pSpeed = new BoundedParameter("SPD", .52);
    final BoundedParameter pCount = new BoundedParameter("COUNT", 4.0 / 15.0);
    final BoundedParameter pSat = new BoundedParameter("SAT", 5.0 / 15.0);
    final BoundedParameter pHue = new BoundedParameter("HUE", .5);
    double rotation = 0; // degrees

    public Wedges(LX lx) {
        super(lx);

        addParameter(pSpeed);
        addParameter(pCount);
        addParameter(pSat);
        addParameter(pHue);
        rotation = 0;
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        float vSpeed = pSpeed.getValuef();
        float vCount = pCount.getValuef();
        float vSat = pSat.getValuef();
        float vHue = pHue.getValuef();

        rotation += deltaMs / 1000.0f * (2 * (vSpeed - .5f) * 360.0f * 1.0f);
        rotation = rotation % 360.0f;

        double sections = Math.floor(1.0f + vCount * 10.0f);
        double quant = 360.0f / sections;

        for (BaseCube cube : model.baseCubes) {
            colors[cube.index] =
                    LXColor.hsb(
                            Math.floor((rotation - cube.transformedTheta) / quant) * quant
                                    + vHue * 360.0f,
                            (1 - vSat) * 100,
                            100);
        }
    }
}
