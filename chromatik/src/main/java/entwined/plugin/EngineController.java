package entwined.plugin;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import entwined.pattern.kyle_fleming.BrightnessScaleEffect;
import entwined.pattern.kyle_fleming.ScrambleEffect;
import entwined.pattern.kyle_fleming.SpeedEffect;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.mixer.LXAbstractChannel;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class EngineController {
    LX lx;

    int baseChannelIndex; // the starting channel that the engine controls - ie, 8
    int numChannels;      // the number of channels controlled by this controller is 3

    int startEffectIndex; // these are the limits of the IPAD EFFECTS
    int endEffectIndex;

    boolean isAutoplaying;
    TSAutomationRecorder automation;
    boolean[] previousChannelIsOn;

    ArrayList<TSEffectController> effectControllers = new ArrayList<TSEffectController>();
    int activeEffectControllerIndex = -1;

    SpeedEffect speedEffect;
    // SpinEffect spinEffect;
    BlurEffect blurEffect;
    ScrambleEffect scrambleEffect;
    BrightnessScaleEffect masterBrightnessEffect;
    BrightnessScaleEffect autoplayBrightnessEffect;
    BoundedParameterProxy outputBrightness;
    AutoPauseTask autoPauseTask;

    double masterBrightnessStash = 1.0;

    EngineController(LX lx) {
      this.lx = lx;

      System.out.println("creating auto pause task");
      this.autoPauseTask = new AutoPauseTask();
      lx.engine.addLoopTask(this.autoPauseTask);
    }

    // this gets the 'iPad channels' only
    List<LXAbstractChannel> getChannels() {
      return lx.engine.mixer.getChannels().subList(baseChannelIndex, baseChannelIndex + numChannels);
    }

    // The indexes here are real indexes, because when we gave the channel, we gave the actual index
    void setChannelPattern(int channelIndex, int patternIndex) {
      if (patternIndex == -1) {
        patternIndex = 0;
      } else {
        patternIndex++;
      }
      lx.engine.mixer.getChannel(channelIndex).goIndex(patternIndex);
    }

    void setChannelVisibility(int channelIndex, double visibility) {
      // have to be sure
      LXAbstractChannel channel = lx.engine.mixer.getChannel(channelIndex);
      //channel.enabled.setValue(true);
      channel.fader.setValue(visibility);
    }

    void setActiveColorEffect(int effectIndex) {
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
      speedEffect.speed.setValue(amount);
    }

    /*
    void setSpin(double amount) {
      spinEffect.spin.setValue(amount);
    }
    */

    void setBlur(double amount) {
      blurEffect.setAmount(amount);
    }

    void setScramble(double amount) {
      scrambleEffect.setAmount(amount);
    }

    // this controls the OUTPUT brightness for controlling the amount
    // of power consumed, it will NOT effect what you see in the model
    // on processing
    void setMasterBrightness(double amount) {
      masterBrightnessEffect.setAmount(amount);
    }

    double getMasterBrightness() {
      double ret = masterBrightnessEffect.getValue();
      return( ret );
    }

    // This brightness effect only takes effect when auto-play
    // is running. It allows separate control in times and spaces
    // when we want to have a persistent way of controlling brightness
    void setAutoplayBrightness(double amount) {
      autoplayBrightnessEffect.setAmount(amount);
    }

    double getAutoplayBrightness() {
      double ret = masterBrightnessEffect.getValue();
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

    // If true, this enables the base channels and starts the auto-play.
    // if false, this disables the base channels and enables the IPad channels
    //    and keeps the prior channel state and resets to that

    void setAutoplay(boolean autoplay, boolean forceUpdate) {
      if (autoplay != isAutoplaying || forceUpdate) {
        isAutoplaying = autoplay;
        automation.setPaused(!autoplay);

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

        for (LXAbstractChannel channel : lx.engine.mixer.getChannels()) {
          boolean toEnable;
          if (channel.getIndex() < baseChannelIndex) {
            toEnable = autoplay; // base channels
          } else if (channel.getIndex() < baseChannelIndex + numChannels) {
            toEnable = !autoplay; // server channels
          } else {
            toEnable = autoplay; // others
          }

          if (toEnable) {
            channel.enabled.setValue(previousChannelIsOn[channel.getIndex()]);
            //System.out.println(" setAutoplay: toEnable true: channel "+channel.getIndex()+" setting to "+previousChannelIsOn[channel.getIndex()]);
          } else {
            previousChannelIsOn[channel.getIndex()] = channel.enabled.isOn();
            channel.enabled.setValue(false);
            //System.out.println(" setAutoplay: toEnable false: channel "+channel.getIndex()+" setting to false");
          }
        }

        for (int i = 0; i < lx.engine.getEffects().size(); i++) {
          LXEffect effect = lx.engine.getEffects().get(i);
          if (i < startEffectIndex) {
            effect.enabled.setValue(autoplay);
          } else if (i < endEffectIndex) {
            effect.enabled.setValue(!autoplay);
          }
        }

        // this effect is enabled or disabled if autoplay
        autoplayBrightnessEffect.enabled.setValue(autoplay);
        // this is a bit of a hack. If we're coming out of "controller" and
        //

      }
    }

    /* force pauses whenever autoplay is playing (only then) */
    /* moved this into EngineController because it seems more right, it controls things
       and also we need to bang it from the network, which has an Engine Controller but
       no easy link to Engine */
    class AutoPauseTask implements LXLoopTask {

      long startTime = System.currentTimeMillis() / 1000;
      boolean lightsOn = true;

      boolean fadeing = false;
      long    fadeStart;
      Long    fadeEnd;
      boolean fadeIn = false; // or its is a fade out

      // can't use lightson / lightsoff because that takes into account whether we are autoplay
      boolean pauseStateRunning() {

        // if not configured, running
        if (Config.pauseRunMinutes == 0.0 || Config.pausePauseMinutes == 0.0) return(true);

        double timeRemaining;
        long now = ( System.currentTimeMillis() / 1000);
        long totalPeriod = (long) ((Config.pauseRunMinutes + Config.pausePauseMinutes) * 60.0);
        long secsIntoPeriod = (now - startTime) % totalPeriod;

        // paused
        if ((Config.pauseRunMinutes * 60.0) <= secsIntoPeriod) {
          //log("pauseStateRunning: false");
          return(false);
        }
        //log("pauseStateRunning: true");
        return(true);
      }

      // number of seconds left in current state
      // does NOT include fade
      // does NOT account for whether we are in auto-play
      double pauseTimeRemaining() {

        if (Config.pauseRunMinutes == 0.0 || Config.pausePauseMinutes == 0.0) return(0.0);
        final double pauseRunSeconds = Config.pauseRunMinutes * 60.0;
        final double pausePauseSeconds = Config.pausePauseMinutes * 60.0;

        double timeRemaining;
        long now = ( System.currentTimeMillis() / 1000);
        long totalPeriod = (long) (pauseRunSeconds + pausePauseSeconds);
        long secsIntoPeriod = (now - startTime) % totalPeriod;

        // we're in paused
        if (pauseRunSeconds <= secsIntoPeriod) {
          timeRemaining = pausePauseSeconds  - (secsIntoPeriod - pauseRunSeconds);
        }
        // we're in running
        else {
          timeRemaining = pauseRunSeconds - secsIntoPeriod;
        }

        //log("pauseTimeRemaining: "+timeRemaining);

        return(timeRemaining);
      }

      // reset to beginning of running - next loop around will do the right thing
      void pauseResetRunning() {
        log("pause: Reset to Running: ");
        startTime = System.currentTimeMillis() / 1000;
      }

      // reset to beginning of pause (which is in the past), this is a little counter intuitive but the start of Pause is Run in the past
      void pauseResetPaused() {
        log("pause: reset to Paused: ");
        startTime = ( System.currentTimeMillis() / 1000 ) - (long)Math.floor(Config.pauseRunMinutes * 60.0);
      }

      // these nows are in milliseconds
      void startFadeIn() {
        fadeStart = System.currentTimeMillis();
        fadeEnd = fadeStart + (long)( Config.pauseFadeInSeconds * 1000 );
        fadeIn = true;
        fadeing = true;
        //log(" start Fade In ");
        // no point in trying to set not, hasn't changed enough
      }

      // these nows are in milliseconds
      void startFadeOut() {
        fadeStart = System.currentTimeMillis();
        fadeEnd = fadeStart + (long)( Config.pauseFadeOutSeconds * 1000 );
        fadeIn = false;
        fadeing = true;
        //log(" start fade out ");
        // no point in trying to set not, hasn't changed enough
      }

      void setFadeValue() {
        long now = System.currentTimeMillis();
        if (now > fadeEnd) {
          fadeing = false;
          outputBrightness.setValue(fadeIn ? 1.0f : 0.0f);
          lightsOn = fadeIn ? true : false;
          //log(" fade over ");
          return;
        }
        double value = ((double)(now - fadeStart)) / (double)((fadeEnd - fadeStart));
        // log(" fade value: fadeStart "+fadeStart+" fadeEnd "+fadeEnd+" now "+now);
        if (fadeIn == false) { value = 1.0f - value; }
        outputBrightness.setValue(value);
        //log(" fadeing: "+(fadeIn?"in ":"out ")+" value: "+value);
      }

      @Override
      public void loop(double deltaMs) {

        // if not configured just quit (allows for on-the-fly-config-change)
        if (Config.pauseRunMinutes == 0.0 || Config.pausePauseMinutes == 0.0) {
          return;
        }

        // no matter what, if we start fading, finish it
        if (fadeing) {
          setFadeValue();
          return;
        }

        // if we are not autoplaying, the ipad has us, and we trust the ipad
        if (! isAutoplaying ) {
          if (lightsOn == false) {
            log( " PauseTask: not autoplaying, lightson unconditionally " );
            startFadeIn();
          }
          return;
        }

         // move these to seconds for better scale
        long now = ( System.currentTimeMillis() / 1000);

        // check if I should be on or off
        boolean shouldLightsOn = true;
        long totalPeriod = (long) ((Config.pauseRunMinutes + Config.pausePauseMinutes) * 60.0);
        long secsIntoPeriod = (now - startTime) % totalPeriod;
        if ((Config.pauseRunMinutes * 60.0) <= secsIntoPeriod) shouldLightsOn = false;

        //log( " PauseTask: totalPeriod "+totalPeriod+" timeIntoPeriod "+secsIntoPeriod+" should: "+shouldLightsOn );
        //log( " PauseTask: now  "+now+" startTime "+startTime );

        if (shouldLightsOn && lightsOn == false) {
          log( " PauseTask: lightson: for "+Config.pauseRunMinutes+" minutes" );
            startFadeIn();
        }
        else if (shouldLightsOn == false && lightsOn) {
          log(" PauseTask: lightsoff: for "+Config.pausePauseMinutes+" minutes" );
            startFadeOut();
        }
      }
    }

     // Log Helper
    ZoneId localZone = ZoneId.of("America/Los_Angeles");
    void log(String s) {
        System.out.println(
          ZonedDateTime.now( localZone ).format( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) + " " + s );
    }

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
