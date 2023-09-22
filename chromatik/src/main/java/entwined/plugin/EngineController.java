package entwined.plugin;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import entwined.core.TSTriggerablePattern;
import entwined.modulator.Recordings;
import entwined.pattern.anon.ColorEffect;
import entwined.pattern.kyle_fleming.BrightnessScaleEffect;
import entwined.pattern.kyle_fleming.CandyCloudTextureEffect;
import entwined.pattern.kyle_fleming.CandyTextureEffect;
import entwined.pattern.kyle_fleming.ColorStrobeTextureEffect;
import entwined.pattern.kyle_fleming.FadeTextureEffect;
import entwined.pattern.kyle_fleming.ScrambleEffect;
import entwined.pattern.kyle_fleming.StaticEffect;
import entwined.pattern.kyle_fleming.SpeedEffect;
import heronarts.lx.LX;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.mixer.LXAbstractChannel;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.pattern.LXPattern;

/*
 * Provides interface and controls for external controllers (iPad app, NFC controller)
 */

public class EngineController {
  LX lx;

  int baseChannelIndex; // the starting channel that the engine controls - ie, 8
  int numServerChannels;      // the number of channels controlled by this controller is 3

  boolean isAutoplaying;
  boolean[] previousChannelIsOn;
  boolean[] previousEffectIsOn;
  boolean wasRunningAutomation = false;

  ArrayList<TSEffectController> effectControllers = new ArrayList<TSEffectController>();
  int activeEffectControllerIndex = -1;

  SpeedEffect speedEffect;
  // SpinEffect spinEffect;
  BlurEffect blurEffect;
  StaticEffect staticEffect;
  ScrambleEffect scrambleEffect;
  BrightnessScaleEffect masterBrightnessEffect;
  BrightnessScaleEffect autoplayBrightnessEffect;
  BoundedParameterProxy outputBrightness;
  Entwined engine;

  double masterBrightnessStash = 1.0;

  EngineController(LX lx, Entwined engine) {
    this.lx = lx;
    this.engine = engine;

    baseChannelIndex = Config.NUM_BASE_CHANNELS;
    numServerChannels = Config.NUM_SERVER_CHANNELS;

    registerIPadEffects();

    outputBrightness = new BoundedParameterProxy(1);
    autoplayBrightnessEffect.setAmount(Config.autoplayBrightness);
  }

  void shutdown() {
  }

  <T extends TSTriggerablePattern> T setupPatternEffect(Class<T> clazz) {
    TSTriggerablePattern pattern = (TSTriggerablePattern)Entwined.setupTriggerablePattern(lx, engine.effectsChannel, clazz);
    if (pattern != null) {
      pattern.enableTriggerMode();
    } else {
      System.out.println("Could not create pattern for class " + clazz);
    }
    return (T) pattern;
  }

  void addPatternEffect(TSTriggerablePattern pattern) {
    engine.effectsChannel.addPattern(pattern);
  }
  /*
   * getChannels()
   * Gets the 'iPad channels' only
   */
  List<LXAbstractChannel> getChannels() {
    System.out.println("ENTWINED: Base channel index is " + baseChannelIndex + ", num server channels is " + numServerChannels);
    return lx.engine.mixer.getChannels().subList(baseChannelIndex, baseChannelIndex + numServerChannels);
  }

  /*
   * setChannelPattern()
   * Set pattern on a channel. The index for the channel is the real index,
   * not the index of the patterns that we are supposed to be controlling.
   */
  void setChannelPattern(int channelIndex, int patternIndex) {
    if (isAutoplaying) {
      System.out.println("ENTWINED: Attempting to remotely change channel during autoplay, aborting");
      return;
    }
    if (patternIndex == -1) {
      patternIndex = 0;
    } else {
      patternIndex++;
    }
    LXAbstractChannel abstractChannel = lx.engine.mixer.getChannel(channelIndex);
    if (abstractChannel instanceof LXChannel) {
      LXChannel channel = (LXChannel)abstractChannel;
      channel.goPatternIndex(patternIndex);
    }
  }

  void setChannelPattern(int channelIndex, String patternName) {
    if (isAutoplaying) {
      System.out.println("ENTWINED: Attempting to remotely change channel during autoplay, aborting");
      return;
    }
    System.out.println("Set channel pattern - idx " + channelIndex + " name " + patternName);
    LXAbstractChannel abstractChannel = lx.engine.mixer.getChannel(channelIndex);
    if (abstractChannel instanceof LXChannel) {
      LXChannel channel = (LXChannel)abstractChannel;
      LXPattern pattern = channel.getPattern(patternName); 
      if (pattern != null) {
        channel.goPattern(pattern);
      } else {
        System.out.println("could not find pattern for " + patternName);
      }
    }
  }

  /*
   * registerIPadEffects()
   *
   * Make sure that the effects associated with the iPad are available, and registered as invokable
   * by the iPad.
   * XXX - Like iPad patterns, these should be specified in the config file.
   */

  private void registerIPadEffects() {

    // Set up iPad registered effects.
    // Note that the names of these effects start with 'iPad'. This is how I'm differentiating
    // between iPad and non-iPad effects when we toggle between iPad mode and Autoplay mode
    ColorEffect colorEffect =
      Entwined.setupMasterEffectWithName(lx, ColorEffect.class, "iPad - Color");
    ColorStrobeTextureEffect colorStrobeTextureEffect =
      Entwined.setupMasterEffectWithName(lx, ColorStrobeTextureEffect.class, "iPad - ColorStrobe");
    FadeTextureEffect fadeTextureEffect =
      Entwined.setupMasterEffectWithName(lx, FadeTextureEffect.class, "iPad - Fade");
    CandyTextureEffect candyTextureEffect =
      Entwined.setupMasterEffectWithName(lx, CandyTextureEffect.class, "iPad - Candy");
    CandyCloudTextureEffect candyCloudTextureEffect =
      Entwined.setupMasterEffectWithName(lx, CandyCloudTextureEffect.class, "iPad - Cloud");
    // NB not hooking up RotationEffect, SpinEffect, or GhostEffect.  -- CSW

    // General global effects at the end - they (ideally) operate after the other effects.
    // XXX - how to guarantee this? The only way currently is to futz with the project file, and
    // manually put them in the correct order.
    // Essentially, I've got a race condition going on that I cannot win - I need general effects, then ipad effects,
    // and then global effects.
    speedEffect = Entwined.setupMasterEffect(lx, SpeedEffect.class);
    blurEffect = Entwined.setupMasterEffect(lx, BlurEffect.class);  // XXX - replace with standard blur effect?
    scrambleEffect = Entwined.setupMasterEffect(lx, ScrambleEffect.class);
    staticEffect = Entwined.setupMasterEffect(lx, StaticEffect.class);
    masterBrightnessEffect = Entwined.setupMasterEffectWithName(lx, BrightnessScaleEffect.class, Entwined.masterBrightnessName);
    autoplayBrightnessEffect = Entwined.setupMasterEffectWithName(lx, BrightnessScaleEffect.class, Entwined.autoplayBrightnessName);

    registerEffectController("Rainbow", candyCloudTextureEffect, candyCloudTextureEffect.amount);
    registerEffectController("Candy Chaos", candyTextureEffect, candyTextureEffect.amount);
    registerEffectController("Color Strobe", colorStrobeTextureEffect, colorStrobeTextureEffect.amount);
    registerEffectController("Fade", fadeTextureEffect, fadeTextureEffect.amount);
    registerEffectController("Monochrome", colorEffect, colorEffect.mono);
    registerEffectController("White", colorEffect, colorEffect.desaturation);
  }


  private void registerEffectController(String name, LXEffect effect, LXListenableNormalizedParameter parameter) {
    ParameterTriggerableAdapter triggerable = new ParameterTriggerableAdapter(lx, parameter);
    TSEffectController effectController = new TSEffectController(name, effect, triggerable);

    effectControllers.add(effectController);
  }

  /*******************************************************
   *
   * The following methods are called by the IPadServer when it receives a command
   * from the iPad
   *
   *******************************************************/

  void setChannelVisibility(int channelIndex, double visibility) {
    // have to be sure
    LXAbstractChannel channel = lx.engine.mixer.getChannel(channelIndex);
    channel.fader.setValue(visibility);
    if (visibility > 0) {
      channel.enabled.setValue(true);
    }
  }

  void setActiveColorEffect(int effectIndex) {
    if (isAutoplaying) {
      return;
    }

    if (activeEffectControllerIndex == effectIndex) {
      return;
    }
    if (activeEffectControllerIndex != -1) {
      TSEffectController effectController = effectControllers.get(activeEffectControllerIndex);
      effectController.setEnabled(false);
    }
    activeEffectControllerIndex = effectIndex;
    if (activeEffectControllerIndex != -1) {
      TSEffectController effectController = effectControllers.get(activeEffectControllerIndex);
      effectController.setEnabled(true);
    }
  }

  void setSpeed(double amount) {
    if (!isAutoplaying) {
      speedEffect.speed.setValue(amount);
      if (amount > 0) {
        speedEffect.enable();
      } else {
        speedEffect.disable();
      }
    }
  }

  /*
  void setSpin(double amount) {
    if (!isAutoplaying) {
      spinEffect.spin.setValue(amount);
    }
  }
  */

  void setBlur(double amount) {
    if (!isAutoplaying) {
      blurEffect.level.setValue(amount);
      if (amount > 0) { 
        blurEffect.enable();
      } else {
        blurEffect.disable();
      }
    }
  }

  void setScramble(double amount) {
    if (!isAutoplaying) {
      scrambleEffect.setAmount(amount);
      if (amount > 0) {
        scrambleEffect.enable();
      } else {
        scrambleEffect.disable();
      }
    }
  }

  void setStatic(double amount) {
    if (!isAutoplaying) {
      staticEffect.setAmount(amount);
      if (amount > 0) {
        staticEffect.enable();
      } else {
        staticEffect.disable();
      }
    }
  }

  // this controls the OUTPUT brightness for controlling the amount
  // of power consumed, it will NOT effect what you see in the model
  // on processing
  // XXX - I don't think that's true any more. --CSW
  void setMasterBrightness(double amount) {
    masterBrightnessEffect.setAmount(amount);
  }

  double getMasterBrightness() {
    double ret = masterBrightnessEffect.getAmount();
    return( ret );
  }

  // This brightness effect only takes effect when auto-play
  // is running. It allows separate control in times and spaces
  // when we want to have a persistent way of controlling brightness
  void setAutoplayBrightness(double amount) {
    autoplayBrightnessEffect.setAmount(amount);
  }

  double getAutoplayBrightness() {
    double ret = masterBrightnessEffect.getAmount();
    return( ret );
  }

  void setHue(double amount) {
    System.out.println("Set Master Hue: "+amount+" not implemented yet");
  }

  double getHue() {
    System.out.println("Get Master Hue: stub");
    return(0.0f);
  }

  void setAutoplay(boolean autoplay) {
    setAutoplay(autoplay, false);
  }

  /***************************************************************
   *
   * setAutoPlay
   * @param autoplay
   * @param forceUpdate
   *
   * Switches control between autoplay (using base channels) and
   * iPad (using server channels).
   *
   * Stashes previous base channel state (and brightness) when
   * switching to Ipad mode, restores when going back to autoplay
   *
   * Forceupdate forces the state change logic even if it appears
   * that autoplay is already in the desired state
   *
   * XXX - I have no idea how this works with Mark's stuff
   *
   ***************************************************************/
  // If true, this enables the base channels and starts the auto-play.
  // if false, this disables the base channels and enables the IPad channels
  //    and keeps the prior channel state and resets to that
  void setAutoplay(boolean autoplay, boolean forceUpdate) {
    if (autoplay != isAutoplaying || forceUpdate) {
      isAutoplaying = autoplay;
      Recordings recordings = Entwined.findModulator(lx, Recordings.class);
      if (!autoplay && recordings != null) {
        wasRunningAutomation = recordings.isRunning();
        recordings.stop();
      }

      // if we are disabling auto-play, stash the last brightness
      if (autoplay) {
        setMasterBrightness(masterBrightnessStash);
      } else {
        masterBrightnessStash = getMasterBrightness();
      }

      // I think this should only effect base channels? bb
      if (previousChannelIsOn == null) {
        previousChannelIsOn = new boolean[lx.engine.mixer.getChannels().size()];
        for (LXAbstractChannel channel : lx.engine.mixer.getChannels()) {
          previousChannelIsOn[channel.getIndex()] = channel.enabled.isOn();
        }
      }

      if (previousEffectIsOn == null ) {
        previousEffectIsOn = new boolean[lx.engine.mixer.masterBus.effects.size()];
        int effectIdx = 0;
        for (LXEffect effect : lx.engine.mixer.masterBus.effects) {
          previousEffectIsOn[effectIdx] = effect.isEnabled();
          effectIdx++;
        }
      }

      // The ipad effects do include blur and color and speed and spin and scramble... it's
      // everything but the master controllers. So previously I'd set the enable flag on those effects to off.
      // XXX - deal better with effects that the ipad can use. And this weird ipad only thing FIXME
      for (LXAbstractChannel channel : lx.engine.mixer.getChannels()) {
        boolean toEnable;
        int channelIdx = channel.getIndex();
        /*
        if (channelIdx < baseChannelIndex) {
          toEnable = autoplay; // base channels
        } else if (channelIdx < baseChannelIndex + numServerChannels) {
          toEnable = !autoplay; // server channels
        } else {
          toEnable = autoplay; // others // XXX - be very careful about disablng the Effects channel, which the ipad depends on FIXME
        }
        */

        if (channelIdx < previousChannelIsOn.length) {  // Safety XXX - one could have a listener for channel changes.
          if (channelIdx >= baseChannelIndex && channelIdx < baseChannelIndex + numServerChannels) { // we're an ipad channel
            channel.enabled.setValue(!autoplay);
          } else { // non ipad channel
            if (autoplay) {  // turning off ipad
              channel.enabled.setValue(previousChannelIsOn[channelIdx]);
              //System.out.println(" setAutoplay: toEnable true: channel "+channel.getIndex()+" setting to "+previousChannelIsOn[channel.getIndex()]);
            } else { // turning on ipad
              previousChannelIsOn[channelIdx] = channel.enabled.isOn();
              channel.enabled.setValue(false);
              //System.out.println(" setAutoplay: toEnable false: channel "+channel.getIndex()+" setting to false");
            }
          }
        }
      }

      /* Toggle between global effects that are registered to the iPad, and global effects that aren't.
       * A couple of things here - like channels, we want to save the previous values.
       * And we want to make sure not to turn off the interactive effects */
      for (int i = 0; i < lx.engine.mixer.masterBus.effects.size(); i++) {
        LXEffect effect = lx.engine.mixer.masterBus.effects.get(i);
        if (effect.getLabel().startsWith("iPad")) {
          effect.enabled.setValue(!autoplay);
        } else {
          if (i < previousEffectIsOn.length) {
            if (autoplay) {
              effect.enabled.setValue(previousEffectIsOn[i]);
            } else {
              if (!effect.getLabel().startsWith("Interactive")) {  // XXX FIXME. We should register these and then ask rather than depending on their names!!!
                effect.enabled.setValue(false);
              }
            }
          }
        }
      }

      // this effect is enabled or disabled if autoplay
      autoplayBrightnessEffect.enabled.setValue(autoplay);
      if (recordings != null && autoplay && wasRunningAutomation) {
        recordings.start();
      }
    }
  }


   // Log Helper
  ZoneId localZone = ZoneId.of("America/Los_Angeles");
  void log(String s) {
        System.out.println(
          ZonedDateTime.now( localZone ).format( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) + " " + s );
    }

  /*
  class TreesTransition extends LXTransition {

    private final LXChannel channel;
    private final LXModel model;
    public final DiscreteParameter blendMode = new DiscreteParameter("MODE", 4);
    private LXColor.Blend blendType = LXColor.Blend.ADD;
    final ChannelTreeLevels[] channelTreeLevels;
    final ChannelShrubLevels[] channelShrubLevels;
    final ChannelFairyCircleLevels[] channelFairyCircleLevels;
    final ChannelSpotLevels[] channelSpotLevels;
    final BoundedParameter fade = new BoundedParameter("FADE", 1);

    TreesTransition(LX lx, LXChannel channel, LXModel model, ChannelTreeLevels[] channelTreeLevels, ChannelShrubLevels[] channelShrubLevels, ChannelFairyCircleLevels[] channelFairyCircleLevels, ChannelSpotLevels[] channelSpotLevels) {
      super(lx);
      this.model = model;
      addParameter(blendMode);
      this.channel = channel;
      this.channelTreeLevels = channelTreeLevels;
      this.channelShrubLevels = channelShrubLevels;
      this.channelFairyCircleLevels = channelFairyCircleLevels;
      this.channelSpotLevels = channelSpotLevels;

      blendMode.addListener(new LXParameterListener() {
        @Override
        public void onParameterChanged(LXParameter parameter) {
          switch (blendMode.getValuei()) {
            case 0:
              blendType = LXColor.Blend.ADD;
              break;
            case 1:
              blendType = LXColor.Blend.MULTIPLY;
              break;
            case 2:
              blendType = LXColor.Blend.LIGHTEST;
              break;
            case 3:
              blendType = LXColor.Blend.SUBTRACT;
              break;
          }
        }
      });
    }

    // I think this modifies the outputs of trees and shrubs specifically, using the tree and shrub
    // sliders. Currently, we don't have shrub sliders, and there's no way to set a level on a tree
    // that would effect triggerables

    // it appears the functionlity is to blend c1 and c2 into the colors output, mediated
    // by the channelTreeLevels.
    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {

      // these levels only exist on channels that show up in the screen, because they're
      // tied to the screen. Bypass if it's a channel without this slider
      //if (this.channel.getIndex() >= this.channelTreeLevels.length) {
      //  System.out.println(" computeBlend: channel index too high "+this.channel.getIndex() );
      //  return;
      //}
      int treeIndex = 0;
      double treeLevel;
      for (Tree tree : model.trees) {
        float amount = 1.0f; // default value if there is no extra level
        if (this.channel.getIndex() < this.channelTreeLevels.length) {
          treeLevel = this.channelTreeLevels[this.channel.getIndex()].getValue(treeIndex);
          amount = (float) (progress * treeLevel);
        }
        if (amount == 0.0f) {
          for (LXPoint p : tree.points) {
            colors[p.index] = c1[p.index];
          }
        } else if (amount == 1.0f) {
          for (LXPoint p : tree.points) {
            int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
            colors[p.index] = LXColor.blend(c1[p.index], color2, this.blendType);
          }
        } else {
          for (LXPoint p : tree.points) {
            int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
            colors[p.index] = LXColor.lerp(c1[p.index], LXColor.blend(c1[p.index], color2, this.blendType), amount);
          }
        }
        treeIndex++;
      }

      int shrubIndex = 0;
      double shrubLevel;
      for (Shrub shrub : model.shrubs) {
        float amount = 1.0f; // default value if there is no extra level
        if (this.channel.getIndex() < this.channelShrubLevels.length) {
          shrubLevel = this.channelShrubLevels[this.channel.getIndex()].getValue(shrubIndex);
          amount = (float) (progress * shrubLevel);
        }
        if (amount == 0.0f) {
          for (LXPoint p : shrub.points) {
            colors[p.index] = c1[p.index];
          }
        } else if (amount == 1.0f) {
          for (LXPoint p : shrub.points) {
            int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
            colors[p.index] = LXColor.blend(c1[p.index], color2, this.blendType);
          }
        } else {
          for (LXPoint p : shrub.points) {
            int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
            colors[p.index] = LXColor.lerp(c1[p.index], LXColor.blend(c1[p.index], color2, this.blendType), amount);
          }
        }
        shrubIndex++;
      }

      int fcIndex = 0;
      double fcLevel;
      for (FairyCircle fairyCircle : model.fairyCircles) {
        float amount = 1.0f; // default value if there is no extra level
        if (this.channel.getIndex() < this.channelFairyCircleLevels.length) {
          fcLevel = this.channelFairyCircleLevels[this.channel.getIndex()].getValue(fcIndex);
          amount = (float) (progress * fcLevel);
        }
        if (amount == 0.0f) {
          for (LXPoint p : fairyCircle.points) {
            colors[p.index] = c1[p.index];
          }
        } else if (amount == 1.0f) {
          for (LXPoint p : fairyCircle.points) {
            int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
            colors[p.index] = LXColor.blend(c1[p.index], color2, this.blendType);
          }
        } else {
          for (LXPoint p : fairyCircle.points) {
            int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
            colors[p.index] = LXColor.lerp(c1[p.index], LXColor.blend(c1[p.index], color2, this.blendType), amount);
          }
        }
        fcIndex++;
      }

      int spotIndex = 0;
      double spotLevel;
      for (Spot spot : model.spots) {
        float amount = 1.0f; // default value if there is no extra level
        if (this.channel.getIndex() < this.channelSpotLevels.length) {
          spotLevel = this.channelSpotLevels[this.channel.getIndex()].getValue(spotIndex);
          amount = (float) (progress * spotLevel);
        }
        if (amount == 0.0f) {
          for (LXPoint p : spot.points) {
            colors[p.index] = c1[p.index];
          }
        } else if (amount == 1.0f) {
          for (LXPoint p : spot.points) {
            int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
            colors[p.index] = LXColor.blend(c1[p.index], color2, this.blendType);
          }
        } else {
          for (LXPoint p : spot.points) {
            int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
            colors[p.index] = LXColor.lerp(c1[p.index], LXColor.blend(c1[p.index], color2, this.blendType), amount);
          }
        }
        spotIndex++;
      }
    }
  }
  */
}

class BoundedParameterProxy extends BoundedParameter {

  final List<BoundedParameter> parameters = new ArrayList<BoundedParameter>();

  BoundedParameterProxy(double value) {
    super("Proxy", value);
  }

  @Override
    protected double updateValue(double value) {
    for (BoundedParameter parameter : parameters) {
      parameter.setValue(value);
    }
    return value;
  }
}
