package entwined.pattern.eric_gauderman;

import entwined.utils.EntwinedUtils;
import entwined.utils.Vec3D;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;

public class DiscreteColors extends LXPattern {
    final CompoundParameter hue1Param = new CompoundParameter("HU1", 60, 1, 360);
    // final CompoundParameter sat1Param = new CompoundParameter("SAT1", 100, 0, 100);
    // final CompoundParameter bright1Param = new CompoundParameter("BRIGHT1", 100, 0,
    // 100);

    final CompoundParameter hue2Param = new CompoundParameter("HU2", 240, 1, 360);
    // final CompoundParameter sat2Param = new CompoundParameter("SAT2", 100, 0, 100);
    // final CompoundParameter bright2Param = new CompoundParameter("BRIGHT2", 100, 0,
    // 100);

    final CompoundParameter hue3Param = new CompoundParameter("HU3", 300, 1, 360);
    // final CompoundParameter sat3Param = new CompoundParameter("SAT3", 0, 0, 100);
    // final CompoundParameter bright3Param = new CompoundParameter("BRIGHT3", 0, 0, 100);

    final DiscreteParameter numberOfColorsParam = new DiscreteParameter("NUM", 2, 1, 4);

    final DiscreteParameter xRotationParam = new DiscreteParameter("PITCH", 0, -360, 360);
    final DiscreteParameter yRotationParam = new DiscreteParameter("YAW", 0, -360, 360);

    final CompoundParameter adjustParam = new CompoundParameter("ADJUST", 0, -1, 1);

    final Vec3D directionVector = new Vec3D();
    final Vec3D cubeUtilityVector = new Vec3D();
    final float degToRad = LX.PIf / 180;
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

    public DiscreteColors(LX lx) {
        super(lx);

        addParameter("HU1", hue1Param);
        // addParameter(sat1Param);
        // addParameter(bright1Param);

        addParameter("HU2", hue2Param);
        // addParameter(sat2Param);
        // addParameter(bright2Param);

        addParameter("HU3", hue3Param);
        // addParameter(sat3Param);
        // addParameter(bright3Param);

        addParameter("ncolors", numberOfColorsParam);

        addParameter("xrot", xRotationParam);
        addParameter("yrot", yRotationParam);

        addParameter("adjustment", adjustParam);

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
        for (LXPoint cube : model.points) {
            // The following requires the direction vector to be a unit vector
            float cubeDistAlongVec = getCubeDistAlongVec(cube);
            minDistAlongVec = Math.min(minDistAlongVec, cubeDistAlongVec);
            maxDistAlongVec = Math.max(maxDistAlongVec, cubeDistAlongVec);
        }
    }

    // XXX - is this local coordinates or global coordinates?
    float getCubeDistAlongVec(LXPoint cube) {
        cubeUtilityVector.set(cube.x, cube.y, cube.z);
        return cubeUtilityVector.dot(directionVector);
    }

    @Override
    protected void run(double deltaMs) {
        for (LXPoint cube : model.points) {
            // The following requires the direction vector to be a unit vector
            float cubeDistAlongVec = getCubeDistAlongVec(cube);

            // Floor the lerp result to get the zero-indexed value of which color this cube
            // should be
            int colorIndex = (int) EntwinedUtils.lerp(
                    0,
                    numberOfColorsParam.getValuef(),
                    EntwinedUtils.map(
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

