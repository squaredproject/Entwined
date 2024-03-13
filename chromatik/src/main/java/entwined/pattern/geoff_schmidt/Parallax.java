package entwined.pattern.geoff_schmidt;

import entwined.core.CubeManager;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class Parallax extends LXPattern {
  final CompoundParameter pHue = new CompoundParameter("HUE", 0.5);
  final CompoundParameter pSpeed = new CompoundParameter("SPD", 0);
  final CompoundParameter pCount = new CompoundParameter("BARS", .25);
  final CompoundParameter pBounceMag = new CompoundParameter("BNC", 0);
  final SinLFO bounceLFO = new SinLFO(-1.0, 1.0, 750);
  ColorBar[] colorBars;
  double now = 0;

  public Parallax(LX lx) {
    super(lx);
    addParameter("hue", pHue);
    addParameter("speed", pSpeed);
    addParameter("count", pCount);
    addParameter("bounce_magnitude", pBounceMag);
    addModulator(bounceLFO).start();
    colorBars = new ColorBar[0];
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    int targetCount = (int)(pCount.getValuef() * 20) + 1;

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

    for (LXPoint cube : model.points) {
      colors[cube.index] = LX.hsb(0, 0, 0);

      for (ColorBar colorBar : colorBars) {  // XXX - cube.transformedY
        if (colorBar.intersects(bouncedNow, CubeManager.getCube(lx, cube.index).localY)) {
          colors[cube.index] = colorBar.getColor(pHue.getValuef() * 360);
          break;
        }
      }
    }

    // number of cubes: 224
    // number of shrubcubes: 120
    // number of colors: 344
    // System.out.println("number of tree cubes: " + model.cubes.size());
    // System.out.println("number of shrubcubes: " + model.shrubCubes.size());
    // System.out.println("number of colors: " + colors.length);

  }
}
