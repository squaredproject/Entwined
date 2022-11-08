package entwined.modulator;

import heronarts.glx.ui.component.UIButton;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.midi.surface.APC40;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.modulation.UIModulator;
import heronarts.lx.studio.ui.modulation.UIModulatorControls;

@LXCategory("Entwined")
@LXModulator.Global("Triggerables")
public class Triggerables extends LXModulator implements UIModulatorControls<Triggerables> {

  public static interface TriggerAction {
    public void onEnable();
    public void onDisable();
    public void onTimeout();
  }

  public static final int NUM_ROWS = APC40.CLIP_LAUNCH_ROWS + 1;
  public static final int NUM_COLS = APC40.NUM_CHANNELS + 1;

  public final BooleanParameter[][] grid;

  public final TriggerAction[][] actions = new TriggerAction[NUM_ROWS][NUM_COLS];

  private final double[][] timeouts = new double[NUM_ROWS][NUM_COLS];

  private static final double TIMEOUT_MS = 3000;

  public Triggerables(LX lx) {
    super("Triggerables");
    this.grid = new BooleanParameter[NUM_ROWS][NUM_COLS];
    for (int i = 0; i < NUM_ROWS; ++i) {
      for (int j = 0; j < NUM_COLS; ++j) {
        this.grid[i][j] =
          new BooleanParameter("Grid[" + i + "][" + j + "]", false)
          .setMode(BooleanParameter.Mode.MOMENTARY)
          .setDescription("Grid button " + i + "/" + j);
        addParameter("grid-" + i + "-" + j, this.grid[i][j]);
      }
    }

    // TODO: can hardcode whatever sorts of configuration you want here to wire up
    // what does what on all these triggerables...
  }

  public Triggerables setAction(int row, int col, TriggerAction action) {
    this.actions[row][col] = action;
    return this;
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    for (int i = 0; i < NUM_ROWS; ++i) {
      for (int j = 0; j < NUM_COLS; ++j) {
        if (this.grid[i][j] == p) {
          final boolean isOn = this.grid[i][j].isOn();
          final TriggerAction action = this.actions[i][j];
          if (action != null) {
            if (isOn) {
              this.timeouts[i][j] = TIMEOUT_MS;
              action.onEnable();
            } else {
              this.timeouts[i][j] = -1;
              action.onDisable();
            }
          }
          return;
        }
      }
    }
  }

  @Override
  public void buildModulatorControls(UI ui, UIModulator uiModulator, Triggerables modulator) {
    for (int row = 0; row < this.grid.length; ++row) {
      for (int col = 0; col < this.grid[row].length; ++col) {
        new UIButton(3 + col*23, 3 + row*23, 19, 19).setParameter(grid[row][col]).addToContainer(uiModulator);
      }
    }
    uiModulator.setContentHeight(140);
  }

  @Override
  protected double computeValue(double deltaMs) {
    // Countdown timers for actions that need to expire...
    for (int i = 0; i < NUM_ROWS; ++i) {
      for (int j = 0; j < NUM_COLS; ++j) {
        if (this.timeouts[i][j] > 0) {
          this.timeouts[i][j] -= deltaMs;
          if (this.timeouts[i][j] <= 0) {
            final TriggerAction action = this.actions[i][j];
            if (action != null) {
              action.onTimeout();
            }
          }
        }
      }
    }
    return 0;
  }

}
