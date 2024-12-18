package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import java.util.ArrayList;
import java.util.Iterator;

abstract class MultiObjectPattern<ObjectType extends MultiObject>
        extends EntwinedTriggerablePattern {

    BoundedParameter frequency;

    final boolean shouldAutofade;
    float fadeTime = 1000;

    final ArrayList<ObjectType> objects;
    double pauseTimerCountdown = 0;
    //  BoundedParameter fadeLength

    MultiObjectPattern(LX lx) {
        this(lx, true);
    }

    MultiObjectPattern(LX lx, double initial_frequency) {
        this(lx, true);
        frequency.setValue(initial_frequency);
    }

    MultiObjectPattern(LX lx, boolean shouldAutofade) {
        super(lx);

        patternMode = PATTERN_MODE_FIRED;

        frequency = getFrequencyParameter();
        addParameter(frequency);

        this.shouldAutofade = shouldAutofade;
        //    if (shouldAutofade) {

        objects = new ArrayList<ObjectType>();
    }

    BoundedParameter getFrequencyParameter() {
        return new BoundedParameter(
                "FREQ", .5, .1, 40); // TODO(meawoppl), BoundedParameter.Scaling.QUAD_IN);
    }

    //  BoundedParameter getAutofadeParameter() {
    //    return new BoundedParameter("TAIL",
    //  }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        if (triggered) {
            pauseTimerCountdown -= deltaMs;

            if (pauseTimerCountdown <= 0) {
                float delay = 1000 / frequency.getValuef();
                pauseTimerCountdown = Utilities.random(delay / 2) + delay * 3 / 4;
                makeObject(0);
            }
        } else if (objects.size() == 0) {
            setCallRun(false);
        }

        if (shouldAutofade) {
            for (BaseCube cube : model.baseCubes) {
                blendColor(
                        cube.index,
                        LX.hsb(0, 0, 100 * Utilities.max(0, (float) (1 - deltaMs / fadeTime))),
                        LXColor.Blend.MULTIPLY);
            }

        } else {
            clearColors();
        }

        if (objects.size() > 0) {
            Iterator<ObjectType> iter = objects.iterator();
            while (iter.hasNext()) {
                ObjectType object = iter.next();
                if (!object.running) {
                    layers.remove(object);
                    iter.remove();
                }
            }
        }
    }

    void makeObject(float strength) {
        ObjectType object = generateObject(strength);
        object.init();
        addLayer(object);
        objects.add(object);
    }

    public void onTriggered(float strength) {
        super.onTriggered(strength);

        makeObject(strength);
    }

    abstract ObjectType generateObject(float strength);
}
