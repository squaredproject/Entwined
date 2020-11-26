package com.charlesgadeken.entwined.patterns.contributors.ireneZhou;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.model.Cube;
import heronarts.lx.LX;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.Click;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;

public class Pulley extends EntwinedTriggerablePattern { //ported from SugarCubes
    final int NUM_DIVISIONS = 2;
    private final Accelerator[] gravity = new Accelerator[NUM_DIVISIONS];
    private final float[] baseSpeed = new float[NUM_DIVISIONS];
    private final Click[] delays = new Click[NUM_DIVISIONS];
    private final Click turnOff = new Click(9000);

    private boolean isRising = false;
    boolean triggered = true;
    float coil = 10;

    private BoundedParameter sz = new BoundedParameter("SIZE", 0.5);
    private BoundedParameter beatAmount = new BoundedParameter("BEAT", 0);
    private BooleanParameter automated = new BooleanParameter("AUTO", true);
    private BoundedParameter speed = new BoundedParameter("SPEED", 1, -3, 3);


    public Pulley(LX lx) {
        super(lx);

        patternMode = PATTERN_MODE_CUSTOM;

        for (int i = 0; i < NUM_DIVISIONS; ++i) {
            addModulator(gravity[i] = new Accelerator(0, 0, 0));
            addModulator(delays[i] = new Click(0));
        }
        addParameter(sz);
        addParameter(beatAmount);
        addParameter(speed);
        addParameter(automated);
        onParameterChanged(speed);
        addModulator(turnOff);
    }

    public void onParameterChanged(LXParameter parameter) {
        super.onParameterChanged(parameter);
        if (parameter == speed && isRising) {
            for (int i = 0; i < NUM_DIVISIONS; ++i) {
                gravity[i].setVelocity(baseSpeed[i] * speed.getValuef());
            }
        }
        if (parameter == automated) {
            if (automated.isOn()) {
                trigger();
            }
        }
    }

    private void trigger() {
        isRising = !isRising;
        int i = 0;
        for (int j = 0; j < NUM_DIVISIONS; ++j) {
            if (isRising) {
                baseSpeed[j] = Utilities.random(20, 33);
                gravity[j].setSpeed(baseSpeed[j], 0).start();
            }
            else {
                gravity[j].setVelocity(0).setAcceleration(-420);
                delays[j].setPeriod(Utilities.random(0, 500)).trigger();
            }
            ++i;
        }
    }

    public void run(double deltaMS) {
        if (getChannel().fader.getNormalized() == 0) return;

        if (turnOff.click()) {
            triggered = false;
            setColors(LX.hsb(0,0,0));
            turnOff.stop();
            turnOff.reset();
        }
        if(triggered) {
            if (!isRising) {
                int j = 0;
                for (Click d : delays) {
                    if (d.click()) {
                        gravity[j].start();
                        d.stop();
                    }
                    ++j;
                }
                for (Accelerator g : gravity) {
                    if (g.getValuef() < 0) { //bounce
                        g.setValue(-g.getValuef());
                        g.setVelocity(-g.getVelocityf() * Utilities.random(0.74f, 0.84f));
                    }
                }
            }

            float fPos = 1 -lx.engine.tempo.rampf();
            if (fPos < .2f) {
                fPos = .2f + 4 * (.2f - fPos);
            }

            float falloff = 100.f / (3 + sz.getValuef() * 36 + fPos * beatAmount.getValuef()*48);
            for (Cube cube : model.cubes) {
                int gi = (int) Utilities.constrain((cube.x - model.xMin) * NUM_DIVISIONS / (model.xMax - model.xMin), 0, NUM_DIVISIONS-1);
                float yn =  cube.transformedY/model.yMax;
                colors[cube.index] = LX.hsb(
                    (lx.engine.palette.getHuef() + Utilities.abs(cube.x - model.cx)*.8f + cube.transformedY*.4f) % 360,
                    Utilities.constrain(100 *(0.8f -  yn * yn), 0, 100),
                    Utilities.max(0, 100 - Utilities.abs(cube.transformedY/2 - 50 - gravity[gi].getValuef())*falloff)
                );
            }
        }
    }

    public void onTriggerableModeEnabled() {
        super.onTriggerableModeEnabled();
        triggered = false;
    }

    public void onTriggered(float strength) {
        triggered = true;
        isRising = true;
        turnOff.start();

        for (Accelerator g: gravity) {
            g.setValue(225);
        }
        trigger();
    }

    public void onRelease() {
    }
}
