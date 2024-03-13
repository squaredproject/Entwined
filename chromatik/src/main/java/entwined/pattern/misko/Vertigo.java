package entwined.pattern.misko;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class Vertigo extends LXPattern {

  private float speedMult = 1000;
  final CompoundParameter hue = new CompoundParameter("hue", 135, 0, 360);
  final CompoundParameter width = new CompoundParameter("width", 45, 0, 100);
  final CompoundParameter globalTheta = new CompoundParameter("globalTheta", 1.0, 0, 1.0);
  final CompoundParameter colorSpeed = new CompoundParameter("colorSpeed", 0, 0, 200);
  final CompoundParameter speedParam = new CompoundParameter("Speed", 1.5, 3, .01);
  final CompoundParameter glow = new CompoundParameter("glow", 0.1, 0.0, 1.0);
  final SawLFO wave = new SawLFO(0, 360, 1000);
  float total_ms=0;
  int shrub_offset[];
  private float max_height=0.0f;

  public Vertigo(LX lx) {
    super(lx);
    addModulator(wave).start();
    addParameter("hue", hue);
    addParameter("globalTheta", globalTheta);
    addParameter("speed", speedParam);
    addParameter("colorSpeed", colorSpeed);
    addParameter("glow", glow);
    addParameter("width", width);

    max_height=0.0f;
    for (LXPoint cube : model.points) {
      if (max_height<cube.y) {
        max_height=cube.y;
      }
    }

  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    wave.setPeriod(speedParam.getValuef() * speedMult  );
    total_ms+=deltaMs*speedParam.getValuef();
    //float offset = (wave.getValuef()+360.0f)%360.0f;
    for (LXPoint cube : model.points) {
      float h = hue.getValuef();
      float b =  ((10.0f-cube.y/max_height + (total_ms/3000.0f))%1.0f)*100.0f ; //? 100.0f : 0.0f;
      colors[cube.index] = LX.hsb(h,
            100.0f,
            b);
    }
  }
}