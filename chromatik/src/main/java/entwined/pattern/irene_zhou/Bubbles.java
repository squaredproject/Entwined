package entwined.pattern.irene_zhou;

import java.util.ArrayList;
import java.util.List;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.core.TSTriggerablePattern;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.utils.LXUtils;

public class Bubbles extends TSTriggerablePattern {
  public final DiscreteParameter ballCount = new DiscreteParameter("NUM", 10, 1, 150);
  final CompoundParameter maxRadius = new CompoundParameter("RAD", 50, 5, 100);
  final CompoundParameter speed = new CompoundParameter("SPEED", 1, 0, 5);
  final CompoundParameter hue = new CompoundParameter("HUE", 0, 0, 360);
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
      theta = EntwinedUtils.random(0, 360);
      bHue = EntwinedUtils.random(0, 30);
      baseSpeed = EntwinedUtils.random(2, 5);
      radius = EntwinedUtils.random(5, maxRadius);
      yPos = model.yMin - radius * EntwinedUtils.random(1,10);
    }

    public void move(float speed) {
      yPos += baseSpeed * speed;
    }
  }

  public Bubbles(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;

    addParameter("ballCount", ballCount);
    addParameter("maxRadius", maxRadius);
    addParameter("speed", speed);
    addParameter("hue", hue);
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

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    if (!triggered && bubbles.size() == 0) {
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
      for (LXPoint cube : model.points) {
        CubeData cdata = CubeManager.getCube(lx, cube.index);
        if (EntwinedUtils.abs(bubble.theta - cdata.localTheta) < bubble.radius && EntwinedUtils.abs(bubble.yPos - (cdata.localY - model.yMin)) < bubble.radius) {
          float distTheta = LXUtils.wrapdistf(bubble.theta, cdata.localTheta, 360) * 0.8f;
          float distY = bubble.yPos - (cdata.localY - model.yMin);
          float distSq = distTheta * distTheta + distY * distY;

          if (distSq < bubble.radius * bubble.radius) {
            float dist = EntwinedUtils.sqrt(distSq);
            colors[cube.index] = LX.hsb(
              (bubble.bHue + hue.getValuef()) % 360,
              50 + dist/bubble.radius * 50,
              EntwinedUtils.constrain(cdata.localY/model.yMax * 125 - 50 * (dist/bubble.radius), 0, 100)
            );
          }
        }
      }

      bubble.move(speed.getValuef() * (float)deltaMs * 60 / 1000);
    }
  }

  @Override
  public void onTriggered() {
    super.onTriggered();  // XXX do a different call so I don't have to invoke the superclass?

    numBubbles += 25;
    decay.setRange(numBubbles, 10);
    decay.reset().start();
  }

  @Override
  public void onReleased() {
    super.onReleased();

    decay.setRange(numBubbles, 0);
    decay.reset().start();
  }

}
