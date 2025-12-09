package entwined.pattern.sam_brocchini;

import heronarts.lx.LX;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;

import entwined.core.CubeData;
import entwined.core.CubeManager;

public class RingoDown extends LXPattern {

  CompoundParameter thickness = new CompoundParameter("Thickness", 10, 0, 40);
  CompoundParameter colorOfCubes = new CompoundParameter("Color", 20, 0, 360);
  CompoundParameter speed = new CompoundParameter("Speed", 1000, 300, 3000);

  SawLFO position = new SawLFO(350, 0, speed);

  public RingoDown(LX lx) {
    super(lx);
    addParameter("thickness", thickness);
    addParameter("color", colorOfCubes);
    addParameter("speed", speed);
    addModulator(position).start();
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0)
      return;

    double thick = thickness.getValue();
    double brightness = 0;
    double pos = position.getValue();

    for (LXPoint cube : model.points) {
      // float saturation = 50;
      CubeData cubeData = CubeManager.getCube(lx, cube.index);
      double newPosition = (pos + (50 * Math.sin((cubeData.localX) / 100)));

      if (Math.abs(cubeData.localY - newPosition) < thick) {
        brightness = 100;
      } else if ((cubeData.localY - newPosition) > thick
        && (cubeData.localY - newPosition) < (5 * thick)) {
        brightness = 100
          - 100 * (cubeData.localY - newPosition - thick) / (4 * (thick));
      } else {
        brightness = 0;
      }

      colors[cube.index] = LX.hsb(colorOfCubes.getValuef(), 100,
        (float) brightness);
    }
  }
}
