package entwined.pattern.irene_zhou;

import java.util.ArrayList;
import java.util.List;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.core.TSTriggerablePattern;
import entwined.utils.EntwinedUtils;
import entwined.utils.Vec2D;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.utils.LXUtils;


public class Fireflies extends TSTriggerablePattern {
  final DiscreteParameter flyCount = new DiscreteParameter("NUM", 20, 1, 100);
  final CompoundParameter speed = new CompoundParameter("SPEED", 1, 0, 7.5);
  final CompoundParameter hue = new CompoundParameter("HUE", 0, 0, 360);
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
      theta = EntwinedUtils.random(0, 360);
      yPos = EntwinedUtils.random(model.yMin, model.yMax);
      velocity = new Vec2D(EntwinedUtils.random(-1,1), EntwinedUtils.random(0.25f, 1));
      radius = 30;
      blinkIndex = (int) EntwinedUtils.random(0, blinkers.length);
    }

    public void move(float speed) {
      theta = (theta + speed * velocity.x) % 360;
      yPos += speed * velocity.y;

    }
  }

  public Fireflies(LX lx) {
    this(lx, 20, 1, 0);
  }

  Fireflies(LX lx, int initial_flyCount, float initial_speed, float initial_hue) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;

    addParameter("flyCount", flyCount);
    addParameter("speed", speed);
    addParameter("hue", hue);
    addModulator(decay);

    flyCount.setValue(initial_flyCount);
    speed.setValue(initial_speed);
    hue.setValue(initial_hue);

    for (int i = 0; i < blinkers.length; ++i) {
      blinkers[i] = new SinLFO(0, 75, 1000  * EntwinedUtils.random(1.0f, 3.0f));
      addModulator(blinkers[i]).setValue(EntwinedUtils.random(0,50)).start();
    }

    fireflies = new ArrayList<Firefly>(numFireflies);
    queue = new ArrayList<Firefly>();
    for (int i = 0; i < numFireflies; ++i) {
      fireflies.add(new Firefly());
    }
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    if (!triggered && fireflies.size() == 0) {
      enabled.setValue(false);
      // setCallRun(false);
    }

    for (LXPoint cube : model.points) {
      colors[cube.index] = LX.hsb(
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

    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    for (Firefly fly:fireflies) {
      for (LXPoint cube : model.points) {
        CubeData cdata = CubeManager.getCube(lx, cube.index);
        if (EntwinedUtils.abs(fly.yPos - cdata.localY) <= radius && EntwinedUtils.abs(fly.theta - cdata.localTheta) <= radius) {
          float distSq = EntwinedUtils.pow((LXUtils.wrapdistf(fly.theta, cdata.localTheta, 360)), 2) + EntwinedUtils.pow(fly.yPos - cdata.localY, 2);
          float brt = EntwinedUtils.max(0, 100 - EntwinedUtils.sqrt(distSq * 4) - blinkers[fly.blinkIndex].getValuef());
          if (brt > LXColor.b(colors[cube.index])) {
            colors[cube.index] = LX.hsb(
              (currentBaseHue + hue.getValuef()) % 360,
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

  @Override
  public void onTriggered() {
    super.onTriggered();

    numFireflies += 25;
    decay.setRange(numFireflies, 10);
    decay.reset().start();
  }

  @Override
  public void onReleased() {
    super.onReleased();

    decay.setRange(numFireflies, 0);
    decay.reset().start();
  }

}
