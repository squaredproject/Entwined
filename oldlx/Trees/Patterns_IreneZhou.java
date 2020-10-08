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

class Fireflies extends TSTriggerablePattern {
  final DiscreteParameter flyCount = new DiscreteParameter("NUM", 20, 1, 100);
  final BasicParameter speed = new BasicParameter("SPEED", 1, 0, 7.5); 
  final BasicParameter hue = new BasicParameter("HUE", 0, 0, 360);
  private float radius = 40;
  private int numFireflies = 0;
  private List<Firefly> fireflies;
  private List<Firefly> queue;
  private SinLFO[] blinkers = new SinLFO[10];
  private LinearEnvelope decay = new LinearEnvelope(0,0,3000);
  
  
  private class Firefly {
    public float theta = 0;
    public float yPos = 0;
    public Vec2D velocity = new Vec2D(0,0);
    public float radius = 0;
    public int blinkIndex = 0;

    public Firefly() {
      theta = Utils.random(0, 360);
      yPos = Utils.random(model.yMin, model.yMax);
      velocity = new Vec2D(Utils.random(-1,1), Utils.random(0.25f, 1));
      radius = 30;
      blinkIndex = (int) Utils.random(0, blinkers.length);
    }

    public void move(float speed) {
      theta = (theta + speed * velocity.x) % 360;
      yPos += speed * velocity.y;

    }
  }
  
  Fireflies(LX lx) {
    this(lx, 20, 1, 0);
  }

  Fireflies(LX lx, int initial_flyCount, float initial_speed, float initial_hue) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;

    addParameter(flyCount);
    addParameter(speed);
    addParameter(hue);
    addModulator(decay);

    flyCount.setValue(initial_flyCount);
    speed.setValue(initial_speed);
    hue.setValue(initial_hue);

    for (int i = 0; i < blinkers.length; ++i) {
      blinkers[i] = new SinLFO(0, 75, 1000  * Utils.random(1.0f, 3.0f));      
      addModulator(blinkers[i]).setValue(Utils.random(0,50)).start();
    }
    
    fireflies = new ArrayList<Firefly>(numFireflies);
    queue = new ArrayList<Firefly>();
    for (int i = 0; i < numFireflies; ++i) {
      fireflies.add(new Firefly());
    }
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    if (!triggered && fireflies.size() == 0) {
      setCallRun(false);
    }

    for (Cube cube : model.cubes) {
      colors[cube.index] = lx.hsb(
        0,
        0,
        0
      );
    }

    if (triggerableModeEnabled) {
      numFireflies = (int) decay.getValuef();
    } else {
      numFireflies = flyCount.getValuei();  
    }
    
    if (fireflies.size() < numFireflies) { 
      for (int i = 0; i < numFireflies - fireflies.size(); ++i) {
        queue.add(new Firefly());
      }
    }

    for (int i = 0; i < queue.size(); ++i) { //only add fireflies when they're about to blink on
      if (blinkers[queue.get(i).blinkIndex].getValuef() > 70) {
        fireflies.add(queue.remove(i));
      }
    }

    for (int i = 0; i < fireflies.size(); ++i) { //remove fireflies while blinking off
      if (numFireflies < fireflies.size()) {
        if (blinkers[fireflies.get(i).blinkIndex].getValuef() > 70) {
          fireflies.remove(i);
        }
      }
    }

    for (int i = 0; i < fireflies.size(); ++i) {
      if (fireflies.get(i).yPos > model.yMax + radius) {
          fireflies.get(i).yPos = model.yMin - radius;
      }
    }

    for (Firefly fly:fireflies) {
      for (Cube cube: model.cubes) {
        if (Utils.abs(fly.yPos - cube.transformedY) <= radius && Utils.abs(fly.theta - cube.transformedTheta) <= radius) {
          float distSq = Utils.pow((LXUtils.wrapdistf(fly.theta, cube.transformedTheta, 360)), 2) + Utils.pow(fly.yPos - cube.transformedY, 2);
          float brt = Utils.max(0, 100 - Utils.sqrt(distSq * 4) - blinkers[fly.blinkIndex].getValuef());
          if (brt > LXColor.b(colors[cube.index])) {
            colors[cube.index] = lx.hsb(
              (lx.getBaseHuef() + hue.getValuef()) % 360,
              100 - brt,
              brt
            );
          }
        }
      }
    }

    for (Firefly firefly: fireflies) {
      firefly.move(speed.getValuef() * (float)deltaMs * 60 / 1000);
    }
  }

  public void onTriggered(float strength) {
    super.onTriggered(strength);

    numFireflies += 25;
    decay.setRange(numFireflies, 10);
    decay.reset().start();
  }

  public void onRelease() {
    super.onRelease();

    decay.setRange(numFireflies, 0);
    decay.reset().start();
  }

}

class Lattice extends TSPattern {
  final SawLFO spin = new SawLFO(0, 4320, 24000); 
  final SinLFO yClimb = new SinLFO(60, 30, 24000);
  final BasicParameter hue = new BasicParameter("HUE", 0, 0, 360);
  final BasicParameter yHeight = new BasicParameter("HEIGHT", 0, -500, 500);

  float coil(float basis) {
    return Utils.sin(basis*Utils.PI);
  }

  Lattice(LX lx) {
    super(lx);
    addModulator(spin).start();
    addModulator(yClimb).start();
    addParameter(hue);
    addParameter(yHeight);
  }

  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    float spinf = spin.getValuef();
    float coilf = 2*coil(spin.getBasisf());
    for (Cube cube : model.cubes) {
      float wrapdistleft = LXUtils.wrapdistf(cube.transformedTheta, spinf + (model.yMax - cube.transformedY) * coilf, 180);
      float wrapdistright = LXUtils.wrapdistf(cube.transformedTheta, -spinf - (model.yMax - cube.transformedY) * coilf, 180);
      float width = yClimb.getValuef() + ((cube.transformedY - yHeight.getValuef())/model.yMax) * 50;
      float df = Utils.min(100, 3 * Utils.max(0, wrapdistleft - width) + 3 * Utils.max(0, wrapdistright - width));

      colors[cube.index] = lx.hsb(
        (hue.getValuef() + lx.getBaseHuef() + .2f*cube.transformedY - 360) % 360, 
        100, 
        df
      );
    }
  }
}

class Fire extends TSTriggerablePattern {
  final BasicParameter maxHeight = new BasicParameter("HEIGHT", 0.8, 0.3, 1);
  final BasicParameter flameSize = new BasicParameter("SIZE", 30, 10, 75);  
  final BasicParameter flameCount = new BasicParameter ("FLAMES", 75, 0, 75);
  final BasicParameter hue = new BasicParameter("HUE", 0, 0, 360);
  private LinearEnvelope fireHeight = new LinearEnvelope(0,0,500);

  private float height = 0;
  private int numFlames = 12;
  private List<Flame> flames;
  
  private class Flame {
    public float flameHeight = 0;
    public float theta = Utils.random(0, 360);
    public LinearEnvelope decay = new LinearEnvelope(0,0,0);
  
    public Flame(float maxHeight, boolean groundStart){
      float flameHeight;
      if (Utils.random(1) > .2f) {
        flameHeight = Utils.pow(Utils.random(0, 1), 3) * maxHeight * 0.3f;
      } else {
        flameHeight = Utils.pow(Utils.random(0, 1), 3) * maxHeight;
      }
      decay.setRange(model.yMin, (model.yMax * 0.9f) * flameHeight, Utils.min(Utils.max(200, 900 * flameHeight), 800));
      if (!groundStart) {
        decay.setBasis(Utils.random(0,.8f));
      }
      addModulator(decay).start();
    }
  }

  Fire(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;

    addParameter(maxHeight);
    addParameter(flameSize);
    addParameter(flameCount);
    addParameter(hue);
    addModulator(fireHeight);

    flames = new ArrayList<Flame>(numFlames);
    for (int i = 0; i < numFlames; ++i) {
      flames.add(new Flame(height, false));
    }
  }

  public void updateNumFlames(int numFlames) {
    for (int i = flames.size(); i < numFlames; ++i) {
      flames.add(new Flame(height, false));
    }
  }

  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    if (!triggered && flames.size() == 0) {
      setCallRun(false);
    }

    if (!triggerableModeEnabled) {
      height = maxHeight.getValuef();
      numFlames = (int) (flameCount.getValue() / 75 * 30); // Convert for backwards compatibility
    } else {
      height = fireHeight.getValuef();
    }

    if (flames.size() != numFlames) {
      updateNumFlames(numFlames);
    }
    for (int i = 0; i < flames.size(); ++i) {
      if (flames.get(i).decay.finished()) {
        removeModulator(flames.get(i).decay);
        if (flames.size() <= numFlames) {
          flames.set(i, new Flame(height, true));
        } else {
          flames.remove(i);
          i--;
        }
      }
    }

    for (Cube cube: model.cubes) {
      float yn = (cube.transformedY - model.yMin) / model.yMax;
      float cBrt = 0;
      float cHue = 0;
      float flameWidth = flameSize.getValuef() / 2;
      for (int i = 0; i < flames.size(); ++i) {
        if (Utils.abs(flames.get(i).theta - cube.transformedTheta) < (flameWidth * (1- yn))) {
          cBrt = Utils.min(100, Utils.max(0, Utils.max(cBrt, (100 - 2 * Utils.abs(cube.transformedY - flames.get(i).decay.getValuef()) - flames.get(i).decay.getBasisf() * 25) * Utils.min(1, 2 * (1 - flames.get(i).decay.getBasisf())) )));
          cHue = Utils.max(0,  (cHue + cBrt * 0.7f) * 0.5f);
        }
      }
      colors[cube.index] = lx.hsb(
        (cHue + hue.getValuef()) % 360,
        100,
        Utils.min(100, cBrt + Utils.pow(Utils.max(0, (height - 0.3f) / 0.7f), 0.5f) * Utils.pow(Utils.max(0, 0.8f - yn), 2) * 75)
      );
    }
  }

  public void onTriggered(float strength) {
    super.onTriggered(strength);

    fireHeight.setRange(1,0.6f);
    fireHeight.reset().start();
  };

  public void onRelease() {
    super.onRelease();

    fireHeight.setRange(height, 0);
    fireHeight.reset().start();
  }
}

class Bubbles extends TSTriggerablePattern {
  final DiscreteParameter ballCount = new DiscreteParameter("NUM", 10, 1, 150);
  final BasicParameter maxRadius = new BasicParameter("RAD", 50, 5, 100);
  final BasicParameter speed = new BasicParameter("SPEED", 1, 0, 5); 
  final BasicParameter hue = new BasicParameter("HUE", 0, 0, 360);
  private LinearEnvelope decay = new LinearEnvelope(0,0,2000);
  private int numBubbles = 0;
  private List<Bubble> bubbles;
  
  private class Bubble {
    public float theta = 0;
    public float yPos = 0;
    public float bHue = 0;
    public float baseSpeed = 0;
    public float radius = 0;

    public Bubble(float maxRadius) {
      theta = Utils.random(0, 360);
      bHue = Utils.random(0, 30);
      baseSpeed = Utils.random(2, 5);
      radius = Utils.random(5, maxRadius);
      yPos = model.yMin - radius * Utils.random(1,10);
    }

    public void move(float speed) {
      yPos += baseSpeed * speed;
    }
  }
  
  Bubbles(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;

    addParameter(ballCount);
    addParameter(maxRadius);
    addParameter(speed);
    addParameter(hue);
    addModulator(decay);
    
    bubbles = new ArrayList<Bubble>(numBubbles);
    for (int i = 0; i < numBubbles; ++i) {
      bubbles.add(new Bubble(maxRadius.getValuef()));
    }
  }
  
  public void addBubbles(int numBubbles) {
    for (int i = bubbles.size(); i < numBubbles; ++i) {
      bubbles.add(new Bubble(maxRadius.getValuef()));
    }
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    if (!triggered && bubbles.size() == 0) {
      setCallRun(false);
    }

    for (Cube cube : model.cubes) {
      colors[cube.index] = lx.hsb(
        0,
        0,
        0
      );
    }
    if (!triggerableModeEnabled) {
      numBubbles = ballCount.getValuei();  
    } else {
      numBubbles = (int) decay.getValuef();
    }
    
    if (bubbles.size() < numBubbles) {
      addBubbles(numBubbles);
    }
    
    for (int i = 0; i < bubbles.size(); ++i) {
      if (bubbles.get(i).yPos > model.yMax + bubbles.get(i).radius) { //bubble is now off screen
        if (numBubbles < bubbles.size()) {
          bubbles.remove(i);
          i--;
        } else {
          bubbles.set(i, new Bubble(maxRadius.getValuef()));
        }
      }
    }
      
    for (Bubble bubble: bubbles) {
      for (Cube cube : model.cubes) {
        if (Utils.abs(bubble.theta - cube.transformedTheta) < bubble.radius && Utils.abs(bubble.yPos - (cube.transformedY - model.yMin)) < bubble.radius) {
          float distTheta = LXUtils.wrapdistf(bubble.theta, cube.transformedTheta, 360) * 0.8f;
          float distY = bubble.yPos - (cube.transformedY - model.yMin);
          float distSq = distTheta * distTheta + distY * distY;
          
          if (distSq < bubble.radius * bubble.radius) {
            float dist = Utils.sqrt(distSq);
            colors[cube.index] = lx.hsb(
              (bubble.bHue + hue.getValuef()) % 360,
              50 + dist/bubble.radius * 50,
              Utils.constrain(cube.transformedY/model.yMax * 125 - 50 * (dist/bubble.radius), 0, 100)
            );
          }
        }
      }
    
      bubble.move(speed.getValuef() * (float)deltaMs * 60 / 1000);
    }
  }

  public void onTriggered(float strength) {
    super.onTriggered(strength);

    numBubbles += 25;
    decay.setRange(numBubbles, 10);
    decay.reset().start();
  }

  public void onRelease() {
    super.onRelease();

    decay.setRange(numBubbles, 0);
    decay.reset().start();
  }

}

class Voronoi extends TSPattern {
  final BasicParameter speed = new BasicParameter("SPEED", 1, 0, 5);
  final BasicParameter width = new BasicParameter("WIDTH", 0.75, 0.5, 1.25);
  final BasicParameter hue = new BasicParameter("HUE", 0, 0, 360);
  final int NUM_SITES = 15;
  private Site[] sites = new Site[NUM_SITES];
  
  private class Site {
    public float theta = 0;
    public float yPos = 0;
    public Vec2D velocity = new Vec2D(0,0);
    
    public Site() {
      theta = Utils.random(0, 360);
      yPos = Utils.random(model.yMin, model.yMax);
      velocity = new Vec2D(Utils.random(-1,1), Utils.random(-1,1));
    }
    
    public void move(float speed) {
      theta = (theta + speed * velocity.x) % 360;
      yPos += speed * velocity.y;
      if ((yPos < model.yMin - 20) || (yPos > model.yMax + 20)) {
        velocity.y *= -1;
      }
    }
  }
  
  Voronoi(LX lx) {
    super(lx);
    addParameter(speed);
    addParameter(width);
    addParameter(hue);
    for (int i = 0; i < sites.length; ++i) {
      sites[i] = new Site();
    }
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    for (Cube cube: model.cubes) {
      float minDistSq = 1000000;
      float nextMinDistSq = 1000000;
      for (int i = 0; i < sites.length; ++i) {
        if (Utils.abs(sites[i].yPos - cube.transformedY) < 150) { //restraint on calculation
          float distSq = Utils.pow((LXUtils.wrapdistf(sites[i].theta, cube.transformedTheta, 360)), 2) + Utils.pow(sites[i].yPos - cube.transformedY, 2);
          if (distSq < nextMinDistSq) {
            if (distSq < minDistSq) {
              nextMinDistSq = minDistSq;
              minDistSq = distSq;
            } else {
              nextMinDistSq = distSq;
            }
          }
        }
      }
      colors[cube.index] = lx.hsb(
        (lx.getBaseHuef() + hue.getValuef()) % 360,
        100,
        Utils.max(0, Utils.min(100, 100 - Utils.sqrt(nextMinDistSq - minDistSq) / width.getValuef()))
      );
    }
    for (Site site: sites) {
      site.move(speed.getValuef() * (float)deltaMs * 60 / 1000);
    }
  }
}

class Cells extends TSPattern {
  final BasicParameter speed = new BasicParameter("SPEED", 1, 0, 5);
  final BasicParameter width = new BasicParameter("WIDTH", 0.75, 0.5, 1.25);
  final BasicParameter hue = new BasicParameter("HUE", 0, 0, 360);
  final int NUM_SITES = 15;
  private Site[] sites = new Site[NUM_SITES];
  
  private class Site {
    public float theta = 0;
    public float yPos = 0;
    public Vec2D velocity = new Vec2D(0,0);
    
    public Site() {
      theta = Utils.random(0, 360);
      yPos = Utils.random(model.yMin, model.yMax);
      velocity = new Vec2D(Utils.random(-1,1), Utils.random(-1,1));
    }
    
    public void move(float speed) {
      theta = (theta + speed * velocity.x) % 360;
      yPos += speed * velocity.y;
      if ((yPos < model.yMin - 20) || (yPos > model.yMax + 20)) {
        velocity.y *= -1;
      }
    }
  }
  
  Cells(LX lx) {
    super(lx);
    addParameter(speed);
    addParameter(width);
    addParameter(hue);
    for (int i = 0; i < sites.length; ++i) {
      sites[i] = new Site();
    }
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    for (Cube cube: model.cubes) {
      float minDistSq = 1000000;
      float nextMinDistSq = 1000000;
      for (int i = 0; i < sites.length; ++i) {
        if (Utils.abs(sites[i].yPos - cube.transformedY) < 150) { //restraint on calculation
          float distSq = Utils.pow((LXUtils.wrapdistf(sites[i].theta, cube.transformedTheta, 360)), 2) + Utils.pow(sites[i].yPos - cube.transformedY, 2);
          if (distSq < nextMinDistSq) {
            if (distSq < minDistSq) {
              nextMinDistSq = minDistSq;
              minDistSq = distSq;
            } else {
              nextMinDistSq = distSq;
            }
          }
        }
      }
      colors[cube.index] = lx.hsb(
        (lx.getBaseHuef() + hue.getValuef()) % 360,
        100,
        Utils.max(0, Utils.min(100, 100 - Utils.sqrt(nextMinDistSq - 2 * minDistSq)))
      );
    }
    for (Site site: sites) {
      site.move(speed.getValuef() * (float)deltaMs * 60 / 1000);
    }
  }
}


class Fumes extends TSPattern {
  final BasicParameter speed = new BasicParameter("SPEED", 2, 0, 20);
  final BasicParameter hue = new BasicParameter("HUE", 0, 0, 360);
  final BasicParameter sat = new BasicParameter("SAT", 25, 0, 100);
  final int NUM_SITES = 15;
  private Site[] sites = new Site[NUM_SITES];
  
  private class Site {
    public float theta = 0;
    public float yPos = 0;
    public Vec2D velocity = new Vec2D(0,0);
    
    public Site() {
      theta = Utils.random(0, 360);
      yPos = Utils.random(model.yMin, model.yMax);
      velocity = new Vec2D(Utils.random(0,1), Utils.random(0,0.75f));
    }
    
    public void move(float speed) {
      theta = (theta + speed * velocity.x) % 360;
      yPos += speed * velocity.y;
      if (yPos < model.yMin - 50) {
        velocity.y *= -1;
      }
      if (yPos > model.yMax + 50) {
        yPos = model.yMin - 50;
      }
    }
  }
  
  Fumes(LX lx) {
    super(lx);
    addParameter(hue);
    addParameter(speed);
    addParameter(sat);
    for (int i = 0; i < sites.length; ++i) {
      sites[i] = new Site();
    }
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;
    
    float minSat = sat.getValuef();
    for (Cube cube: model.cubes) {
      float minDistSq = 1000000;
      float nextMinDistSq = 1000000;
      for (int i = 0; i < sites.length; ++i) {
        if (Utils.abs(sites[i].yPos - cube.transformedY) < 150) { //restraint on calculation
          float distSq = Utils.pow((LXUtils.wrapdistf(sites[i].theta, cube.transformedTheta, 360)), 2) + Utils.pow(sites[i].yPos - cube.transformedY, 2);
          if (distSq < nextMinDistSq) {
            if (distSq < minDistSq) {
              nextMinDistSq = minDistSq;
              minDistSq = distSq;
            } else {
              nextMinDistSq = distSq;
            }
          }
        }
      }
      float brt = Utils.max(0, 100 - Utils.sqrt(nextMinDistSq));
      colors[cube.index] = lx.hsb(
        (lx.getBaseHuef() + hue.getValuef()) % 360,
        100 - Utils.min( minSat, brt),
        brt
      );
    }
    for (Site site: sites) {
      site.move(speed.getValuef() * (float)deltaMs * 60 / 1000);
    }
  }
}

class Pulley extends TSTriggerablePattern { //ported from SugarCubes
  final int NUM_DIVISIONS = 2;
  private final Accelerator[] gravity = new Accelerator[NUM_DIVISIONS];
  private final float[] baseSpeed = new float[NUM_DIVISIONS];
  private final Click[] delays = new Click[NUM_DIVISIONS];
   private final Click turnOff = new Click(9000);

  private boolean isRising = false;
  boolean triggered = true;
  float coil = 10;

  private BasicParameter sz = new BasicParameter("SIZE", 0.5);
  private BasicParameter beatAmount = new BasicParameter("BEAT", 0);
  private BooleanParameter automated = new BooleanParameter("AUTO", true);
  private BasicParameter speed = new BasicParameter("SPEED", 1, -3, 3);
  

  Pulley(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_CUSTOM;

    for (int i = 0; i < NUM_DIVISIONS; ++i) {
      addModulator(gravity[i] = new Accelerator(0, 0, 0));
      addModulator(delays[i] = new Click(0));
    }
    addParameter(sz);
    addParameter(beatAmount);
    addParameter(speed);
    addParameter(automated);
    onParameterChanged(speed);
    addModulator(turnOff);
  }

  public void onParameterChanged(LXParameter parameter) {
    super.onParameterChanged(parameter);
    if (parameter == speed && isRising) {
      for (int i = 0; i < NUM_DIVISIONS; ++i) {
        gravity[i].setVelocity(baseSpeed[i] * speed.getValuef());
      }
    }
    if (parameter == automated) {
      if (automated.isOn()) {
        trigger();
      }
    }
  }
  
  private void trigger() {
    isRising = !isRising;
    int i = 0;
    for (int j = 0; j < NUM_DIVISIONS; ++j) {
      if (isRising) {
        baseSpeed[j] = Utils.random(20, 33);
        gravity[j].setSpeed(baseSpeed[j], 0).start();
      } 
      else {
        gravity[j].setVelocity(0).setAcceleration(-420);
        delays[j].setPeriod(Utils.random(0, 500)).trigger();
      }
      ++i;
    }
  }

  public void run(double deltaMS) {
    if (getChannel().getFader().getNormalized() == 0) return;

    if (turnOff.click()) {
      triggered = false;
      setColors(lx.hsb(0,0,0));
      turnOff.stopAndReset();      
    }
    if(triggered) {
      if (!isRising) {
        int j = 0;
        for (Click d : delays) {
          if (d.click()) {
            gravity[j].start();
            d.stop();
          }
          ++j;
        }
        for (Accelerator g : gravity) {
          if (g.getValuef() < 0) { //bounce
            g.setValue(-g.getValuef());
            g.setVelocity(-g.getVelocityf() * Utils.random(0.74f, 0.84f));
          }
        }
      }
  
      float fPos = 1 -lx.tempo.rampf();
      if (fPos < .2f) {
        fPos = .2f + 4 * (.2f - fPos);
      }
  
      float falloff = 100.f / (3 + sz.getValuef() * 36 + fPos * beatAmount.getValuef()*48);
      for (Cube cube : model.cubes) {
        int gi = (int) Utils.constrain((cube.x - model.xMin) * NUM_DIVISIONS / (model.xMax - model.xMin), 0, NUM_DIVISIONS-1);
        float yn =  cube.transformedY/model.yMax;
        colors[cube.index] = lx.hsb(
          (lx.getBaseHuef() + Utils.abs(cube.x - model.cx)*.8f + cube.transformedY*.4f) % 360, 
          Utils.constrain(100 *(0.8f -  yn * yn), 0, 100), 
          Utils.max(0, 100 - Utils.abs(cube.transformedY/2 - 50 - gravity[gi].getValuef())*falloff)
        );
      }
    }
  }

  public void onTriggerableModeEnabled() {
    super.onTriggerableModeEnabled();
    triggered = false;
  }

  public void onTriggered(float strength) {
    triggered = true;
    isRising = true;
    turnOff.start();
    
    for (Accelerator g: gravity) {
      g.setValue(225);
    }
    trigger();
  }

  public void onRelease() {
  }
}


class Springs extends TSPattern {
  final BasicParameter hue = new BasicParameter("HUE", 0, 0, 360);
  private BooleanParameter automated = new BooleanParameter("AUTO", true);
  private final Accelerator gravity = new Accelerator(0, 0, 0);
  private final Click reset = new Click(9600);
  private boolean isRising = false;
  final SinLFO spin = new SinLFO(0, 360, 9600);
  
  float coil(float basis) {
    return 4 * Utils.sin(basis*Utils.TWO_PI + Utils.PI) ;
  }

  Springs(LX lx) {
    super(lx);
    addModulator(gravity);
    addModulator(reset).start();
    addModulator(spin).start();
    addParameter(hue);
    addParameter(automated);
    trigger();
  }

  public void onParameterChanged(LXParameter parameter) {
    super.onParameterChanged(parameter);
    if (parameter == automated) {
      if (automated.isOn()) {
        trigger();
      }
    }
  }  

  private void trigger() {
    isRising = !isRising;
    if (isRising) {
      gravity.setSpeed(0.25f, 0).start();
    } 
    else {
      gravity.setVelocity(0).setAcceleration(-1.75f);
    }
  }

  public void run(double deltaMS) {
    if (getChannel().getFader().getNormalized() == 0) return;

    if (!isRising) {
      gravity.start();
      if (gravity.getValuef() < 0) {
        gravity.setValue(-gravity.getValuef());
        gravity.setVelocity(-gravity.getVelocityf() * Utils.random(0.74f, 0.84f));
      }
    }

    float spinf = spin.getValuef();
    float coilf = 2*coil(spin.getBasisf());
    
    for (Cube cube : model.cubes) {
      float yn =  cube.transformedY/model.yMax;
      float width = (1-yn) * 25;
      float wrapdist = LXUtils.wrapdistf(cube.transformedTheta, spinf + (cube.transformedY) * 1/(gravity.getValuef() + 0.2f), 360);
      float df = Utils.max(0, 100 - Utils.max(0, wrapdist-width));
      colors[cube.index] = lx.hsb(
        Utils.max(0, (lx.getBaseHuef() - yn * 20 + hue.getValuef()) % 360), 
        Utils.constrain((1- yn) * 100 + wrapdist, 0, 100),
        Utils.max(0, df - yn * 50)
      );
    }
  }
}

class Pulleys extends TSTriggerablePattern { //ported from SugarCubes
  private BasicParameter sz = new BasicParameter("SIZE", 0.5);
  private BasicParameter beatAmount = new BasicParameter("BEAT", 0);
  private BooleanParameter automated = new BooleanParameter("AUTO", true);
  private BasicParameter speed = new BasicParameter("SPEED", 1, -3, 3);
  final DiscreteParameter pulleyCount = new DiscreteParameter("NUM", 1, 1, 5);
  private Click dropPulley = new Click(4000);


  private boolean isRising = false; //are the pulleys rising or falling
  boolean triggered = true; //has the trigger to rise/fall been pulled
  boolean autoMode = true; //triggerMode vs autoMode.
  private int numPulleys = 0;
  private List<Pulley> pulleys = new ArrayList<Pulley>(numPulleys);
  

  private class Pulley {
    public float baseSpeed = 0;
    public Click delay = new Click(0);
    public Click turnOff = new Click(0);
    public final Accelerator gravity = new Accelerator(0,0,0);
    public float baseHue = 0;
    public LinearEnvelope maxBrt = new LinearEnvelope(0,0,0);
    
    public Pulley() {
      baseSpeed = Utils.random(10,50);
      baseHue = Utils.random(0, 30);
      delay.setPeriod(Utils.random(0,500));
      gravity.setSpeed(this.baseSpeed, 0);
      if (autoMode) {
        maxBrt.setRange(0,1,3000);
      } else {
        maxBrt.setRange(0.5f,1,3000);
      }
      
      turnOff.setPeriod(6000);
      addModulator(gravity);
      addModulator(delay);
      addModulator(maxBrt).start();
      addModulator(turnOff);
    }
  }

  Pulleys(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;

    addParameter(sz);
    addParameter(beatAmount);
    addParameter(speed);
    addParameter(automated);
    addParameter(pulleyCount);
    onParameterChanged(speed);
    addModulator(dropPulley);

    for (int i = 0; i < numPulleys; i++) {
      pulleys.add(new Pulley());
    } 
  }

  public void onParameterChanged(LXParameter parameter) {
    super.onParameterChanged(parameter);
    if (parameter == speed && isRising) {
      for (int i = 0; i < pulleys.size(); i++) {
        pulleys.get(i).gravity.setVelocity(pulleys.get(i).baseSpeed * speed.getValuef());
      }
    }
    if (parameter == automated) {
      if (automated.isOn()) {
        trigger();
      }
    }
  }

  private void trigger() {
    if (autoMode) {
      isRising = !isRising;
    }
    for (int j = 0; j < pulleys.size(); j++) {
      if (isRising) {
        pulleys.get(j).gravity.setSpeed(pulleys.get(j).baseSpeed,0).start();
      } 
      else {
        pulleys.get(j).gravity.setVelocity(0).setAcceleration(-420);
        pulleys.get(j).delay.trigger();
      }
    }
  }

  public void run(double deltaMS) {
    if (getChannel().getFader().getNormalized() == 0) return;

    if (!triggered && pulleys.size() == 0) {
      setCallRun(false);
    }

    if (autoMode) {
      numPulleys = pulleyCount.getValuei();
      
      if (numPulleys < pulleys.size()) {
        for (int i = numPulleys; i < pulleys.size(); i++) {
          pulleys.get(i).maxBrt.start();  //fadeOut then delete
        }
      }
    } else {
      if (dropPulley.click()) {
        numPulleys += 1;
      }
    }
    
    if (numPulleys > pulleys.size()) {
      addPulleys(numPulleys);
    }

    for (int i = 0; i < pulleys.size(); i++) {
      if (pulleys.get(i).maxBrt.finished()) {
        if (pulleys.get(i).maxBrt.getValuef() == 1) {
          pulleys.get(i).maxBrt.setRange(1,0,3000).reset();
        } else {
          removePulley(i);
          numPulleys -= 1;
        }
      }
    }

    for (int i = 0; i < pulleys.size(); i++) {
      if (pulleys.get(i).turnOff.click()) {
        pulleys.get(i).maxBrt.start();
      }
    }
    
    if(triggered) {
      if (!isRising) {
        for (int j = 0; j < pulleys.size(); ++j) {
          if (pulleys.get(j).delay.click()) {
            pulleys.get(j).gravity.start();
            pulleys.get(j).delay.stop();
          }
          if (pulleys.get(j).gravity.getValuef() < 0) { //bouncebounce
            pulleys.get(j).gravity.setValue(-pulleys.get(j).gravity.getValuef());
            pulleys.get(j).gravity.setVelocity(-pulleys.get(j).gravity.getVelocityf() * Utils.random(0.74f,0.84f));
          }
        }
      }
  
      float fPos = 1 -lx.tempo.rampf();
      if (fPos < .2f) {
        fPos = .2f + 4 * (.2f - fPos);
      }
  
      float falloff = 100.f / (3 + sz.getValuef() * 36 + fPos * beatAmount.getValuef()*48);
      for (Cube cube : model.cubes) {
        float cBrt = 0;
        float cHue = 0;
        for (int j = 0; j < pulleys.size(); ++j) {
          cHue = (lx.getBaseHuef() + Utils.abs(cube.x - model.cx)*.8f + cube.transformedY*.4f + pulleys.get(j).baseHue) % 360;
          cBrt += Utils.max(0, pulleys.get(j).maxBrt.getValuef() * (100 - Utils.abs(cube.transformedY/2 - 50 - pulleys.get(j).gravity.getValuef())*falloff));
        }
        float yn =  cube.transformedY/model.yMax;
        colors[cube.index] = lx.hsb(
          cHue, 
          Utils.constrain(100 *(0.8f -  yn * yn), 0, 100), 
          Utils.min(100, cBrt)
        );
      }
    }
  }

  public void addPulleys(int numPulleys) {
    for (int i = pulleys.size(); i < numPulleys; ++i) {
      Pulley newPulley = new Pulley();
      if (isRising) {
        newPulley.gravity.setSpeed(newPulley.baseSpeed,0).start();
      } else {
        if (autoMode) {
          newPulley.gravity.setValue(Utils.random(0,225));
        } else {
          newPulley.gravity.setValue(250);
          newPulley.turnOff.start();
        }

        newPulley.gravity.setVelocity(0).setAcceleration(-420);
        newPulley.delay.trigger();
      }
      pulleys.add(newPulley);
    }
  }

  public void removePulley(int index) {
    Pulley pulley = pulleys.remove(index);
    removeModulator(pulley.turnOff);
    removeModulator(pulley.gravity);
    removeModulator(pulley.maxBrt);
  }

  public void onTriggerableModeEnabled() {
    super.onTriggerableModeEnabled();
    autoMode = false;
    isRising = false;
  }

  public void onTriggered(float strength) {
    numPulleys +=1;
    dropPulley.start();
  }
  
  public void onRelease() {
    dropPulley.stopAndReset();
  }
}

//class MirageEffect extends ModelTransform {
//  final BasicParameter amplitude  = new BasicParameter("AMP", 0, 0, 0.5);
//  final SinLFO ripple = new SinLFO(0, 1, 300);
//  final SawLFO rotate = new SawLFO(0, 360, 6000);
//
//  MirageEffect(LX lx) {
//    super(lx);
//    addModulator(ripple.start());
//  }
//
//  void transform(Model model) {
//    for (Cube cube: model.cubes) {
//      cube.transformedY = cube.transformedY * ( 1 - ripple.getValuef() * amplitude.getValuef() * Utils.sin((cube.transformedTheta + rotate) / 30 * Utils.PI ));
//    }
//  }
//}



// class Ripple extends TSPattern {
//   final BasicParameter speed = new BasicParameter("Speed", 15000, 25000, 8000);
//   final BasicParameter baseBrightness = new BasicParameter("Bright", 0, 0, 100);
//   final SawLFO rippleAge = new SawLFO(0, 100, speed);
//   float hueVal;
//   float brightVal;
//   boolean resetDone = false;
//   float yCenter;
//   float thetaCenter;
//   Ripple(LX lx) {
//     super(lx);
//     addParameter(speed);
//     addParameter(baseBrightness);
//     addModulator(rippleAge.start());    
//   }
  
//   public void run(double deltaMs) {
//     if (getChannel().getFader().getNormalized() == 0) return;

//     if (rippleAge.getValuef() < 5){
//       if (!resetDone){
//         yCenter = 150 + Utils.random(300);
//         thetaCenter = Utils.random(360);
//         resetDone = true;
//       }
//     }
//     else {
//       resetDone = false;
//     }
//     float radius = Utils.pow(rippleAge.getValuef(), 2) / 3;
//     for (Cube cube : model.cubes) {
//       float distVal = Utils.sqrt(Utils.pow((LXUtils.wrapdistf(thetaCenter, cube.transformedTheta, 360)) * 0.8f, 2) + Utils.pow(yCenter - cube.transformedY, 2));
//       float heightHueVariance = 0.1f * cube.transformedY;
//       if (distVal < radius){
//         float rippleDecayFactor = (100 - rippleAge.getValuef()) / 100;
//         float timeDistanceCombination = distVal / 20 - rippleAge.getValuef();
//         hueVal = (lx.getBaseHuef() + 40 * Utils.sin(Utils.TWO_PI * (12.5f + rippleAge.getValuef() )/ 200) * rippleDecayFactor * Utils.sin(timeDistanceCombination) + heightHueVariance + 360) % 360;
//         brightVal = Utils.constrain((baseBrightness.getValuef() + rippleDecayFactor * (100 - baseBrightness.getValuef()) + 80 * rippleDecayFactor * Utils.sin(timeDistanceCombination + Utils.TWO_PI / 8)), 0, 100);
//       }
//       else {
//         hueVal = (lx.getBaseHuef() + heightHueVariance) % 360;
//         brightVal = baseBrightness.getValuef(); 
//       }
//       colors[cube.index] = lx.hsb(hueVal,  100, brightVal);
//     }
//   }
// }

// class Ripples extends TSPattern {
//   Ripples(LX lx) {
//     super(lx);
//   }

//   public void run(double deltaMs) {

//   }
// }


