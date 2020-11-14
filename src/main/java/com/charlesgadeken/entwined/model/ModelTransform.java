package com.charlesgadeken.entwined.model;

import heronarts.lx.LX;

public abstract class ModelTransform extends Effect {
    ModelTransform(LX lx) {

        super(lx);
        model.addModelTransform(this);
    }

    @Override
    public void run(double deltaMs) {}

    abstract void transform(Model model);
}
