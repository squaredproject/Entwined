package com.charlesgadeken.entwined.patterns.original;

import com.charlesgadeken.entwined.EntwinedCategory;
import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.Conversions;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;

@LXCategory(EntwinedCategory.ORIGINAL)
public class SweepPattern extends EntwinedBasePattern {

    final SinLFO speedMod = new SinLFO(3000, 9000, 5400);
    final SinLFO yPos = new SinLFO(model.yMin, model.yMax, speedMod);
    final SinLFO width = new SinLFO("WIDTH", 2 * Conversions.FEET, 20 * Conversions.FEET, 19000);

    final SawLFO offset = new SawLFO(0, Utilities.TWO_PI, 9000);

    final BoundedParameter amplitude =
            new BoundedParameter("AMP", 10 * Conversions.FEET, 0, 20 * Conversions.FEET);
    final BoundedParameter speed = new BoundedParameter("SPEED", 1, 0, 3);
    final BoundedParameter height = new BoundedParameter("HEIGHT", 0, -300, 300);
    final SinLFO amp = new SinLFO(0, amplitude, 5000);

    public SweepPattern(LX lx) {
        super(lx);
        addModulator(speedMod).start();
        addModulator(yPos).start();
        addModulator(width).start();
        addParameter("sweepPattern/amplitude", amplitude);
        addParameter("sweepPattern/speed", speed);
        addParameter("sweepPattern/height", height);
        addModulator(amp).start();
        addModulator(offset).start();
    }

    public void onParameterChanged(LXParameter parameter) {
        super.onParameterChanged(parameter);
        if (parameter == speed) {
            float speedVar = 1 / speed.getValuef();
            speedMod.setRange(9000 * speedVar, 5400 * speedVar);
        }
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            float yp =
                    yPos.getValuef()
                            + amp.getValuef()
                                    * Utilities.sin(
                                            (cube.cx - model.cx) * .01f + offset.getValuef());
            colors[cube.index] =
                    lx.hsb(
                            (lx.engine.palette.getHuef()
                                            + Utilities.abs(cube.x - model.cx) * .2f
                                            + cube.cz * .1f
                                            + cube.cy * .1f)
                                    % 360,
                            Utilities.constrain(
                                    Utilities.abs(cube.transformedY - model.cy), 0, 100),
                            Utilities.max(
                                    0,
                                    100
                                            - (100 / width.getValuef())
                                                    * Utilities.abs(
                                                            cube.cy - yp - height.getValuef())));
        }
    }
}
