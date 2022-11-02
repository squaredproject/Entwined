package entwined.core;

import java.util.ArrayList;
import java.util.Iterator;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;

public abstract class MultiObjectPattern <ObjectType extends MultiObject> extends TSTriggerablePattern {

  protected BoundedParameter frequency;

  final boolean shouldAutofade;
  protected float fadeTime = 1000;

  final ArrayList<ObjectType> objects;
  double pauseTimerCountdown = 0;
//  BoundedParameter fadeLength

  public MultiObjectPattern(LX lx) {
    this(lx, true);
  }

  protected MultiObjectPattern(LX lx, double initial_frequency) {
    this(lx, true);
    frequency.setValue(initial_frequency);
  }

  protected MultiObjectPattern(LX lx, boolean shouldAutofade) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;

    frequency = getFrequencyParameter();
    addParameter("frequency", frequency);

    this.shouldAutofade = shouldAutofade;
//    if (shouldAutofade) {


    objects = new ArrayList<ObjectType>();
  }

  protected BoundedParameter getFrequencyParameter() {
    return new BoundedParameter("FREQ", .5, .1, 40).setExponent(2);
  }

//  BoundedParameter getAutofadeParameter() {
//    return new BoundedParameter("TAIL",s
//  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    if (triggered) {
      pauseTimerCountdown -= deltaMs;

      if (pauseTimerCountdown <= 0) {
        float delay = 1000 / frequency.getValuef();
        pauseTimerCountdown = EntwinedUtils.random(delay / 2) + delay * 3 / 4;
        makeObject(0);
      }
    } else if (objects.size() == 0) {
      setCallRun(false);
    }

    if (shouldAutofade) {
      for (LXPoint cube : model.points) {
        blendColor(cube.index, LX.hsb(0, 0, 100 * EntwinedUtils.max(0, (float)(1 - deltaMs / fadeTime))), LXColor.Blend.MULTIPLY);
      }

    } else {
      clearColors();
    }

    if (objects.size() > 0) {
      Iterator<ObjectType> iter = objects.iterator();
      while (iter.hasNext()) {
        ObjectType object = iter.next();
        if (!object.running) {
          removeLayer(object);
          //layers.remove(object);
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

  @Override
  public void onTriggered(float strength) {
    super.onTriggered(strength);

    makeObject(strength);
  }

  protected abstract ObjectType generateObject(float strength);
}

