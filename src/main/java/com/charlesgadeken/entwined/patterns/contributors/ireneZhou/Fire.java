package com.charlesgadeken.entwined.patterns.contributors.ireneZhou;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BoundedParameter;
import java.util.ArrayList;
import java.util.List;

@LXCategory("Irene Zhou")
public class Fire extends EntwinedTriggerablePattern {
    final BoundedParameter maxHeight = new BoundedParameter("HEIGHT", 0.8, 0.3, 1);
    final BoundedParameter flameSize = new BoundedParameter("SIZE", 30, 10, 75);
    final BoundedParameter flameCount = new BoundedParameter("FLAMES", 75, 0, 75);
    final BoundedParameter hue = new BoundedParameter("HUE", 0, 0, 360);
    private LinearEnvelope fireHeight = new LinearEnvelope(0, 0, 500);

    private float height = 0;
    private int numFlames = 12;
    private List<Flame> flames;

    private class Flame {
        public float flameHeight = 0;
        public float theta = Utilities.random(0, 360);
        public LinearEnvelope decay = new LinearEnvelope(0, 0, 0);

        public Flame(float maxHeight, boolean groundStart) {
            float flameHeight;
            if (Utilities.random(1) > .2f) {
                flameHeight = Utilities.pow(Utilities.random(0, 1), 3) * maxHeight * 0.3f;
            } else {
                flameHeight = Utilities.pow(Utilities.random(0, 1), 3) * maxHeight;
            }
            decay.setRange(
                    model.yMin,
                    (model.yMax * 0.9f) * flameHeight,
                    Utilities.min(Utilities.max(200, 900 * flameHeight), 800));
            if (!groundStart) {
                decay.setBasis(Utilities.random(0, .8f));
            }
            addModulator(decay).start();
        }
    }

    public Fire(LX lx) {
        super(lx);

        patternMode = PATTERN_MODE_FIRED;

        addParameter(maxHeight);
        addParameter(flameSize);
        addParameter(flameCount);
        addParameter(hue);
        addModulator(fireHeight);

        flames = new ArrayList<Flame>(numFlames);
        for (int i = 0; i < numFlames; ++i) {
            flames.add(new Flame(height, false));
        }
    }

    public void updateNumFlames(int numFlames) {
        for (int i = flames.size(); i < numFlames; ++i) {
            flames.add(new Flame(height, false));
        }
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        if (!triggered && flames.size() == 0) {
            setCallRun(false);
        }

        if (!triggerableModeEnabled) {
            height = maxHeight.getValuef();
            numFlames =
                    (int) (flameCount.getValue() / 75 * 30); // Convert for backwards compatibility
        } else {
            height = fireHeight.getValuef();
        }

        if (flames.size() != numFlames) {
            updateNumFlames(numFlames);
        }
        for (int i = 0; i < flames.size(); ++i) {
            if (flames.get(i).decay.finished()) {
                removeModulator(flames.get(i).decay);
                if (flames.size() <= numFlames) {
                    flames.set(i, new Flame(height, true));
                } else {
                    flames.remove(i);
                    i--;
                }
            }
        }

        for (BaseCube cube : model.baseCubes) {
            float yn = (cube.transformedY - model.yMin) / model.yMax;
            float cBrt = 0;
            float cHue = 0;
            float flameWidth = flameSize.getValuef() / 2;
            for (int i = 0; i < flames.size(); ++i) {
                if (Utilities.abs(flames.get(i).theta - cube.getTransformedTheta())
                        < (flameWidth * (1 - yn))) {
                    cBrt =
                            Utilities.min(
                                    100,
                                    Utilities.max(
                                            0,
                                            Utilities.max(
                                                    cBrt,
                                                    (100
                                                                    - 2
                                                                            * Utilities.abs(
                                                                                    cube.transformedY
                                                                                            - flames.get(
                                                                                                            i)
                                                                                                    .decay
                                                                                                    .getValuef())
                                                                    - flames.get(i)
                                                                                    .decay
                                                                                    .getBasisf()
                                                                            * 25)
                                                            * Utilities.min(
                                                                    1,
                                                                    2
                                                                            * (1
                                                                                    - flames.get(i)
                                                                                            .decay
                                                                                            .getBasisf())))));
                    cHue = Utilities.max(0, (cHue + cBrt * 0.7f) * 0.5f);
                }
            }
            colors[cube.index] =
                    LX.hsb(
                            (cHue + hue.getValuef()) % 360,
                            100,
                            Utilities.min(
                                    100,
                                    cBrt
                                            + Utilities.pow(
                                                            Utilities.max(
                                                                    0, (height - 0.3f) / 0.7f),
                                                            0.5f)
                                                    * Utilities.pow(Utilities.max(0, 0.8f - yn), 2)
                                                    * 75));
        }
    }

    public void onTriggered(float strength) {
        super.onTriggered(strength);

        fireHeight.setRange(1, 0.6f);
        fireHeight.reset().start();
    };

    public void onRelease() {
        super.onRelease();

        fireHeight.setRange(height, 0);
        fireHeight.reset().start();
    }
}
