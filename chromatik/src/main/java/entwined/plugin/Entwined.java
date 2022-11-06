package entwined.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.surface.APC40;
import heronarts.lx.mixer.LXBus;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.LXStudio.UI;

import entwined.core.CubeManager;
import entwined.pattern.anon.ColorEffect;
import entwined.pattern.interactive.InteractiveCandyChaosEffect;
import entwined.pattern.interactive.InteractiveDesaturationEffect;
import entwined.pattern.interactive.InteractiveFireEffect;
import entwined.pattern.interactive.InteractiveHSVEffect;
import entwined.pattern.interactive.InteractiveRainbowEffect;
import entwined.pattern.kyle_fleming.BrightnessScaleEffect;
import entwined.pattern.kyle_fleming.CandyCloudTextureEffect;
import entwined.pattern.kyle_fleming.CandyTextureEffect;
import entwined.pattern.kyle_fleming.ColorStrobeTextureEffect;
import entwined.pattern.kyle_fleming.FadeTextureEffect;
import entwined.pattern.kyle_fleming.ScrambleEffect;
import entwined.pattern.kyle_fleming.SpeedEffect;
import entwined.pattern.kyle_fleming.TSBlurEffect2;

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
    // NB - all available patterns - including those available on the iPad - should be registered here
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
    lx.registry.addEffect(entwined.pattern.interactive.InteractiveCandyChaosEffect.InteractiveCandyChaos.class);
    lx.registry.addEffect(entwined.pattern.interactive.InteractiveDesaturationEffect.InteractiveDesaturation.class);
    lx.registry.addEffect(entwined.pattern.interactive.InteractiveFireEffect.InteractiveFire.class);
    lx.registry.addEffect(entwined.pattern.interactive.InteractiveRainbowEffect.InteractiveRainbow.class);

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
    // 'Content' here is our custom patterns and effects.
    if (lx.flags.classpathPlugins.contains("entwined.plugin.Entwined")) {
      registerEntwinedContent(lx);
    }

    this.lx = lx;

    // This sets up interface for the APC 40
    log("Set up Triggerables");
    lx.engine.registerComponent("entwined-triggers", getTriggerables());

    // Set up some master parameters... XXX - not sure if this is really used.
    // XXX - it *was* also used to do autoplayback and (I think) recording, which is
    // a crucial feature.
    engineController = new EngineController(lx);
    engineController.masterBrightnessEffect = new BrightnessScaleEffect(lx);
    engineController.autoplayBrightnessEffect = new BrightnessScaleEffect(lx);
    engineController.outputBrightness = new BoundedParameterProxy(1);
    engineController.autoplayBrightnessEffect.setAmount(Config.autoplayBrightness);

    // Set up to initialize midi commands from the APC40, when the midi system
    // is ready for it.
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
          configureChannels();
          // Additional stuff for tracking cube properties
          CubeManager.init(lx);  // XXX - don't already have a change listener on the cube manager? Has an on model change handler

        }
      }
    });


    // Set up TCP listener for iPad commands. Uses the engineController to actually do the work.
    configureServer(); // turns on the TCP listener

    // Set up Canopy listener (also TCP) for interactive commands
    configureCanopy();  // XXX - do I need to do this after project changed has happened? I really don't care about project changed, I just want to do it once at startup


    // bad code I know
    // (shouldn't mess with engine internals)
    // maybe need a way to specify a deck shouldn't be focused?
    // essentially this lets us have extra decks for the drumpad
    // patterns without letting them be assigned to channels
    // -kf
    // lx.engine.mixer.focusedChannel.setRange(Engine.NUM_BASE_CHANNELS);
    lx.engine.mixer.focusedChannel.setRange(8);  // XXX CSW - What is NUM_BASE_CHANNELS, and where is it now?
  }

  void configureCanopy() {
    // Canopy - interactive effects from web (or potentially rPi)
    // this special filter is used by Canopy -- the interactive effects
    interactiveHSVEffect = new InteractiveHSVEffect(lx);
    lx.addEffect(interactiveHSVEffect);
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
  }

  // NOTE! Entwined can be installed without any trees, or with
  // trees not at 0.0. Several patterns make assumptions about the
  // location of the "main tree", those have been removed until
  // fixed - ShrubRiver, SpiralArms


  // ArrayList<LXPattern> serverChannelPatterns = new ArrayList<LXPattern>();
  String [] serverChannelPatterns;


  void registerServerPatterns() {
    // Get list of iPad patterns from Config file
    serverChannelPatterns = Config.iPadPatterns;
  }

  /*
  void registerIPadPatterns() {
    registerPatternController("None", new NoPattern(lx));
    registerPatternController("Twister", new Twister(lx));
    registerPatternController("TwisterGlobal", new TwisterGlobal(lx));
    registerPatternController("Candy Cloud", new CandyCloud(lx));
    registerPatternController("BeachBall", new BeachBall(lx));
    registerPatternController("Breath", new Breath(lx));

    registerPatternController("Lottor", new MarkLottor(lx));
    registerPatternController("Ripple", new Ripple(lx));
    registerPatternController("Stripes", new Stripes(lx));
    registerPatternController("Lattice", new Lattice(lx));
    registerPatternController("Leaves", new ColoredLeaves(lx));

    registerPatternController("Voronoi", new Voronoi(lx));
    registerPatternController("Galaxy Cloud", new GalaxyCloud(lx));
    registerPatternController("Parallax", new Parallax(lx));
    registerPatternController("Burst", new Burst(lx));

    registerPatternController("Peppermint", new Peppermint(lx));

    registerPatternController("Ice Crystals", new IceCrystals(lx));
    registerPatternController("Fire", new Fire(lx));
    registerPatternController("Acid Trip", new AcidTrip(lx));
    registerPatternController("Rain", new Rain(lx));

    registerPatternController("Pond", new Pond(lx));
    registerPatternController("Planes", new Planes(lx));
    registerPatternController("Growth", new Growth(lx));

    registerPatternController("Lightning", new Lightning(lx));
    registerPatternController("Sparkle Takeover", new SparkleTakeOver(lx));
    registerPatternController("SparkleHelix", new SparkleHelix(lx));

    registerPatternController("Multi-Sine", new MultiSine(lx));
    registerPatternController("Seesaw", new SeeSaw(lx));
    registerPatternController("Cells", new Cells(lx));
    registerPatternController("Fade", new Fade(lx));
    registerPatternController("Springs", new Springs(lx));

    // registerPatternController("Bass Slam", new BassSlam(lx));  // XXX add when working

    registerPatternController("Fireflies", new Fireflies(lx));
    registerPatternController("Bubbles", new Bubbles(lx));

    registerPatternController("Wisps", new Wisps(lx));
    registerPatternController("Fireworks", new Explosions(lx));

    registerPatternController("ColorWave", new ColorWave(lx));
    registerPatternController("Wedges", new Wedges(lx));

    // Lindsay
    registerPatternController("SparkleWave", new SparkleWave(lx));

    // Mattaniah
    registerPatternController("OscillatingDarkRing", new OscillatingDarkRing(lx));
    registerPatternController("RadialGradiant", new RadialGradiant(lx));

    // Quinn Keck
    registerPatternController("ButterflyEffect", new ButterflyEffect(lx));

    // BB
    registerPatternController("MultiColor", new MultiColor(lx));
    registerPatternController("MultiColor2", new MultiColor2(lx));
    registerPatternController("StripeStatic", new StripeStatic(lx));

    // Misko's patterns
    registerPatternController("Circles", new Circles(lx));
    registerPatternController("LineScan", new LineScan(lx));
    registerPatternController("WaveScan", new WaveScan(lx));
    registerPatternController("Stringy", new Stringy(lx));
    registerPatternController("RainbowWaveScan", new RainbowWaveScan(lx));
    // registerPatternController("SyncSpinner", new SyncSpinner(lx));  // XXX add when working
    registerPatternController("LightHouse", new LightHouse(lx));
    //registerPatternController("ShrubRiver", new ShrubRiver(lx));
    registerPatternController("ColorBlast", new ColorBlast(lx));
    registerPatternController("Vertigo", new Vertigo(lx));

    // Adam Croston and Katie Ballinger's patterns.
    registerPatternController("ExpandingCircles", new ExpandingCircles(lx));
    //registerPatternController("SpiralArms", new SpiralArms(lx));
    registerPatternController("Sparks", new Sparks(lx));
    registerPatternController("Blooms", new Blooms(lx));
    registerPatternController("MovingPoint", new MovingPoint(lx));
    //registerPatternController("WavesToMainTree", new WavesToMainTree(lx));
    registerPatternController("Undulation", new Undulation(lx));
    registerPatternController("HueRibbons", new HueRibbons(lx));
    registerPatternController("VerticalColorWaves", new VerticalColorWaves(lx));
    registerPatternController("FlockingPoints", new FlockingPoints(lx));

    // Evy's patterns
    registerPatternController("CircleBreath", new CircleBreath(lx));
    registerPatternController("FirefliesNcase", new FirefliesNcase(lx));

    // Sydney
    registerPatternController("RoseGarden", new RoseGarden(lx));

    //Lorenz
    registerPatternController("Fountain", new Fountain(lx));


    registerPatternController("Fumes", new Fumes(lx));
    registerPatternController("Color Strobe", new ColorStrobe(lx));
    registerPatternController("Strobe", new Strobe(lx));

  }
  */

  // XXX - is Strobe in main list? I may have forgotten it.
  /*
  void registerPatternController(String name, LXPattern pattern) {
    LXTransition t = new DissolveTransition(lx).setDuration(dissolveTime);
    pattern.setTransition(t);
    pattern.readableName = name;
    patterns.add(pattern);
  }
  */
  /*
  void registerServerChannelPattern(String className) {
    // Attempt to create a new lx pattern from the class
    // XXX - need to see if the pattern is already registered, and not bother if it is.
    // XXX - heh. I don't seem to have to create an instance, just register the class.
    try {
      Class<?> c = Class.forName(className);
      Constructor<?> cons = c.getConstructor(LX.class);
      Object pattern = cons.newInstance(lx);
      serverChannelPatterns.add((LXPattern)pattern);
    } catch (Exception e) {
      System.out.println("Could not instantiate pattern class " + className + ", continuing"); // XXX - want to make this a standard log
    }
  }
  */

  void configureChannels() {
    // Check if there are already NUM_BASE_CHANNELS channels. If there are not,
    // create them.
    int currentNumChannels = lx.engine.mixer.channels.size();
    System.out.println("Configure channels - have " + currentNumChannels + " channels, base channels is " + Config.NUM_BASE_CHANNELS);
    if (currentNumChannels < Config.NUM_BASE_CHANNELS) {
      System.out.println("Adding base channels");
      for (int i=0; i<Config.NUM_BASE_CHANNELS - currentNumChannels; i++) {
        lx.engine.mixer.addChannel();
      }
    }

    // Check if there are also and additional NUM_SERVER_CHANNELS. If not,
    // create them.
    currentNumChannels = lx.engine.mixer.channels.size();

    System.out.println("Configure channels - have " + currentNumChannels + " channels, server channels is " + Config.NUM_SERVER_CHANNELS);

    if (lx.engine.mixer.channels.size() < Config.NUM_SERVER_CHANNELS + Config.NUM_BASE_CHANNELS) {
      for (int i=0; i<Config.NUM_SERVER_CHANNELS + Config.NUM_BASE_CHANNELS - currentNumChannels; i++) {
        System.out.println("Adding server channel");
        LXChannel channel = lx.engine.mixer.addChannel();
        ArrayList <LXPattern> patternArray = getIPadPatterns();
        LXPattern patterns [] = new LXPattern[patternArray.size()];
        channel.setPatterns(patternArray.toArray(patterns));
      }
    }

    engineController.baseChannelIndex = Config.NUM_BASE_CHANNELS - 1;

    // For all the server channels, set the ipad patterns, and set the dissolve

    // XXX - ask Mark... I want to set a dissolve fade on the iPad channels for when
    // I swap them. How to do this?

    // Set the pattern name on the server channels to SERVER as a note to people not to fuck with them.
    // XXX -todo
  }

  ArrayList<LXPattern> getIPadPatterns( ) {
    ArrayList<LXPattern> iPadPatterns = new ArrayList<LXPattern>();
    for (String patternClassName : Config.iPadPatterns) {
      System.out.println("Attempting to create object of class " + patternClassName);
      for (Class<? extends LXPattern> clazz : lx.registry.patterns) {
        if (clazz.getCanonicalName() == patternClassName) {  // assuming Java does string compare in the expected way
          try {
            Class<?> c = Class.forName(patternClassName);
            System.out.println("Have matching class in registry, about to get constructor");
            Constructor<?> cons = c.getConstructor(LX.class);
            System.out.println("Have constructor");
            LXPattern pattern = (LXPattern)cons.newInstance(lx);
            System.out.println("Created new instance of " + patternClassName);
            iPadPatterns.add(pattern);
          } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            log("Could not create class " + patternClassName + " for ipod, ignoring");
          }
        }
      }
    }
    return iPadPatterns;
  }


  // XXX - One question I have here is whether we are registering effects twice with lx, and if so, why
  // We appear to be registering twice with the patterns. I do not know why we would do this.
  void registerIPadEffects() {
    ColorEffect colorEffect = new ColorEffect(lx);
    ColorStrobeTextureEffect colorStrobeTextureEffect = new ColorStrobeTextureEffect(lx);
    FadeTextureEffect fadeTextureEffect = new FadeTextureEffect(lx);
    // AcidTripTextureEffect acidTripTextureEffect = new AcidTripTextureEffect(lx);
    CandyTextureEffect candyTextureEffect = new CandyTextureEffect(lx);
    CandyCloudTextureEffect candyCloudTextureEffect = new CandyCloudTextureEffect(lx);
    // GhostEffect ghostEffect = new GhostEffect(lx);
    // RotationEffect rotationEffect = new RotationEffect(lx);

    SpeedEffect speedEffect = engineController.speedEffect = new SpeedEffect(lx);
    // SpinEffect spinEffect = engineController.spinEffect = new SpinEffect(lx);
    BlurEffect blurEffect = engineController.blurEffect = new TSBlurEffect2(lx);
    ScrambleEffect scrambleEffect = engineController.scrambleEffect = new ScrambleEffect(lx);
    // StaticEffect staticEffect = engineController.staticEffect = new StaticEffect(lx);

    lx.addEffect(blurEffect);
    lx.addEffect(colorEffect);
    // lx.addEffect(staticEffect);
    // lx.addEffect(spinEffect);
    lx.addEffect(speedEffect);
    lx.addEffect(colorStrobeTextureEffect);
    lx.addEffect(fadeTextureEffect);
    // lx.addEffect(acidTripTextureEffect);
    lx.addEffect(candyTextureEffect);
    lx.addEffect(candyCloudTextureEffect);
    // lx.addEffect(ghostEffect);
    lx.addEffect(scrambleEffect);
    // lx.addEffect(rotationEffect);
    registerEffectController("Rainbow", candyCloudTextureEffect, candyCloudTextureEffect.amount);
    registerEffectController("Candy Chaos", candyTextureEffect, candyTextureEffect.amount);
    registerEffectController("Color Strobe", colorStrobeTextureEffect, colorStrobeTextureEffect.amount);
    registerEffectController("Fade", fadeTextureEffect, fadeTextureEffect.amount);
    registerEffectController("Monochrome", colorEffect, colorEffect.mono);
    registerEffectController("White", colorEffect, colorEffect.desaturation);
  }

  void registerEffectController(String name, LXEffect effect, LXListenableNormalizedParameter parameter) {
    ParameterTriggerableAdapter triggerable = new ParameterTriggerableAdapter(lx, parameter);
    TSEffectController effectController = new TSEffectController(name, effect, triggerable);

    engineController.effectControllers.add(effectController);
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
