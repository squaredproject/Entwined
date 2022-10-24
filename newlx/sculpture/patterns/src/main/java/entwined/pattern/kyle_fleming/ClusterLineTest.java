package entwined.pattern.kyle_fleming;

import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import entwined.utils.Vec2D;
import entwined.utils.VecUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

public class ClusterLineTest extends LXPattern {

  final BoundedParameter y;
  final BoundedParameter theta;
  final BoundedParameter spin;

  public ClusterLineTest(LX lx) {
    super(lx);

    addParameter("\u0398", theta = new BoundedParameter("\u0398", 0, -90, 430));
    addParameter("Y", y = new BoundedParameter("Y", 200, model.yMin, model.yMax));
    addParameter("spin", spin = new BoundedParameter("SPIN", 0, -90, 430));
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    Vec2D origin = new Vec2D(theta.getValuef(), y.getValuef());
    for (LXPoint cube : model.points) {
      Vec2D cubePointPrime = VecUtils.movePointToSamePlane(origin, CubeManager.getCube(cube.index).cylinderPoint);
      float dist = origin.distanceTo(cubePointPrime);
      float cubeTheta = (spin.getValuef() + 15) + cubePointPrime.sub(origin).heading() * 180 / LX.PIf + 360;
      colors[cube.index] = LX.hsb(135, 100, 100
          * LXUtils.constrainf((1 - EntwinedUtils.abs(cubeTheta % 90 - 15) / 100 / EntwinedUtils.asin(20 / EntwinedUtils.max(20, dist))), 0, 1));
    }
  }
}
