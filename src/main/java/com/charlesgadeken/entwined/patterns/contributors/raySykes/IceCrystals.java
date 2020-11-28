package com.charlesgadeken.entwined.patterns.contributors.raySykes;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import com.charlesgadeken.entwined.triggers.ParameterTriggerableAdapter;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;

@LXCategory("Ray Sykes")
public class IceCrystals extends EntwinedTriggerablePattern {
    private IceCrystalLine crystal;
    final BoundedParameter propagationSpeed = new BoundedParameter("Speed", 5, 1, 20);
    final BoundedParameter lineWidth = new BoundedParameter("Width", 60, 20, 150);
    final DiscreteParameter recursionDepth = new DiscreteParameter("Danger", 7, 12);
    final IceCrystalSettings settingsObj;

    public IceCrystals(LX lx) {
        super(lx);
        addParameter(propagationSpeed);
        addParameter(lineWidth);
        addParameter(recursionDepth);
        recursionDepth.setRange(5, 14);
        settingsObj = new IceCrystalSettings(14);
        crystal = new IceCrystalLine(0, settingsObj);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) {
            if (crystal.lifeCycleState != -1) {
                crystal.doReset();
            }
            return;
        }

        if (crystal.isDone()) {
            startCrystal();
        }
        crystal.doUpdate();

        for (BaseCube cube : model.baseCubes) {
            float lineFactor = crystal.getLineFactor(cube.transformedY, cube.transformedTheta);
            if (lineFactor > 110) {
                lineFactor = 200 - lineFactor;
            }
            float hueVal;
            float satVal;
            float brightVal = Utilities.min(100, 20 + lineFactor);
            if (lineFactor > 100) {
                brightVal = 100;
                hueVal = 180;
                satVal = 0;
            } else if (lineFactor < 20) {
                hueVal = 220;
                satVal = 100;
            } else if (lineFactor < 50) {
                hueVal = 240;
                satVal = 60;
            } else {
                hueVal = 240;
                satVal = 60 - 60 * (lineFactor / 100);
            }
            colors[cube.index] = lx.hsb(hueVal, satVal, brightVal);
        }
    }

    void startCrystal() {
        crystal.doReset();
        settingsObj.doSettings(
                recursionDepth.getValuei(),
                lineWidth.getValuef(),
                150,
                propagationSpeed.getValuef());
        crystal.doStart(100, Utilities.random(360), (7 + ((int) Utilities.random(2.9f))) % 8);
    }

    ParameterTriggerableAdapter getParameterTriggerableAdapter() {
        return new ParameterTriggerableAdapter(lx, getChannel().fader) {
            public void onTriggered(float strength) {
                startCrystal();
                super.onTriggered(strength);
            }
        };
    }
}
