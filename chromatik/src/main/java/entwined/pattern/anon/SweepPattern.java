package entwined.pattern.anon;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

import entwined.utils.EntwinedUtils;

// Seems to work
public class SweepPattern extends LXPattern {

  final int FEET = 12;  // 12inches in a foot

  final SinLFO speedMod = new SinLFO(3000, 9000, 5400);
  final SinLFO yPos = new SinLFO(model.yMin, model.yMax, speedMod);
  final SinLFO width = new SinLFO("WIDTH", 2*FEET, 20*FEET, 19000);

  final SawLFO offset = new SawLFO(0, LX.TWO_PIf, 9000);

  final CompoundParameter amplitude = new CompoundParameter("AMP", 10*FEET, 0, 20*FEET);
  final CompoundParameter speed = new CompoundParameter("SPEED", 1, 0, 3);
  final CompoundParameter height = new CompoundParameter("HEIGHT", 0, -300, 300);
  final SinLFO amp = new SinLFO(0, amplitude, 5000);

  public SweepPattern(LX lx) {
    super(lx);
    addModulator(speedMod).start();
    addModulator(yPos).start();
    addModulator(width).start();
    addParameter("amplitude", amplitude);
    addParameter("speed", speed);
    addParameter("height", height);
    addModulator(amp).start();
    addModulator(offset).start();
  }

  @Override
  public void onParameterChanged(LXParameter parameter) {
    super.onParameterChanged(parameter);
    if (parameter == speed) {
      float speedVar = 1/speed.getValuef();
      speedMod.setRange(9000 * speedVar,5400 * speedVar);
    }
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    for (LXPoint cube : model.points) {
      float yp = yPos.getValuef() + amp.getValuef() * LXUtils.sinf((cube.x - model.cx) * .01f + offset.getValuef());
      colors[cube.index] = LX.hsb(
        (currentBaseHue + Math.abs(cube.x - model.cx) * .2f +  cube.z*.1f + cube.y*.1f) % 360,
        EntwinedUtils.constrain(Math.abs(cube.y - model.cy), 0, 100),
        Math.max(0, 100 - (100/width.getValuef())*Math.abs(cube.y - yp - height.getValuef()))
      );
    }
  }
}
