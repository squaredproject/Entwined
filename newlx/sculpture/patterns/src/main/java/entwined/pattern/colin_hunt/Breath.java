package entwined.pattern.colin_hunt;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.pattern.LXPattern;

/**
Breath in, breath out
*/
public class Breath extends LXPattern {

  // Variable declarations, parameters, and modulators go here
  //final BoundedParameter parameterName = new BoundedParameter("parameterName", startValue, minValue, maxValue);
  float minValue = 0.f;
  float maxValue = 100.f;
  float period = 8000;
  final SinLFO breath = new SinLFO(minValue, maxValue, period);

  float hue = 180;
  boolean changeHue = false;

  int highestSoFar = -1;
  int treeyes = 0;
  int shrubtes = 0;

  // Constructor
  public Breath(LX lx) {
    super(lx);
    // Add any needed modulators or parameters here
    addModulator(breath).start();
    //addParameter(parameterName);
  }

  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs) {

    if (getChannel().fader.getNormalized() == 0) return;

    if (changeHue == true && breath.getValuef() < 3) {
      hue = (float)Math.random() * 360;
      changeHue = false;
    }

    //breath.setPeriod(period - (Math.abs(breath.getValuef() - 50.0f) * 50));
    // Use a for loop here to set the cube colors
    for (LXPoint cube : model.points) {
      colors[cube.index] = LX.hsb( hue, 55, breath.getValuef());
    }

    if (breath.getValuef() > 90) {
      changeHue = true;
    }
  }
}