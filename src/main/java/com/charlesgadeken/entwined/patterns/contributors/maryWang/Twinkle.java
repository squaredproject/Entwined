package com.charlesgadeken.entwined.patterns.contributors.maryWang;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.Cube;
import com.charlesgadeken.entwined.model.ShrubCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Mary Wang")
public class Twinkle extends EntwinedBasePattern {

    private SinLFO[] bright;
    final BoundedParameter brightnessParam = new BoundedParameter("Brightness", 0.8, 0.5, 1);
    final int numBrights = 18;
    final int density = 20;
    int[] sparkleTimeOuts;
    int[] cubeToModulatorMapping;

    public Twinkle(LX lx) {
        super(lx);
        addParameter("maryWang/twinkle/brightness", brightnessParam);

        sparkleTimeOuts = new int[model.cubes.size() + model.shrubCubes.size()];
        cubeToModulatorMapping = new int[model.cubes.size() + model.shrubCubes.size()];

        for (int i = 0; i < cubeToModulatorMapping.length; i++) {
            cubeToModulatorMapping[i] = (int) Utilities.random(numBrights);
        }

        bright = new SinLFO[numBrights];
        int numLight =
                density / 100 * bright.length; // number of brights array that are most bright
        int numDarkReverse =
                (bright.length - numLight)
                        / 2; // number of brights array that go from light to dark

        for (int i = 0; i < bright.length; i++) {
            if (i <= numLight) {
                if (Utilities.random(1) < 0.5f) {
                    bright[i] =
                            new SinLFO(
                                    (int) Utilities.random(80, 100),
                                    0,
                                    (int) Utilities.random(2300, 7700));
                } else {
                    bright[i] =
                            new SinLFO(
                                    0,
                                    (int) Utilities.random(70, 90),
                                    (int) Utilities.random(5300, 9200));
                }
            } else if (i < numDarkReverse) {
                bright[i] =
                        new SinLFO(
                                (int) Utilities.random(50, 70),
                                0,
                                (int) Utilities.random(3300, 11300));
            } else {
                bright[i] =
                        new SinLFO(
                                0,
                                (int) Utilities.random(30, 80),
                                (int) Utilities.random(3100, 9300));
            }
            addModulator(bright[i]).start();
        }
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (Cube cube : model.cubes) {
            if (sparkleTimeOuts[cube.index] < Utilities.millis()) {
                // randomly change modulators
                if (Utilities.random(10) <= 3) {
                    cubeToModulatorMapping[cube.index] = (int) Utilities.random(numBrights);
                }
                sparkleTimeOuts[cube.index] =
                        Utilities.millis() + (int) Utilities.random(11100, 23300);
            }
            colors[cube.index] =
                    LX.hsb(
                            0,
                            0,
                            bright[cubeToModulatorMapping[cube.index]].getValuef()
                                    * brightnessParam.getValuef());
        }

        for (ShrubCube cube : model.shrubCubes) {
            if (sparkleTimeOuts[cube.index] < Utilities.millis()) {
                // randomly change modulators
                if (Utilities.random(10) <= 3) {
                    cubeToModulatorMapping[cube.index] = (int) Utilities.random(numBrights);
                }
                sparkleTimeOuts[cube.index] =
                        Utilities.millis() + (int) Utilities.random(11100, 23300);
            }
            colors[cube.index] =
                    LX.hsb(
                            0,
                            0,
                            bright[cubeToModulatorMapping[cube.index]].getValuef()
                                    * brightnessParam.getValuef());
        }
    }
}
