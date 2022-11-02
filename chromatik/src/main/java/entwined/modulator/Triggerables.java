package entwined.modulator;

import entwined.plugin.Entwined;
import heronarts.glx.ui.component.UIButton;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.modulation.UIModulator;
import heronarts.lx.studio.ui.modulation.UIModulatorControls;

@LXCategory("Entwined")
@LXModulator.Global("Triggerables")
public class Triggerables extends LXModulator implements UIModulatorControls<Triggerables> {

  public Triggerables(LX lx) {
    super("Triggerables");
  }

  @Override
  public void buildModulatorControls(UI ui, UIModulator uiModulator, Triggerables modulator) {
    BooleanParameter[][] grid = Entwined.getTriggerables().grid;
    for (int row = 0; row < grid.length; ++row) {
      for (int col = 0; col < grid[row].length; ++col) {
        new UIButton(3 + col*23, 3 + row*23, 19, 19).setParameter(grid[row][col]).addToContainer(uiModulator);
      }
    }
    uiModulator.setContentHeight(140);
  }

  @Override
  protected double computeValue(double deltaMs) {
    return 0;
  }

}
