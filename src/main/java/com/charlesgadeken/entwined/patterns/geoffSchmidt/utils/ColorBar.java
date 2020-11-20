package com.charlesgadeken.entwined.patterns.geoffSchmidt.utils;

import com.charlesgadeken.entwined.Utilities;
import heronarts.lx.color.LXColor;

public class ColorBar {
  double s, b;
  double startTime;
  double velocity;
  double barHeight;

  public ColorBar(double now) {
    startTime = now;
    velocity = Utilities.random(6.0f, 12.0f*25.0f); // upward velocity, inches per second
    s = Utilities.random(0.1f, 1.0f) * 100;
    b = Utilities.random(0.1f, 1.0f) * 100;
    barHeight = Utilities.random(12.0f, 10.0f*12.0f);
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
