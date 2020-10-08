import java.util.ArrayList;
import java.util.Iterator;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import toxi.geom.Vec2D;
import toxi.math.noise.PerlinNoise;
import toxi.math.noise.SimplexNoise;

class MappingPattern extends TSPattern {

  int numBits;
  int count;
  int numCompleteResetCycles = 10;
  int numCyclesToShowFrame = 2;
  int numResetCycles = 3;
  int numCyclesBlack = 4;
  int cycleCount = 0;

  MappingPattern(LX lx) {
    super(lx);

    numBits = model.cubes.size();
  }

  public void run(double deltaMs) {
    if (count >= numBits) {
      if (numBits + numCyclesBlack <= count && count < numBits + numCyclesBlack + numCompleteResetCycles) {
        setColors(LXColor.WHITE);
      } else {
        setColors(LXColor.BLACK);
      }
    } else if (cycleCount >= numCyclesToShowFrame) {
      if (numCyclesToShowFrame + numCyclesBlack <= cycleCount && cycleCount < numCyclesToShowFrame + numCyclesBlack + numResetCycles) {
        setColors(LXColor.WHITE);
      } else {
        setColors(LXColor.BLACK);
      }
    } else {
      for (Cube cube : model.cubes) {
        setColor(cube.index, cube.index == count ? LXColor.WHITE : LXColor.BLACK);
      }
    }
    cycleCount = (cycleCount + 1) % (numCyclesToShowFrame + numResetCycles + 2*numCyclesBlack);
    if (cycleCount == 0) {
      count = (count + 1) % (numBits + numCompleteResetCycles + 2*numCyclesBlack);
    }
  }
}

class TurnOffDeadPixelsEffect extends Effect {
  int[] deadPixelIndices = new int[] { };
  int[] deadPixelClusters = new int[] { };
  
  TurnOffDeadPixelsEffect(LX lx) {
    super(lx);
  }
  
  public void run(double deltaMs) {
    for (int i = 0; i < deadPixelIndices.length; i++) {
      //Cluster cluster = model.clusters.get(deadPixelClusters[i]);
      //Cube cube = cluster.cubes.get(deadPixelIndices[i]);
      //colors[cube.index] = LXColor.BLACK;
    }
  }
}

class VecUtils {
  static boolean insideOfBoundingBox(Vec2D origin, Vec2D point, float xTolerance, float yTolerance) {
    return Utils.abs(origin.x - point.x) <= xTolerance && Utils.abs(origin.y - point.y) <= yTolerance;
  }
   
  static float wrapDist2d(Vec2D a, Vec2D b) {
    return Utils.sqrt(Utils.pow((LXUtils.wrapdistf(a.x, b.x, 360)), 2) + Utils.pow(a.y - b.y, 2));
  }
   
  static Vec2D movePointToSamePlane(Vec2D reference, Vec2D point) {
    return new Vec2D(VecUtils.moveThetaToSamePlane(reference.x, point.x), point.y);
  }
   
  // Assumes thetaA as a reference point
  // Moves thetaB to within 180 degrees, letting thetaB go beyond [0, 360)
  static float moveThetaToSamePlane(float thetaA, float thetaB) {
    if (thetaA - thetaB > 180) {
      return thetaB + 360;
    } else if (thetaB - thetaA > 180) {
      return thetaB - 360;
    } else {
      return thetaB;
    }
  }

  static float thetaDistance(float thetaA, float thetaB) {
    return LXUtils.wrapdistf(thetaA, thetaB, 360);
  }

}

class BassSlam extends TSTriggerablePattern {
  
  final private double flashTimePercent = 0.1f;
  final private int patternHue = 200;
  
  BassSlam(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    if (triggerableModeEnabled) {
      firedTimer += deltaMs / 800;
      if (firedTimer > 1) {
        setCallRun(false);
        return;
      }
    }

    if (progress() < flashTimePercent) {
      setColors(lx.hsb(patternHue, 100, 100));
    } else {
      float time = (float)((progress() - flashTimePercent) / (1 - flashTimePercent) * 1.3755f);
      float y;
      // y = 0 when time = 1.3755f
      if (time < 1) {
        y = 1 + Utils.pow(time + 0.16f, 2) * Utils.sin(18 * (time + 0.16f)) / 4;
      } else {
        y = 1.32f - 20 * Utils.pow(time - 1, 2);
      }
      y = Utils.max(0, 100 * (y - 1) + 250);
      
      for (Cube cube : model.cubes) {
        setColor(cube.index, lx.hsb(patternHue, 100, LXUtils.constrainf(100 - 2 * Utils.abs(y - cube.transformedY), 0, 100)));
      }
    }
  }

  double progress() {
    return triggerableModeEnabled ? ((firedTimer + flashTimePercent) % 1) : lx.tempo.ramp();
  }
}

abstract class MultiObjectPattern <ObjectType extends MultiObject> extends TSTriggerablePattern {
  
  BasicParameter frequency;
  
  final boolean shouldAutofade;
  float fadeTime = 1000;
  
  final ArrayList<ObjectType> objects;
  double pauseTimerCountdown = 0;
//  BasicParameter fadeLength
  
  MultiObjectPattern(LX lx) {
    this(lx, true);
  }
  
  MultiObjectPattern(LX lx, double initial_frequency) {
    this(lx, true);
    frequency.setValue(initial_frequency);
  }

  MultiObjectPattern(LX lx, boolean shouldAutofade) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;
    
    frequency = getFrequencyParameter();
    addParameter(frequency);
    
    this.shouldAutofade = shouldAutofade;
//    if (shouldAutofade) {
      
    
    objects = new ArrayList<ObjectType>();
  }
  
  BasicParameter getFrequencyParameter() {
    return new BasicParameter("FREQ", .5, .1, 40, BasicParameter.Scaling.QUAD_IN);
  }
  
//  BasicParameter getAutofadeParameter() {
//    return new BasicParameter("TAIL", 
//  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    if (triggered) {
      pauseTimerCountdown -= deltaMs;

      if (pauseTimerCountdown <= 0) {
        float delay = 1000 / frequency.getValuef();
        pauseTimerCountdown = Utils.random(delay / 2) + delay * 3 / 4;
        makeObject(0);
      }
    } else if (objects.size() == 0) {
      setCallRun(false);
    }
    
    if (shouldAutofade) {
      for (Cube cube : model.cubes) {
        blendColor(cube.index, lx.hsb(0, 0, 100 * Utils.max(0, (float)(1 - deltaMs / fadeTime))), LXColor.Blend.MULTIPLY);
      }
    } else {
      clearColors();
    }
    
    if (objects.size() > 0) {
      Iterator<ObjectType> iter = objects.iterator();
      while (iter.hasNext()) {
        ObjectType object = iter.next();
        if (!object.running) {
          layers.remove(object);
          iter.remove();
        }
      }
    }
  }
  
  void makeObject(float strength) {
    ObjectType object = generateObject(strength);
    object.init();
    addLayer(object);
    objects.add(object);
  }
  
  public void onTriggered(float strength) {
    super.onTriggered(strength);

    makeObject(strength);
  }
    
  abstract ObjectType generateObject(float strength);
}

abstract class MultiObject extends Layer {
  
  boolean firstRun = true;
  float runningTimer = 0;
  float runningTimerEnd = 1000;
  boolean running = true;
  float progress;
  int hue = LXColor.BLACK;
  float thickness;
  boolean shouldFade = true;
  
  Vec2D lastPoint;
  Vec2D currentPoint;
  float fadeIn = 1;
  float fadeOut = 1;
  
  MultiObject(LX lx) {
    super(lx);
  }
  
  public void run(double deltaMs) {
    if (running) {
      if (firstRun) {
        advance(0);
        firstRun = false;
      } else {
        advance(deltaMs);
      }
      if (running) {
        for (Cube cube : model.cubes) {
          blendColor(cube.index, getColorForCube(cube), LXColor.Blend.LIGHTEST);
        }
      }
    }
  }
  
  protected void advance(double deltaMs) {
    if (running) {
      runningTimer += deltaMs;
      if (runningTimer >= runningTimerEnd) {
        running = false;
      } else {
        progress = runningTimer / runningTimerEnd;
        if (shouldFade) {
          fadeIn = Utils.min(1, 3 * (1 - progress));
          fadeOut = Utils.min(1, 3 * progress);
        }
        if (currentPoint == null) {
        }
        lastPoint = currentPoint;
        onProgressChanged(progress);
      }
    }
  }
  
  public int getColorForCube(Cube cube) {
    return lx.hsb(hue, 100, getBrightnessForCube(cube));
  }
  
  public float getBrightnessForCube(Cube cube) {
    Vec2D cubePointPrime = VecUtils.movePointToSamePlane(currentPoint, cube.transformedCylinderPoint);
    float dist = Float.MAX_VALUE;

    Vec2D localLastPoint = lastPoint;
    if (localLastPoint != null) {
      while (localLastPoint.distanceToSquared(currentPoint) > 100) {
        Vec2D point = currentPoint.sub(localLastPoint);
        point.limit(10).addSelf(localLastPoint);

        if (isInsideBoundingBox(cube, cubePointPrime, point)) {
          dist = Utils.min(dist, getDistanceFromGeometry(cube, cubePointPrime, point));
        }
        localLastPoint = point;
      }
    }

    if (isInsideBoundingBox(cube, cubePointPrime, currentPoint)) {
      dist = Utils.min(dist, getDistanceFromGeometry(cube, cubePointPrime, currentPoint));
    }
    return 100 * Utils.min(Utils.max(1 - dist / thickness, 0), 1) * fadeIn * fadeOut;
  }

  public boolean isInsideBoundingBox(Cube cube, Vec2D cubePointPrime, Vec2D currentPoint) {
    return VecUtils.insideOfBoundingBox(currentPoint, cubePointPrime, thickness, thickness);
  }

  public float getDistanceFromGeometry(Cube cube, Vec2D cubePointPrime, Vec2D currentPoint) {
    return cubePointPrime.distanceTo(currentPoint);
  }
  
  void init() { }
  
  public void onProgressChanged(float progress) { }
}

class Explosions extends MultiObjectPattern<Explosion> {
  
  ArrayList<Explosion> explosions;
  
  Explosions(LX lx) {
    this(lx, 0.5f);
  }
  
  Explosions(LX lx, double speed) {
    super(lx, false);
    
    explosions = new ArrayList<Explosion>();

    frequency.setValue(speed);
  }
  
  BasicParameter getFrequencyParameter() {
    return new BasicParameter("FREQ", .50, .1, 20, BasicParameter.Scaling.QUAD_IN);
  }
  
  Explosion generateObject(float strength) {
    Explosion explosion = new Explosion(lx);
    explosion.origin = new Vec2D(Utils.random(360), (float)LXUtils.random(model.yMin + 50, model.yMax - 50));
    explosion.hue = (int) Utils.random(360);
    return explosion;
  }
}

class Explosion extends MultiObject {
  
  final static int EXPLOSION_STATE_IMPLOSION_EXPAND = 1 << 0;
  final static int EXPLOSION_STATE_IMPLOSION_WAIT = 1 << 1;
  final static int EXPLOSION_STATE_IMPLOSION_CONTRACT = 1 << 2;
  final static int EXPLOSION_STATE_EXPLOSION = 1 << 3;
  
  Vec2D origin;
  
  float accelOfImplosion = 3000;
  Accelerator implosionRadius;
  float implosionWaitTimer = 100;
  Accelerator explosionRadius;
  LXModulator explosionFade;
  float explosionThetaOffset;
  
  int state = EXPLOSION_STATE_IMPLOSION_EXPAND;
  
  Explosion(LX lx) {
    super(lx);
  }
  
  void init() {
    explosionThetaOffset = Utils.random(360);
    implosionRadius = new Accelerator(0, 700, -accelOfImplosion);
    addModulator(implosionRadius).start();
    explosionFade = new LinearEnvelope(1, 0, 1000);
  }
  
  protected void advance(double deltaMs) {
    switch (state) {
      case EXPLOSION_STATE_IMPLOSION_EXPAND:
        if (implosionRadius.getVelocityf() <= 0) {
          state = EXPLOSION_STATE_IMPLOSION_WAIT;
          implosionRadius.stop();
        }
        break;
      case EXPLOSION_STATE_IMPLOSION_WAIT:
        implosionWaitTimer -= deltaMs;
        if (implosionWaitTimer <= 0) {
          state = EXPLOSION_STATE_IMPLOSION_CONTRACT;
          implosionRadius.setAcceleration(-8000);
          implosionRadius.start();
        }
        break;
      case EXPLOSION_STATE_IMPLOSION_CONTRACT:
        if (implosionRadius.getValuef() < 0) {
          removeModulator(implosionRadius).stop();
          state = EXPLOSION_STATE_EXPLOSION;
          explosionRadius = new Accelerator(0, -implosionRadius.getVelocityf(), -300);
          addModulator(explosionRadius).start();
          addModulator(explosionFade).start();
        }
        break;
      default:
        if (explosionFade.getValuef() <= 0) {
          running = false;
          removeModulator(explosionRadius).stop();
          removeModulator(explosionFade).stop();
        }
        break;
    }
  }
  
  public float getBrightnessForCube(Cube cube) {
    Vec2D cubePointPrime = VecUtils.movePointToSamePlane(origin, cube.transformedCylinderPoint);
    float dist = origin.distanceTo(cubePointPrime);
    switch (state) {
      case EXPLOSION_STATE_IMPLOSION_EXPAND:
      case EXPLOSION_STATE_IMPLOSION_WAIT:
      case EXPLOSION_STATE_IMPLOSION_CONTRACT:
        return 100 * LXUtils.constrainf((implosionRadius.getValuef() - dist) / 10, 0, 1);
      default:
        float theta = explosionThetaOffset + cubePointPrime.sub(origin).heading() * 180 / Utils.PI + 360;
        return 100
            * LXUtils.constrainf(1 - (dist - explosionRadius.getValuef()) / 10, 0, 1)
            * LXUtils.constrainf(1 - (explosionRadius.getValuef() - dist) / 200, 0, 1)
            * LXUtils.constrainf((1 - Utils.abs(theta % 30 - 15) / 100 / Utils.asin(20 / Utils.max(20, dist))), 0, 1)
            * explosionFade.getValuef();
    }
  }
}

class Wisps extends MultiObjectPattern<Wisp> {
  
  final BasicParameter baseColor = new BasicParameter("COLR", 210, 360);
  final BasicParameter colorVariability = new BasicParameter("CVAR", 10, 180);
  final BasicParameter direction = new BasicParameter("DIR", 90, 360);
  final BasicParameter directionVariability = new BasicParameter("DVAR", 20, 180);
  final BasicParameter thickness = new BasicParameter("WIDT", 3.5f, 1, 20, BasicParameter.Scaling.QUAD_IN);
  final BasicParameter speed = new BasicParameter("SPEE", 10, 1, 20, BasicParameter.Scaling.QUAD_IN);

  // Possible other parameters:
  //  Distance
  //  Distance variability
  //  width variability
  //  Speed variability
  //  frequency variability
  //  Fade time

  Wisps(LX lx) {
    this(lx, .5f, 210, 10, 90, 20, 3.5f, 10);
  }

  Wisps(LX lx, double initial_frequency, double initial_color,
        double initial_colorVariability, double initial_direction,
        double initial_directionVariability, double initial_thickness,
        double initial_speed) {
    super(lx, initial_frequency);

    addParameter(baseColor);
    addParameter(colorVariability);
    addParameter(direction);
    addParameter(directionVariability);
    addParameter(thickness);
    addParameter(speed);

    baseColor.setValue(initial_color);
    colorVariability.setValue(initial_colorVariability);
    direction.setValue(initial_direction);
    directionVariability.setValue(initial_directionVariability);
    thickness.setValue(initial_thickness);
    speed.setValue(initial_speed);

  };

  Wisp generateObject(float strength) {
    Wisp wisp = new Wisp(lx);
    wisp.runningTimerEnd = 5000 / speed.getValuef();
    float pathDirection = (float)(direction.getValuef()
      + LXUtils.random(-directionVariability.getValuef(), directionVariability.getValuef())) % 360;
    float pathDist = (float)LXUtils.random(200, 400);
    float startTheta = Utils.random(360);
    float startY = (float)LXUtils.random(Utils.max(model.yMin, model.yMin - pathDist * Utils.sin(Utils.PI * pathDirection / 180)),
      Utils.min(model.yMax, model.yMax - pathDist * Utils.sin(Utils.PI * pathDirection / 180)));
    wisp.startPoint = new Vec2D(startTheta, startY);
    wisp.endPoint = Vec2D.fromTheta(pathDirection * Utils.PI / 180);
    wisp.endPoint.scaleSelf(pathDist);
    wisp.endPoint.addSelf(wisp.startPoint);
    wisp.hue = (int)(baseColor.getValuef()
      + LXUtils.random(-colorVariability.getValuef(), colorVariability.getValuef())) % 360;
    wisp.thickness = 10 * thickness.getValuef() + (float)LXUtils.random(-3, 3);
    
    return wisp;
  }
}

class Wisp extends MultiObject {
  
  Vec2D startPoint;
  Vec2D endPoint;
  
  Wisp(LX lx) {
    super(lx);
  }
  
  public void onProgressChanged(float progress) {
    currentPoint = startPoint.interpolateTo(endPoint, progress);
  }
}

class Rain extends MultiObjectPattern<RainDrop> {
  
  Rain(LX lx) {
    super(lx);
    fadeTime = 500;
  }
  
  BasicParameter getFrequencyParameter() {
    return new BasicParameter("FREQ", 40, 1, 75);
  }
   
  RainDrop generateObject(float strength) {
    RainDrop rainDrop = new RainDrop(lx);

    rainDrop.runningTimerEnd = 180 + Utils.random(20);
    rainDrop.theta = Utils.random(360);
    rainDrop.startY = model.yMax + 20;
    rainDrop.endY = model.yMin - 20;
    rainDrop.hue = 200 + (int)Utils.random(20);
    rainDrop.thickness = 10 * (1.5f + Utils.random(.6f));
    
    return rainDrop;
  }
}

class RainDrop extends MultiObject {
  
  float theta;
  float startY;
  float endY;
  
  RainDrop(LX lx) {
    super(lx);
    shouldFade = false;
  }
  
  public void onProgressChanged(float progress) {
    currentPoint = new Vec2D(theta, (float)LXUtils.lerp(startY, endY, progress));
  }
}

class Strobe extends TSTriggerablePattern {
  
  final BasicParameter speed = new BasicParameter("SPEE", 200, 3000, 30, BasicParameter.Scaling.QUAD_OUT);
  final BasicParameter balance = new BasicParameter("BAL", .5, .01, .99);

  int timer = 0;
  boolean on = false;
  
  Strobe(LX lx) {
    super(lx);
    
    addParameter(speed);
    addParameter(balance);
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    if (triggered) {
      timer += deltaMs;
      if (timer >= speed.getValuef() * (on ? balance.getValuef() : 1 - balance.getValuef())) {
        timer = 0;
        on = !on;
      }
      
      setColors(on ? LXColor.WHITE : LXColor.BLACK);
    }
  }
  
  public void onTriggered(float strength) {
    super.onTriggered(strength);

    on = true;
  }
  
  public void onRelease() {
    super.onRelease();

    timer = 0;
    on = false;
    setColors(LXColor.BLACK);
  }
}

class StrobeOneshot extends TSTriggerablePattern {
  
  StrobeOneshot(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;

    setColors(LXColor.WHITE);
  }
  
  public void run(double deltaMs) {
    firedTimer += deltaMs;
    if (firedTimer >= 80) {
      setCallRun(false);
    }
  }
}

class Brightness extends TSTriggerablePattern {
  
  Brightness(LX lx) {
    super(lx);
  }
  
  public void run(double deltaMs) {
  }
  
  public void onTriggered(float strength) {
    setColors(lx.hsb(0, 0, 100 * strength));
  }
  
  public void onRelease() {
    setColors(LXColor.BLACK);
  }
}

class RandomColor extends TSPattern {
  
  final BasicParameter speed = new BasicParameter("Speed", 1, 1, 10);
  
  int frameCount = 0;
  
  RandomColor(LX lx) {
    super(lx);
    addParameter(speed);
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    frameCount++;
    if (frameCount >= speed.getValuef()) {
      for (Cube cube : model.cubes) {
        colors[cube.index] = lx.hsb(
          Utils.random(360),
          100,
          100
        );
      }
      frameCount = 0;
    }
  }
}

class ColorStrobe extends TSTriggerablePattern {

  double timer = 0;
  
  ColorStrobe(LX lx) {
    super(lx);
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    timer += deltaMs;
    if (timer > 16) {
      timer = 0;
      setColors(lx.hsb(Utils.random(360), 100, 100));
    }
  }
}

class RandomColorGlitch extends TSPattern {
  
  RandomColorGlitch(LX lx) {
    super(lx);
  }
  
  final int brokenCubeIndex = (int)Utils.random(model.cubes.size());
  final int cubeColor = (int)Utils.random(360);
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    for (Cube cube : model.cubes) {
      if (cube.index == brokenCubeIndex) {
        colors[cube.index] = lx.hsb(
          Utils.random(360),
          100,
          100
        );
      } else {
        colors[cube.index] = lx.hsb(
          cubeColor,
          100,
          100
        );
      }
    }
  }
}

class Fade extends TSPattern {
  
  final BasicParameter speed = new BasicParameter("SPEE", 11000, 100000, 1000, BasicParameter.Scaling.QUAD_OUT);
  final BasicParameter smoothness = new BasicParameter("SMOO", 100, 1, 100, BasicParameter.Scaling.QUAD_IN);

  final SinLFO colr = new SinLFO(0, 360, speed);

  Fade(LX lx) {
    super(lx);
    addParameter(speed);
    addParameter(smoothness);
    addModulator(colr).start();
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    for (Cube cube : model.cubes) {
      colors[cube.index] = lx.hsb(
        (int)((int)colr.getValuef() * smoothness.getValuef() / 100) * 100 / smoothness.getValuef(), 
        100, 
        100
      );
    }
  }
}


class Palette extends TSPattern {
  
  Palette(LX lx) {
    super(lx);
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    for (Cube cube : model.cubes) {
      colors[cube.index] = lx.hsb(
        cube.index % 360,
        100,
        100
      );
    }
  }
}

class SolidColor extends TSPattern {
  // 235 = blue, 135 = green, 0 = red
  final BasicParameter hue = new BasicParameter("HUE", 135, 0, 360);
  final BasicParameter brightness = new BasicParameter("BRT", 100, 0, 100);
  
  SolidColor(LX lx) {
    super(lx);
    addParameter(hue);
    addParameter(brightness);
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    setColors(lx.hsb(hue.getValuef(), 100, (float)brightness.getValue()));
  }
}

class ClusterLineTest extends TSPattern {
  
  final BasicParameter y;
  final BasicParameter theta;
  final BasicParameter spin;
  
  ClusterLineTest(LX lx) {
    super(lx);
    
    addParameter(theta = new BasicParameter("\u0398", 0, -90, 430));
    addParameter(y = new BasicParameter("Y", 200, lx.model.yMin, lx.model.yMax));
    addParameter(spin = new BasicParameter("SPIN", 0, -90, 430));
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;
    
    Vec2D origin = new Vec2D(theta.getValuef(), y.getValuef());
    for (Cube cube : model.cubes) {
      Vec2D cubePointPrime = VecUtils.movePointToSamePlane(origin, cube.transformedCylinderPoint);
      float dist = origin.distanceTo(cubePointPrime);
      float cubeTheta = (spin.getValuef() + 15) + cubePointPrime.sub(origin).heading() * 180 / Utils.PI + 360;
      colors[cube.index] = lx.hsb(135, 100, 100
          * LXUtils.constrainf((1 - Utils.abs(cubeTheta % 90 - 15) / 100 / Utils.asin(20 / Utils.max(20, dist))), 0, 1));
    }
  }
}

class GhostEffect extends Effect {
  
  final BasicParameter amount = new BasicParameter("GHOS", 0, 0, 1, BasicParameter.Scaling.QUAD_IN);
  
  GhostEffect(LX lx) {
    super(lx);
    addLayer(new GhostEffectsLayer(lx));
  }
  
  protected void run(double deltaMs) {
  }
  
  class GhostEffectsLayer extends Layer {
    
    GhostEffectsLayer(LX lx) {
      super(lx);
      addParameter(amount);
    }
  
    float timer = 0;
    ArrayList<GhostEffectLayer> ghosts = new ArrayList<GhostEffectLayer>();
    
    public void run(double deltaMs) {
      if (amount.getValue() != 0) {
        timer += deltaMs;
        float lifetime = (float)amount.getValue() * 2000;
        if (timer >= lifetime) {
          timer = 0;
          GhostEffectLayer ghost = new GhostEffectLayer(lx);
          ghost.lifetime = lifetime * 3;
          addLayer(ghost);
          ghosts.add(ghost);
        }
      }
      if (ghosts.size() > 0) {
        Iterator<GhostEffectLayer> iter = ghosts.iterator();
        while (iter.hasNext()) {
          GhostEffectLayer ghost = iter.next();
          if (!ghost.running) {
            layers.remove(ghost);
            iter.remove();
          }
        }
      }      
    }
    
    public void onParameterChanged(LXParameter parameter) {
      if (parameter == amount && parameter.getValue() == 0) {
        timer = 0;
      }
    }
  }
  
  class GhostEffectLayer extends Layer {
    
    float lifetime;
    boolean running = true;
  
    private int[] ghostColors = null;
    float timer = 0;
    
    GhostEffectLayer(LX lx) {
      super(lx);
    }
    
    public void run(double deltaMs) {
      if (running) {
        timer += (float)deltaMs;
        if (timer >= lifetime) {
          running = false;
        } else {
          if (ghostColors == null) {
            ghostColors = new int[colors.length];
            for (int i = 0; i < colors.length; i++) {
              ghostColors[i] = colors[i];
            }
          }
          
          for (int i = 0; i < colors.length; i++) {
            ghostColors[i] = LXColor.blend(ghostColors[i], lx.hsb(0, 0, 100 * Utils.max(0, (float)(1 - deltaMs / lifetime))), LXColor.Blend.MULTIPLY);
            blendColor(i, ghostColors[i], LXColor.Blend.LIGHTEST);
          }
        }
      }
    }
  }
}

class ScrambleEffect extends Effect {
  
  final BasicParameter amount = new BasicParameter("SCRA");
  final int offset;
  
  ScrambleEffect(LX lx) {
    super(lx);
    
    offset = lx.total / 4 + 5;
  }

  int getAmount() {
    return (int)(amount.getValue() * lx.total / 2);
  }
  
  protected void run(double deltaMs) {
    for (Tree tree : model.trees) {
      for (int i = Utils.min(tree.cubes.size() - 1, getAmount()); i > 0; i--) {
        colors[tree.cubes.get(i).index] = colors[tree.cubes.get((i + offset) % tree.cubes.size()).index];
      }
    }
  }
}

class StaticEffect extends Effect {
  
  final BasicParameter amount = new BasicParameter("STTC");
  
  private boolean isCreatingStatic = false;
  
  StaticEffect(LX lx) {
    super(lx);
  }
  
  protected void run(double deltaMs) {
    if (amount.getValue() > 0) {
      if (isCreatingStatic) {
        double chance = Utils.random(1);
        if (chance > amount.getValue()) {
          isCreatingStatic = false;
        }
      } else {
        double chance = Utils.random(1);
        if (chance < amount.getValue()) {
          isCreatingStatic = true;
        }
      }
      if (isCreatingStatic) {
        for (int i = 0; i < colors.length; i++) {
          colors[i] = (int)Utils.random(255);
        }
      }
    }
  }
}

class SpeedEffect extends Effect {

  final BasicParameter speed = new BasicParameter("SPEED", 1, .1, 10, BasicParameter.Scaling.QUAD_IN);

  SpeedEffect(final LX lx) {
    super(lx);

    speed.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        lx.engine.setSpeed(speed.getValue());
      }
    });
  }

  protected void onEnable() {
    super.onEnable();
    lx.engine.setSpeed(speed.getValue());
  }

  public void run(double deltaMs) {}
}

class RotationEffect extends ModelTransform {
  
  final BasicParameter rotation = new BasicParameter("ROT", 0, 0, 360);

  RotationEffect(LX lx) {
    super(lx);
  }

  void transform(Model model) {
    if (rotation.getValue() > 0) {
      float rotationTheta = rotation.getValuef();
      for (Cube cube : model.cubes) {
        cube.transformedTheta = (cube.transformedTheta + 360 - rotationTheta) % 360;
      }
    }
  }
}

class SpinEffect extends ModelTransform {
  
  final BasicParameter spin = new BasicParameter("SPIN");
  final FunctionalParameter rotationPeriodMs = new FunctionalParameter() {
    public double getValue() {
      return 5000 - 4800 * spin.getValue();
    }
  };
  final SawLFO rotation = new SawLFO(0, 360, rotationPeriodMs);

  SpinEffect(LX lx) {
    super(lx);

    addModulator(rotation);

    spin.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (spin.getValue() > 0) {
          rotation.start();
          rotation.setLooping(true);
        } else {
          rotation.setLooping(false);
        }
      }
    });
  }

  void transform(Model model) {
    if (rotation.getValue() > 0 && rotation.getValue() < 360) {
      float rotationTheta = rotation.getValuef();
      for (Cube cube : model.cubes) {
        cube.transformedTheta = (cube.transformedTheta + 360 - rotationTheta) % 360;
      }
    }
  }
}

class ColorStrobeTextureEffect extends Effect {

  final BasicParameter amount = new BasicParameter("SEIZ", 0, 0, 1, BasicParameter.Scaling.QUAD_IN);

  ColorStrobeTextureEffect(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    if (amount.getValue() > 0) {
      float newHue = Utils.random(360);
      int newColor = lx.hsb(newHue, 100, 100);
      for (int i = 0; i < colors.length; i++) {
        int oldColor = colors[i];
        int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
        colors[i] = lx.hsb(LXColor.h(blendedColor), LXColor.s(blendedColor), LXColor.b(oldColor));
      }
    }
  }
}

class FadeTextureEffect extends Effect {

  final BasicParameter amount = new BasicParameter("FADE");

  final SawLFO colr = new SawLFO(0, 360, 10000);

  FadeTextureEffect(LX lx) {
    super(lx);

    addModulator(colr).start();
  }

  public void run(double deltaMs) {
    if (amount.getValue() > 0) {
      float newHue = colr.getValuef();
      int newColor = lx.hsb(newHue, 100, 100);
      for (int i = 0; i < colors.length; i++) {
        int oldColor = colors[i];
        int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
        colors[i] = lx.hsb(LXColor.h(blendedColor), LXColor.s(blendedColor), LXColor.b(oldColor));
      }
    }
  }
}

class AcidTripTextureEffect extends Effect {

  final BasicParameter amount = new BasicParameter("ACID");
  
  final SawLFO trails = new SawLFO(360, 0, 7000);

  AcidTripTextureEffect(LX lx) {
    super(lx);
    
    addModulator(trails).start();
  }

  public void run(double deltaMs) {
    if (amount.getValue() > 0) {
      for (int i = 0; i < colors.length; i++) {
        int oldColor = colors[i];
        Cube cube = model.cubes.get(i);
        float newHue = Utils.abs(model.cy - cube.transformedY) + Utils.abs(model.cy - cube.transformedTheta) + trails.getValuef() % 360;
        int newColor = lx.hsb(newHue, 100, 100);
        int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
        colors[i] = lx.hsb(LXColor.h(blendedColor), LXColor.s(blendedColor), LXColor.b(oldColor));
      }
    }
  }
}

class CandyTextureEffect extends Effect {

  final BasicParameter amount = new BasicParameter("CAND");

  double time = 0;

  CandyTextureEffect(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    if (amount.getValue() > 0) {
      time += deltaMs;
      for (int i = 0; i < colors.length; i++) {
        int oldColor = colors[i];
        float newHue = i * 127 + 9342 + (float)time % 360;
        int newColor = lx.hsb(newHue, 100, 100);
        int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
        colors[i] = lx.hsb(LXColor.h(blendedColor), LXColor.s(blendedColor), LXColor.b(oldColor));
      }
    }
  }
}

class CandyCloudTextureEffect extends Effect {

  final BasicParameter amount = new BasicParameter("CLOU");

  double time = 0;
  final double scale = 2400;
  final double speed = 1.0f / 5000;

  CandyCloudTextureEffect(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    if (amount.getValue() > 0) {
      time += deltaMs;
      for (int i = 0; i < colors.length; i++) {
        int oldColor = colors[i];
        Cube cube = model.cubes.get(i);

        double adjustedX = cube.x / scale;
        double adjustedY = cube.y / scale;
        double adjustedZ = cube.z / scale;
        double adjustedTime = time * speed;

        float newHue = ((float)SimplexNoise.noise(adjustedX, adjustedY, adjustedZ, adjustedTime) + 1) / 2 * 1080 % 360;
        int newColor = lx.hsb(newHue, 100, 100);

        int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
        colors[i] = lx.hsb(LXColor.h(blendedColor), LXColor.s(blendedColor), LXColor.b(oldColor));
      }
    }
  }
}

class CandyCloud extends TSPattern {

  final BasicParameter darkness = new BasicParameter("DARK", 8, 0, 12);

  final BasicParameter scale = new BasicParameter("SCAL", 2400, 600, 10000);
  final BasicParameter speed = new BasicParameter("SPD", 1, 1, 2);

  double time = 0;

  CandyCloud(LX lx) {
    super(lx);

    addParameter(darkness);
  }

  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    time += deltaMs;
    for (Cube cube : model.cubes) {
      double adjustedX = cube.x / scale.getValue();
      double adjustedY = cube.y / scale.getValue();
      double adjustedZ = cube.z / scale.getValue();
      double adjustedTime = time * speed.getValue() / 5000;

      float hue = ((float)SimplexNoise.noise(adjustedX, adjustedY, adjustedZ, adjustedTime) + 1) / 2 * 1080 % 360;

      float brightness = Utils.min(Utils.max((float)SimplexNoise.noise(cube.x / 250, cube.y / 250, cube.z / 250 + 10000, time / 5000) * 8 + 8 - darkness.getValuef(), 0), 1) * 100;
      
      colors[cube.index] = lx.hsb(hue, 100, brightness);
    }
  }
}

class GalaxyCloud extends TSPattern {

  double time = 0;

  GalaxyCloud(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    // Blue to purple
    float hueMin = 240;
    float hueMax = 280;
    float hueMinExtra = 80;
    float hueMaxExtra = 55;

    float hueSpread = hueMax - hueMin;
    float hueMid = hueSpread / 2 + hueMin;
    float initialSpreadSize = hueMinExtra + hueMaxExtra;
    float initialSpreadMin = hueMid - hueMinExtra;

    time += deltaMs;
    for (Cube cube : model.cubes) {
      float adjustedTheta = cube.transformedTheta / 360;
      float adjustedY = (cube.transformedY - model.yMin) / (model.yMax - model.yMin);
      float adjustedTime = (float)time / 5000;

      // Use 2 textures so we don't have a seam. Interpolate between them between -45 & 45 and 135 & 225
      PerlinNoise perlinNoise = new PerlinNoise();
      float hue1 = perlinNoise.noise(4 * adjustedTheta, 4 * adjustedY, adjustedTime);
      float hue2 = perlinNoise.noise(4 * ((adjustedTheta + 0.5f) % 1), 4 * adjustedY + 100, adjustedTime);
      float hue = Utils.lerp(hue1, hue2, Utils.min(Utils.max(Utils.abs(((adjustedTheta * 4 + 1) % 4) - 2) - 0.5f, 0), 1));

      float adjustedHue = hue * initialSpreadSize + initialSpreadMin;
      hue = Utils.min(Utils.max(adjustedHue, hueMin), hueMax);

      // make it black if the hue would go below hueMin or above hueMax
      // normalizedFadeOut: 0 = edge of the initial spread, 1 = edge of the hue spread, >1 = in the hue spread
      float normalizedFadeOut = (adjustedHue - hueMid + hueMinExtra) / (hueMinExtra - hueSpread / 2);
      // scaledFadeOut <0 = black sooner, 0-1 = fade out gradient, >1 = regular color
      float scaledFadeOut = normalizedFadeOut * 5 - 4.5f;
      float brightness = Utils.min(Utils.max(scaledFadeOut, 0), 1) * 100;

      // float brightness = Utils.min(Utils.max((float)SimplexNoise.noise(4 * adjustedX, 4 * adjustedY, 4 * adjustedZ + 10000, adjustedTime) * 8 - 1, 0), 1) * 100;

      colors[cube.index] = lx.hsb(hue, 100, brightness);
    }
  }
}

class TSBlurEffect extends BlurEffect {
  TSBlurEffect(LX lx) {
    super(lx);
  }

  @Override
  public void loop(double deltaMs) {
    if (isEnabled()) {
      super.loop(deltaMs);
    }
  }
}

class TSBlurEffect2 extends TSBlurEffect {
  TSBlurEffect2(LX lx) {
    super(lx);
  }
}

class NoPattern extends TSPattern {
  NoPattern(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
  }
}
