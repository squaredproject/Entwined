package com.charlesgadeken.entwined.model;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.List;

public class Entwined extends LXModel {

  public static class Metrics {

    public final int length;

    private final LXVector origin = new LXVector(0, 0, 0);
    private final LXVector spacing = new LXVector(1, 0, 0);

    public Metrics(int length) {
      this.length = length;
    }

    public Metrics setOrigin(float x, float y, float z) {
      this.origin.set(x, y, z);
      return this;
    }

    public Metrics setOrigin(LXVector v) {
      this.origin.set(v);
      return this;
    }

    public Metrics setSpacing(float x, float y, float z) {
      this.spacing.set(x, y, z);
      return this;
    }

    public Metrics setSpacing(LXVector v) {
      this.spacing.set(v);
      return this;
    }
  }

  public final Metrics metrics;

  public final int length;

  public Entwined(Metrics metrics) {
    super(makePoints(metrics), LXModel.Key.STRIP);
    this.metrics = metrics;
    this.length = metrics.length;
  }

  public Entwined(int length) {
    this(new Metrics(length));
  }

  private static List<LXPoint> makePoints(Metrics metrics) {
    List<LXPoint> points = new ArrayList<LXPoint>(metrics.length);
    for (int i = 0; i < metrics.length; ++i) {
      points.add(new LXPoint(
          metrics.origin.x + i * metrics.spacing.x,
          metrics.origin.y + i * metrics.spacing.y,
          metrics.origin.z + i * metrics.spacing.z
      ));
    }
    return points;
  }
}