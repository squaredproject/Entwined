package com.charlesgadeken.entwined.patterns.maryWang;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.Cube;
import com.charlesgadeken.entwined.model.ShrubCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Mary Wang")
public class VerticalSweep extends EntwinedBasePattern {

  final BoundedParameter saturationParam = new BoundedParameter("Saturation", 100, 0, 100);
  final BoundedParameter hue1Param = new BoundedParameter("Hue1", 60, 0, 360);
  final BoundedParameter hue2Param = new BoundedParameter("Hue2", 110, 0, 360);
  final BoundedParameter hue3Param = new BoundedParameter("Hue3", 180, 0, 360);

  final SawLFO range = new SawLFO(0, 1, 5000);

  public VerticalSweep(LX lx) {
    super(lx);
    addModulator(range).start();
    addParameter("maryWang/verticalSweep/saturation", saturationParam);
    addParameter("maryWang/verticalSweep/hue1", hue1Param);
    addParameter("maryWang/verticalSweep/hue2", hue2Param);
    addParameter("maryWang/verticalSweep/hue3", hue3Param);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float[] colorPalette = {
        hue1Param.getValuef(), hue2Param.getValuef(), hue3Param.getValuef()
    };
    int saturation = (int) saturationParam.getValuef();

    for (Cube cube : model.cubes) {
      float progress = ((cube.transformedTheta / 360.0f) + range.getValuef()) % 1; // value is 0-1
      float scaledProgress = (colorPalette.length) * progress; // value is 0-3
      int color1Index = Utilities.floor(scaledProgress);
      int color1Hue = (int) colorPalette[color1Index];
      int color2Hue = (int) colorPalette[Utilities.ceil(scaledProgress) % colorPalette.length];
      int color1 = LX.hsb( color1Hue, saturation, 100 );
      int color2 = LX.hsb( color2Hue, saturation, 100 );
      float amt = scaledProgress-color1Index;

      colors[cube.index] = LXColor.lerp(color1, color2, amt);
    }

    for (ShrubCube cube : model.shrubCubes) {
      float progress = ((cube.transformedTheta / 360.0f) + range.getValuef()) % 1; // value is 0-1
      float scaledProgress = (colorPalette.length) * progress; // value is 0-3
      int color1Index = Utilities.floor(scaledProgress);
      int color1Hue = (int) colorPalette[color1Index];
      int color2Hue = (int) colorPalette[Utilities.ceil(scaledProgress) % colorPalette.length];
      int color1 = LX.hsb( color1Hue, saturation, 100 );
      int color2 = LX.hsb( color2Hue, saturation, 100 );
      float amt = scaledProgress-color1Index;

      colors[cube.index] = LXColor.lerp(color1, color2, amt);
    }
  }
}