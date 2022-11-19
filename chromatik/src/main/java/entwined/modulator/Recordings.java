package entwined.modulator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import heronarts.glx.ui.UI2dContainer;
import heronarts.glx.ui.component.UIButton;
import heronarts.glx.ui.component.UILabel;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponent;
import heronarts.lx.LXPath;
import heronarts.lx.LXSerializable;
import heronarts.lx.clip.LXClip;
import heronarts.lx.mixer.LXBus;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.TriggerParameter;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.modulation.UIModulator;
import heronarts.lx.studio.ui.modulation.UIModulatorControls;

@LXCategory("Entwined")
@LXModulator.Global("Recordings")
public class Recordings extends LXModulator implements UIModulatorControls<Recordings> {

  public Recordings(LX lx) {
    super("Recordings");
  }


  @Override
  public void buildModulatorControls(UI ui, UIModulator uiModulator, Recordings recordings) {
    uiModulator.setLayout(UIModulator.Layout.VERTICAL);
    uiModulator.setChildSpacing(4);

    final TriggerParameter stopTrigger = new TriggerParameter("Stop");
    final TriggerParameter stopPlaying = new TriggerParameter("Stop Playing");
    final TriggerParameter stopRecording = new TriggerParameter("Stop Recording");

    final UILabel name = (UILabel)
      new UILabel(0, 0, uiModulator.getContentWidth() - 44, 18)
      .setLabel("<default>")
      .setBackgroundColor(ui.theme.listBackgroundColor)
      .setBorderColor(ui.theme.listBorderColor)
      .setBorderRounding(4)
      .setTextAlignment(VGraphics.Align.CENTER, VGraphics.Align.MIDDLE);

    final UIButton save = (UIButton) new UIButton(0, 0, 18, 18) {
      @Override
      public void onClick() {
        stopTrigger.trigger();
        ui.lx.showSaveFileDialog(
          "Save Recording",
          "Recording File",
          new String[] { "lxr" },
          ui.lx.getMediaFile(LX.Media.PROJECTS, "default.lxr").toString(),
          (path) -> {
            File file = new File(path);
            saveRecording(ui.lx, file);
            name.setLabel(file.getName());
          }
        );
      }
    }
    .setIcon(ui.theme.iconSaveAs)
    .setMomentary(true)
    .setDescription("Save Recording As...");

    final UIButton open = (UIButton) new UIButton(0, 0, 18, 18) {
      @Override
      public void onClick() {
        stopTrigger.trigger();
        ui.lx.showOpenFileDialog(
          "Open Recording",
          "Recording File",
          new String[] { "lxr" },
          ui.lx.getMediaFile(LX.Media.PROJECTS, "default.lxr").toString(),
          (path) -> {
            File file = new File(path);
            openRecording(ui.lx, new File(path));
            name.setLabel(file.getName());
          }
        );
      }
    }
    .setIcon(ui.theme.iconOpen)
    .setMomentary(true)
    .setDescription("Open Recording...");

    final UIButton play = (UIButton) new UIButton(0, 0, 67, 16) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          stopRecording.trigger();
          playRecording(lx);
        } else {
          stopTrigger.trigger();
        }
      }
    }
    .setLabel("PLAY")
    .setBorderRounding(2);

    final UIButton record = (UIButton) new UIButton(0, 0, 66, 16) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          String label = name.getLabel();
          if (!label.endsWith("*")) {
            name.setLabel(label + "*");
          }

          stopPlaying.trigger();
          for (LXBus bus : lx.engine.mixer.channels) {
            bus.removeClip  (0);
            LXClip clip = bus.addClip(0);
            clip.loop.setValue(true);
            bus.arm.setValue(true);
          }
          lx.engine.mixer.masterBus.removeClip(0);
          lx.engine.mixer.masterBus.arm.setValue(true);
          LXClip clip = lx.engine.mixer.masterBus.addClip(0);
          clip.loop.setValue(true);
          ui.lx.engine.clips.launchScene(0);
        } else {
          stopTrigger.trigger();
        }
      }
    }
    .setLabel("RECORD")
    .setBorderRounding(2);

    final UIButton stop = (UIButton) new UIButton(0, 0, 67, 16) {
      @Override
      public void onClick() {
        stopTrigger.trigger();
      }
    }
    .setLabel("STOP")
    .setMomentary(true)
    .setBorderRounding(2);

    stopTrigger.addListener(p -> {
      ui.lx.engine.clips.stopClips();
      play.setActive(false);
      record.setActive(false);
    });

    stopPlaying.addListener(p -> {
      play.setActive(false);
    });

    stopRecording.addListener(p -> {
      record.setActive(false);
    });

    // Build UI
    uiModulator.addChildren(
      UI2dContainer.newHorizontalContainer(18, 4, name, save, open),
      UI2dContainer.newHorizontalContainer(16, 4, play, record, stop)
    );
  }

  private void saveClip(LX lx, LXBus bus, JsonArray clips) {
    LXClip clip = bus.getClip(0);
    if (clip != null) {
      JsonObject clipObj = new JsonObject();
      clipObj.addProperty("path", bus.getCanonicalPath(lx.engine.mixer));
      clipObj.add("clip", LXSerializable.Utils.toObject(clip, true));
      clips.add(clipObj);
    }
  }

  private void saveRecording(LX lx, File file) {
    JsonObject obj = new JsonObject();
    obj.addProperty("version", LX.VERSION);
    obj.addProperty("timestamp", System.currentTimeMillis());
    JsonArray clips = new JsonArray();
    for (LXBus bus : lx.engine.mixer.channels) {
      saveClip(lx, bus, clips);
    }
    saveClip(lx, lx.engine.mixer.masterBus, clips);
    obj.add("clips", clips);

    try (JsonWriter writer = new JsonWriter(new FileWriter(file))) {
      writer.setIndent("  ");
      new GsonBuilder().create().toJson(obj, writer);
      LX.log("Recording saved successfully to " + file.toString());
    } catch (IOException iox) {
      LX.error(iox, "Could not write recording to output file: " + file.toString());
    }
  }

  public void openRecording(LX lx, File file) {
    try (FileReader fr = new FileReader(file)) {
      JsonObject obj = new Gson().fromJson(fr, JsonObject.class);
      JsonArray clips = obj.get("clips").getAsJsonArray();
      for (int i = 0; i < clips.size(); ++i) {
        JsonObject clipObj = clips.get(i).getAsJsonObject();
        String path = clipObj.get("path").getAsString();
        LXComponent component = LXPath.getComponent(lx.engine.mixer, path);
        if (!(component instanceof LXBus)) {
          lx.pushError("Channel in recording file does not exist: " + path);
        } else {
          LXBus bus = (LXBus) component;
          bus.removeClip(0);
          bus.addClip(clipObj.get("clip").getAsJsonObject(), 0);
        }
      }
    } catch (Throwable x) {
      LX.error(x, "Could not load recording file: " + x.getMessage());
    }
  }

  public void playRecording(LX lx) {
    for (LXBus bus : lx.engine.mixer.channels) {
      bus.arm.setValue(false);
    }
    lx.engine.mixer.masterBus.arm.setValue(false);
    lx.engine.clips.launchScene(0);
  }

  @Override
  protected double computeValue(double deltaMs) {
    return 0;
  }

}
