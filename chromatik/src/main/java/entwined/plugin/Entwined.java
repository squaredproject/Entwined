package entwined.plugin;

import java.io.File;

import entwined.core.CubeManager;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.surface.APC40;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.LXStudio.UI;

public class Entwined implements LXStudio.Plugin {

  public static class Triggerables extends LXComponent {

    public static final int NUM_ROWS = APC40.CLIP_LAUNCH_ROWS + 1;
    public static final int NUM_COLS = APC40.NUM_CHANNELS + 1;

    public final BooleanParameter[][] grid;

    Triggerables() {
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
    }
  }

  private static Triggerables triggers = null;

  public static Triggerables getTriggerables() {
    if (triggers == null) {
      triggers = new Triggerables();
    }
    return triggers;
  }

  public static void log(String str) {
    LX.log("[ENTWINED] " + str);
  }

  @Override
  public void initialize(LX lx) {
    log("Entwined.initialize()");

    log("Set up Triggerables");
    lx.engine.registerComponent("entwined-triggers", getTriggerables());

    log("CubeManager.init(lx)");
    CubeManager.init(lx);

    // NOTE(mcslee): start up things here like the Server, ServerController, CanopyServer
    // global stuff can go directly in the initialize method.

    lx.engine.midi.whenReady(() -> {
      final LXMidiInput apc = lx.engine.midi.findInput(APC40.DEVICE_NAME);
      if (apc == null) {
        return;
      }
      apc.addListener(new LXMidiListener() {
        public void noteOnReceived(MidiNoteOn note) {
          noteReceived(note, true);
        }

        public void noteOffReceived(MidiNote note) {
          noteReceived(note, false);
        }

        private void noteReceived(MidiNote note, boolean on) {
          log("APC40:" + (on ? "On" : "Off") + ":" + note);
          final int channel = note.getChannel();
          final int pitch = note.getPitch();
          if (pitch >= APC40.CLIP_LAUNCH && pitch <= APC40.CLIP_LAUNCH_MAX) {
            getTriggerables().grid[pitch - APC40.CLIP_LAUNCH][channel].setValue(on);
          } else if (pitch == APC40.CLIP_STOP) {
            getTriggerables().grid[Triggerables.NUM_COLS - 1][channel].setValue(on);
          } else if (pitch >= APC40.SCENE_LAUNCH && pitch <= APC40.SCENE_LAUNCH_MAX) {
            getTriggerables().grid[pitch - APC40.SCENE_LAUNCH][Triggerables.NUM_ROWS - 1].setValue(on);
          }
        }
      });
    });

    lx.addProjectListener(new LX.ProjectListener() {
      @Override
      public void projectChanged(File file, Change change) {
        if (change == Change.NEW || change == Change.OPEN) {
          log("Entwined.projectChanged(" + change + ")");
          // NOTE(mcslee): a new project file has been opened! may need to
          // initialize or re-initialize things that depend upon the project
          // state here
        }
      }
    });
  }

  @Override
  public void initializeUI(LXStudio lx, UI ui) {
    // NOTE(mcslee): probably nothing to do here
    log("Entwined.initializeUI");

  }

  @Override
  public void onUIReady(LXStudio lx, UI ui) {
    // NOTE(mcslee): potentially something here if we ever want custom UI components, but
    // most likely they are also not needed
    log("Entwined.onUIReady");
  }

}
