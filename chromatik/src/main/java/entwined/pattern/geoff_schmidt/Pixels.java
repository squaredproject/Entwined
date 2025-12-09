package entwined.pattern.geoff_schmidt;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class Pixels extends LXPattern {
  final CompoundParameter pSpeed = new CompoundParameter("SPD", 2.0/15.0);
  final CompoundParameter pLifetime = new CompoundParameter("LIFE", 3.0/15.0);
  final CompoundParameter pHue = new CompoundParameter("HUE", 0.5);
  final CompoundParameter pSat = new CompoundParameter("SAT", 0.5);
  final SawLFO hueLFO = new SawLFO(0.0, 1.0, 1000);

  PixelState[] pixelStates;
  double now = 0;
  double lastFireTime = 0;

  public Pixels(LX lx) {
    super(lx);

    addParameter("speed", pSpeed);
    addParameter("lifetime", pLifetime);
    addParameter("saturation", pSat);
    addParameter("hue", pHue);
    addModulator(hueLFO).start();

    int numCubes = model.points.length;
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
    int numCubes = model.points.length;
    while (lastFireTime + timeBetween < now) {
      int which = (int)EntwinedUtils.random(0, numCubes);
      pixelStates[which].fire(now, vLifetime * 1000 + 10, hueLFO.getValuef(), (1 - vSat));
      lastFireTime += timeBetween;
    }

    int i = 0;
    for (i = 0; i < numCubes; i++) {
      colors[i] = pixelStates[i].currentColor(now);
    }
  }
}
