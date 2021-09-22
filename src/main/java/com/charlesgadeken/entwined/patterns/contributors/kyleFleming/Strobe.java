package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;

public class Strobe extends EntwinedTriggerablePattern {

    final BoundedParameter speed =
            new BoundedParameter(
                    "SPEE", 200, 3000, 30); // TODO(meawoppl), BoundedParameter.Scaling.QUAD_OUT);
    final BoundedParameter balance = new BoundedParameter("BAL", .5, .01, .99);

    int timer = 0;
    boolean on = false;

    public Strobe(LX lx) {
        super(lx);

        addParameter(speed);
        addParameter(balance);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        if (triggered) {
            timer += deltaMs;
            if (timer >= speed.getValuef() * (on ? balance.getValuef() : 1 - balance.getValuef())) {
                timer = 0;
                on = !on;
            }

            setColors(on ? LXColor.WHITE : LXColor.BLACK);
        }
    }

    public void onTriggered(float strength) {
        super.onTriggered(strength);

        on = true;
    }

    public void onRelease() {
        super.onRelease();

        timer = 0;
        on = false;
        setColors(LXColor.BLACK);
    }
}
