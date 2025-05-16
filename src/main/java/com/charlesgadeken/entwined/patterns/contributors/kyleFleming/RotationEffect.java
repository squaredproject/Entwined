package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.effects.ModelTransform;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;

public class RotationEffect extends ModelTransform {

    final BoundedParameter rotation = new BoundedParameter("ROT", 0, 0, 360);

    public RotationEffect(LX lx) {
        super(lx);
    }

    public void transform(Model model) {
        if (rotation.getValue() > 0) {
            float rotationTheta = rotation.getValuef();
            for (BaseCube cube : model.baseCubes) {
                cube.transformedTheta = (cube.transformedTheta + 360 - rotationTheta) % 360;
            }
        }
    }
}
