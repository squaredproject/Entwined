package com.charlesgadeken.entwined.patterns.geoffSchmidt;

import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import com.charlesgadeken.entwined.patterns.geoffSchmidt.utils.ColorBar;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Geoff Schmidt")
public class Parallax extends EntwinedBasePattern {
  final BoundedParameter pHue = new BoundedParameter("HUE", 0.5);
  final BoundedParameter pSpeed = new BoundedParameter("SPD", 0);
  final BoundedParameter pCount = new BoundedParameter("BARS", .25);
  final BoundedParameter pBounceMag = new BoundedParameter("BNC", 0);
  final SinLFO bounceLFO = new SinLFO(-1.0, 1.0, 750);
  ColorBar[] colorBars;
  double now = 0;

  public Parallax(LX lx) {
    super(lx);
    addParameter("geoffSchmidt/parallax/hue",pHue);
    addParameter("geoffSchmidt/parallax/speed",pSpeed);
    addParameter("geoffSchmidt/parallax/count",pCount);
    addParameter("geoffSchmidt/parallax/bounceMag",pBounceMag);
    addModulator("geoffSchmidt/parallax/bounceLFO",bounceLFO).start();
    colorBars = new ColorBar[0];
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    int targetCount = (int) (pCount.getValuef() * 20) + 1;

    if (targetCount != colorBars.length) {
      // If I knew any Java, I might know how to resize an array
      ColorBar[] newColorBars = new ColorBar[targetCount];
      for (int i = 0; i < targetCount; i++) {
        newColorBars[i] = i < colorBars.length ? colorBars[i] : null;
      }
      colorBars = newColorBars;
    }

    now += deltaMs * (pSpeed.getValuef() * 2.0f + .5f);
    double bouncedNow = now + bounceLFO.getValuef() * pBounceMag.getValuef() * 1000.0f;

    for (int i = 0; i < colorBars.length; i++) {
      if (colorBars[i] == null || colorBars[i].offscreen(now))
        colorBars[i] = new ColorBar(now);
    }

    for (BaseCube cube : model.baseCubes) {
      colors[cube.index] = lx.hsb(0, 0, 0);

      for (ColorBar colorBar : colorBars) {
        if (colorBar.intersects(bouncedNow, cube.transformedY)) {
          colors[cube.index] = colorBar.getColor(pHue.getValuef() * 360);
          break;
        }
      }
    }
  }
}
