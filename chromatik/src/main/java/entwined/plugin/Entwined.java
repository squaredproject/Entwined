package entwined.plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.LXDeviceComponent;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.surface.APC40;
import heronarts.lx.mixer.LXAbstractChannel;
import heronarts.lx.mixer.LXBus;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.LXStudio.UI;

import entwined.core.Triggerable;
import entwined.modulator.Recordings;
import entwined.modulator.Triggerables;
import entwined.pattern.anon.ColorEffect;
import entwined.pattern.anon.HueFilterEffect;
import entwined.pattern.interactive.InteractiveCandyChaosEffect;
import entwined.pattern.interactive.InteractiveDesaturationEffect;
import entwined.pattern.interactive.InteractiveFireEffect;
import entwined.pattern.interactive.InteractiveHSVEffect;
import entwined.pattern.interactive.InteractiveRainbowEffect;
import entwined.pattern.irene_zhou.Bubbles;
import entwined.pattern.irene_zhou.Cells;
import entwined.pattern.kyle_fleming.AcidTripTextureEffect;
import entwined.pattern.kyle_fleming.BrightnessScaleEffect;
import entwined.pattern.kyle_fleming.CandyCloudTextureEffect;
import entwined.pattern.kyle_fleming.CandyTextureEffect;
import entwined.pattern.kyle_fleming.ColorStrobeTextureEffect;
import entwined.pattern.kyle_fleming.FadeTextureEffect;
import entwined.pattern.kyle_fleming.GhostEffect;
import entwined.pattern.kyle_fleming.ScrambleEffect;
import entwined.pattern.kyle_fleming.SpeedEffect;
import entwined.pattern.kyle_fleming.StaticEffect;
import entwined.pattern.kyle_fleming.Wisps;
import entwined.pattern.ray_sykes.Lightning;

public class Entwined implements LXStudio.Plugin {

  LX lx;

  EngineController engineController;
  CanopyController canopyController;
  Triggerables triggerables;
  InteractiveHSVEffect interactiveHSVEffect;
  InteractiveFireEffect interactiveFireEffect;
  InteractiveCandyChaosEffect interactiveCandyChaosEffect;
  InteractiveRainbowEffect interactiveRainbowEffect;
  InteractiveDesaturationEffect interactiveDesaturationEffect;
  AppServer iPadServer;
  NFCServer nfcServer;

  BrightnessScaleEffect masterBrightnessEffect;
  BrightnessScaleEffect autoplayBrightnessEffect;

  static String autoplayBrightnessName = "Autoplay Brightness";
  static String masterBrightnessName = "Master Brightness";

  LXChannel effectsChannel;

  public static void log(String str) {
    LX.log("[ENTWINED] " + str);
  }

  private void registerEntwinedContent(LX lx) {
    // NB - all available patterns - including those available on the iPad - should be registered here

    lx.registry.addModulator(entwined.modulator.GlobalEffects.class);
    lx.registry.addModulator(entwined.modulator.PatternChooser.class);
    lx.registry.addModulator(entwined.modulator.Triggerables.class);
    lx.registry.addModulator(entwined.modulator.Recordings.class);

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
    lx.registry.addPattern(entwined.pattern.bbulkow.VideoPlayer.class);
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
    lx.registry.addPattern(entwined.pattern.eric_gauderman.FreeFall.class);
    lx.registry.addPattern(entwined.pattern.eric_gauderman.Radar.class);
    lx.registry.addPattern(entwined.pattern.eric_gauderman.UpDown.class);
    lx.registry.addPattern(entwined.pattern.evy.CircleBreath.class);
    lx.registry.addPattern(entwined.pattern.evy.FirefliesNcase.class);
    lx.registry.addPattern(entwined.pattern.geoff_schmidt.Parallax.class);
    lx.registry.addPattern(entwined.pattern.geoff_schmidt.Pixels.class);
    lx.registry.addPattern(entwined.pattern.geoff_schmidt.Wedges.class);
    // lx.registry.addPattern(entwined.pattern.grant_patterson.GrantTest.class);
    lx.registry.addPattern(entwined.pattern.grant_patterson.Growth.class);
    lx.registry.addPattern(entwined.pattern.grant_patterson.Planes.class);
    lx.registry.addPattern(entwined.pattern.grant_patterson.Pond.class);
    // NB - I note that the following are containers for LXEffects, not LXEffects themselves.
    // We communicate with them to enable and disable their per-component child effects
    // lx.registry.addEffect(entiwned.pattern.interactive.InteractiveCandyChaosEffect.class);
    // lx.registry.addEffect(entwined.pattern.interactive.InteractiveDesaturationEffect.class);
    // lx.registry.addEffect(entwined.pattern.interactive.InteractiveFireEffect.class);
    // lx.registry.addEffect(entwined.pattern.interactive.InteractiveRainbowEffect.class);
    // XXX - I'm guessing that these - the children of the above - don't have to be registered either, since they are never
    // accessed directly by the UI. Trying to check with Mark about this...
    //lx.registry.addEffect(entwined.pattern.interactive.InteractiveCandyChaosEffect.InteractiveCandyChaos.class);
    //lx.registry.addEffect(entwined.pattern.interactive.InteractiveDesaturationEffect.InteractiveDesaturation.class);
    //lx.registry.addEffect(entwined.pattern.interactive.InteractiveFireEffect.InteractiveFire.class);
    //lx.registry.addEffect(entwined.pattern.interactive.InteractiveRainbowEffect.InteractiveRainbow.class);

    lx.registry.addEffect(entwined.pattern.interactive.InteractiveHSVEffect.class);
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
    lx.registry.addPattern(entwined.pattern.kyle_fleming.NoPattern.class);
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
    lx.registry.addPattern(entwined.pattern.misko.WaveScanRainbow.class);
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

  /* configureServers */
  private void configureServers() {
    iPadServer = new AppServer(lx, engineController);
    iPadServer.start();
    nfcServer = new NFCServer(lx, engineController);
    nfcServer.start();
  }

  private void shutdownServers() {
    if (iPadServer != null) {
      iPadServer.shutdown();
      iPadServer = null;
    }
    if (nfcServer != null) {
      nfcServer.shutdown();
      nfcServer = null;
    }
  }

  private void shutdownCanopy() {
    if (canopyController != null) {
      canopyController.shutdown();
      canopyController = null;
    }
  }


  /* Instantiate standard effects and add them to the master output if they
   * aren't already there.
   */
  private void setupMasterEffects() {

    System.out.println("*** SETUP MASTER EFFECTS ***");
    setupMasterEffect(lx, BlurEffect.class);
    setupMasterEffect(lx, ColorEffect.class);
    setupMasterEffect(lx, HueFilterEffect.class);
    setupMasterEffect(lx, GhostEffect.class);
    setupMasterEffect(lx, ScrambleEffect.class);
    setupMasterEffect(lx, StaticEffect.class);
    setupMasterEffect(lx, SpeedEffect.class);
    setupMasterEffect(lx, ColorStrobeTextureEffect.class);
    setupMasterEffect(lx, FadeTextureEffect.class);
    setupMasterEffect(lx, AcidTripTextureEffect.class);
    setupMasterEffect(lx, CandyTextureEffect.class);
    setupMasterEffect(lx, CandyCloudTextureEffect.class);

    // Master brightness and autoplay brightness also go on the master channel,
    // but they are also tied to some internal logic.
    masterBrightnessEffect   = setupMasterEffectWithName(lx, BrightnessScaleEffect.class, masterBrightnessName);
    autoplayBrightnessEffect = setupMasterEffectWithName(lx, BrightnessScaleEffect.class, autoplayBrightnessName);
  }

  private void configureTriggeredEffects() {
    setupMasterEffects();     // sets up standard effects on master channel
    configureTriggerables();  // Configures triggerable effects, including effects for APC40
  }


  @Override
  public void initialize(LX lx) {
    log("Entwined.initialize()");

    // Initialize configuration system from the json file
    Config.Init("config.json");

    // Check if we are running from Eclipse/IDE mode, where the Entwined
    // plugin is on the classpath, not in a dynamic JAR, and was specified
    // from the CLI args. In this case we need to register our content classes
    // manually, as they will not be dynamically loaded from a JAR.
    // 'Content' here is our custom patterns and effects.
    if (lx.flags.classpathPlugins.contains("entwined.plugin.Entwined")) {
      registerEntwinedContent(lx);
    }

    this.lx = lx;

    // Set up some master parameters... XXX - not sure if this is really used.
    // XXX - it *was* also used to do autoplayback and (I think) recording, which is
    // a crucial feature.


    // Set up to initialize midi commands from the APC40, when the midi system
    // is ready for it.
    lx.engine.midi.whenReady(() -> {
      final LXMidiInput apc = lx.engine.midi.findInput(APC40.DEVICE_NAME);
      if (apc == null) {
        return;
      }
      apc.addListener(new LXMidiListener() {
        @Override
        public void noteOnReceived(MidiNoteOn note) {
          noteReceived(note, true);
        }

        @Override
        public void noteOffReceived(MidiNote note) {
          noteReceived(note, false);
        }

        private void noteReceived(MidiNote note, boolean on) {
          log("APC40:" + (on ? "On" : "Off") + ":" + note);
          if (triggerables != null) {
            final int channel = note.getChannel();
            final int pitch = note.getPitch();
            if (pitch >= APC40.CLIP_LAUNCH && pitch <= APC40.CLIP_LAUNCH_MAX) {
              triggerables.grid[pitch - APC40.CLIP_LAUNCH][channel].setValue(on);
            } else if (pitch == APC40.CLIP_STOP) {
              triggerables.grid[Triggerables.NUM_COLS - 1][channel].setValue(on);
            } else if (pitch >= APC40.SCENE_LAUNCH && pitch <= APC40.SCENE_LAUNCH_MAX) {
              triggerables.grid[pitch - APC40.SCENE_LAUNCH][Triggerables.NUM_ROWS - 1].setValue(on);
            }
          }
        }
      });
    });

    Entwined entwined = this;
    lx.addProjectListener(new LX.ProjectListener() {
      @Override
      public void projectChanged(File file, Change change) {
        if (change == Change.NEW || change == Change.OPEN) {
          log("Entwined.projectChanged(" + change + ")");
          // NOTE(mcslee): a new project file has been opened! may need to
          // initialize or re-initialize things that depend upon the project
          // state here

          // Rip down servers before building up again. This makes sure
          // we can load new projects
          shutdownCanopy();
          shutdownServers();
          if (engineController != null) {
            engineController.shutdown();
            engineController = null;
          }

          // Set up the channels
          configureChannels();

// Grab the triggerables object if it exists
      triggerables = findModulator(lx, Triggerables.class);
    
      // Set up triggerable events
      if (triggerables != null) {
        configureTriggeredEffects();
      }

      // Set up the low level iPad Controller
      engineController = new EngineController(lx, entwined);  // XXX might want to have a listener on the controller, rather than newing up the engine controller here

          // Set up high level iPad Server. Uses the iPadController to actually do the work.
          configureServers(); // turns on the TCP listener

          // Set up Canopy listener (also TCP) for interactive commands
          configureCanopy();

      Recordings recordings = findModulator(lx, Recordings.class); 
      if (recordings != null) {
            // TODO - perhaps come up with a more elegant solution here for specifying
        // what the file to auto-play is?
        File autoplayFile = new File("autoplay.lxr");
if ((autoplayFile != null) && autoplayFile.exists()) {
          log("Auto-playing saved recording file: " + autoplayFile); 
          recordings.openRecording(lx, autoplayFile);
          recordings.playRecording(lx);
            }
            else {
          log(" autoplay file not found, continuing");
            }
          }
        }
      }
    });
  }

  @Override
  public void dispose() {
    shutdownServers();
    shutdownCanopy();
  }

  // Configure the array of triggerables.
  void configureTriggerables()
  {
    // There are several types of triggerables -
      // Events (which derive from LXEvent)
    // Standard patterns
    // One-shot patterns
    // Patterns that use a parameter in addition to the fader for fading in and out

    // other breadcrumbs:
    // the "setAction" will register an action for the grid of APC40 / Triggers array
    // 0,0 is the upper left corner <row>,<column>; so 0,1 is the second button on the top row
    //    1,0 is the first button on the second row
          
          // how to add one: look at the textures on the master channel.
    // find one you want. Look up its class and find a parameter you want to change.
    // Follow the pattern.

    // Instead of turning the action on or off, let's change the color. 
    // ReleaseDisables indicates that this is an effect that is always going to be on - we're just playing with its values.
    CandyCloudTextureEffect cctEffect = Entwined.findMasterEffect(lx, CandyCloudTextureEffect.class);
    triggerables.setAction(0,0, new EventTrigger(cctEffect, cctEffect.amount, 0.7).releaseDisables(false));
    cctEffect.enabled.setValue(false);

    // straight up on/off on the effect - turn on or off the standard color effect
    // you don't want to do this. Effects have to be on all the time.
    // triggerables.setAction(0,0, new EventTrigger(Entwined.findMasterEffect(lx, ColorEffect.class)));

    // Instead of turning the action on or off, let's change a value. 
    ColorEffect colorEffect = Entwined.findMasterEffect(lx, ColorEffect.class);
    triggerables.setAction(0,1, new EventTrigger(colorEffect, colorEffect.hueShift, 1.0).releaseDisables(false));
    colorEffect.enabled.setValue(false);

    // The same sort of thing can be used for triggering patterns on the 'Events' channel..
    Bubbles bubbles = Entwined.findPattern(effectsChannel, Bubbles.class);
    if (bubbles == null) {
      bubbles = new Bubbles(lx);
      effectsChannel.addPattern(bubbles);
    }
    triggerables.setAction(0,2, new EventTrigger(bubbles, bubbles.ballCount, 50.0));  // Lots of bubbles
    triggerables.setAction(0,3, new EventTrigger(bubbles, bubbles.ballCount, 5.0));   // Not so many bubbles
    bubbles.enabled.setValue(false);

    // Let's set up a lot of Wisps as well. That seemed to be popular in the previous version of LX
    // Here I construct a large number of slightly different versions.
    Wisps downward_yellow_wisp = Entwined.findPatternWithName(effectsChannel, Wisps.class, "Wisp: yellow");
    if (downward_yellow_wisp == null) {
      downward_yellow_wisp = new Wisps(lx, 1, 60, 50, 270, 20, 3.5, 10);
      downward_yellow_wisp.label.setValue("Wisp: yellow");
      effectsChannel.addPattern(downward_yellow_wisp);
    }
    triggerables.setAction(0, 4, downward_yellow_wisp);
    downward_yellow_wisp.enabled.setValue(false);

    Wisps colorful_wisp_storm = Entwined.findPatternWithName(effectsChannel, Wisps.class, "Wisp: storm");
    if (colorful_wisp_storm == null) {
      colorful_wisp_storm = new Wisps(lx, 30, 210, 100, 90, 20, 3.5, 10);
      colorful_wisp_storm.label.setValue("Wisp: storm");
      effectsChannel.addPattern(colorful_wisp_storm);
    }
    triggerables.setAction(0, 5, colorful_wisp_storm);
    colorful_wisp_storm.enableTriggerMode();


    Wisps multidirection_wisps = Entwined.findPatternWithName(effectsChannel, Wisps.class, "Wisp: multi");
    if (multidirection_wisps == null) {
      multidirection_wisps = new Wisps(lx, 1, 210, 100, 90, 130, 3.5, 10);
      multidirection_wisps.label.setValue("Wisp: multi");
      effectsChannel.addPattern(multidirection_wisps);
    }
    multidirection_wisps.enableTriggerMode();
    triggerables.setAction(0, 6, multidirection_wisps);


    Wisps rainstorm_wisps = Entwined.findPatternWithName(effectsChannel, Wisps.class, "Wisp: rain");
    if (rainstorm_wisps == null) {
      rainstorm_wisps = new Wisps(lx, 3, 210, 10, 270, 0, 3.5, 10);
      rainstorm_wisps.label.setValue("Wisp: rain");
      effectsChannel.addPattern(rainstorm_wisps);
    }
    rainstorm_wisps.enableTriggerMode();
    triggerables.setAction(0, 7, rainstorm_wisps);


    Wisps twister_wisps = Entwined.findPatternWithName(effectsChannel, Wisps.class, "Wisp: twister");
    if (twister_wisps == null) {
      twister_wisps = new Wisps(lx, 35, 210, 180, 180, 15, 2, 15);
      twister_wisps.label.setValue("Wisp: twister");
      effectsChannel.addPattern(twister_wisps);
    }
    twister_wisps.enableTriggerMode();
    triggerables.setAction(0, 8, twister_wisps);

    // Here's another way of setting things up if the pattern isn't designed for triggers Very simple!
    triggerables.createPatternAction(1, 0, Entwined.setupTriggerablePattern(lx, effectsChannel, Cells.class));

    // And some lightning because - why not?
    Lightning lightning = Entwined.findPattern(effectsChannel, Lightning.class);
    if (lightning == null) {
      lightning = new Lightning(lx);
      effectsChannel.addPattern(lightning);
    }
    lightning.enableTriggerMode();
    triggerables.setAction(1,1, lightning);

    ScrambleEffect scrambleEffect = Entwined.findMasterEffect(lx, ScrambleEffect.class);
    triggerables.setAction(1,7, new EventTrigger(scrambleEffect, scrambleEffect.amount, 1.0).releaseDisables(false));
    scrambleEffect.enabled.setValue(false);

    // 1.0 amount is a bit harsh
    ColorStrobeTextureEffect cstEffect = Entwined.findMasterEffect(lx, ColorStrobeTextureEffect.class);
    triggerables.setAction(1,8, new EventTrigger(cstEffect, cstEffect.amount, 0.7).releaseDisables(false));
    cstEffect.enabled.setValue(false);

  }


  class EventTrigger implements Triggerable{
    LXDeviceComponent effect;
    Boolean isTriggered = false;
    double onAmount;
    double originalAmount;
    boolean releaseDisables = true;
    LXListenableNormalizedParameter amountControl = null;

    EventTrigger(LXDeviceComponent effect){
      this.effect = effect;
    }

    EventTrigger(LXDeviceComponent effect, LXListenableNormalizedParameter amountControl, double onAmount){
      this.effect = effect;
      this.amountControl = amountControl;
      this.onAmount = onAmount;
    }

    public EventTrigger releaseDisables(boolean tf) {
      this.releaseDisables = tf;
      return this;
    }

    @Override
    public void onTriggered() {
      System.out.println("Event trigger triggered!!");
      isTriggered = true;
      if (amountControl != null) {
        originalAmount = amountControl.getValue();
        amountControl.setValue(onAmount);
      }
      if (effect instanceof LXEffect) {
        ((LXEffect)effect).enabled.setValue(true);
      } else if (effect instanceof LXPattern) {
        ((LXPattern)effect).enabled.setValue(true);
      }
    }

    @Override
    public void onReleased() {
      System.out.println("Event trigger released!!");
      shutdownTriggeredEvent();
    }

    void shutdownTriggeredEvent() {
      isTriggered = false;
      if (amountControl != null) {
        amountControl.setValue(originalAmount);
      }
      if (releaseDisables) {
        if (effect instanceof LXEffect) {
          ((LXEffect)effect).enabled.setValue(false);
        } else if (effect instanceof LXPattern) {
          ((LXPattern)effect).enabled.setValue(false);
        }
      }
    }

    @Override
    public void onTimeout() {
      System.out.println("Event trigger timeout");
      shutdownTriggeredEvent();
    }
  }

  /*
   * configureCanopy
   *
   * Canopy is the system used to control per-component interactivity from external devices,
   * such as the web interface on a phone, or an rPi hooked up to some sensor.
   *
   * The configuration here creates these special interactive effects and sets up the
   * Canopy controller.
   *
   * Note that currently the pattern for effects used by Canopy is a little non-obvious. For
   * each type of effect we want to have available to Canopy, we create a separate global effect
   * for each component. (So N types of effects, M components, N*M global effects). This probably
   * needs a little more thinking through - there are probably ways of abstracting it so we
   * can use a config file, have a CanopyEffect class, and don't have to create quite so many
   * global effects.
   */

  void configureCanopy() {
    System.out.println("Configuring canopy, lx is " + lx);

    interactiveCandyChaosEffect = setupMasterEffect(lx, InteractiveCandyChaosEffect.class);
    interactiveDesaturationEffect = setupMasterEffect(lx, InteractiveDesaturationEffect.class);
    interactiveRainbowEffect = setupMasterEffect(lx, InteractiveRainbowEffect.class);
    interactiveFireEffect = setupMasterEffect(lx, InteractiveFireEffect.class);
    interactiveHSVEffect = setupMasterEffect(lx, InteractiveHSVEffect.class);

    // must be after creation of the filter effect(s) used
    canopyController = new CanopyController(this);


    // tell the canopyController what it should be up to. The system alternates between running
    // and pausing, so the information being conveyed here is -
    //  - what state we're currently in (running)
    //  - when we next change state
    //  - the length of the pause and running intervals
    // This on/off thing was a requirement of the Parks Department in times of Covid, to
    // keep people from enjoying the sculpture too much. If pausePauseMinutes is 0, it
    // doesn't pause.
    ZonedDateTime firstPause = ZonedDateTime.now();
    firstPause.plusSeconds( (int) (Config.pauseRunMinutes * 60.0) );
    canopyController.modelUpdate(true , (int) (Config.pauseRunMinutes * 60.0f) ,
      (int) (Config.pausePauseMinutes * 60.0f) ,"run" ,firstPause);
  }

  /*
  void registerPatternController(String name, LXPattern pattern) {
    LXTransition t = new DissolveTransition(lx).setDuration(dissolveTime);
    pattern.setTransition(t);
    pattern.readableName = name;
    patterns.add(pattern);
  }
  // XXX - do I want this to come back? This is an interesting little bit of code.
  */


  /*
   * configureChannels
   *
   * The system relies on two separate sets of channels - a set of 8 channels
   * that is used by the APC40, and a separate set of 3 channels that is used by
   * the iPad app. These are the 'base channels' and the 'server channels, respectively.
   *
   * In addition, there is a single channel for triggerable effects,
   * the effectChannel.
   *
   * This code is called at startup to make sure that we have enough channels allocated. It will
   * also populate any new server channels with the default iPad patterns.
   */

  void configureChannels() {

    int numCurrentChannels = 0;
    for (LXAbstractChannel abstractChannel : lx.engine.mixer.channels) {
      if (abstractChannel instanceof LXChannel) {
        numCurrentChannels++;
      }
    }

    log("Configure channels - have " + numCurrentChannels + " channels, base channels is " + Config.NUM_BASE_CHANNELS + ", server channels is " + Config.NUM_SERVER_CHANNELS);

    // If there are not already NUM_BASE_CHANNELS channels,
    // create them.
    int numAddedChannels = 0;
    if (numCurrentChannels < Config.NUM_BASE_CHANNELS) {
      for (int i=0; i<Config.NUM_BASE_CHANNELS - numCurrentChannels; i++) {
        lx.engine.mixer.addChannel();
        numAddedChannels++;
      }
    }

    // Check if there are also and additional NUM_SERVER_CHANNELS. If not,
    // create them.
    numCurrentChannels = numCurrentChannels + numAddedChannels;
    numAddedChannels = 0;
    if (numCurrentChannels < Config.NUM_SERVER_CHANNELS + Config.NUM_BASE_CHANNELS) {
      for (int i=0; i<Config.NUM_SERVER_CHANNELS + Config.NUM_BASE_CHANNELS - numCurrentChannels; i++) {
        LXChannel channel = lx.engine.mixer.addChannel();
        channel.setPatterns(getIPadPatterns(lx));  // NB - This is intentionally. Only configure if not configured in project file.
        numAddedChannels++;
      }
    }

    // And if we need the effects channel, create it
    numCurrentChannels += numAddedChannels;
    if (numCurrentChannels < Config.NUM_BASE_CHANNELS + Config.NUM_SERVER_CHANNELS + 1) {
      lx.engine.mixer.addChannel();
      numCurrentChannels += 1;
    }

    // Now let's rename the channels for additional sanity
    int currentChannelIdx = 0;
    for (LXAbstractChannel abstractChannel : lx.engine.mixer.channels) {
      if (abstractChannel instanceof LXChannel) {
        if ((currentChannelIdx >= Config.NUM_BASE_CHANNELS) &&
            (currentChannelIdx < Config.NUM_BASE_CHANNELS + Config.NUM_SERVER_CHANNELS)) {
           abstractChannel.label.setValue("IPad");
           abstractChannel.label.setDescription("Patterns and channels used by iPad application");
           abstractChannel.fader.setValue(1);
        } else if (currentChannelIdx == Config.NUM_BASE_CHANNELS + Config.NUM_SERVER_CHANNELS) {
          abstractChannel.label.setValue("Effects");
          abstractChannel.label.setDescription("Channel handling patterns that produce triggerable effects. Set up by plugin, do not modify");
          effectsChannel = (LXChannel)abstractChannel;
        }
        currentChannelIdx++;
      }
    }

    // And let's make sure that the effects channel is in the right mode
    for (LXPattern pattern : effectsChannel.patterns) {
      pattern.enabled.setValue(false);
    }
    effectsChannel.compositeMode.setValue(LXChannel.CompositeMode.BLEND);
    effectsChannel.fader.setValue(1.0);
    effectsChannel.compositeDampingEnabled.setValue(true);
    effectsChannel.compositeDampingTimeSecs.setValue(1);
  }

  /*
   * getIPadPatterns
   *
   * Construct an array consisting of the default iPad patterns, as specified in the config file.
   * This array is used to fill out the patterns available on the Server channels, which are used
   * by the iPad app.
   */

  LXPattern[] getIPadPatterns(LX lx ) {
    ArrayList<LXPattern> iPadPatterns = new ArrayList<LXPattern>();
    for (Class<?> patternClass : Config.iPadPatterns) {
      try {
        iPadPatterns.add(lx.instantiatePattern(patternClass.asSubclass(LXPattern.class)));
      } catch (LX.InstantiationException ix) {
        log("Could not create class " + patternClass + " for iPad, ignoring");
      }
    }

    return iPadPatterns.toArray(new LXPattern[0]);
  }

  // XXX - Can I get a channel listener for these channels, and a global listener, so I can update
  // the iPad if these channels are changing under it? TODO


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


  /*
   * Some handy helper functions
   */

  @SuppressWarnings("unchecked")
  public static <T extends LXEffect> T setupMasterEffect(LX lx, Class<T> clazz) {
    LXEffect effect = findMasterEffect(lx, clazz);
    if (effect == null) {
      try {
        effect = (clazz.getConstructor(LX.class).newInstance(lx));
        lx.addEffect(effect);
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
        System.out.println("Constructor for class " + clazz + "failing, continuing on");
      }
    }
    return (T)effect;
  }

  // Utility function for checking for the existence of an effect with this particular *name*, as well
  // as class.
  //
  @SuppressWarnings("unchecked")
  public static <T extends LXEffect> T setupMasterEffectWithName(LX lx, Class<T> clazz, String name) {
    LXEffect effect = findMasterEffectWithName(lx, clazz, name);
    if (effect == null) {
      try {
        effect = (clazz.getConstructor(LX.class).newInstance(lx));
        effect.label.setValue(name);
        lx.addEffect(effect);
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
        System.out.println("Constructor for class " + clazz + "failing, continuing on");
      }
    }
    return (T)effect;
  }

  @SuppressWarnings("unchecked")
  public static <T extends LXPattern> T setupTriggerablePattern(LX lx, LXChannel channel, Class<T> clazz) {
    LXPattern pattern = findPattern(channel, clazz);
    if (pattern == null) {
      try {
        pattern = (clazz.getConstructor(LX.class).newInstance(lx));
        channel.addPattern(pattern);
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
        e.printStackTrace();
        return null;
      } // XXX would like to be able to specify constructors in many instances. Erk
    }

    pattern.enabled.setValue(false);
    return (T)pattern;
  }

  @SuppressWarnings("unchecked")
  public static <T extends LXEffect> T findMasterEffect(LX lx, Class<T> clazz) {
    return findEffect(lx.engine.mixer.masterBus, clazz);
  }

  @SuppressWarnings("unchecked")
  public static <T extends LXEffect> T findMasterEffectWithName(LX lx, Class<T> clazz, String name) {
    return findEffectWithName(lx.engine.mixer.masterBus, clazz, name);
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
  public static <T extends LXEffect> T findEffectWithName(LXBus bus, Class<T> clazz, String name) {
    for (LXEffect effect : bus.effects) {
      if (effect.getClass().equals(clazz) && effect.label.getString().equals(name)) {
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

  @SuppressWarnings("unchecked")
  public static <T extends LXPattern> T findPatternWithNameOnly(LXChannel channel, String name) {
    for (LXPattern pattern : channel.patterns) {
      if (pattern.label.getString().equals(name)) {
        return (T) pattern;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T extends LXPattern> T findPatternWithName(LXChannel channel, Class<T> clazz, String name) {
    for (LXPattern pattern : channel.patterns) {
      if (pattern.getClass().equals(clazz) && pattern.label.getString().equals(name)) {
        return (T) pattern;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T extends LXModulator> T findModulator(LX lx, Class<T> clazz) {
    for (LXModulator modulator : lx.engine.modulation.modulators) {
      if (modulator.getClass().equals(clazz)) {
        return (T) modulator;
      }
    }
    return null;
  }

  public static int getCubeCluster(int idx) {
    return idx % 12;
  }

  public static int getCubeLayer(int idx) {
    return idx/12;
  }

}
  
