package com.charlesgadeken.entwined.triggers;

import heronarts.lx.parameter.LXParameterListener;

public interface Triggerable {
    boolean isTriggered();

    void onTriggered(float strength);

    void onRelease();

    void addOutputTriggeredListener(LXParameterListener listener);
}
