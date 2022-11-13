package entwined.plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import heronarts.lx.LX;
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
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.LXStudio.UI;

import entwined.core.CubeManager;
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
import entwined.pattern.kyle_fleming.TSBlurEffect;

public class Entwined implements LXStudio.Plugin {


  LX lx;

  IPadServerController engineController;
  CanopyController canopyController;
  Triggerables triggerables;
  InteractiveHSVEffect interactiveHSVEffect;
  InteractiveFireEffect interactiveFireEffect;
  InteractiveCandyChaosEffect interactiveCandyChaosEffect;
  InteractiveRainbowEffect interactiveRainbowEffect;
  InteractiveDesaturationEffect interactiveDesaturationEffect;

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
  private void configureServer() {
    new AppServer(lx, engineController).start();
  }


  /* Instantiate standard effects and add them to the master output if they
   * aren't already there.
   */
  private void setupEffects() {

    // Add master effects
    /*
    TSBlurEffect blur                               = setupMasterEffect(lx, TSBlurEffect.class);
    ColorEffect colorEfect                          = setupMasterEffect(lx, ColorEffect.class);
    HueFilterEffect hueFilterEffect                 = setupMasterEffect(lx, HueFilterEffect.class);
    GhostEffect ghostEffect                         = setupMasterEffect(lx, GhostEffect.class);
    ScrambleEffect scrambleEffect                   = setupMasterEffect(lx, ScrambleEffect.class);
    StaticEffect staticEffect                       = setupMasterEffect(lx, StaticEffect.class);
    SpeedEffect speedEffect                         = setupMasterEffect(lx, SpeedEffect.class);
    ColorStrobeTextureEffect strobeTextureEffect    = setupMasterEffect(lx, ColorStrobeTextureEffect.class);
    FadeTextureEffect fadeTextureEffect             = setupMasterEffect(lx, FadeTextureEffect.class);
    AcidTripTextureEffect acidTripTextureEffect     = setupMasterEffect(lx, AcidTripTextureEffect.class);
    CandyTextureEffect candyTextureEffect           = setupMasterEffect(lx, CandyTextureEffect.class);
    CandyCloudTextureEffect candyCloudTextureEffect = setupMasterEffect(lx, CandyCloudTextureEffect.class);
    */

    setupMasterEffect(lx, TSBlurEffect.class);
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

    // We assume at this point that the effects channel for pattern effects has been created.
    if (effectsChannel == null) {
      // XXX complain vociferously
    }

    // XXX - and these two. How do I add them and make they aren't already there?
    // Ditto on the canopy effects. Or wait - I'm adding the canopy effects
    // damn. I keep forgetting about ipad channels vs interactive effects. Too much to remember.

    BrightnessScaleEffect masterBrightnessEffect = new BrightnessScaleEffect(lx);
    BrightnessScaleEffect autoplayBrightnessEffect = new BrightnessScaleEffect(lx);

    lx.addEffect(masterBrightnessEffect);
    lx.addEffect(autoplayBrightnessEffect);   // XXX dear god why are there two? XXX

    /* configureCanopyEffects(); */
  }

  private void configureTriggeredEffects() {
    setupEffects();
    configureTriggerables();
  }


  @Override
  public void initialize(LX lx) {
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
        public void noteOnReceived(MidiNoteOn note) {
          noteReceived(note, true);
        }

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

    CubeManager.init(lx);

    lx.addProjectListener(new LX.ProjectListener() {
      @Override
      public void projectChanged(File file, Change change) {
        if (change == Change.NEW || change == Change.OPEN) {
          log("Entwined.projectChanged(" + change + ")");
          // NOTE(mcslee): a new project file has been opened! may need to
          // initialize or re-initialize things that depend upon the project
          // state here
          // Additional stuff for tracking cube properties

          // Set up the channels
          configureChannels();

          // Set up the low level iPad Controller
          engineController = new IPadServerController(lx);  // XXX might want to have a listener on the controller, rather than newing up the engine controller here

          // Set up high level iPad Server. Uses the iPadController to actually do the work.
          configureServer(); // turns on the TCP listener

          // Set up Canopy listener (also TCP) for interactive commands
          configureCanopy();

          // Grab the triggerables object if it exists
          triggerables = findModulator(lx, Triggerables.class);

          // Set up triggerable events
          if (triggerables != null) {
            configureTriggeredEffects();
          }

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
          }

          // bad code I know
          // (shouldn't mess with engine internals)
          // maybe need a way to specify a deck shouldn't be focused?
          // essentially this lets us have extra decks for the drumpad
          // patterns without letting them be assigned to channels
          // -kf
          // // This basically prevents the UI from changing (or accessing, really) the
          // channels in the high range - the iPad channels and the effect channels.
          //lx.engine.mixer.focusedChannel.setRange(Config.NUM_BASE_CHANNELS);
        }
      }
    });

  }

  // Configure the array of triggerables.
  void configureTriggerables()
  {
    // There are several types of triggerables -
    // Events (which derive from LXEvent)
    // Standard patterns
    // One-shot patterns
    // Patterns that use a parameter in addition to the fader for fading in and out


    // TODO: can hardcode whatever sorts of configuration you want here to wire up
    // what does what on all these triggerables...
    // XXX - there's this whole issue of 'amounts' on our triggers. In the original code
    // we specified a parameter that was used to catch the on/off event. For the moment,
    // I'm ignoring this.

    // straight up on/off on the effect. XXX want to be able to set for different values
    triggerables.setAction(0,0, new EventTrigger(Entwined.findMasterEffect(lx, ColorEffect.class), 0, 1));
    triggerables.setAction(0,1, new EventTrigger(Entwined.findMasterEffect(lx, ColorEffect.class), 0, 0.5f));

    // straight up pattern effect
    // XXX - I probably need to turn off the pattern by default if I'm using the effects channel with its
    // compositing. This *should* have happened, but...
    // XXX - He's got a function called instantiate class that I can use maybe. Ctor issue.
    triggerables.setAction(0,2, Entwined.findPattern(effectsChannel, Cells.class));
    // One shot or something
    Bubbles bubbles = Entwined.findPattern(effectsChannel, Bubbles.class);
    triggerables.setAction(0,3, bubbles);

    // triggerables.setAction(0, 3, ParameterTriggerableAdapter());
    //triggerables.setAction
    // XXX - it's better to hard code these in the plugin, with triggerables.setAction()
    // And then I can set up these parameterized things. Yay.

  }

  //This is not really a good way of doing this. ipad effects are different?
  class EventTrigger implements Triggerable{
    LXEffect effect;
    Boolean isTriggered = false;
    EventTrigger(LXEffect effect){
      this.effect = effect;
    }
    EventTrigger(LXEffect effect, float onAmount, float offAmount){
      this.effect = effect;
      isTriggered = true;
    }
    @Override
    public void onTriggered() {
      effect.enabled.setValue(true);
    }
    @Override
    public void onReleased() {
      isTriggered = false;
      effect.enabled.setValue(false);
    }
    @Override
    public void onTimeout() {
      isTriggered = false;
      effect.enabled.setValue(false);
    }

    @Override
    public boolean isTriggered() {
      return isTriggered;
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
    // XXX FIXME THIS SHOULDNT BE HERE XXX
    ZonedDateTime firstPause = ZonedDateTime.now();
    firstPause.plusSeconds( (int) (Config.pauseRunMinutes * 60.0) );
    canopyController.modelUpdate(true /*interactive*/, (int) (Config.pauseRunMinutes * 60.0f) /*runSeconds*/,
      (int) (Config.pausePauseMinutes * 60.0f) /*pauseSeconds*/,"run" /*state*/,firstPause);
  }

  // NOTE! Entwined can be installed without any trees, or with
  // trees not at 0.0. Several patterns make assumptions about the
  // location of the "main tree", those have been removed until
  // fixed - ShrubRiver, SpiralArms


  /*
  void registerPatternController(String name, LXPattern pattern) {
    LXTransition t = new DissolveTransition(lx).setDuration(dissolveTime);
    pattern.setTransition(t);
    pattern.readableName = name;
    patterns.add(pattern);
  }
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
      effectsChannel.compositeMode.setValue(LXChannel.CompositeMode.BLEND);
    }
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

  @SuppressWarnings("unchecked")
  public static <T extends LXPattern> T setupTriggerablePattern(LXChannel channel, Class<T> clazz) {
    LXPattern pattern = findPattern(channel, clazz);
    if (pattern == null) {
      try {
        pattern = (clazz.newInstance());
        channel.addPattern(pattern);
      } catch (InstantiationException | IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } // XXX would like to be able to specify constructors in many instances. Erk
    }
    return (T)pattern;
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

  @SuppressWarnings("unchecked")
  public static <T extends LXModulator> T findModulator(LX lx, Class<T> clazz) {
    for (LXModulator modulator : lx.engine.modulation.modulators) {
      if (modulator.getClass().equals(clazz)) {
        return (T) modulator;
      }
    }
    return null;
  }

}
