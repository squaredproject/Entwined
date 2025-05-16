package com.charlesgadeken.entwined.model;

import heronarts.lx.LXLoopTask;

public class ModelTransformTask implements LXLoopTask {

    protected final Model model;

    public ModelTransformTask(Model model) {
        this.model = model;
    }

    @Override
    public void loop(double deltaMs) {
        model.runTransforms();
        //        model.runShrubTransforms();
    }
}
