package entwined.pattern.kyle_fleming;

import heronarts.lx.pattern.LXPattern;
import heronarts.lx.LX;
import heronarts.lx.LXComponentName;

@LXComponentName("NoPattern")
public class NoPattern extends LXPattern {
  public NoPattern(LX lx) {
    super(lx);
    // label = new StringParameter("NoPattern", "Turns off all effects");
  }

  @Override
  public void run(double deltaMs) {
  }
}
