package com.charlesgadeken.entwined.patterns.contributors.raySykes;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.model.Cube;
import com.charlesgadeken.entwined.model.Shrub;
import com.charlesgadeken.entwined.model.ShrubCube;
import com.charlesgadeken.entwined.model.Tree;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;

public class Lightning extends EntwinedTriggerablePattern {
    private LightningLine[] bolts = new LightningLine[model.trees.size() + model.shrubs.size()];
    final BoundedParameter boltAngle = new BoundedParameter("Angle", 35, 0, 55);
    final BoundedParameter propagationSpeed = new BoundedParameter("Speed", 10, 0.5, 20);
    final BoundedParameter maxBoltWidth = new BoundedParameter("Width", 60, 20, 150);
    final BoundedParameter lightningChance = new BoundedParameter("Chance", 5, 1, 10);
    final BoundedParameter forkingChance = new BoundedParameter("Fork", 3, 1, 10);
    final BooleanParameter firesOnBeat = new BooleanParameter("Beat");
    int[] randomCheckTimeOuts = new int[model.trees.size() + model.shrubs.size()];

    public Lightning(LX lx) {
        super(lx);

        patternMode = PATTERN_MODE_FIRED;
        for (int i = 0; i < (model.trees.size() + model.shrubs.size()); i++) {
            bolts[i] = makeBolt();
            randomCheckTimeOuts[i] = 0;
        }
        addParameter(boltAngle);
        addParameter(propagationSpeed);
        addParameter(maxBoltWidth);
        addParameter(lightningChance);
        addParameter(forkingChance);
        addParameter(firesOnBeat);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        int treeIndex = 0;

        if (!triggered) {
            boolean running = false;
            for (Tree tree : model.trees) {
                if (!bolts[treeIndex].isDead()) {
                    running = true;
                    break;
                }
                treeIndex++;
            }
            if (!running) {
                setCallRun(false);
            }
        }

        treeIndex = 0;
        for (Tree tree : model.trees) {
            if (triggered) {
                if (bolts[treeIndex].isDead()) {
                    if (firesOnBeat.isOn()) {
                        if (lx.engine.tempo.beat()) {
                            randomCheckTimeOuts[treeIndex] = Utilities.millis() + 100;
                            bolts[treeIndex] = makeBolt();
                        }
                    } else {
                        if (randomCheckTimeOuts[treeIndex] < Utilities.millis()) {
                            randomCheckTimeOuts[treeIndex] = Utilities.millis() + 100;
                            if (Utilities.random(15) < lightningChance.getValuef()) {
                                bolts[treeIndex] = makeBolt();
                            }
                        }
                    }
                }
            }
            for (Cube cube : tree.cubes) {
                float hueVal = 300;
                float lightningFactor =
                        bolts[treeIndex].getLightningFactor(
                                cube.transformedY, cube.transformedTheta);
                float brightVal = lightningFactor;
                float satVal;
                if (lightningFactor < 20) {
                    hueVal = 300;
                    satVal = 100;
                } else if (lightningFactor < 50) {
                    hueVal = 280;
                    satVal = 100;
                } else {
                    hueVal = 280;
                    satVal = 100 - 2 * (lightningFactor - 50);
                }
                colors[cube.index] = lx.hsb(hueVal, satVal, brightVal);
            }
            treeIndex++;
        }

        int shrubIndex = model.trees.size();

        if (!triggered) {
            boolean running = false;
            for (Shrub shrub : model.shrubs) {
                if (!bolts[shrubIndex].isDead()) {
                    running = true;
                    break;
                }
                shrubIndex++;
            }
            if (!running) {
                setCallRun(false);
            }
        }

        shrubIndex = model.trees.size();
        for (Shrub shrub : model.shrubs) {
            if (triggered) {
                if (bolts[shrubIndex].isDead()) {
                    if (firesOnBeat.isOn()) {
                        if (lx.engine.tempo.beat()) {
                            randomCheckTimeOuts[shrubIndex] = Utilities.millis() + 100;
                            bolts[shrubIndex] = makeBolt();
                        }
                    } else {
                        if (randomCheckTimeOuts[shrubIndex] < Utilities.millis()) {
                            randomCheckTimeOuts[shrubIndex] = Utilities.millis() + 100;
                            if (Utilities.random(15) < lightningChance.getValuef()) {
                                bolts[shrubIndex] = makeBolt();
                            }
                        }
                    }
                }
            }
            for (ShrubCube cube : shrub.cubes) {
                float hueVal = 300;
                float lightningFactor =
                        bolts[shrubIndex].getLightningFactor(
                                cube.transformedY, cube.transformedTheta);
                float brightVal = lightningFactor;
                float satVal;
                if (lightningFactor < 20) {
                    hueVal = 300;
                    satVal = 100;
                } else if (lightningFactor < 50) {
                    hueVal = 280;
                    satVal = 100;
                } else {
                    hueVal = 280;
                    satVal = 100 - 2 * (lightningFactor - 50);
                }
                colors[cube.index] = lx.hsb(hueVal, satVal, brightVal);
            }
            shrubIndex++;
        }
    }

    LightningLine makeBolt() {
        float theta = 45 * (int) Utilities.random(8);
        float boltWidth =
                (maxBoltWidth.getValuef() + Utilities.random(maxBoltWidth.getValuef())) / 2;
        return new LightningLine(
                Utilities.millis(),
                550,
                theta,
                boltAngle.getValuef(),
                propagationSpeed.getValuef(),
                boltWidth,
                3,
                forkingChance.getValuef());
    }

    public void onTriggered(float strength) {
        super.onTriggered(strength);

        propagationSpeed.setNormalized(strength);

        int treeIndex = 0;

        for (Tree tree : model.trees) {
            if (bolts[treeIndex].isDead()) {
                randomCheckTimeOuts[treeIndex] = Utilities.millis() + 100;
                bolts[treeIndex] = makeBolt();
            }
            treeIndex++;
        }
        int shrubIndex = model.trees.size();

        for (Shrub shrub : model.shrubs) {
            if (bolts[shrubIndex].isDead()) {
                randomCheckTimeOuts[shrubIndex] = Utilities.millis() + 100;
                bolts[shrubIndex] = makeBolt();
            }
            shrubIndex++;
        }
    }
}
