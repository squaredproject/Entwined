package com.charlesgadeken.entwined.patterns.contributors.geoffSchmidt.utils;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

public class PixelState {
  LX lx;
  double when; // time last triggered (possibly zero)
  float h, s, life; // parameters when last triggered

  public PixelState(LX _lx) {
    lx = _lx;
    when = -1000 * 60 * 60; // arbitrary time far in the past
    h = s = life = 0;
  }

  public void fire(double now, float _life, float _h, float _s) {
    when = now;
    life = _life;
    h = _h;
    s = _s;
  }

  public int currentColor(double now) {
    double age = (life - (now - when)) / life;
    if (age < 0)
      age = 0;
    return LXColor.hsb(h * 360, s * 100, age * 100);
  }
}
