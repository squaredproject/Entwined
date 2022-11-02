package entwined.pattern.alchemy;

import heronarts.lx.LX;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;

import entwined.core.CubeManager;

public class Zebra extends LXPattern {

 BoundedParameter thickness =  new BoundedParameter ("THIC", 160,0,200);
 BoundedParameter  period= new BoundedParameter ("PERI", 500, 300, 3000);
  double timer = 0;

  SinLFO position =new SinLFO(0, 200, period);

  public Zebra(LX lx) {
    super(lx);
    addParameter("thickness", thickness);
    addParameter("period", period);
    addModulator(position).start();
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    timer = timer + deltaMs;
    for (LXPoint cube : model.points){
      float hue = .4f;
      float saturation;
      float brightness = 1;

      if (((CubeManager.getCube(cube.index).localY + position.getValue() + CubeManager.getCube(cube.index).localTheta) % 200) > thickness.getValue()) {
        saturation=0;
        brightness=1;
      } else {
        saturation=1;
        brightness=0;
      }

      colors[cube.index] = LX.hsb (
        360 * hue,
        100 * saturation,
        100 * brightness
      );
  }
 }
}
