package com.charlesgadeken.entwined.model;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.List;

public class Entwined extends LXModel {
  public final Entwined.Metrics metrics;
  public final int length;

  public Entwined(Entwined.Metrics metrics) {
    super(makePoints(metrics), new String[]{"strip"});
    this.metrics = metrics;
    this.length = metrics.length;
  }

  public Entwined(int length) {
    this(new Entwined.Metrics(length));
  }

  private static List<LXPoint> makePoints(Entwined.Metrics metrics) {
    List<LXPoint> points = new ArrayList(metrics.length);

    for(int i = 0; i < metrics.length; ++i) {
      points.add(new LXPoint(metrics.origin.x + (float)i * metrics.spacing.x, metrics.origin.y + (float)i * metrics.spacing.y, metrics.origin.z + (float)i * metrics.spacing.z));
    }

    return points;
  }

  public static class Metrics {
    public final int length;
    private final LXVector origin = new LXVector(0.0F, 0.0F, 0.0F);
    private final LXVector spacing = new LXVector(1.0F, 0.0F, 0.0F);

    public Metrics(int length) {
      this.length = length;
    }

    public Entwined.Metrics setOrigin(float x, float y, float z) {
      this.origin.set(x, y, z);
      return this;
    }

    public Entwined.Metrics setOrigin(LXVector v) {
      this.origin.set(v);
      return this;
    }

    public Entwined.Metrics setSpacing(float x, float y, float z) {
      this.spacing.set(x, y, z);
      return this;
    }

    public Entwined.Metrics setSpacing(LXVector v) {
      this.spacing.set(v);
      return this;
    }
  }
}
