package com.charlesgadeken.entwined.patterns.test;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Testing Patterns")
public class TestCubePattern extends EntwinedBasePattern {

  int CUBE_MOD = 14;

  final BoundedParameter period = new BoundedParameter("RATE", 3000, 2000, 6000);
  final SinLFO cubeIndex = new SinLFO(0, CUBE_MOD, period);

  public TestCubePattern(LX lx) {
    super(lx);
    addModulator(cubeIndex).start();
    addParameter("test/testCubes/period", period);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    int ci = 0;
    for (BaseCube cube : model.baseCubes) {
      setColor(cube, LX.hsb(
          (lx.engine.palette.getHuef() + cube.cx + cube.cy) % 360,
          100,
          Utilities.max(0, 100 - 30*Utilities.abs((ci % CUBE_MOD) - cubeIndex.getValuef()))
      ));
      ++ci;
    }
  }
}
