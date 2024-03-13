package entwined.pattern.mary_wang;

import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class VerticalSweep extends LXPattern {

  final CompoundParameter saturationParam = new CompoundParameter("Saturation", 100, 0, 100);
  final CompoundParameter hue1Param = new CompoundParameter("Hue1", 60, 0, 360);
  final CompoundParameter hue2Param = new CompoundParameter("Hue2", 110, 0, 360);
  final CompoundParameter hue3Param = new CompoundParameter("Hue3", 180, 0, 360);

  final SawLFO range = new SawLFO(0, 1, 5000);

  public VerticalSweep(LX lx) {
    super(lx);
    addModulator(range).start();
    addParameter("saturation", saturationParam);
    addParameter("hue1", hue1Param);
    addParameter("hue2", hue2Param);
    addParameter("hue3", hue3Param);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float[] colorPalette = {
      hue1Param.getValuef(), hue2Param.getValuef(), hue3Param.getValuef()
      };
      int saturation = (int) saturationParam.getValuef();

    for (LXPoint cube : model.points) {
      float progress = ((CubeManager.getCube(lx, cube.index).localTheta / 360.0f) + range.getValuef()) % 1; // value is 0-1
      float scaledProgress = (colorPalette.length) * progress; // value is 0-3
      int color1Index = EntwinedUtils.floor(scaledProgress);
      int color1Hue = (int) colorPalette[color1Index];
      int color2Hue = (int) colorPalette[EntwinedUtils.ceil(scaledProgress) % colorPalette.length];
      int color1 = LX.hsb( color1Hue, saturation, 100 );
      int color2 = LX.hsb( color2Hue, saturation, 100 );
      float amt = scaledProgress-color1Index;

      colors[cube.index] = LXColor.lerp(color1, color2, amt);
    }
  }
}

