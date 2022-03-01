import heronarts.lx.LX;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import toxi.geom.Vec3D;
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
                    time / 20000 * 360 + 160 - 80 * brightnessValue / 100,
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
                colors[cube.index] = getColors(cube, fairyCirclesSwirlProgress[fairyCircle.index],
                        fairyCirclesColorOffset[fairyCircle.index]);
            }
        }
    }

    int getColors(BaseCube cube, double swirlProgress, double colorOffset) {
        return LX.hsb(
                (float) (cube.theta + colorOffset - time / 7000 * 360),
                100,
                50 + 50 * Utils.sin(
                        time / 2000 * Utils.TWO_PI + cube.theta * Utils.TWO_PI / 360));

        // + Utils.map(cube.y, model.yMin, model.yMax) * Utils.PI
        // +
        // (float) fairyCirclesSwirlProgress[shrub.index] * Utils.TWO_PI
        // (float) (100 * fairyCirclesSwirlProgress[fairyCircle.index]));
    }
}

class EG_DiscreteColors extends TSPattern {
    final BasicParameter hue1Param = new BasicParameter("HU1", 60, 1, 360);
    // final BasicParameter sat1Param = new BasicParameter("SAT1", 100, 0, 100);
    // final BasicParameter bright1Param = new BasicParameter("BRIGHT1", 100, 0,
    // 100);

    final BasicParameter hue2Param = new BasicParameter("HU2", 240, 1, 360);
    // final BasicParameter sat2Param = new BasicParameter("SAT2", 100, 0, 100);
    // final BasicParameter bright2Param = new BasicParameter("BRIGHT2", 100, 0,
    // 100);

    final BasicParameter hue3Param = new BasicParameter("HU3", 300, 1, 360);
    // final BasicParameter sat3Param = new BasicParameter("SAT3", 0, 0, 100);
    // final BasicParameter bright3Param = new BasicParameter("BRIGHT3", 0, 0, 100);

    final DiscreteParameter numberOfColorsParam = new DiscreteParameter("NUM", 2, 1, 4);

    final DiscreteParameter xRotationParam = new DiscreteParameter("PITCH", 0, -360, 360);
    final DiscreteParameter yRotationParam = new DiscreteParameter("YAW", 0, -360, 360);

    final BasicParameter adjustParam = new BasicParameter("ADJUST", 0, -1, 1);

    final Vec3D directionVector = new Vec3D();
    final Vec3D cubeUtilityVector = new Vec3D();
    final float degToRad = Utils.PI / 180;
    float minDistAlongVec;
    float maxDistAlongVec;

    // Each vector below is the furthest extent of the entire box containing the
    // cubes. See Octant on Wikipedia for this notation. P = positive or +, N =
    // negative or -.
    final Vec3D octantExtent_PPP = new Vec3D(model.xMax, model.yMax, model.zMax);
    final Vec3D octantExtent_PPN = new Vec3D(model.xMax, model.yMax, model.zMin);
    final Vec3D octantExtent_PNP = new Vec3D(model.xMax, model.yMin, model.zMax);
    final Vec3D octantExtent_PNN = new Vec3D(model.xMax, model.yMin, model.zMin);
    final Vec3D octantExtent_NPP = new Vec3D(model.xMin, model.yMax, model.zMax);
    final Vec3D octantExtent_NPN = new Vec3D(model.xMin, model.yMax, model.zMin);
    final Vec3D octantExtent_NNP = new Vec3D(model.xMin, model.yMin, model.zMax);
    final Vec3D octantExtent_NNN = new Vec3D(model.xMin, model.yMin, model.zMin);

    EG_DiscreteColors(LX lx) {
        super(lx);

        addParameter(hue1Param);
        // addParameter(sat1Param);
        // addParameter(bright1Param);

        addParameter(hue2Param);
        // addParameter(sat2Param);
        // addParameter(bright2Param);

        addParameter(hue3Param);
        // addParameter(sat3Param);
        // addParameter(bright3Param);

        addParameter(numberOfColorsParam);

        addParameter(xRotationParam);
        addParameter(yRotationParam);

        addParameter(adjustParam);

        updateDirectionVector();
    }

    @Override
    public void onParameterChanged(LXParameter param) {
        super.onParameterChanged(param);
        if (param == xRotationParam || param == yRotationParam) {
            updateDirectionVector();
        }
    }

    void updateDirectionVector() {
        // Default direction points up
        directionVector.set(Vec3D.Y_AXIS);
        directionVector.rotateX(xRotationParam.getValuef() * degToRad);
        directionVector.rotateY(yRotationParam.getValuef() * degToRad);

        minDistAlongVec = Float.POSITIVE_INFINITY;
        maxDistAlongVec = Float.NEGATIVE_INFINITY;
        for (BaseCube cube : model.baseCubes) {
            // The following requires the direction vector to be a unit vector
            float cubeDistAlongVec = getCubeDistAlongVec(cube);
            minDistAlongVec = Math.min(minDistAlongVec, cubeDistAlongVec);
            maxDistAlongVec = Math.max(maxDistAlongVec, cubeDistAlongVec);
        }
    }

    float getCubeDistAlongVec(BaseCube cube) {
        cubeUtilityVector.set(cube.x, cube.y, cube.z);
        return cubeUtilityVector.dot(directionVector);
    }

    @Override
    protected void run(double deltaMs) {
        for (BaseCube cube : model.baseCubes) {
            // The following requires the direction vector to be a unit vector
            float cubeDistAlongVec = getCubeDistAlongVec(cube);

            // Floor the lerp result to get the zero-indexed value of which color this cube
            // should be
            int colorIndex = (int) Utils.lerp(
                    0,
                    numberOfColorsParam.getValuef(),
                    Utils.map(
                            cubeDistAlongVec,
                            minDistAlongVec,
                            maxDistAlongVec)
                            + adjustParam.getValuef());

            float hue;
            // float sat;
            // float bright;
            if (numberOfColorsParam.getValuei() > 2 && colorIndex >= 2) {
                hue = hue3Param.getValuef();
                // sat = sat3Param.getValuef();
                // bright = bright3Param.getValuef();
            } else if (numberOfColorsParam.getValuei() > 1 && colorIndex >= 1) {
                hue = hue2Param.getValuef();
                // sat = sat2Param.getValuef();
                // bright = bright2Param.getValuef();
            } else {
                hue = hue1Param.getValuef();
                // sat = sat1Param.getValuef();
                // bright = bright1Param.getValuef();
            }

            colors[cube.index] = LX.hsb(
                    hue,
                    100,
                    100);
        }
    }
}
