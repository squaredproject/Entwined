package entwined.pattern.misko;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class ColorBlast extends LXPattern {

  private float speedMult = 1000;
  final CompoundParameter hue = new CompoundParameter("hue", 135, 0, 360);
  final CompoundParameter width = new CompoundParameter("width", 0.15, 0, 2);
  final CompoundParameter globalTheta = new CompoundParameter("globalTheta", 1.0, 0, 1.0);
  final CompoundParameter colorSpeed = new CompoundParameter("colorSpeed", 85, 0, 200);
  final CompoundParameter speedParam = new CompoundParameter("Speed", 5, 20, .01);
  final CompoundParameter glow = new CompoundParameter("glow", 0.1, 0.0, 1.0);
  final SawLFO wave = new SawLFO(0, 360, 1000);
  float total_ms=0;
  int shrub_offset[];

  public ColorBlast(LX lx) {
      super(lx);
      addModulator(wave).start();
    addParameter("hue", hue);
    addParameter("globalTheta", globalTheta);
    addParameter("speed", speedParam);
    addParameter("colorSpeed", colorSpeed);
    addParameter("glow", glow);
    addParameter("width", width);

  }
  private float dist(float x, float y, float z) {
      return (float)Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
  }
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    wave.setPeriod(speedParam.getValuef() * speedMult  );
    total_ms+=deltaMs*speedParam.getValuef();
    // float offset = (wave.getValuef()+360.0f)%360.0f;
    for (LXPoint cube : model.points) {
      float b = 100.0f; //diff<width.getValuef() ? 100.0f : 0.0f;
      float h = (hue.getValuef() +
          dist(cube.x,cube.y,cube.z)*width.getValuef()+
             -total_ms*colorSpeed.getValuef()/10000)%360;
      colors[cube.index] = LX.hsb(h,
            100,
            b);
    }
  }
}

