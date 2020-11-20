package com.charlesgadeken.entwined.patterns.colinHunt;

import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SinLFO;

@LXCategory("Colin Hunt")
public class Breath extends EntwinedBasePattern {
  float minValue = 0.f;
  float maxValue = 100.f;
  float period = 10000;
  final SinLFO breath = new SinLFO(minValue, maxValue, period);

  // Constructor
  public Breath(LX lx) {
    super(lx);

    addModulator(breath).start();
  }

  @Override
  public void run(double deltaMs) {
    breath.setPeriod(period - (Math.abs(breath.getValuef() - 50.0f) * 50));

    // Use a for loop here to set the cube colors
    for (BaseCube cube : model.baseCubes) {
      colors[cube.index] = LX.hsb( 180, 25, breath.getValuef());
    }
  }
}
