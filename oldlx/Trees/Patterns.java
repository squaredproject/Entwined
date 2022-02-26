import toxi.geom.Vec2D;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;


class DoubleHelix extends TSPattern {
  
  final SinLFO rate = new SinLFO(400, 3000, 11000);
  final SawLFO theta = new SawLFO(0, 180, rate);
  final SinLFO coil = new SinLFO(0.2, 2, 13000);
  
  DoubleHelix(LX lx) {
    super(lx);
    addModulator(rate).start();
    addModulator(theta).start();
    addModulator(coil).start();
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    for (BaseCube cube : model.baseCubes) {
      float coilf = coil.getValuef() * (cube.cy - model.cy);
      colors[cube.index] = lx.hsb(
        lx.getBaseHuef() + .4f*Utils.abs(cube.transformedY - model.cy) +.2f* Utils.abs(cube.transformedTheta - 180),
        100,
        Utils.max(0, 100 - 2*LXUtils.wrapdistf(cube.transformedTheta, theta.getValuef() + coilf, 180))
      );
    }
  }
}

class ColoredLeaves extends TSPattern {
  
  private SawLFO[] movement;
  private SinLFO[] bright;
  
  ColoredLeaves(LX lx) {
    super(lx);
    movement = new SawLFO[3];
    for (int i = 0; i < movement.length; ++i) {
      movement[i] = new SawLFO(0, 360, 60000 / (1 + i));
      addModulator(movement[i]).start();
    }
    bright = new SinLFO[5];
    for (int i = 0; i < bright.length; ++i) {
      bright[i] = new SinLFO(100, 0, 60000 / (1 + i));
      addModulator(bright[i]).start();
    }
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    for (BaseCube cube : model.baseCubes) {
      colors[cube.index] = lx.hsb(
        (360 + movement[cube.index  % movement.length].getValuef()) % 360,
        100,
        bright[cube.index % bright.length].getValuef()
      );
    }
  }
}

class SeeSaw extends TSPattern {
  
  final LXProjection projection = new LXProjection(model);

  final SinLFO rate = new SinLFO(2000, 11000, 19000);
  final SinLFO rz = new SinLFO(-15, 15, rate);
  final SinLFO rx = new SinLFO(-70, 70, 11000);
  final SinLFO width = new SinLFO(1*Geometry.FEET, 8*Geometry.FEET, 13000);

  final BasicParameter bgLevel = new BasicParameter("BG", 25, 0, 50);
  
  SeeSaw(LX lx) {
    super(lx);
    addModulator(rate).start();
    addModulator(rx).start();
    addModulator(rz).start();
    addModulator(width).start();
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    projection
      .reset()
      .center()
      .rotate(rx.getValuef() * Utils.PI / 180, 1, 0, 0)
      .rotate(rz.getValuef() * Utils.PI / 180, 0, 0, 1);
    for (LXVector v : projection) {
      colors[v.index] = lx.hsb(
        (lx.getBaseHuef() + Utils.min(120, Utils.abs(v.y))) % 360,
        100,
        Utils.max(bgLevel.getValuef(), 100 - (100/(1*Geometry.FEET))*Utils.max(0, Utils.abs(v.y) - 0.5f*width.getValuef()))
      );
    }
  }
}

class Twister extends TSPattern {

  final SinLFO spin = new SinLFO(0, 5*360, 16000);
  
  float coil(float basis) {
    return Utils.sin(basis*Utils.TWO_PI - Utils.PI);
  }
  
  Twister(LX lx) {
    super(lx);
    addModulator(spin).start();
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    float spinf = spin.getValuef();
    float coilf = 2*coil(spin.getBasisf());
    for (BaseCube cube : model.baseCubes) {
      float wrapdist = LXUtils.wrapdistf(cube.transformedTheta, spinf + (model.yMax - cube.transformedY)*coilf, 360);
      float yn = (cube.transformedY / model.yMax);
      float width = 10 + 30 * yn;
      float df = Utils.max(0, 100 - (100 / 45) * Utils.max(0, wrapdist-width));
      colors[cube.index] = lx.hsb(
        (lx.getBaseHuef() + .2f*cube.transformedY - 360 - wrapdist) % 360,
        Utils.max(0, 100 - 500*Utils.max(0, yn-.8f)),
        df
      );
    }
  }
}

class TwisterGlobal extends TSPattern {

  final SinLFO spin = new SinLFO(0, 5*360, 16000);
  
  float coil(float basis) {
    return Utils.sin(basis*Utils.TWO_PI - Utils.PI);
  }
  
  TwisterGlobal(LX lx) {
    super(lx);
    addModulator(spin).start();
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    float spinf = spin.getValuef();
    float coilf = 2*coil(spin.getBasisf());
    for (BaseCube cube : model.baseCubes) {
      float wrapdist = LXUtils.wrapdistf(cube.globalTheta, spinf + (model.yMax - cube.y)*coilf, 360);
      float yn = (cube.y / model.yMax);
      float width = 10 + 30 * yn;
      float df = Utils.max(0, 100 - (100 / 45) * Utils.max(0, wrapdist-width));
      colors[cube.index] = lx.hsb(
        (lx.getBaseHuef() + .2f*cube.y - 360 - wrapdist) % 360,
        Utils.max(0, 100 - 500*Utils.max(0, yn-.8f)),
        df
      );
    }
  }
}


class SweepPattern extends TSPattern {
  
  final SinLFO speedMod = new SinLFO(3000, 9000, 5400);
  final SinLFO yPos = new SinLFO(model.yMin, model.yMax, speedMod);
  final SinLFO width = new SinLFO("WIDTH", 2*Geometry.FEET, 20*Geometry.FEET, 19000);
  
  final SawLFO offset = new SawLFO(0, Utils.TWO_PI, 9000);
  
  final BasicParameter amplitude = new BasicParameter("AMP", 10*Geometry.FEET, 0, 20*Geometry.FEET);
  final BasicParameter speed = new BasicParameter("SPEED", 1, 0, 3);
  final BasicParameter height = new BasicParameter("HEIGHT", 0, -300, 300);
  final SinLFO amp = new SinLFO(0, amplitude, 5000);
  
  SweepPattern(LX lx) {
    super(lx);
    addModulator(speedMod).start();
    addModulator(yPos).start();
    addModulator(width).start();
    addParameter(amplitude);
    addParameter(speed);
    addParameter(height);
    addModulator(amp).start();
    addModulator(offset).start();
  }
  
  public void onParameterChanged(LXParameter parameter) {
    super.onParameterChanged(parameter);
    if (parameter == speed) {
      float speedVar = 1/speed.getValuef();
      speedMod.setRange(9000 * speedVar,5400 * speedVar);
    }
  }
  
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    for (BaseCube cube : model.baseCubes) {
      float yp = yPos.getValuef() + amp.getValuef() * Utils.sin((cube.cx - model.cx) * .01f + offset.getValuef());
      colors[cube.index] = lx.hsb(
        (lx.getBaseHuef() + Utils.abs(cube.x - model.cx) * .2f +  cube.cz*.1f + cube.cy*.1f) % 360,
        Utils.constrain(Utils.abs(cube.transformedY - model.cy), 0, 100),
        Utils.max(0, 100 - (100/width.getValuef())*Utils.abs(cube.cy - yp - height.getValuef()))
      );
    }
  }
}

class DiffusionTestPattern extends TSPattern {
  
  final BasicParameter hue = new BasicParameter("HUE", 0, 360);
  final BasicParameter sat = new BasicParameter("SAT", 1);
  final BasicParameter brt = new BasicParameter("BRT", 1);
  final BasicParameter spread = new BasicParameter("SPREAD", 0, 360);
  
  DiffusionTestPattern(LX lx) {
    super(lx);
    addParameter(hue);
    addParameter(sat);
    addParameter(brt);
    addParameter(spread);
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    setColors(LXColor.BLACK);
    for (int i = 0; i < 12; ++i) {
      colors[i] = lx.hsb(
        (hue.getValuef() + (i / 4) * spread.getValuef()) % 360,
        sat.getValuef() * 100,
        Utils.min(100, brt.getValuef() * (i+1) / 12.f * 200)
      );
    }
  }
}

class TestPattern extends TSPattern {
  
  int CUBE_MOD = 14;
  
  final BasicParameter period = new BasicParameter("RATE", 3000, 2000, 6000);
  final SinLFO cubeIndex = new SinLFO(0, CUBE_MOD, period);
  
  TestPattern(LX lx) {
    super(lx);
    addModulator(cubeIndex).start();
    addParameter(period);
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    int ci = 0;
    for (BaseCube cube : model.baseCubes) {
      setColor(cube, lx.hsb(
        (lx.getBaseHuef() + cube.cx + cube.cy) % 360,
        100,
        Utils.max(0, 100 - 30*Utils.abs((ci % CUBE_MOD) - cubeIndex.getValuef()))
      ));
      ++ci;
    }
  }
}


class ColorEffect extends Effect {
  
  final BasicParameter desaturation = new BasicParameter("WHT", 0);
  final BasicParameter hueShift = new BasicParameter("HUE", 0, 360);
  final BasicParameter sharp = new BasicParameter("SHRP", 0);
  final BasicParameter soft = new BasicParameter("SOFT", 0);
  final BasicParameter mono = new BasicParameter("MON", 0);
  final BasicParameter rainbow = new BasicParameter("ACID", 0);
  
  private final DampedParameter hueShiftd = new DampedParameter(hueShift, 180);
  private final DampedParameter rainbowd = new DampedParameter(rainbow, 1);
  
  private float[] hsb = new float[3];

  // if set to a value >= 0, the effects are limited to only
  // the piece with that index (used for interactive effects)
  protected int pieceIndex = -1;
  
  ColorEffect(LX lx) {
    super(lx);
    addParameter(desaturation);
    addParameter(hueShift);
    addParameter(sharp);
    addParameter(soft);
    addParameter(mono);
    addParameter(rainbow);
    
    addModulator(hueShiftd).start();
    addModulator(rainbowd).start();
  }
  
  protected void run(double deltaMs) {
    float desatf = desaturation.getValuef();
    float huef = hueShiftd.getValuef();
    float sharpf = sharp.getValuef();
    float softf = soft.getValuef();
    float monof = mono.getValuef();
    float rainbowf = rainbowd.getValuef();
    if (desatf > 0 || huef > 0 || sharpf > 0 || softf > 0 || monof > 0 || rainbowf > 0) {
      float pSharp = 1/(1-.99f*sharpf);
      for (int i = 0; i < colors.length; ++i) {
        BaseCube cube = model.baseCubes.get(i);
        // if we're only applying this effect to a given pieceIndex, filter out and don't set colors on other cubes
        if (pieceIndex >= 0 && cube.pieceIndex != pieceIndex) continue;

        float b = LXColor.b(colors[i]) / 100.f;
        float bOrig = b;
        if (sharpf > 0) {
          if (b < .5f) {
            b = Utils.pow(b, pSharp);
          } else {
            b = 1-Utils.pow(1-b, pSharp);
          }
        }
        if (softf > 0) {
          if (b > 0.5f) {
            b = Utils.lerp(b, 0.5f + 2 * (b-0.5f)*(b-0.5f), softf);
          } else {
            b = Utils.lerp(b, 0.5f * Utils.sqrt(2*b), softf);
          }
        }
        
        float h = LXColor.h(colors[i]);
        float bh = lx.getBaseHuef();
        if (rainbowf > 0) {
          h = bh + (h - bh) * (1+3*rainbowf);
          h = (h + 5*360) % 360;
        }
        if (Utils.abs(h - bh) > 180) {
          if (h > bh) {
            bh += 360;
          } else {
            h += 360;
          }
        }
        
        colors[i] = lx.hsb(
          (Utils.lerp(h, bh, monof) + huef) % 360,
          LXColor.s(colors[i]) * (1 - desatf),
          100*b
        );
      }
    }
  }
}


// Three settings (?):
// which hue to use as the center
// how "wide" to make it
// consider: "squish" vs "push"

// Problem: it would be nice, especially on certain holidays, to have a filter that gives the entire sculpture
// a certain color. Valentine's day, St Patrics Day, Halloween, Christmas.... all have colors
// One option today is to put a Solid Color and Multiply - but this means the entire sculpture becomes
// that one color. We'd rather have white come through as white - which is why doing the filter in HSV would give
// a better result. Next question is how sophisticated to get - you could convolve with a sin curve (which)
//
// Need a way to enable and disable the effect. Not sure how to do that yet.
//
// First: let's just set the hue to a color, period. That should leave white and black where they are, because
// they aren't touched by Hue.
// Second: let's try having an "angle" (like 15 degrees) and everything outside that gets nailed to the hue in that range
// artist seems OK with the concept of "hue" and "angle", which gives us two real controls.
// We want DEG to be "transparent" at 0, and most at 180 (which is kinda backward)
// Author: Brian Bulkowski 2021 brian@bulkowsk.org

class HueFilterEffect extends Effect {
  
  final BasicParameter hueFilter = new BasicParameter("HUEF", 0, 360); // 0 to 360 starting at 0
  final BasicParameter amount = new BasicParameter("HDEG", 0, 180);
  
  //private float[] hsb = new float[3];
  
  HueFilterEffect(LX lx) {
    super(lx);
    addParameter(hueFilter);
    addParameter(amount);
  }

  static float norm360(float i) {
    while (i < 0.0f) {
      i += 360.0f;
    }
    while (i > 360.0f) {
      i -= 360.0f;
    }
    return(i);
  }


  // distance between a and b in degrees, absolute
  static float absdist360(float a, float b) {
    float r = Math.abs( a - b );
    if (r < 180.0f) return(r);
    return( 360.0f - r );
  }

  // distance between a and b in degrees, negative means a is counterclockwise
  // so for example a = 0, b = 190, the distance is 170, but postive, because the short path is clockwise
  static float dist360(float a, float b) {
    float r = a - b;
    if (Math.abs(r) <= 180.0f) return(r);
    if (r < 0.0f) return( r + 360.0f );
    return( r - 360.0f );
  }


  // quick bit of math: interpolate on the hue circle
  // but do so with a limit. Make sure the color is never outside of 
  // a certain number of degress. There's some sublty in when/how to clip the
  // edges, so will try a few things.
  // Interesting, having a limitdeg of 180 means no effect, that's the blend (basically)
  static float hueBlend(float src, float dst, float limitDeg) {
    float r;
    float dist = dist360(src,dst);
    r = dst + ( (dist / 180.0f) * limitDeg );
    r = norm360(r);

    // test: output should be within the limit distance of dst
    if ( absdist360(r, dst) > limitDeg ) {
      System.out.println("HueFilterEffect: hueBlendFail: src "+src+" dst "+dst+" res "+r+" limit "+limitDeg);
    }

    return(r);
  }
  
  protected void run(double deltaMs) {
    float huef = hueFilter.getValuef();
    float amountf = amount.getValuef();
    amountf = Math.abs(amountf - 180f);

    // todo: if enabled

    for (int i = 0; i < colors.length; ++i) {
        // float h = lerp360(LXColor.h(colors[i]), huef, amountf);

        float h = hueBlend(LXColor.h(colors[i]), huef, amountf);   

        colors[i] = lx.hsb( h, LXColor.s(colors[i]), LXColor.b(colors[i]) );
    }
  }
}



class TestShrubSweep extends TSPattern {
    
    final BasicParameter x;
    final BasicParameter y;
    final BasicParameter z;
    final BasicParameter beam;

    TestShrubSweep(LX lx) {
        super(lx);
        addParameter(x = new BasicParameter("X", 0, lx.model.xMin, lx.model.xMax));
        // the following y param should light the two shortest rods of a shrub when the beam is set to 1
        // may be useful for adjusting the rotation of the shrubs in the JSON config
//        addParameter(y = new BasicParameter("Y", 20.8, lx.model.yMin, lx.model.yMax)); 
        addParameter(y = new BasicParameter("Y", 0, lx.model.yMin, lx.model.yMax));
        addParameter(z = new BasicParameter("Z", 0, lx.model.zMin, lx.model.zMax));
        addParameter(beam = new BasicParameter("beam", 5, 1, 15));
    }
    
    public void run(double deltaMs) {
        if (getChannel().getFader().getNormalized() == 0) return;
        
        for (BaseCube cube : model.baseCubes) {
            if (Utils.abs(cube.ax - x.getValuef()) < beam.getValuef() || 
                    Utils.abs(cube.ay - y.getValuef()) < beam.getValuef() || 
                    Utils.abs(cube.az - z.getValuef()) < beam.getValuef()) {
                colors[cube.index] = lx.hsb(135, 100, 100);    
            } else {
                colors[cube.index] = lx.hsb(135, 100, 0);    
            }
        }
    }
}

class TestShrubLayers extends TSPattern {
    
    final BasicParameter rodLayer;
    final BasicParameter clusterIndex;
    final BasicParameter shrubIndex;
    
    TestShrubLayers(LX lx) {
        super(lx);
        // lowest level means turning that param off
        addParameter(rodLayer = new BasicParameter("layer", 0, 0, 5));
        addParameter(clusterIndex = new BasicParameter("clusterIndex", -1, -1, 11));
        addParameter(shrubIndex = new BasicParameter("shrubIndex", -1, -1, 19));
        
    }
    
    public void run(double deltaMs) {
        if (getChannel().getFader().getNormalized() == 0) return;
        
        for (BaseCube cube : model.baseCubes) {
            if (cube.pieceType == PieceType.SHRUB) {
                ShrubCube shrubCube = (ShrubCube) cube;              
                
                if (shrubCube.config.rodIndex == (int)rodLayer.getValue() || shrubCube.config.clusterIndex == (int)clusterIndex.getValue() || shrubCube.config.shrubIndex == (int)shrubIndex.getValue()) {
                    colors[cube.index] = lx.hsb(135, 100, 100);    
                } else {
                    colors[cube.index] = lx.hsb(135, 100, 0);    
                }
            }
        }
    }
}
