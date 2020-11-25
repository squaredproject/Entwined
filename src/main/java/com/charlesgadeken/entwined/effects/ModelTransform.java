package com.charlesgadeken.entwined.effects;

import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;

public abstract class ModelTransform extends EntwinedBaseEffect {
    public ModelTransform(LX lx) {
        super(lx);
        model.addModelTransform(this);
    }

    @Override
    public void run(double deltaMs, double unused) {}

    public abstract void transform(Model model);
}
