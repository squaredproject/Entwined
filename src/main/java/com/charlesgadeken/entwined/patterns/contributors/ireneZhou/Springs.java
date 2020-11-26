package com.charlesgadeken.entwined.patterns.contributors.ireneZhou;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.Click;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Irene Zhou")
public class Springs extends EntwinedBasePattern {
    final BoundedParameter hue = new BoundedParameter("HUE", 0, 0, 360);
    private BooleanParameter automated = new BooleanParameter("AUTO", true);
    private final Accelerator gravity = new Accelerator(0, 0, 0);
    private final Click reset = new Click(9600);
    private boolean isRising = false;
    final SinLFO spin = new SinLFO(0, 360, 9600);

    float coil(float basis) {
        return 4 * Utilities.sin(basis*Utilities.TWO_PI + Utilities.PI) ;
    }

    public Springs(LX lx) {
        super(lx);
        addModulator(gravity);
        addModulator(reset).start();
        addModulator(spin).start();
        addParameter(hue);
        addParameter(automated);
        trigger();
    }

    public void onParameterChanged(LXParameter parameter) {
        super.onParameterChanged(parameter);
        if (parameter == automated) {
            if (automated.isOn()) {
                trigger();
            }
        }
    }

    private void trigger() {
        isRising = !isRising;
        if (isRising) {
            gravity.setSpeed(0.25f, 0).start();
        }
        else {
            gravity.setVelocity(0).setAcceleration(-1.75f);
        }
    }

    public void run(double deltaMS) {
        if (getChannel().fader.getNormalized() == 0) return;

        if (!isRising) {
            gravity.start();
            if (gravity.getValuef() < 0) {
                gravity.setValue(-gravity.getValuef());
                gravity.setVelocity(-gravity.getVelocityf() * Utilities.random(0.74f, 0.84f));
            }
        }

        float spinf = spin.getValuef();
        float coilf = 2*coil(spin.getBasisf());

        for (BaseCube cube : model.baseCubes) {
            float yn =  cube.transformedY/model.yMax;
            float width = (1-yn) * 25;
            float wrapdist = LXUtils.wrapdistf(cube.transformedTheta, spinf + (cube.transformedY) * 1/(gravity.getValuef() + 0.2f), 360);
            float df = Utilities.max(0, 100 - Utilities.max(0, wrapdist-width));
            colors[cube.index] = LX.hsb(
                Utilities.max(0, (lx.engine.palette.getHuef() - yn * 20 + hue.getValuef()) % 360),
                Utilities.constrain((1- yn) * 100 + wrapdist, 0, 100),
                Utilities.max(0, df - yn * 50)
            );
        }
    }
}
