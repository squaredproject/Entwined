package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.effects.ModelTransform;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.model.Cube;
import com.charlesgadeken.entwined.model.Model;
import com.charlesgadeken.entwined.model.ShrubCube;
import heronarts.lx.LX;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class SpinEffect extends ModelTransform {

    final BoundedParameter spin = new BoundedParameter("SPIN");
    final FunctionalParameter rotationPeriodMs =
            new FunctionalParameter() {
                public double getValue() {
                    return 5000 - 4800 * spin.getValue();
                }
            };
    final SawLFO rotation = new SawLFO(0, 360, rotationPeriodMs);

    public SpinEffect(LX lx) {
        super(lx);

        addModulator(rotation);

        spin.addListener(
                new LXParameterListener() {
                    public void onParameterChanged(LXParameter parameter) {
                        if (spin.getValue() > 0) {
                            rotation.start();
                            rotation.setLooping(true);
                        } else {
                            rotation.setLooping(false);
                        }
                    }
                });
    }

    public void transform(Model model) {
        if (rotation.getValue() > 0 && rotation.getValue() < 360) {
            float rotationTheta = rotation.getValuef();
            for (BaseCube cube : model.baseCubes) {
                cube.transformedTheta = (cube.transformedTheta + 360 - rotationTheta) % 360;
            }
            for (Cube cube : model.cubes) {
                cube.transformedTheta = (cube.transformedTheta + 360 - rotationTheta) % 360;
            }
            for (ShrubCube cube : model.shrubCubes) {
                cube.transformedTheta = (cube.transformedTheta + 360 - rotationTheta) % 360;
            }
        }
    }
}
