package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedBaseEffect;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;

public class StaticEffect extends EntwinedBaseEffect {

    public final BoundedParameter amount = new BoundedParameter("STTC");

    private boolean isCreatingStatic = false;

    public StaticEffect(LX lx) {
        super(lx);
    }

    protected void run(double deltaMs, double unused) {
        if (amount.getValue() > 0) {
            if (isCreatingStatic) {
                double chance = Utilities.random(1);
                if (chance > amount.getValue()) {
                    isCreatingStatic = false;
                }
            } else {
                double chance = Utilities.random(1);
                if (chance < amount.getValue()) {
                    isCreatingStatic = true;
                }
            }
            if (isCreatingStatic) {
                for (int i = 0; i < colors.length; i++) {
                    colors[i] = (int) Utilities.random(255);
                }
            }
        }
    }
}
