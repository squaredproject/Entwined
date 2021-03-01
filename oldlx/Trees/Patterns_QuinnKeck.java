import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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


class ButterflyEffect extends TSPattern {
  
   float minValue = 0;
   float maxValue = 100;
   final SinLFO brightSin = new SinLFO(minValue, maxValue, 5000);

  float startValue = 65;
  final BasicParameter satParam = new BasicParameter("Saturation", startValue, minValue, maxValue);

  float periodMs = 10;
  final SawLFO hueSaw = new SawLFO(minValue, maxValue, periodMs);

 ButterflyEffect(LX lx) {
    super(lx);
  }

float time = 10000;
  public void run(double deltaMs) {

      for (BaseCube cube : model.baseCubes) {
        time += deltaMs/50;
        colors[cube.index] = lx.hsb(cube.globalTheta, satParam.getValuef(), (float) (-(time)));
        if (time > 8800000.0) { 
          time = 1;
        }

      }
  }
}
