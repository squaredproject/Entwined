package com.charlesgadeken.entwined.patterns.contributors.geoffSchmidt;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import com.charlesgadeken.entwined.patterns.contributors.geoffSchmidt.utils.PixelState;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Geoff Schmidt")
public class Pixels extends EntwinedBasePattern {
  final BoundedParameter pSpeed = new BoundedParameter("SPD", 2.0 / 15.0);
  final BoundedParameter pLifetime = new BoundedParameter("LIFE", 3.0 / 15.0);
  final BoundedParameter pHue = new BoundedParameter("HUE", 0.5);
  final BoundedParameter pSat = new BoundedParameter("SAT", 0.5);
  final SawLFO hueLFO = new SawLFO(0.0, 1.0, 1000);

  PixelState[] pixelStates;
  double now = 0;
  double lastFireTime = 0;

  public Pixels(LX lx) {
    super(lx);

    addParameter("geoffSchmidt/pixels/speed", pSpeed);
    addParameter("geoffSchmidt/pixels/lifetime", pLifetime);
    addParameter("geoffSchmidt/pixels/sat", pSat);
    addParameter("geoffSchmidt/pixels/hue", pHue);
    addModulator(hueLFO).start();

    int numCubes = model.baseCubes.size();
    pixelStates = new PixelState[numCubes];
    for (int n = 0; n < numCubes; n++)
      pixelStates[n] = new PixelState(lx);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    now += deltaMs;

    float vSpeed = pSpeed.getValuef();
    float vLifetime = pLifetime.getValuef();
    float vHue = pHue.getValuef();
    float vSat = pSat.getValuef();

    hueLFO.setPeriod(vHue * 30000 + 1000);

    float minFiresPerSec = 5;
    float maxFiresPerSec = 2000;
    float firesPerSec = minFiresPerSec + vSpeed * (maxFiresPerSec - minFiresPerSec);
    float timeBetween = 1000 / firesPerSec;
    while (lastFireTime + timeBetween < now) {
      int which = (int) Utilities.random(0, model.cubes.size() + model.shrubCubes.size());
      pixelStates[which].fire(now, vLifetime * 1000 + 10, hueLFO.getValuef(), (1 - vSat));
      lastFireTime += timeBetween;
    }

    int i = 0;
    for (i = 0; i < (model.baseCubes.size()); i++) {
      colors[i] = pixelStates[i].currentColor(now);
    }
  }
}
