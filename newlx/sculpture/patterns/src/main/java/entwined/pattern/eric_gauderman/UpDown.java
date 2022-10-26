package entwined.pattern.eric_gauderman;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.pattern.LXPattern;


import entwined.utils.EntwinedUtils;
import entwined.core.CubeData;
import entwined.core.CubeManager;

public class UpDown extends LXPattern {
    float time = 0;
    // All values below are on the scale from zero to one.
    // std dev of the gaussian function that determines the thickness of the line
    final double deviation = 0.05;
    final double minLineCenterY = 0 - 2 * deviation;
    final double maxLineCenterY = 1 + 2 * deviation;
    final SinLFO upDownModulator = new SinLFO(minLineCenterY, maxLineCenterY, 5000);

    public UpDown(LX lx) {
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
        for (LXModel component : model.children) {
          for (LXPoint cube : model.points) {
              CubeData cubeData = CubeManager.getCube(cube.index);
              colors[cube.index] = LX.hsb(
                      cubeData.localTheta + time / 6000 * 360,  // XXX check that this is the right theta...
                      100,
                      100 * gaussian(EntwinedUtils.map(cube.y, component.yMin, component.yMax), scanHeight));
          }
        }
    }
}


