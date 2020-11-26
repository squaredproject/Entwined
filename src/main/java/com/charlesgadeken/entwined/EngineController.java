package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.effects.TSEffectController;
import com.charlesgadeken.entwined.effects.original.ScrambleEffect;
import com.charlesgadeken.entwined.effects.original.SpeedEffect;
import com.charlesgadeken.entwined.effects.original.SpinEffect;
import heronarts.lx.LX;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.mixer.LXAbstractChannel;
import heronarts.lx.mixer.LXChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EngineController {
    LX lx;

    public int baseChannelIndex;
    public int numChannels;

    public int startEffectIndex;
    public int endEffectIndex;

    public boolean isAutoplaying;
    EntwinedAutomationRecorder automation;
    boolean[] previousChannelIsOn;

    public List<TSEffectController> effectControllers = new ArrayList<>();
    public int activeEffectControllerIndex = -1;

    public SpeedEffect speedEffect;
    public SpinEffect spinEffect;
    public BlurEffect blurEffect;
    public ScrambleEffect scrambleEffect;

    public EngineController(LX lx) {
        this.lx = lx;
    }

    public List<LXChannel> getChannels() {
        return lx.engine.mixer.getChannels()
                .subList(baseChannelIndex, baseChannelIndex + numChannels).stream()
                .filter(c -> c instanceof LXChannel)
                .map(c -> (LXChannel) c)
                .collect(Collectors.toList());
    }

    public void setChannelPattern(int channelIndex, int patternIndex) {
        if (patternIndex == -1) {
            patternIndex = 0;
        } else {
            patternIndex++;
        }

        LXAbstractChannel c = lx.engine.mixer.getChannel(channelIndex);
        if (c instanceof LXChannel) {
            ((LXChannel) c).goPatternIndex(patternIndex);
        } else {
            System.err.printf(
                    "WARNING: Ignoring attempt to set channel %d to pattern %d",
                    channelIndex, patternIndex);
        }
    }

    public void setChannelVisibility(int channelIndex, double visibility) {
        lx.engine.mixer.getChannel(channelIndex).fader.setValue(visibility);
    }

    public void setActiveColorEffect(int effectIndex) {
        if (activeEffectControllerIndex == effectIndex) {
            return;
        }
        if (activeEffectControllerIndex != -1) {
            TSEffectController effectController =
                    effectControllers.get(activeEffectControllerIndex);
            effectController.setEnabled(false);
        }
        activeEffectControllerIndex = effectIndex;
        if (activeEffectControllerIndex != -1) {
            TSEffectController effectController =
                    effectControllers.get(activeEffectControllerIndex);
            effectController.setEnabled(true);
        }
    }

    public void setSpeed(double amount) {
        speedEffect.speed.setValue(amount);
    }

    public void setSpin(double amount) {
        spinEffect.spin.setValue(amount);
    }

    public void setBlur(double amount) {
        blurEffect.level.setValue(amount);
    }

    public void setScramble(double amount) {
        scrambleEffect.amount.setValue(amount);
    }

    public void setAutoplay(boolean autoplay) {
        setAutoplay(autoplay, false);
    }

    void setAutoplay(boolean autoplay, boolean forceUpdate) {
        if (autoplay != isAutoplaying || forceUpdate) {
            isAutoplaying = autoplay;
            automation.setPaused(!autoplay);

            if (previousChannelIsOn == null) {
                previousChannelIsOn = new boolean[lx.engine.mixer.getChannels().size()];
                for (LXAbstractChannel channel : lx.engine.mixer.getChannels()) {
                    previousChannelIsOn[channel.getIndex()] = channel.enabled.isOn();
                }
            }

            for (LXAbstractChannel channel : lx.engine.mixer.getChannels()) {
                boolean toEnable;
                if (channel.getIndex() < baseChannelIndex) {
                    toEnable = autoplay;
                } else if (channel.getIndex() < baseChannelIndex + numChannels) {
                    toEnable = !autoplay;
                } else {
                    toEnable = autoplay;
                }

                if (toEnable) {
                    channel.enabled.setValue(previousChannelIsOn[channel.getIndex()]);
                } else {
                    previousChannelIsOn[channel.getIndex()] = channel.enabled.isOn();
                    channel.enabled.setValue(false);
                }
            }

            List<LXEffect> effects = lx.engine.mixer.masterBus.getEffects();
            for (int i = 0; i < effects.size(); i++) {
                LXEffect effect = effects.get(i);
                if (i < startEffectIndex) {
                    effect.enabled.setValue(autoplay);
                } else if (i < endEffectIndex) {
                    effect.enabled.setValue(!autoplay);
                }
            }
        }
    }
}
