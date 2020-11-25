package com.charlesgadeken.entwined.effects;

import com.charlesgadeken.entwined.triggers.Triggerable;
import heronarts.lx.effect.LXEffect;

public class TSEffectController {
    String name;
    LXEffect effect;
    Triggerable triggerable;

    public TSEffectController(String name, LXEffect effect, Triggerable triggerable) {
        this.name = name;
        this.effect = effect;
        this.triggerable = triggerable;
    }

    public String getName() {
        return name;
    }

    public boolean getEnabled() {
        return triggerable.isTriggered();
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            triggerable.onTriggered(1);
        } else {
            triggerable.onRelease();
        }
    }
}
