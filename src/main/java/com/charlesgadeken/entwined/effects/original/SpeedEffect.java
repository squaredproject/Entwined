package com.charlesgadeken.entwined.effects.original;

import com.charlesgadeken.entwined.effects.EntwinedBaseEffect;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;

public class SpeedEffect extends EntwinedBaseEffect {
    public final BoundedParameter speed = new BoundedParameter("SPEED", 1, .1, 10);

    public SpeedEffect(LX lx) {
        super(lx);
        speed.addListener((unused) -> lx.engine.setSpeed(speed.getValue()));
    }

    protected void onEnable() {
        super.onEnable();
        lx.engine.setSpeed(speed.getValue());
    }

    public void run(double deltaMs, double unused) {}
}
