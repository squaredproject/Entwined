package com.charlesgadeken.entwined.model;

import heronarts.lx.LXLoopTask;

public class ShrubModelTransformTask implements LXLoopTask {

    protected final ShrubModel model;

    ShrubModelTransformTask(ShrubModel model) {
        this.model = model;
    }

    @Override
    public void loop(double deltaMs) {
        model.runShrubTransforms();
    }
}
