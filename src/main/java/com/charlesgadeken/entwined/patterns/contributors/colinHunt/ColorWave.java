package com.charlesgadeken.entwined.patterns.contributors.colinHunt;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.cube.BaseCube;
import com.charlesgadeken.entwined.model.cube.Cube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Colin Hunt")
public class ColorWave extends EntwinedBasePattern {

    // Variable Declarations go here
    private float minx = Float.MAX_VALUE;
    private float maxx = -Float.MAX_VALUE;
    private float waveWidth = 1;
    private float speedMult = 1000;

    final BoundedParameter speedParam = new BoundedParameter("Speed", 5, 20, .01);
    final BoundedParameter waveSlope = new BoundedParameter("waveSlope", 360, 1, 720);
    final SawLFO wave = new SawLFO(0, 360, speedParam.getValuef() * speedMult);

    // add speed, wave width

    // Constructor and initial setup
    // Remember to use addParameter and addModulator if you're using Parameters or sin waves
    public ColorWave(LX lx) {
        super(lx);
        addModulator(wave).start();
        addParameter("colinHunt/colorWave/waveSlow", waveSlope);
        addParameter("colinHunt/colorWave/speedParam", speedParam);

        for (Cube cube : model.cubes) {
            if (cube.x < minx) {
                minx = cube.x;
            }
            if (cube.x > maxx) {
                maxx = cube.x;
            }
        }
    }

    @Override
    public void run(double deltaMs) {
        wave.setPeriod(speedParam.getValuef() * speedMult);

        // Use a for loop here to set the cube colors
        for (BaseCube cube : model.baseCubes) {
            colors[cube.index] =
                    LX.hsb(
                            (float)
                                    ((wave.getValuef()
                                                    + waveSlope.getValuef()
                                                            * Utilities.map(cube.x, minx, maxx))
                                            % 360),
                            100,
                            100);
        }
    }
}
