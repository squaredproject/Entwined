package entwined.plugin;

import java.io.File;
import java.time.ZonedDateTime;
import heronarts.lx.LXComponent;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.surface.APC40;
import heronarts.lx.mixer.LXBus;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.LXStudio.UI;

import entwined.core.CubeManager;
import entwined.pattern.interactive.InteractiveCandyChaosEffect;
import entwined.pattern.interactive.InteractiveDesaturationEffect;
import entwined.pattern.interactive.InteractiveFireEffect;
import entwined.pattern.interactive.InteractiveHSVEffect;
import entwined.pattern.interactive.InteractiveRainbowEffect;

public class Entwined implements LXStudio.Plugin {


  EngineController engineController;
  LX lx;

  CanopyController canopyController;
  InteractiveHSVEffect interactiveHSVEffect;
  InteractiveFireEffect interactiveFireEffect;
  InteractiveCandyChaosEffect interactiveCandyChaosEffect;
  InteractiveRainbowEffect interactiveRainbowEffect;
  InteractiveDesaturationEffect interactiveDesaturationEffect;


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

  private void registerEntwinedContent(LX lx) {
    // lx.registry.addPattern(entwined.pattern.adam_n_katie.AutographedPattern.class);
    lx.registry.addPattern(entwined.pattern.adam_n_katie.Blooms.class);
    // lx.registry.addPattern(entwined.pattern.adam_n_katie.Boid.class);
    lx.registry.addPattern(entwined.pattern.adam_n_katie.ExpandingCircles.class);
    lx.registry.addPattern(entwined.pattern.adam_n_katie.FlockingPoints.class);
    lx.registry.addPattern(entwined.pattern.adam_n_katie.HueRibbons.class);
    lx.registry.addPattern(entwined.pattern.adam_n_katie.MovingPoint.class);
    // lx.registry.addPattern(entwined.pattern.adam_n_katie.Patterns_AdamNKatie.class);
    lx.registry.addPattern(entwined.pattern.adam_n_katie.Sparks.class);
    lx.registry.addPattern(entwined.pattern.adam_n_katie.SpiralArms.class);
    lx.registry.addPattern(entwined.pattern.adam_n_katie.Undulation.class);
    lx.registry.addPattern(entwined.pattern.adam_n_katie.VerticalColorWaves.class);
    lx.registry.addPattern(entwined.pattern.adam_n_katie.WavesToMainTree.class);
    lx.registry.addPattern(entwined.pattern.alchemy.Zebra.class);
    lx.registry.addPattern(entwined.pattern.anon.ColoredLeaves.class);
    lx.registry.addEffect(entwined.pattern.anon.ColorEffect.class);
    lx.registry.addPattern(entwined.pattern.anon.DiffusionTestPattern.class);
    lx.registry.addPattern(entwined.pattern.anon.DoubleHelix.class);
    lx.registry.addEffect(entwined.pattern.anon.HueFilterEffect.class);
    // lx.registry.addPattern(entwined.pattern.anon.Patterns.class);
    lx.registry.addPattern(entwined.pattern.anon.SeeSaw.class);
    lx.registry.addPattern(entwined.pattern.anon.SweepPattern.class);
    lx.registry.addPattern(entwined.pattern.anon.TestPattern.class);
    lx.registry.addPattern(entwined.pattern.anon.TestShrubLayers.class);
    lx.registry.addPattern(entwined.pattern.anon.TestShrubSweep.class);
    lx.registry.addPattern(entwined.pattern.anon.Twister.class);
    lx.registry.addPattern(entwined.pattern.anon.TwisterGlobal.class);
    lx.registry.addPattern(entwined.pattern.bbulkow.MultiColor.class);
    lx.registry.addPattern(entwined.pattern.bbulkow.MultiColor2.class);
    lx.registry.addPattern(entwined.pattern.bbulkow.StripeStatic.class);
    lx.registry.addPattern(entwined.pattern.charlie_stigler.Burst.class);
    lx.registry.addPattern(entwined.pattern.colin_hunt.BeachBall.class);
    lx.registry.addPattern(entwined.pattern.colin_hunt.BleepBloop.class);
    lx.registry.addPattern(entwined.pattern.colin_hunt.Bloop.class);
    lx.registry.addPattern(entwined.pattern.colin_hunt.Breath.class);
    lx.registry.addPattern(entwined.pattern.colin_hunt.ColorWave.class);
    lx.registry.addPattern(entwined.pattern.colin_hunt.CottonCandy.class);
    lx.registry.addPattern(entwined.pattern.colin_hunt.PartyRings.class);
    lx.registry.addPattern(entwined.pattern.colin_hunt.Peppermint.class);
    lx.registry.addPattern(entwined.pattern.colin_hunt.SimplexSparkle.class);
    lx.registry.addPattern(entwined.pattern.colin_hunt.UpNDown.class);
    lx.registry.addPattern(entwined.pattern.colin_hunt.Wreathes.class);
    lx.registry.addPattern(entwined.pattern.eric_gauderman.CounterSpin.class);
    lx.registry.addPattern(entwined.pattern.eric_gauderman.DiscreteColors.class);
    lx.registry.addPattern(entwined.pattern.eric_gauderman.Radar.class);
    lx.registry.addPattern(entwined.pattern.eric_gauderman.UpDown.class);
    lx.registry.addPattern(entwined.pattern.evy.CircleBreath.class);
    lx.registry.addPattern(entwined.pattern.evy.FirefliesNcase.class);
    lx.registry.addPattern(entwined.pattern.geoff_schmidt.Parallax.class);
    lx.registry.addPattern(entwined.pattern.geoff_schmidt.Pixels.class);
    lx.registry.addPattern(entwined.pattern.geoff_schmidt.Wedges.class);
    // lx.registry.addPattern(entwined.pattern.grant_patterson.GrantTest.class);
    // lx.registry.addPattern(entwined.pattern.grant_patterson.Growth.class);
    // lx.registry.addPattern(entwined.pattern.grant_patterson.Planes.class);
    lx.registry.addPattern(entwined.pattern.grant_patterson.Pond.class);
    // lx.registry.addEffect(entwined.pattern.interactive.InteractiveDesaturationEffect.class);
    // lx.registry.addEffect(entwined.pattern.interactive.InteractiveFireEffect.class);
    lx.registry.addEffect(entwined.pattern.interactive.InteractiveHSVEffect.class);
    // lx.registry.addEffect(entwined.pattern.interactive.InteractiveRainbowEffect.class);
    lx.registry.addPattern(entwined.pattern.irene_zhou.Bubbles.class);
    lx.registry.addPattern(entwined.pattern.irene_zhou.Cells.class);
    lx.registry.addPattern(entwined.pattern.irene_zhou.Fire.class);
    lx.registry.addPattern(entwined.pattern.irene_zhou.Fireflies.class);
    lx.registry.addPattern(entwined.pattern.irene_zhou.Fumes.class);
    lx.registry.addPattern(entwined.pattern.irene_zhou.Lattice.class);
    // lx.registry.addPattern(entwined.pattern.irene_zhou.Patterns_IreneZhou.class);
    lx.registry.addPattern(entwined.pattern.irene_zhou.Pulley.class);
    lx.registry.addPattern(entwined.pattern.irene_zhou.Pulleys.class);
    lx.registry.addPattern(entwined.pattern.irene_zhou.Springs.class);
    lx.registry.addPattern(entwined.pattern.irene_zhou.Voronoi.class);
    lx.registry.addPattern(entwined.pattern.jake_lampack.AcidTrip.class);
    lx.registry.addEffect(entwined.pattern.kyle_fleming.AcidTripTextureEffect.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.Brightness.class);
    lx.registry.addEffect(entwined.pattern.kyle_fleming.BrightnessScaleEffect.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.CandyCloud.class);
    lx.registry.addEffect(entwined.pattern.kyle_fleming.CandyCloudTextureEffect.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.ClusterLineTest.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.ColorStrobe.class);
    lx.registry.addEffect(entwined.pattern.kyle_fleming.ColorStrobeTextureEffect.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.Explosions.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.Fade.class);
    lx.registry.addEffect(entwined.pattern.kyle_fleming.FadeTextureEffect.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.GalaxyCloud.class);
    lx.registry.addEffect(entwined.pattern.kyle_fleming.GhostEffect.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.MappingPattern.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.Palette.class);
    // lx.registry.addPattern(entwined.pattern.kyle_fleming.Patterns_KyleFleming.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.Rain.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.RandomColor.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.RandomColorGlitch.class);
    lx.registry.addEffect(entwined.pattern.kyle_fleming.ScrambleEffect.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.SolidColor.class);
    lx.registry.addEffect(entwined.pattern.kyle_fleming.SpeedEffect.class);
    lx.registry.addEffect(entwined.pattern.kyle_fleming.StaticEffect.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.Strobe.class);
    lx.registry.addEffect(entwined.pattern.kyle_fleming.TurnOffDeadPixelsEffect.class);
    lx.registry.addPattern(entwined.pattern.kyle_fleming.Wisps.class);
    lx.registry.addPattern(entwined.pattern.kyle_stuart.Snowflakes.class);
    lx.registry.addPattern(entwined.pattern.lindsay_jason.SparkleWave.class);
    lx.registry.addPattern(entwined.pattern.lorenz.Fountain.class);
    lx.registry.addPattern(entwined.pattern.mark_lottor.MarkLottor.class);
    lx.registry.addPattern(entwined.pattern.mary_wang.Twinkle.class);
    lx.registry.addPattern(entwined.pattern.mary_wang.VerticalSweep.class);
    lx.registry.addPattern(entwined.pattern.mattaniah.OscillatingDarkRing.class);
    lx.registry.addPattern(entwined.pattern.mattaniah.RadialGradiant.class);
    lx.registry.addPattern(entwined.pattern.misko.Circles.class);
    lx.registry.addPattern(entwined.pattern.misko.ColorBlast.class);
    lx.registry.addPattern(entwined.pattern.misko.LightHouse.class);
    lx.registry.addPattern(entwined.pattern.misko.LineScan.class);
    lx.registry.addPattern(entwined.pattern.misko.RainbowWaveScan.class);
    lx.registry.addPattern(entwined.pattern.misko.ShrubRiver.class);
    lx.registry.addPattern(entwined.pattern.misko.Stringy.class);
    lx.registry.addPattern(entwined.pattern.misko.Vertigo.class);
    lx.registry.addPattern(entwined.pattern.misko.WaveScan.class);
    lx.registry.addPattern(entwined.pattern.quinn_keck.ButterflyEffect.class);
    lx.registry.addPattern(entwined.pattern.ray_sykes.IceCrystals.class);
    lx.registry.addPattern(entwined.pattern.ray_sykes.Lightning.class);
    lx.registry.addPattern(entwined.pattern.ray_sykes.MultiSine.class);
    lx.registry.addPattern(entwined.pattern.ray_sykes.Ripple.class);
    lx.registry.addPattern(entwined.pattern.ray_sykes.SparkleHelix.class);
    lx.registry.addPattern(entwined.pattern.ray_sykes.SparkleTakeOver.class);
    lx.registry.addPattern(entwined.pattern.ray_sykes.Stripes.class);
    lx.registry.addPattern(entwined.pattern.sydney_parcell.RoseGarden.class);
  }

  /* configureServer */
  void configureServer() {
    new AppServer(lx, engineController).start();
  }

  /*
  // Log Helper
  void log(String s) {
      System.out.println(
      ZonedDateTime.now( localZone ).format( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) + " " + s );
  }
  */

  @Override
  public void initialize(LX lx) {
    System.out.println(" initialize being called ");
    log("Entwined.initialize()");

    // Check if we are running from Eclipse/IDE mode, where the Entwined
    // plugin is on the classpath, not in a dynamic JAR, and was specified
    // from the CLI args. In this case we need to register our content classes
    // manually, as they will not be dynamically loaded from a JAR.
    if (lx.flags.classpathPlugins.contains("entwined.plugin.Entwined")) {
      registerEntwinedContent(lx);
    }

    this.lx = lx;

    log("Set up Triggerables");
    lx.engine.registerComponent("entwined-triggers", getTriggerables());

    engineController = new EngineController(lx);

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

    /*
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
    */


    configureServer(); // turns on the TCP listener


    // this special filter is used by Canopy -- the interactive effects
    interactiveHSVEffect = new InteractiveHSVEffect(lx);
    lx.addEffect(interactiveHSVEffect); /* want this one "on top" of everything else... is it? */
    interactiveHSVEffect.enable();

    // this fire effect, going to make it more generic, but make it work at all now
    interactiveFireEffect = new InteractiveFireEffect(lx, lx.getModel());
    LXEffect[] fireEffects = interactiveFireEffect.getEffects();
    for (LXEffect effect : fireEffects) {
      lx.addEffect(effect);
      effect.enable();
    }

    interactiveCandyChaosEffect = new InteractiveCandyChaosEffect(lx, lx.getModel());
    LXEffect[] candyChaosEffects = interactiveCandyChaosEffect.getEffects();
    for (LXEffect effect: candyChaosEffects) {
      lx.addEffect(effect);
      effect.enable();
    }

    interactiveRainbowEffect = new InteractiveRainbowEffect(lx, lx.getModel());
    LXEffect[] interactiveRainbowEffects = interactiveRainbowEffect.getEffects();
    for (LXEffect effect: interactiveRainbowEffects) {
      lx.addEffect(effect);
      effect.enable();
    }

    interactiveDesaturationEffect = new InteractiveDesaturationEffect(lx, lx.getModel());
    LXEffect[] interactiveDesaturationEffects = interactiveDesaturationEffect.getEffects();
    for (LXEffect effect: interactiveDesaturationEffects) {
      lx.addEffect(effect);
      effect.enable();
    }

    // must be after creation of the filter effect(s) used
    canopyController = new CanopyController(this);


    // tell the canopyController what it should be up to.
    // this perhaps needs to move elsewhere, possibly to the constructor of canopy
    // controller or the main init, unclear it should really be intermixed with EngineController
    ZonedDateTime firstPause = ZonedDateTime.now();
    firstPause.plusSeconds( (int) (Config.pauseRunMinutes * 60.0) );
    canopyController.modelUpdate(true /*interactive*/, (int) (Config.pauseRunMinutes * 60.0f) /*runSeconds*/,
      (int) (Config.pausePauseMinutes * 60.0f) /*pauseSeconds*/,"run" /*state*/,firstPause);


    // bad code I know
    // (shouldn't mess with engine internals)
    // maybe need a way to specify a deck shouldn't be focused?
    // essentially this lets us have extra decks for the drumpad
    // patterns without letting them be assigned to channels
    // -kf
    lx.engine.mixer.focusedChannel.setRange(Engine.NUM_BASE_CHANNELS);
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

  public static <T extends LXEffect> T findMasterEffect(LX lx, Class<T> clazz) {
    return findEffect(lx.engine.mixer.masterBus, clazz);
  }

  @SuppressWarnings("unchecked")
  public static <T extends LXEffect> T findEffect(LXBus bus, Class<T> clazz) {
    for (LXEffect effect : bus.effects) {
      if (effect.getClass().equals(clazz)) {
        return (T) effect;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T extends LXPattern> T findPattern(LXChannel channel, Class<T> clazz) {
    for (LXPattern pattern : channel.patterns) {
      if (pattern.getClass().equals(clazz)) {
        return (T) pattern;
      }
    }
    return null;
  }

}
