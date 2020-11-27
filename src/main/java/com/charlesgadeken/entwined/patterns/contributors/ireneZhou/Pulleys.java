package com.charlesgadeken.entwined.patterns.contributors.ireneZhou;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.model.BaseCube;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.Click;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import java.util.ArrayList;
import java.util.List;

@LXCategory("Irene Zhou")
public class Pulleys extends EntwinedTriggerablePattern { // ported from SugarCubes
    private BoundedParameter sz = new BoundedParameter("SIZE", 0.5);
    private BoundedParameter beatAmount = new BoundedParameter("BEAT", 0);
    private BooleanParameter automated = new BooleanParameter("AUTO", true);
    private BoundedParameter speed = new BoundedParameter("SPEED", 1, -3, 3);
    final DiscreteParameter pulleyCount = new DiscreteParameter("NUM", 1, 1, 5);
    private Click dropPulley = new Click(4000);

    private boolean isRising = false; // are the pulleys rising or falling
    boolean triggered = true; // has the trigger to rise/fall been pulled
    boolean autoMode = true; // triggerMode vs autoMode.
    private int numPulleys = 0;
    private List<Pulley> pulleys = new ArrayList<>(numPulleys);

    private class Pulley {
        public float baseSpeed = 0;
        public Click delay = new Click(0);
        public Click turnOff = new Click(0);
        public final Accelerator gravity = new Accelerator(0, 0, 0);
        public float baseHue = 0;
        public LinearEnvelope maxBrt = new LinearEnvelope(0, 0, 0);

        public Pulley() {
            baseSpeed = Utilities.random(10, 50);
            baseHue = Utilities.random(0, 30);
            delay.setPeriod(Utilities.random(0, 500));
            gravity.setSpeed(this.baseSpeed, 0);
            if (autoMode) {
                maxBrt.setRange(0, 1, 3000);
            } else {
                maxBrt.setRange(0.5f, 1, 3000);
            }

            turnOff.setPeriod(6000);
            addModulator(gravity);
            addModulator(delay);
            addModulator(maxBrt).start();
            addModulator(turnOff);
        }
    }

    public Pulleys(LX lx) {
        super(lx);

        patternMode = PATTERN_MODE_FIRED;

        addParameter(sz);
        addParameter(beatAmount);
        addParameter(speed);
        addParameter(automated);
        addParameter(pulleyCount);
        onParameterChanged(speed);
        addModulator(dropPulley);

        for (int i = 0; i < numPulleys; i++) {
            pulleys.add(new Pulley());
        }
    }

    public void onParameterChanged(LXParameter parameter) {
        super.onParameterChanged(parameter);
        if (parameter == speed && isRising) {
            for (int i = 0; i < pulleys.size(); i++) {
                pulleys.get(i).gravity.setVelocity(pulleys.get(i).baseSpeed * speed.getValuef());
            }
        }
        if (parameter == automated) {
            if (automated.isOn()) {
                trigger();
            }
        }
    }

    private void trigger() {
        if (autoMode) {
            isRising = !isRising;
        }
        for (int j = 0; j < pulleys.size(); j++) {
            if (isRising) {
                pulleys.get(j).gravity.setSpeed(pulleys.get(j).baseSpeed, 0).start();
            } else {
                pulleys.get(j).gravity.setVelocity(0).setAcceleration(-420);
                pulleys.get(j).delay.trigger();
            }
        }
    }

    public void run(double deltaMS) {
        if (getChannel().fader.getNormalized() == 0) return;

        if (!triggered && pulleys.size() == 0) {
            setCallRun(false);
        }

        if (autoMode) {
            numPulleys = pulleyCount.getValuei();

            if (numPulleys < pulleys.size()) {
                for (int i = numPulleys; i < pulleys.size(); i++) {
                    pulleys.get(i).maxBrt.start(); // fadeOut then delete
                }
            }
        } else {
            if (dropPulley.click()) {
                numPulleys += 1;
            }
        }

        if (numPulleys > pulleys.size()) {
            addPulleys(numPulleys);
        }

        for (int i = 0; i < pulleys.size(); i++) {
            if (pulleys.get(i).maxBrt.finished()) {
                if (pulleys.get(i).maxBrt.getValuef() == 1) {
                    pulleys.get(i).maxBrt.setRange(1, 0, 3000).reset();
                } else {
                    removePulley(i);
                    numPulleys -= 1;
                }
            }
        }

        for (int i = 0; i < pulleys.size(); i++) {
            if (pulleys.get(i).turnOff.click()) {
                pulleys.get(i).maxBrt.start();
            }
        }

        if (triggered) {
            if (!isRising) {
                for (int j = 0; j < pulleys.size(); ++j) {
                    if (pulleys.get(j).delay.click()) {
                        pulleys.get(j).gravity.start();
                        pulleys.get(j).delay.stop();
                    }
                    if (pulleys.get(j).gravity.getValuef() < 0) { // bouncebounce
                        pulleys.get(j).gravity.setValue(-pulleys.get(j).gravity.getValuef());
                        pulleys.get(j)
                                .gravity
                                .setVelocity(
                                        -pulleys.get(j).gravity.getVelocityf()
                                                * Utilities.random(0.74f, 0.84f));
                    }
                }
            }

            float fPos = 1 - lx.engine.tempo.rampf();
            if (fPos < .2f) {
                fPos = .2f + 4 * (.2f - fPos);
            }

            float falloff = 100.f / (3 + sz.getValuef() * 36 + fPos * beatAmount.getValuef() * 48);
            for (BaseCube cube : model.baseCubes) {
                float cBrt = 0;
                float cHue = 0;
                for (int j = 0; j < pulleys.size(); ++j) {
                    cHue =
                            (lx.engine.palette.getHuef()
                                            + Utilities.abs(cube.x - model.cx) * .8f
                                            + cube.transformedY * .4f
                                            + pulleys.get(j).baseHue)
                                    % 360;
                    cBrt +=
                            Utilities.max(
                                    0,
                                    pulleys.get(j).maxBrt.getValuef()
                                            * (100
                                                    - Utilities.abs(
                                                                    cube.transformedY / 2
                                                                            - 50
                                                                            - pulleys.get(j)
                                                                                    .gravity
                                                                                    .getValuef())
                                                            * falloff));
                }
                float yn = cube.transformedY / model.yMax;
                colors[cube.index] =
                        lx.hsb(
                                cHue,
                                Utilities.constrain(100 * (0.8f - yn * yn), 0, 100),
                                Utilities.min(100, cBrt));
            }
        }
    }

    public void addPulleys(int numPulleys) {
        for (int i = pulleys.size(); i < numPulleys; ++i) {
            Pulley newPulley = new Pulley();
            if (isRising) {
                newPulley.gravity.setSpeed(newPulley.baseSpeed, 0).start();
            } else {
                if (autoMode) {
                    newPulley.gravity.setValue(Utilities.random(0, 225));
                } else {
                    newPulley.gravity.setValue(250);
                    newPulley.turnOff.start();
                }

                newPulley.gravity.setVelocity(0).setAcceleration(-420);
                newPulley.delay.trigger();
            }
            pulleys.add(newPulley);
        }
    }

    public void removePulley(int index) {
        Pulley pulley = pulleys.remove(index);
        removeModulator(pulley.turnOff);
        removeModulator(pulley.gravity);
        removeModulator(pulley.maxBrt);
    }

    public void onTriggerableModeEnabled() {
        super.onTriggerableModeEnabled();
        autoMode = false;
        isRising = false;
    }

    public void onTriggered(float strength) {
        numPulleys += 1;
        dropPulley.start();
    }

    public void onRelease() {
        dropPulley.stop();
        dropPulley.reset();
    }
}
