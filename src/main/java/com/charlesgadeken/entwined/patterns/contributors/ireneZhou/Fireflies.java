package com.charlesgadeken.entwined.patterns.contributors.ireneZhou;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.model.BaseCube;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.utils.LXUtils;
import java.util.ArrayList;
import java.util.List;
import toxi.geom.Vec2D;

public class Fireflies extends EntwinedTriggerablePattern {
    final DiscreteParameter flyCount = new DiscreteParameter("NUM", 20, 1, 100);
    final BoundedParameter speed = new BoundedParameter("SPEED", 1, 0, 7.5);
    final BoundedParameter hue = new BoundedParameter("HUE", 0, 0, 360);
    private float radius = 40;
    private int numFireflies = 0;
    private List<Firefly> fireflies;
    private List<Firefly> queue;
    private SinLFO[] blinkers = new SinLFO[10];
    private LinearEnvelope decay = new LinearEnvelope(0, 0, 3000);

    private class Firefly {
        public float theta = 0;
        public float yPos = 0;
        public Vec2D velocity = new Vec2D(0, 0);
        public float radius = 0;
        public int blinkIndex = 0;

        public Firefly() {
            theta = Utilities.random(0, 360);
            yPos = Utilities.random(model.yMin, model.yMax);
            velocity = new Vec2D(Utilities.random(-1, 1), Utilities.random(0.25f, 1));
            radius = 30;
            blinkIndex = (int) Utilities.random(0, blinkers.length);
        }

        public void move(float speed) {
            theta = (theta + speed * velocity.x) % 360;
            yPos += speed * velocity.y;
        }
    }

    public Fireflies(LX lx) {
        this(lx, 20, 1, 0);
    }

    public Fireflies(LX lx, int initial_flyCount, float initial_speed, float initial_hue) {
        super(lx);

        patternMode = PATTERN_MODE_FIRED;

        addParameter(flyCount);
        addParameter(speed);
        addParameter(hue);
        addModulator(decay);

        flyCount.setValue(initial_flyCount);
        speed.setValue(initial_speed);
        hue.setValue(initial_hue);

        for (int i = 0; i < blinkers.length; ++i) {
            blinkers[i] = new SinLFO(0, 75, 1000 * Utilities.random(1.0f, 3.0f));
            addModulator(blinkers[i]).setValue(Utilities.random(0, 50)).start();
        }

        fireflies = new ArrayList<>(numFireflies);
        queue = new ArrayList<>();
        for (int i = 0; i < numFireflies; ++i) {
            fireflies.add(new Firefly());
        }
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        if (!isTriggered() && fireflies.size() == 0) {
            setCallRun(false);
        }

        for (BaseCube cube : model.baseCubes) {
            colors[cube.index] = lx.hsb(0, 0, 0);
        }

        if (triggerableModeEnabled) {
            numFireflies = (int) decay.getValuef();
        } else {
            numFireflies = flyCount.getValuei();
        }

        if (fireflies.size() < numFireflies) {
            for (int i = 0; i < numFireflies - fireflies.size(); ++i) {
                queue.add(new Firefly());
            }
        }

        for (int i = 0;
                i < queue.size();
                ++i) { // only add fireflies when they're about to blink on
            if (blinkers[queue.get(i).blinkIndex].getValuef() > 70) {
                fireflies.add(queue.remove(i));
            }
        }

        for (int i = 0; i < fireflies.size(); ++i) { // remove fireflies while blinking off
            if (numFireflies < fireflies.size()) {
                if (blinkers[fireflies.get(i).blinkIndex].getValuef() > 70) {
                    fireflies.remove(i);
                }
            }
        }

        for (int i = 0; i < fireflies.size(); ++i) {
            if (fireflies.get(i).yPos > model.yMax + radius) {
                fireflies.get(i).yPos = model.yMin - radius;
            }
        }

        for (Firefly fly : fireflies) {
            for (BaseCube cube : model.baseCubes) {
                if (Utilities.abs(fly.yPos - cube.transformedY) <= radius
                        && Utilities.abs(fly.theta - cube.transformedTheta) <= radius) {
                    float distSq =
                            Utilities.pow(
                                            (LXUtils.wrapdistf(
                                                    fly.theta, cube.transformedTheta, 360)),
                                            2)
                                    + Utilities.pow(fly.yPos - cube.transformedY, 2);
                    float brt =
                            Utilities.max(
                                    0,
                                    100
                                            - Utilities.sqrt(distSq * 4)
                                            - blinkers[fly.blinkIndex].getValuef());
                    if (brt > LXColor.b(colors[cube.index])) {
                        colors[cube.index] =
                                lx.hsb(
                                        (lx.engine.palette.getHuef() + hue.getValuef()) % 360,
                                        100 - brt,
                                        brt);
                    }
                }
            }
        }

        for (Firefly firefly : fireflies) {
            firefly.move(speed.getValuef() * (float) deltaMs * 60 / 1000);
        }
    }

    public void onTriggered(float strength) {
        super.onTriggered(strength);

        numFireflies += 25;
        decay.setRange(numFireflies, 10);
        decay.reset().start();
    }

    public void onRelease() {
        super.onRelease();

        decay.setRange(numFireflies, 0);
        decay.reset().start();
    }
}
