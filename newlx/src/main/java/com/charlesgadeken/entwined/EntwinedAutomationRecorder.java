package com.charlesgadeken.entwined;

import heronarts.lx.LXEngine;

// TODO(meawoppl)
// @Slee there used to be a class called LXAutomationRecorder...
// Def. not sure how to deal with that...
public class EntwinedAutomationRecorder extends FakeLXAutomationRecorder {

    boolean isPaused;

    EntwinedAutomationRecorder(LXEngine engine) {
        super(engine);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isPaused = false;
    }

    public void setPaused(boolean paused) {
        if (!paused && !isRunning()) {
            start();
        }
        isPaused = paused;
    }

    @Override
    public void loop(double deltaMs) {
        if (!isPaused) {
            super.loop(deltaMs);
        }
    }
}
