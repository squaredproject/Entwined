package entwined.pattern.anon;

import heronarts.lx.LX;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;

import entwined.utils.EntwinedUtils;

// NB - Seems to work
public class SeeSaw extends LXPattern {

  final LXProjection projection = new LXProjection(model);

  final int FEET = 12;  // units are normally in inches

  final SinLFO rate = new SinLFO(2000, 11000, 19000);
  final SinLFO rz = new SinLFO(-15, 15, rate);
  final SinLFO rx = new SinLFO(-70, 70, 11000);
  final SinLFO width = new SinLFO(1*FEET, 8*FEET, 13000);

  final CompoundParameter bgLevel = new CompoundParameter("BG", 25, 0, 50);

  public SeeSaw(LX lx) {
    super(lx);
    addModulator(rate).start();
    addModulator(rx).start();
    addModulator(rz).start();
    addModulator(width).start();
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    // Okay, here we create a projection of the model and then apply
    // the transform. We could do this with things like the model transforms,
    // although they would no longer act globally (maybe a good thing...) XXX CSW
    projection
      .reset()
      .center()
      .rotate(rx.getValuef() * LX.PIf / 180, 1, 0, 0)
      .rotate(rz.getValuef() * LX.PIf / 180, 0, 0, 1);
    for (LXVector v : projection) {
      colors[v.index] = LX.hsb(
        (currentBaseHue + EntwinedUtils.min(120, EntwinedUtils.abs(v.y))) % 360,
        100,
        EntwinedUtils.max(bgLevel.getValuef(), 100 - (100/(1*FEET))*EntwinedUtils.max(0, EntwinedUtils.abs(v.y) - 0.5f*width.getValuef()))
      );
    }
  }
}
