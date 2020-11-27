package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.effects.TSEffectController;
import com.charlesgadeken.entwined.effects.original.ColorEffect;
import com.charlesgadeken.entwined.effects.original.ScrambleEffect;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import com.charlesgadeken.entwined.patterns.contributors.geoffSchmidt.Pixels;
import com.charlesgadeken.entwined.patterns.contributors.grantPatterson.Planes;
import com.charlesgadeken.entwined.patterns.contributors.grantPatterson.Pond;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.*;
import com.charlesgadeken.entwined.patterns.contributors.jackLampack.AcidTrip;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.*;
import com.charlesgadeken.entwined.patterns.contributors.markLottor.MarkLottor;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.*;
import com.charlesgadeken.entwined.patterns.original.SeeSaw;
import com.charlesgadeken.entwined.patterns.original.Twister;
import com.charlesgadeken.entwined.triggers.ParameterTriggerableAdapter;
import com.charlesgadeken.entwined.triggers.Triggerable;
import com.charlesgadeken.entwined.triggers.drumpad.TSDrumpad;
import com.charlesgadeken.entwined.triggers.http.AppServer;
import com.charlesgadeken.entwined.triggers.nfc.NFCEngine;
import heronarts.lx.LX;
import heronarts.lx.blend.DissolveBlend;
import heronarts.lx.blend.LXBlend;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.pattern.LXPattern;
import java.util.ArrayList;
import java.util.Arrays;

public class EntwinedTriggers {
    private final LX lx;
    private NFCEngine nfcEngine;
    final BooleanParameter[][] nfcToggles = new BooleanParameter[6][9];

    private TSDrumpad apc40Drumpad;

    ArrayList<Triggerable>[] apc40DrumpadTriggerablesLists;
    Triggerable[][] apc40DrumpadTriggerables;

    LXListenableNormalizedParameter[] effectKnobParameters;
    public final EngineController engineController;

    public EntwinedTriggers(LX lx) {
        this.lx = lx;
        this.engineController = new EngineController(lx);
    }

    void configureMIDI() {
        apc40Drumpad = new TSDrumpad();
        apc40Drumpad.triggerables = apc40DrumpadTriggerables;

        // TODO(meawoppl)
        // MIDI control
        // midiEngine = new MidiEngine(lx, effectKnobParameters, apc40Drumpad, drumpadVelocity,
        // previewChannels, bpmTool, uiDeck, nfcToggles, outputBrightness, automationSlot,
        // automation, automationStop);
    }

    /* configureNFC */

    void configureNFC() {
        nfcEngine = new NFCEngine(lx);
        nfcEngine.start();

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                nfcToggles[i][j] = new BooleanParameter("toggle");
            }
        }

        nfcEngine.registerReaderPatternTypeRestrictions(
                Arrays.asList(readerPatternTypeRestrictions()));
    }

    // TODO(meawoppl) This is static data...
    VisualType[] readerPatternTypeRestrictions() {
        return new VisualType[] {
            VisualType.Pattern,
            VisualType.Pattern,
            VisualType.Pattern,
            VisualType.OneShot,
            VisualType.OneShot,
            VisualType.OneShot,
            VisualType.Effect,
            VisualType.Effect,
            VisualType.Effect,
            VisualType.Pattern,
        };
    }

    void registerPatternTriggerables() {
        // The 2nd parameter is the NFC tag serial number
        // Specify a blank string to only add it to the apc40 drumpad
        // The 3rd parameter is which row of the apc40 drumpad to add it to.
        // defaults to the 3rd row
        // the row parameter is zero indexed

        registerPattern(new Twister(lx), "3707000050a8fb");
        registerPattern(new MarkLottor(lx), "3707000050a8d5");
        registerPattern(new Ripple(lx), "3707000050a908");
        registerPattern(new Stripes(lx), "3707000050a8ad");
        registerPattern(new Lattice(lx), "3707000050a8b9");
        registerPattern(new Fumes(lx), "3707000050a9b1");
        registerPattern(new Voronoi(lx), "3707000050a952");
        registerPattern(new CandyCloud(lx), "3707000050aab4");
        registerPattern(new GalaxyCloud(lx), "3707000050a91d");

        registerPattern(new ColorStrobe(lx), "3707000050a975", 3);
        registerPattern(new Explosions(lx, 20), "3707000050a8bf", 3);
        registerPattern(new Strobe(lx), "3707000050ab3a", 3);
        registerPattern(new SparkleTakeOver(lx), "3707000050ab68", 3);
        registerPattern(new MultiSine(lx), "3707000050ab38", 3);
        registerPattern(new SeeSaw(lx), "3707000050ab76", 3);
        registerPattern(new Cells(lx), "3707000050abca", 3);
        registerPattern(new Fade(lx), "3707000050a8b0", 3);
        registerPattern(new Pixels(lx), "3707000050ab38", 3);

        registerPattern(new IceCrystals(lx), "3707000050a89b", 5);
        registerPattern(new Fire(lx), "-", 5); // Make red

        // registerPattern(new DoubleHelix(lx), "");
        registerPattern(new AcidTrip(lx), "3707000050a914");
        registerPattern(new Rain(lx), "3707000050a937");

        registerPattern(
                new Wisps(lx, 1, 60, 50, 270, 20, 3.5, 10),
                "3707000050a905"); // downward yellow wisp
        registerPattern(
                new Wisps(lx, 30, 210, 100, 90, 20, 3.5, 10),
                "3707000050ab1a"); // colorful wisp storm
        registerPattern(
                new Wisps(lx, 1, 210, 100, 90, 130, 3.5, 10),
                "3707000050aba4"); // multidirection colorful wisps
        registerPattern(new Wisps(lx, 3, 210, 10, 270, 0, 3.5, 10), ""); // rain storm of wisps
        registerPattern(
                new Wisps(lx, 35, 210, 180, 180, 15, 2, 15), "3707000050a8ee"); // twister of wisps

        registerPattern(new Pond(lx), "");
        registerPattern(new Planes(lx), "");
    }

    void registerOneShotTriggerables() {
        registerOneShot(new Pulleys(lx), "3707000050a939");
        registerOneShot(new StrobeOneshot(lx), "3707000050abb0");
        registerOneShot(new BassSlam(lx), "3707000050a991");
        registerOneShot(new Fireflies(lx, 70, 6, 180), "3707000050ab2e");
        registerOneShot(new Fireflies(lx, 40, 7.5f, 90), "3707000050a92b");

        registerOneShot(new Fireflies(lx), "3707000050ab56", 5);
        registerOneShot(new Bubbles(lx), "3707000050a8ef", 5);
        registerOneShot(new Lightning(lx), "3707000050ab18", 5);
        registerOneShot(new Wisps(lx), "3707000050a9cd", 5);
        registerOneShot(new Explosions(lx), "3707000050ab6a", 5);
    }

    void registerOneShot(EntwinedBasePattern pattern, String nfcSerialNumber) {
        registerOneShot(pattern, nfcSerialNumber, 4);
    }

    void registerOneShot(EntwinedBasePattern pattern, String nfcSerialNumber, int apc40DrumpadRow) {
        registerVisual(pattern, nfcSerialNumber, apc40DrumpadRow, VisualType.OneShot);
    }

    void registerEffectTriggerables() {
        BlurEffect blurEffect = new TSBlurEffect(lx);
        ColorEffect colorEffect = new ColorEffect(lx);
        GhostEffect ghostEffect = new GhostEffect(lx);
        ScrambleEffect scrambleEffect = new ScrambleEffect(lx);
        StaticEffect staticEffect = new StaticEffect(lx);
        RotationEffect rotationEffect = new RotationEffect(lx);
        SpinEffect spinEffect = new SpinEffect(lx);
        SpeedEffect speedEffect = new SpeedEffect(lx);
        ColorStrobeTextureEffect colorStrobeTextureEffect = new ColorStrobeTextureEffect(lx);
        FadeTextureEffect fadeTextureEffect = new FadeTextureEffect(lx);
        AcidTripTextureEffect acidTripTextureEffect = new AcidTripTextureEffect(lx);
        CandyTextureEffect candyTextureEffect = new CandyTextureEffect(lx);
        CandyCloudTextureEffect candyCloudTextureEffect = new CandyCloudTextureEffect(lx);

        lx.addEffect(blurEffect);
        lx.addEffect(colorEffect);
        lx.addEffect(ghostEffect);
        lx.addEffect(scrambleEffect);
        lx.addEffect(staticEffect);
        lx.addEffect(rotationEffect);
        lx.addEffect(spinEffect);
        lx.addEffect(speedEffect);
        lx.addEffect(colorStrobeTextureEffect);
        lx.addEffect(fadeTextureEffect);
        lx.addEffect(acidTripTextureEffect);
        lx.addEffect(candyTextureEffect);
        lx.addEffect(candyCloudTextureEffect);

        registerEffectControlParameter(speedEffect.speed, "3707000050abae", 1, 0.4);
        registerEffectControlParameter(speedEffect.speed, "3707000050a916", 1, 5);
        registerEffectControlParameter(colorEffect.rainbow, "3707000050a98f");
        registerEffectControlParameter(colorEffect.mono, "3707000050aafe");
        registerEffectControlParameter(colorEffect.desaturation, "3707000050a969");
        registerEffectControlParameter(colorEffect.sharp, "3707000050aafc");
        registerEffectControlParameter(blurEffect.level, "3707000050a973", 0.65);
        registerEffectControlParameter(spinEffect.spin, "3707000050ab2c", 0.65);
        registerEffectControlParameter(ghostEffect.amount, "3707000050aaf2", 0, 0.16, 1);
        registerEffectControlParameter(scrambleEffect.amount, "3707000050a8cc", 0, 1, 1);
        registerEffectControlParameter(colorStrobeTextureEffect.amount, "3707000050a946", 0, 1, 1);
        registerEffectControlParameter(fadeTextureEffect.amount, "3707000050a967", 0, 1, 1);
        registerEffectControlParameter(acidTripTextureEffect.amount, "3707000050a953", 0, 1, 1);
        registerEffectControlParameter(candyCloudTextureEffect.amount, "3707000050a92d", 0, 1, 1);
        registerEffectControlParameter(staticEffect.amount, "3707000050a8b3", 0, .3, 1);
        registerEffectControlParameter(candyTextureEffect.amount, "3707000050aafc", 0, 1, 5);

        effectKnobParameters =
                new LXListenableNormalizedParameter[] {
                    colorEffect.hueShift,
                    colorEffect.mono,
                    colorEffect.desaturation,
                    colorEffect.sharp,
                    blurEffect.level,
                    speedEffect.speed,
                    spinEffect.spin,
                    candyCloudTextureEffect.amount
                };
    }

    public void registerPattern(EntwinedBasePattern pattern, String nfcSerialNumber) {
        registerPattern(pattern, nfcSerialNumber, 2);
    }

    public void registerPattern(
            EntwinedTriggerablePattern pattern, String nfcSerialNumber, int apc40DrumpadRow) {
        registerVisual(pattern, nfcSerialNumber, apc40DrumpadRow, VisualType.Pattern);
    }

    void registerVisual(
            EntwinedTriggerablePattern pattern,
            String nfcSerialNumber,
            int apc40DrumpadRow,
            VisualType visualType) {
        LXBlend t =
                new DissolveBlend(
                        lx); // NOTE(meawopp) same question below.  re `.setDuration(dissolveTime);`

        pattern.setTransition(t);

        Triggerable triggerable = configurePatternAsTriggerable(pattern);
        BooleanParameter toggle = null;
        if (apc40Drumpad != null) {
            toggle =
                    apc40DrumpadTriggerablesLists[apc40DrumpadRow].size() < 9
                            ? nfcToggles[apc40DrumpadRow][
                                    apc40DrumpadTriggerablesLists[apc40DrumpadRow].size()]
                            : null;
            apc40DrumpadTriggerablesLists[apc40DrumpadRow].add(triggerable);
        }
        if (nfcEngine != null) {
            nfcEngine.registerTriggerable(nfcSerialNumber, triggerable, visualType, toggle);
        }
    }

    Triggerable configurePatternAsTriggerable(EntwinedTriggerablePattern pattern) {
        LXChannel channel = lx.engine.mixer.addChannel(new EntwinedBasePattern[] {pattern});
        setupChannel(channel, false);

        pattern.onTriggerableModeEnabled();
        return pattern.getTriggerable();
    }

    void setupChannel(final LXChannel channel, boolean noOpWhenNotRunning) {
        // @Slee, honestly no idea what the intention is here...
        // It looks like paterns used to have associated "trastitions" have have moved to the
        // `blend` language, but I don't seen any analogue for that remaining....
        channel.setFaderTransition(
                new TreesTransition(lx, channel, model, channelTreeLevels, channelShrubLevels));
        channel.addListener(
                new LXChannel.AbstractListener() {
                    LXTransition transition;

                    @Override
                    public void patternWillChange(
                            LXChannel channel, LXPattern pattern, LXPattern nextPattern) {
                        if (!channel.enabled.isOn()) {
                            transition = nextPattern.getTransition();
                            nextPattern.setTransition(null);
                        }
                    }

                    @Override
                    public void patternDidChange(LXChannel channel, LXPattern pattern) {
                        if (transition != null) {
                            pattern.setTransition(transition);
                            transition = null;
                        }
                    }
                });

        if (noOpWhenNotRunning) {
            channel.enabled.setValue(channel.fader.getValue() != 0);
            channel.fader.addListener(
                    (parameter) -> channel.enabled.setValue(channel.fader.getValue() != 0));
        }
    }

    void registerPatternController(String name, EntwinedBasePattern pattern) {
        LXBlend t =
                new DissolveBlend(
                        lx); // @Slee is there a modern version of `.setDuration(dissolveTime);` on
        // blends?

        pattern.setTransition(t); // @Slee not sure where `.setTranstion` got to...
        pattern.readableName = name;
        patterns.add(pattern);
    }

    void registerEffect(LXEffect effect, String nfcSerialNumber) {
        if (effect instanceof Triggerable) {
            Triggerable triggerable = (Triggerable) effect;
            BooleanParameter toggle = null;
            if (apc40Drumpad != null) {
                toggle =
                        apc40DrumpadTriggerablesLists[0].size() < 9
                                ? nfcToggles[0][apc40DrumpadTriggerablesLists[0].size()]
                                : null;
                apc40DrumpadTriggerablesLists[0].add(triggerable);
            }
            if (nfcEngine != null) {
                nfcEngine.registerTriggerable(
                        nfcSerialNumber, triggerable, VisualType.Effect, toggle);
            }
        }
    }

    void registerEffectControlParameter(
            LXListenableNormalizedParameter parameter, String nfcSerialNumber) {
        registerEffectControlParameter(parameter, nfcSerialNumber, 0, 1, 0);
    }

    void registerEffectControlParameter(
            LXListenableNormalizedParameter parameter, String nfcSerialNumber, double onValue) {
        registerEffectControlParameter(parameter, nfcSerialNumber, 0, onValue, 0);
    }

    void registerEffectControlParameter(
            LXListenableNormalizedParameter parameter,
            String nfcSerialNumber,
            double offValue,
            double onValue) {
        registerEffectControlParameter(parameter, nfcSerialNumber, offValue, onValue, 0);
    }

    void registerEffectControlParameter(
            LXListenableNormalizedParameter parameter,
            String nfcSerialNumber,
            double offValue,
            double onValue,
            int row) {
        ParameterTriggerableAdapter triggerable =
                new ParameterTriggerableAdapter(lx, parameter, offValue, onValue);
        BooleanParameter toggle = null;
        if (apc40Drumpad != null) {
            toggle =
                    apc40DrumpadTriggerablesLists[row].size() < 9
                            ? nfcToggles[row][apc40DrumpadTriggerablesLists[row].size()]
                            : null;
            apc40DrumpadTriggerablesLists[row].add(triggerable);
        }
        if (nfcEngine != null) {
            nfcEngine.registerTriggerable(nfcSerialNumber, triggerable, VisualType.Effect, toggle);
        }
    }

    void registerEffectController(
            String name, LXEffect effect, LXListenableNormalizedParameter parameter) {
        ParameterTriggerableAdapter triggerable = new ParameterTriggerableAdapter(lx, parameter);
        TSEffectController effectController = new TSEffectController(name, effect, triggerable);

        engineController.effectControllers.add(effectController);
    }

    void configureServer() {
        new AppServer(lx, engineController).start();
    }
}
