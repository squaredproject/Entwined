package com.charlesgadeken.entwined.patterns.original;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.Geometry;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;

@LXCategory("Original")
public class SeeSaw extends EntwinedBasePattern {

  final LXProjection projection = new LXProjection(model);

  final SinLFO rate = new SinLFO(2000, 11000, 19000);
  final SinLFO rz = new SinLFO(-15, 15, rate);
  final SinLFO rx = new SinLFO(-70, 70, 11000);
  final SinLFO width = new SinLFO(1* Geometry.FEET, 8*Geometry.FEET, 13000);

  final BoundedParameter bgLevel = new BoundedParameter("BG", 25, 0, 50);

  public SeeSaw(LX lx) {
    super(lx);
    addModulator(rate).start();
    addModulator(rx).start();
    addModulator(rz).start();
    addModulator(width).start();
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    projection
        .reset()
        .center()
        .rotate(rx.getValuef() * Utilities.PI / 180, 1, 0, 0)
        .rotate(rz.getValuef() * Utilities.PI / 180, 0, 0, 1);
    for (LXVector v : projection) {
      colors[v.index] = LX.hsb(
          (lx.engine.palette.getHuef() + Utilities.min(120, Utilities.abs(v.y))) % 360,
          100,
          Utilities.max(bgLevel.getValuef(), 100 - (100/(1*Geometry.FEET))*Utilities.max(0, Utilities.abs(v.y) - 0.5f*width.getValuef()))
      );
    }
  }
}
