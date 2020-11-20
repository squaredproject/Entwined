package com.charlesgadeken.entwined.patterns.contributors.grantPatterson;

import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import toxi.geom.Plane;
import toxi.geom.Vec3D;
import toxi.math.noise.SimplexNoise;

@LXCategory("Grant Patterson")
public class Planes extends EntwinedBasePattern {
    // Random seed for our noise functions so it's different on every run
    double seed;
    // These offsets increase relative to deltaMs and speed parameters
    double positionOffset = 0;
    double rotationOffset = 0;
    double colorOffset = 0;

    // Number of planes
    final DiscreteParameter countParam = new DiscreteParameter("count", 3, 1, 10);
    // Rate of change of position, rotation, and color
    final BoundedParameter positionSpeedParam = new BoundedParameter("posSpd", 0.2, 0.01, 1);
    final BoundedParameter rotationSpeedParam = new BoundedParameter("rotSpd", 0.1, 0, 1);
    final BoundedParameter colorSpeedParam = new BoundedParameter("clrSpd", 0.2, 0.01, 1);
    // Width of each rendered plane
    final BoundedParameter sizeParam = new BoundedParameter("size", .5, .1, 5);
    // How different each plane is from the others in position, rotation, and color
    // (0 means all planes have the same position/rotation/color)
    final BoundedParameter positionVarianceParam = new BoundedParameter("posVar", 0.5, 0, 0.5);
    final BoundedParameter rotationVarianceParam = new BoundedParameter("rotVar", 0.5, 0, 0.5);
    final BoundedParameter colorVarianceParam = new BoundedParameter("clrVar", 0.3, 0, 0.3);

    public Planes(LX lx) {
        super(lx);
        addParameter("grantPatterson/planes/count", countParam);
        addParameter("grantPatterson/planes/posSpeed", positionSpeedParam);
        addParameter("grantPatterson/planes/rotSpeed", rotationSpeedParam);
        addParameter("grantPatterson/planes/colorSpeed", colorSpeedParam);
        addParameter("grantPatterson/planes/size", sizeParam);
        addParameter("grantPatterson/planes/posVariance", positionVarianceParam);
        addParameter("grantPatterson/planes/rotVariance", rotationVarianceParam);
        addParameter("grantPatterson/planes/colorVariance", colorVarianceParam);

        seed = Math.random() * 1000;
    }

    private void runCube(BaseCube cube, Plane plane, int hue, int saturation) {
        double distance = plane.getDistanceToPoint(new Vec3D(cube.x, cube.y, cube.z));
        colors[cube.index] =
                LXColor.lightest(
                        colors[cube.index],
                        lx.hsb(
                                hue,
                                saturation,
                                (float) Math.max(0, 100 - distance / sizeParam.getValuef())));
    }

    @Override
    public void run(double deltaMs) {
        // Increase each offset based on time since last run() and speed param values
        positionOffset += deltaMs * positionSpeedParam.getValuef() / 1000;
        rotationOffset += deltaMs * rotationSpeedParam.getValuef() / 2000;
        colorOffset += deltaMs * colorSpeedParam.getValuef() / 1000;
        float positionVariance = positionVarianceParam.getValuef();
        float rotationVariance = rotationVarianceParam.getValuef();
        float colorVariance = colorVarianceParam.getValuef();

        // Black out all cubes and add colors from each plane
        clearColors();
        int countValue = (int) countParam.getValue();
        for (int i = 0; i < countValue; i++) {
            // For each plane we want to display, compute position, rotation, and color from
            // SimplexNoise function
            float x =
                    (float)
                            (model.cx
                                    + SimplexNoise.noise(
                                                    i * positionVariance, positionOffset, seed, 0)
                                            * model.xRange
                                            / 2.0);
            float y =
                    (float)
                            (model.cy
                                    + SimplexNoise.noise(
                                                    i * positionVariance, positionOffset, seed, 100)
                                            * model.yRange
                                            / 2.0);
            float z =
                    (float)
                            (model.cz
                                    + SimplexNoise.noise(
                                                    i * positionVariance, positionOffset, seed, 200)
                                            * model.zRange
                                            / 2.0);
            float yrot =
                    (float)
                            (SimplexNoise.noise(i * rotationVariance, rotationOffset, seed, 300)
                                    * Math.PI);
            float zrot =
                    (float)
                            (SimplexNoise.noise(i * rotationVariance, rotationOffset, seed, 400)
                                    * Math.PI);
            Plane plane =
                    new Plane(new Vec3D(x, y, z), new Vec3D(1, 0, 0).rotateY(yrot).rotateZ(zrot));
            // Noise hovers around 0 between -1 and 1; double the hue range so we actually get red
            // sometimes.
            int hue =
                    (int)
                                    ((SimplexNoise.noise(i * colorVariance, colorOffset, seed, 500)
                                                    + 1)
                                            * 360)
                            % 360;
            // Here we want full saturation most of the time, so turn 0 into full and -1 or 1 into
            // none.
            // But take the square root to curve a little back towards less saturation.
            int saturation =
                    (int)
                            ((1.0
                                            - Math.sqrt(
                                                    Math.abs(
                                                            SimplexNoise.noise(
                                                                    i * colorVariance,
                                                                    colorOffset,
                                                                    seed,
                                                                    600))))
                                    * 100);
            for (BaseCube cube : model.baseCubes) {
                runCube(cube, plane, hue, saturation);
            }
        }
    }
}
