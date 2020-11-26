package com.charlesgadeken.entwined.patterns.contributors.ireneZhou;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.model.BaseCube;
import heronarts.lx.LX;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.utils.LXUtils;
import java.util.ArrayList;
import java.util.List;

public class Bubbles extends EntwinedTriggerablePattern {
    final DiscreteParameter ballCount = new DiscreteParameter("NUM", 10, 1, 150);
    final BoundedParameter maxRadius = new BoundedParameter("RAD", 50, 5, 100);
    final BoundedParameter speed = new BoundedParameter("SPEED", 1, 0, 5);
    final BoundedParameter hue = new BoundedParameter("HUE", 0, 0, 360);
    private LinearEnvelope decay = new LinearEnvelope(0, 0, 2000);
    private int numBubbles = 0;
    private List<Bubble> bubbles;

    private class Bubble {
        public float theta = 0;
        public float yPos = 0;
        public float bHue = 0;
        public float baseSpeed = 0;
        public float radius = 0;

        public Bubble(float maxRadius) {
            theta = Utilities.random(0, 360);
            bHue = Utilities.random(0, 30);
            baseSpeed = Utilities.random(2, 5);
            radius = Utilities.random(5, maxRadius);
            yPos = model.yMin - radius * Utilities.random(1, 10);
        }

        public void move(float speed) {
            yPos += baseSpeed * speed;
        }
    }

    public Bubbles(LX lx) {
        super(lx);

        patternMode = PATTERN_MODE_FIRED;

        addParameter(ballCount);
        addParameter(maxRadius);
        addParameter(speed);
        addParameter(hue);
        addModulator(decay);

        bubbles = new ArrayList<Bubble>(numBubbles);
        for (int i = 0; i < numBubbles; ++i) {
            bubbles.add(new Bubble(maxRadius.getValuef()));
        }
    }

    public void addBubbles(int numBubbles) {
        for (int i = bubbles.size(); i < numBubbles; ++i) {
            bubbles.add(new Bubble(maxRadius.getValuef()));
        }
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        if (!triggered && bubbles.size() == 0) {
            setCallRun(false);
        }

        for (BaseCube cube : model.baseCubes) {
            colors[cube.index] = LX.hsb(0, 0, 0);
        }
        if (!triggerableModeEnabled) {
            numBubbles = ballCount.getValuei();
        } else {
            numBubbles = (int) decay.getValuef();
        }

        if (bubbles.size() < numBubbles) {
            addBubbles(numBubbles);
        }

        for (int i = 0; i < bubbles.size(); ++i) {
            if (bubbles.get(i).yPos
                    > model.yMax + bubbles.get(i).radius) { // bubble is now off screen
                if (numBubbles < bubbles.size()) {
                    bubbles.remove(i);
                    i--;
                } else {
                    bubbles.set(i, new Bubble(maxRadius.getValuef()));
                }
            }
        }

        for (Bubble bubble : bubbles) {
            for (BaseCube cube : model.baseCubes) {
                if (Utilities.abs(bubble.theta - cube.transformedTheta) < bubble.radius
                        && Utilities.abs(bubble.yPos - (cube.transformedY - model.yMin))
                                < bubble.radius) {

                    float distTheta =
                            LXUtils.wrapdistf(bubble.theta, cube.transformedTheta, 360) * 0.8f;
                    float distY = bubble.yPos - (cube.transformedY - model.yMin);
                    float distSq = distTheta * distTheta + distY * distY;

                    if (distSq < bubble.radius * bubble.radius) {
                        float dist = Utilities.sqrt(distSq);
                        colors[cube.index] =
                                lx.hsb(
                                        (bubble.bHue + hue.getValuef()) % 360,
                                        50 + dist / bubble.radius * 50,
                                        Utilities.constrain(
                                                cube.transformedY / model.yMax * 125
                                                        - 50 * (dist / bubble.radius),
                                                0,
                                                100));
                    }
                }
            }

            bubble.move(speed.getValuef() * (float) deltaMs * 60 / 1000);
        }
    }

    public void onTriggered(float strength) {
        super.onTriggered(strength);

        numBubbles += 25;
        decay.setRange(numBubbles, 10);
        decay.reset().start();
    }

    public void onRelease() {
        super.onRelease();

        decay.setRange(numBubbles, 0);
        decay.reset().start();
    }
}
