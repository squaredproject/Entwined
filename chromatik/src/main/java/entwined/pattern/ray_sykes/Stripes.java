package entwined.pattern.ray_sykes;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

public class Stripes extends LXPattern {
  final CompoundParameter minSpacing = new CompoundParameter("MinSpacing", 0.5, .3, 2.5);
  final CompoundParameter maxSpacing = new CompoundParameter("MaxSpacing", 2, .3, 2.5);
  final SinLFO spacing = new SinLFO(minSpacing, maxSpacing, 8000);
  final SinLFO slopeFactor = new SinLFO(0.05, 0.2, 19000);

  public Stripes(LX lx) {
    super(lx);
    addParameter("minSpacing", minSpacing);
    addParameter("maxSpacing", maxSpacing);
    addModulator(slopeFactor).start();
    addModulator(spacing).start();
  }

  // XXX what happens with sinf if value not between 0 and 2pi?
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float spacingVal = spacing.getValuef();
    float slopeVal = slopeFactor.getValuef();
    float currentBaseHue = lx.engine.palette.color.getHuef();

    for (LXModel component : model.children) {
      for (LXPoint cube : component.points) {
        float localTheta = (float)Math.atan2(cube.z - component.cz, cube.x - component.cx);
        float hueVal = (currentBaseHue + .1f*cube.y) % 360;
        float brightVal = 50 + 50 * LXUtils.sinf(spacingVal * (LXUtils.sinf(4*localTheta) + slopeVal * cube.y));
        colors[cube.index] = LX.hsb(hueVal,  100, brightVal);
      }
    }
  }
}
