import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.Click;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import toxi.geom.Vec2D;
import org.apache.commons.lang3.ArrayUtils;
import toxi.math.noise.PerlinNoise;
import toxi.math.noise.SimplexNoise;

/**
In order to be like a flag, have multiple colors on the sculpture
*/
class MultiColor extends TSPattern {

  final BasicParameter hue1Param = new BasicParameter("HUE1", 55, 1, 360);
  final BasicParameter bright1Param = new BasicParameter("BRIGHT1", 100, 0, 100);
  final BasicParameter hue2Param = new BasicParameter("HUE2", 200, 1, 360);
  final BasicParameter bright2Param = new BasicParameter("BRIGHT2", 100, 0, 100);
  final BasicParameter hue3Param = new BasicParameter("HUE3", 300, 1, 360);
  final BasicParameter bright3Param = new BasicParameter("BRGHT3", 0, 0, 100);


  // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using Parameters or sin waves
  MultiColor(LX lx) {
    super(lx);
    addParameter(hue1Param);
    addParameter(bright1Param);
    addParameter(hue2Param);
    addParameter(bright2Param);
    addParameter(hue3Param);
    addParameter(bright3Param);
  }

  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    float[]  hue = new float[3];
    float[] brightness = new float[3];
    int nColors = 0;

    if (bright1Param.getValuef() > 1) {
      hue[nColors] = hue1Param.getValuef();
      brightness[nColors] = bright1Param.getValuef();
      nColors++;
    }
    if (bright2Param.getValuef() > 1) {
      hue[nColors] = hue2Param.getValuef();
      brightness[nColors] = bright2Param.getValuef();
      nColors++;
    }
    if (bright3Param.getValuef() > 1) {
      hue[nColors] = hue3Param.getValuef();
      brightness[nColors] = bright3Param.getValuef();
      nColors++;
    }

    if (nColors == 0) return;

    // Use a for loop here to set the cube colors
    for (BaseCube cube : model.baseCubes) {
      int ci = cube.index % nColors;
      colors[cube.index] = lx.hsb(hue[ci],100,brightness[ci]);
    }

  }
}

/**
This will have shrubs and trees different colors
*/
class MultiColor2 extends TSPattern {

  final BasicParameter hue1Param = new BasicParameter("HUE1", 55, 1, 360);
  final BasicParameter bright1Param = new BasicParameter("BRIGHT1", 100, 0, 100);
  final BasicParameter hue2Param = new BasicParameter("HUE2", 200, 1, 360);
  final BasicParameter bright2Param = new BasicParameter("BRIGHT2", 100, 0, 100);
  final BasicParameter hue3Param = new BasicParameter("HUE3", 300, 1, 360);
  final BasicParameter bright3Param = new BasicParameter("BRIGHT3", 0, 0, 100);


  // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using Parameters or sin waves
  MultiColor2(LX lx) {
    super(lx);
    addParameter(hue1Param);
    addParameter(bright1Param);
    addParameter(hue2Param);
    addParameter(bright2Param);
    addParameter(hue3Param);
    addParameter(bright3Param);
  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    float[]  hue = new float[3];
    float[] brightness = new float[3];
    int nColors = 0;

    if (bright1Param.getValuef() > 1) {
      hue[nColors] = hue1Param.getValuef();
      brightness[nColors] = bright1Param.getValuef();
      nColors++;
    }
    if (bright2Param.getValuef() > 1) {
      hue[nColors] = hue2Param.getValuef();
      brightness[nColors] = bright2Param.getValuef();
      nColors++;
    }
    if (bright3Param.getValuef() > 1) {
      hue[nColors] = hue3Param.getValuef();
      brightness[nColors] = bright3Param.getValuef();
      nColors++;
    }

    if (nColors == 0) return;

    // Use a for loop here to set the cube colors
    for (BaseCube cube : model.baseCubes) {
      int ci = cube.sculptureIndex % nColors;
      colors[cube.index] = lx.hsb(hue[ci],100,brightness[ci]);
    }

  }
}

class StripeStatic extends TSPattern {

  // stripes: width in inches
  final BasicParameter widthParam = new BasicParameter("WIDTH", 3*12, 12, 20*12);
  final BasicParameter hue1Param = new BasicParameter("HUE1", 55, 1, 360);
  final BasicParameter bright1Param = new BasicParameter("BRIGHT1", 100, 0, 100);
  final BasicParameter hue2Param = new BasicParameter("HUE2", 200, 1, 360);
  final BasicParameter bright2Param = new BasicParameter("BRIGHT2", 100, 0, 100);
  final BasicParameter hue3Param = new BasicParameter("HUE3", 300, 1, 360);
  final BasicParameter bright3Param = new BasicParameter("BRIGHT3", 0, 0, 100);


  StripeStatic(LX lx) {
    super(lx);
    addParameter(widthParam);
    addParameter(hue1Param);
    addParameter(bright1Param);
    addParameter(hue2Param);
    addParameter(bright2Param);
    addParameter(hue3Param);
    addParameter(bright3Param);
  }

  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;
    if (widthParam.getValuef() <= 0) return;

    float[]  hue = new float[3];
    float[] brightness = new float[3];
    int nColors = 0;

    if (bright1Param.getValuef() > 1) {
      hue[nColors] = hue1Param.getValuef();
      brightness[nColors] = bright1Param.getValuef();
      nColors++;
    }
    if (bright2Param.getValuef() > 1) {
      hue[nColors] = hue2Param.getValuef();
      brightness[nColors] = bright2Param.getValuef();
      nColors++;
    }
    if (bright3Param.getValuef() > 1) {
      hue[nColors] = hue3Param.getValuef();
      brightness[nColors] = bright3Param.getValuef();
      nColors++;
    }

    if (nColors == 0) return;

    // Use a for loop here to set the cube colors
    float stripeWidth = widthParam.getValuef();
    for (BaseCube cube : model.baseCubes) {
      int ci = (int)(cube.x / stripeWidth) % nColors;
      ci = Math.abs(ci);
      colors[cube.index] = lx.hsb(hue[ci],100,brightness[ci]);
    }

  }
}


