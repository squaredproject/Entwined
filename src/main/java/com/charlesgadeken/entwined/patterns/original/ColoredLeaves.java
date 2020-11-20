package com.charlesgadeken.entwined.patterns.original;

import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;

@LXCategory("Original")
public class ColoredLeaves extends EntwinedBasePattern {

  private final SawLFO[] movement;
  private final SinLFO[] bright;

  public ColoredLeaves(LX lx) {
    super(lx);
    movement = new SawLFO[3];
    for (int i = 0; i < movement.length; ++i) {
      movement[i] = new SawLFO(0, 360, 60000 / (i + 1));
      addModulator(movement[i]).start();
    }
    bright = new SinLFO[5];
    for (int i = 0; i < bright.length; ++i) {
      bright[i] = new SinLFO(100, 0, 60000 / (1 + i));
      addModulator(bright[i]).start();
    }
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    for (BaseCube cube : model.baseCubes) {
      colors[cube.index] = LX.hsb(
          (360 + movement[cube.index  % movement.length].getValuef()) % 360,
          100,
          bright[cube.index % bright.length].getValuef()
      );
    }
  }
}
