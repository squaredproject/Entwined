import heronarts.lx.LX;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import toxi.math.noise.PerlinNoise;

class EG_Template extends TSPattern {
    EG_Template(LX lx) {
        super(lx);
    }

    @Override
    protected void run(double deltaMs) {
        for (BaseCube cube : model.baseCubes) {
            colors[cube.index] = LX.hsb(
                    cube.theta,
                    100,
                    100);
        }
    }
}

class EG_UpDown extends TSPattern {
    float time = 0;
    // All values below are on the scale from zero to one.
    // std dev of the gaussian function that determines the thickness of the line
    final double deviation = 0.05;
    final double minLineCenterY = 0 - 2 * deviation;
    final double maxLineCenterY = 1 + 2 * deviation;
    final SinLFO upDownModulator = new SinLFO(minLineCenterY, maxLineCenterY, 5000);

    EG_UpDown(LX lx) {
        super(lx);
        addModulator(upDownModulator).start();
    }

    // Returns the gaussian for that value, based on center and deviation.
    // Everything is based on a zero-to-one scale.
    float gaussian(float value, float center) {
        return (float) Math.exp(-Math.pow(value - center, 2) / twoDeviationSquared);
    }

    final double twoDeviationSquared = 2 * deviation * deviation;

    @Override
    protected void run(double deltaMs) {
        float scanHeight = upDownModulator.getValuef();
        for (BaseCube cube : model.baseCubes) {
            colors[cube.index] = LX.hsb(
                    cube.theta + time / 6000 * 360,
                    100,
                    100 * gaussian(Utils.map(cube.y, model.yMin, model.yMax), scanHeight));
        }
    }
}

class EG_Radar extends TSPattern {
    float time = 0;
    // All values below are on the scale from zero to one.
    // std dev of the gaussian function that determines the thickness of the line
    final double deviation = 0.03;
    final double minSweepCenter = -0.4;
    final double maxSweepCenter = 1.6;
    final double periodMs = 3000;
    final SawLFO radarSweepModulator = new SawLFO(minSweepCenter, maxSweepCenter,
            periodMs);
    final float[] detectedBrightness;

    double previousSweepPosition = minSweepCenter;

    EG_Radar(LX lx) {
        super(lx);
        addModulator(radarSweepModulator).start();

        detectedBrightness = new float[model.baseCubes.size()];
    }

    // Returns the gaussian for that value, based on center and deviation.
    // Everything is based on a zero-to-one scale.
    float gaussian(float value, float center) {
        return (float) Math.exp(-Math.pow(value - center, 2) / twoDeviationSquared);
    }

    final double twoDeviationSquared = 2 * deviation * deviation;

    @Override
    protected void run(double deltaMs) {
        time += deltaMs;
        float sweepPosition = radarSweepModulator.getValuef();
        for (BaseCube cube : model.baseCubes) {
            float mappedCubeZ = 1 - Utils.map(cube.z, model.zMin, model.zMax);
            if (previousSweepPosition < mappedCubeZ && sweepPosition > mappedCubeZ) {
                // Sweep just passed the cube, randomly set whether this cube is "detected"
                if (Math.random() > 0.95) {
                    detectedBrightness[cube.index] = 1;
                }
            }

            float brightnessValue = Math.max(detectedBrightness[cube.index] * 100,
                    10 + gaussian(mappedCubeZ, sweepPosition) * 60);

            colors[cube.index] = LX.hsb(
                    time / 4000 + 160 - 80 * brightnessValue / 100,
                    100 - 30 * brightnessValue / 100,
                    brightnessValue);

            // This was my original math for exponential decay but it's dependent on the
            // framerate being 60fps. I
            // replaced it with the below "exp" method to work with any framerate.
            // detectedBrightness[cube.index] = detectedBrightness[cube.index] * 0.99f;

            // TODO make the 0.75 a parameter from 0.5 to 2, default val 0.75
            detectedBrightness[cube.index] = (float) Math.exp(Math.log(detectedBrightness[cube.index])
                    - deltaMs / (periodMs * 0.5));
        }
        previousSweepPosition = sweepPosition;
    }
}

class EG_CounterSpin extends TSPattern {
    float time = 0;
    final PerlinNoise perlinNoise = new PerlinNoise();
    // Each value will count down from one to zero.
    final double[] treesSwirlProgress;
    final double[] treesColorOffset;
    final double[] shrubsSwirlProgress;
    final double[] shrubsColorOffset;
    final double[] fairyCirclesSwirlProgress;
    final double[] fairyCirclesColorOffset;
    final double swirlDurationMs = 2000;
    final double progressSpeed = 1 / swirlDurationMs;

    EG_CounterSpin(LX lx) {
        super(lx);
        treesSwirlProgress = new double[model.trees.size()];
        treesColorOffset = new double[model.trees.size()];
        shrubsSwirlProgress = new double[model.shrubs.size()];
        shrubsColorOffset = new double[model.shrubs.size()];
        fairyCirclesSwirlProgress = new double[model.fairyCircles.size()];
        fairyCirclesColorOffset = new double[model.fairyCircles.size()];

        for (Tree tree : model.trees) {
            treesColorOffset[tree.index] = Utils.random(360);
        }
        for (Shrub shrub : model.shrubs) {
            shrubsColorOffset[shrub.index] = Utils.random(360);
        }
        for (FairyCircle fairyCircle : model.fairyCircles) {
            fairyCirclesColorOffset[fairyCircle.index] = Utils.random(360);
        }
    }

    @Override
    protected void run(double deltaMs) {
        time += deltaMs;
        for (Tree tree : model.trees) {
            // float a = perlinNoise.noise(tree.x, tree.z, (float) time);
            if (Math.random() > 0.99) {
                treesSwirlProgress[tree.index] = 1;
            } else {
                treesSwirlProgress[tree.index] = Math.max(0, treesSwirlProgress[tree.index] - deltaMs * progressSpeed);
            }
            for (BaseCube cube : tree.cubes) {
                colors[cube.index] = getColors(cube, treesSwirlProgress[tree.index], treesColorOffset[tree.index]);
            }
        }
        for (Shrub shrub : model.shrubs) {
            if (Math.random() > 0.99) {
                shrubsSwirlProgress[shrub.index] = 1;
            } else {
                shrubsSwirlProgress[shrub.index] = Math.max(0,
                        shrubsSwirlProgress[shrub.index] - deltaMs * progressSpeed);
            }
            for (BaseCube cube : shrub.cubes) {
                colors[cube.index] = getColors(cube, shrubsSwirlProgress[shrub.index], shrubsColorOffset[shrub.index]);
            }
        }
        for (FairyCircle fairyCircle : model.fairyCircles) {
            if (Math.random() > 0.99) {
                fairyCirclesSwirlProgress[fairyCircle.index] = 1;
            } else {
                fairyCirclesSwirlProgress[fairyCircle.index] = Math.max(0,
                        fairyCirclesSwirlProgress[fairyCircle.index] - deltaMs * progressSpeed);
            }
            for (BaseCube cube : fairyCircle.cubes) {
                colors[cube.index] = getColors(cube, fairyCirclesSwirlProgress[fairyCircle.index], fairyCirclesColorOffset[fairyCircle.index]);
            }
        }
    }

    int getColors(BaseCube cube, double swirlProgress, double colorOffset) {
        return LX.hsb(
                (float)(cube.theta + colorOffset - time / 4000 * 360),
                100,
                60 + 40 * Utils.sin(
                        time / 2000 * Utils.TWO_PI + cube.theta * Utils.TWO_PI / 360));

        // + Utils.map(cube.y, model.yMin, model.yMax) * Utils.PI
        // +
        // (float) fairyCirclesSwirlProgress[shrub.index] * Utils.TWO_PI
        // (float) (100 * fairyCirclesSwirlProgress[fairyCircle.index]));
    }
}
