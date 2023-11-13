package entwined.pattern.sam_brocchini;

import heronarts.lx.LX;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;

import java.util.Random;

import entwined.core.CubeManager;

public class RingMaster extends LXPattern {

  CompoundParameter thickness =  new CompoundParameter ("Thickness", 10, 0, 40);
  CompoundParameter colorOfCubes =  new CompoundParameter ("Color", 20, 0, 360);
  CompoundParameter speed = new CompoundParameter ("Speed", 1000, 300, 3000);
  double timer = 0;

  SawLFO position = new SawLFO(350, 0, speed);

  public RingMaster(LX lx) {
    super(lx);
    addParameter("thickness", thickness);
    addParameter("color", colorOfCubes);
    addParameter("speed", speed);
    addModulator(position).start();
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    timer = timer + deltaMs;
    for (LXPoint cube : model.points){
      float saturation = 50;
      float brightness = 0;
      float yPos = CubeManager.getCube(lx, cube.index).localY;

      double newPosition = position.getValue() + (50*Math.sin((double) (CubeManager.getCube(lx, cube.index).localX)/100));

      if (Math.abs(yPos  - newPosition) < thickness.getValue()) {
        brightness = 100;
      } else if ((yPos - newPosition) > thickness.getValue() &&
        (yPos - newPosition) < (5*thickness.getValue())) {
        brightness = 100 - 100 * (yPos - (float) newPosition - (float) thickness.getValue())/ (4*((float) thickness.getValue())); ;
        //brightness = 50;
        System.out.println(brightness);
      } else {
        brightness = 0;
      }

      colors[cube.index] = LX.hsb (
        colorOfCubes.getValuef(),
        100,
        brightness
      );
  }
 }
}
