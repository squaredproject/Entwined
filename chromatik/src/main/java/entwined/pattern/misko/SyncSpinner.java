package entwined.pattern.misko;

import entwined.core.TSBufferedPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;

public class SyncSpinner extends TSBufferedPattern {

  private float speedMult = 1000;
  final CompoundParameter hue = new CompoundParameter("hue", 135, 0, 360);
  final CompoundParameter globalTheta = new CompoundParameter("gTheta", 1.0, 0, 1.0);
  final CompoundParameter colorSpeed = new CompoundParameter("cSpeed", 100, 0, 200);
  final CompoundParameter speedParam = new CompoundParameter("Speed", 5, 20, .01);
  final CompoundParameter glow = new CompoundParameter("glow", 0.1, 0.0, 1.0);
  final SawLFO wave = new SawLFO(0, 12, 1000);
  float total_ms=0;
  int shrub_offset[];

  public SyncSpinner(LX lx) {
      super(lx);
      addModulator(wave).start();
    addParameter("hue", hue);
    addParameter("globalTheta", globalTheta);
    addParameter("speed", speedParam);
    addParameter("colorSpeed", colorSpeed);
    addParameter("glow", glow);

  }

  @Override
  public void bufferedRun(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    wave.setPeriod(speedParam.getValuef() * speedMult / 3 );
    total_ms+=deltaMs*speedParam.getValuef();
    for (LXModel shrub : model.sub("SHRUB")) {
      int ry;
      if (model.meta("ry") != null) {
        ry = Integer.parseInt(model.meta("ry"));
      } else {
        ry = 0;
      }
      int shrub_offset = (-ry/30+24)%12;
      int cubeIdx = 0;
      for (LXPoint shrubCube : shrub.points) {

    //System.out.format("%f %d %d | %d\n",ry,shrub_offset,shrubCube.config.clusterIndex,(shrubCube.config.clusterIndex+shrub_offset)%12);
        float diff = (12.0f+(wave.getValuef() - (cubeIdx/12 + shrub_offset))%12.0f)%12.0f;
        if (diff<0) {
          System.out.println(diff);
        }
        float h = (360+(hue.getValuef() +
                   globalTheta.getValuef()*shrubCube.theta +
                   total_ms*colorSpeed.getValuef()/10000)%360)%360;
        float b = Math.min(100,glow.getValuef()*100.0f+(1.0f-glow.getValuef())*diff*(100.0f/12.0f));
        colors[shrubCube.index] = LX.hsb(h,
          100,
          b);
        cubeIdx++;
      }
    }
  }
}

