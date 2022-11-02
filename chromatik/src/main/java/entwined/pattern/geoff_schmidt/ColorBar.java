package entwined.pattern.geoff_schmidt;

import entwined.utils.EntwinedUtils;
import heronarts.lx.color.LXColor;

public class ColorBar {
  double s, b;
  double startTime;
  double velocity;
  double barHeight;

  public ColorBar(double now) {
    startTime = now;
    velocity = EntwinedUtils.random(6.0f, 12.0f*25.0f); // upward velocity, inches per second
    s = EntwinedUtils.random(0.1f, 1.0f) * 100;
    b = EntwinedUtils.random(0.1f, 1.0f) * 100;
    barHeight = EntwinedUtils.random(12.0f, 10.0f*12.0f);
  }

  public boolean intersects(double now, double y) {
    y -= (now - startTime)/1000.0f * velocity;
    return y < 0 && y > -barHeight;
  }

  public boolean offscreen(double now) {
    return (velocity * (now - startTime)/1000.0f) - barHeight > 70*12;
  }

  public int getColor(double h) {
    return LXColor.hsb(h, s, b);
  }
}
