package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.bpm.BPMTool;
import com.charlesgadeken.entwined.config.ConfigLoader;
import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.effects.TSEffectController;
import com.charlesgadeken.entwined.effects.original.ColorEffect;
import com.charlesgadeken.entwined.effects.original.ScrambleEffect;
import com.charlesgadeken.entwined.effects.original.SpeedEffect;
import com.charlesgadeken.entwined.effects.original.SpinEffect;
import com.charlesgadeken.entwined.model.Model;
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
import com.charlesgadeken.entwined.triggers.drumpad.EntwinedDrumpad;
import com.charlesgadeken.entwined.triggers.drumpad.MidiEngine;
import com.charlesgadeken.entwined.triggers.http.AppServer;
import heronarts.lx.LX;
import heronarts.lx.blend.LXBlend;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.parameter.*;
import heronarts.lx.pattern.LXPattern;

public class EntwinedTriggers {
    private final LX lx;

    BPMTool bpmTool;
    private EntwinedDrumpad apc40Drumpad;
    private EntwinedDrumpad.Builder drumpadBuilder;
    MidiEngine midiEngine;
    LXListenableNormalizedParameter[] effectKnobParameters;
    public final EngineController engineController;

    private final EntwinedParameters parameters;
    private final Model model;

    public EntwinedTriggers(
            LX lx, Model model, EngineController engineController, EntwinedParameters parameters) {
        this.lx = lx;
        this.model = model;
        this.engineController = engineController;
        this.parameters = parameters;
        this.drumpadBuilder = new EntwinedDrumpad.Builder();
    }

    @SuppressWarnings("unchecked")
    void configureTriggerables() {
        registerPatternTriggerables();
        registerOneShotTriggerables();
        registerEffectTriggerables();

        if (ConfigLoader.enableIPad) {
            // NOTE(meawoppl) slightly hacky way to get offsets/indices into a parent array
            engineController.startEffectIndex = lx.engine.mixer.masterBus.getEffects().size();
            registerIPadEffects();
            engineController.endEffectIndex = lx.engine.mixer.masterBus.getEffects().size();
        }
    }

    void registerIPadEffects() {
        ColorEffect colorEffect = new ColorEffect(lx);
        ColorStrobeTextureEffect colorStrobeTextureEffect = new ColorStrobeTextureEffect(lx);
        FadeTextureEffect fadeTextureEffect = new FadeTextureEffect(lx);
        AcidTripTextureEffect acidTripTextureEffect = new AcidTripTextureEffect(lx);
        CandyTextureEffect candyTextureEffect = new CandyTextureEffect(lx);
        CandyCloudTextureEffect candyCloudTextureEffect = new CandyCloudTextureEffect(lx);
        // GhostEffect ghostEffect = new GhostEffect(lx);
        // RotationEffect rotationEffect = new RotationEffect(lx);

        SpeedEffect speedEffect = engineController.speedEffect;
        SpinEffect spinEffect = engineController.spinEffect;
        BlurEffect blurEffect = engineController.blurEffect;
        ScrambleEffect scrambleEffect = engineController.scrambleEffect;
        // StaticEffect staticEffect = engineController.staticEffect = new StaticEffect(lx);

        lx.addEffect(blurEffect);
        lx.addEffect(colorEffect);
        // lx.addEffect(staticEffect);
        lx.addEffect(spinEffect);
        lx.addEffect(speedEffect);
        lx.addEffect(colorStrobeTextureEffect);
        lx.addEffect(fadeTextureEffect);
        // lx.addEffect(acidTripTextureEffect);
        lx.addEffect(candyTextureEffect);
        lx.addEffect(candyCloudTextureEffect);
        // lx.addEffect(ghostEffect);
        lx.addEffect(scrambleEffect);
        // lx.addEffect(rotationEffect);

        registerEffectController(
                "Rainbow", candyCloudTextureEffect, candyCloudTextureEffect.amount);
        registerEffectController("Candy Chaos", candyTextureEffect, candyTextureEffect.amount);
        registerEffectController(
                "Color Strobe", colorStrobeTextureEffect, colorStrobeTextureEffect.amount);
        registerEffectController("Fade", fadeTextureEffect, fadeTextureEffect.amount);
        registerEffectController("Monochrome", colorEffect, colorEffect.mono);
        registerEffectController("White", colorEffect, colorEffect.desaturation);
    }

    void configureMIDI() {
        apc40Drumpad = drumpadBuilder.build();
        bpmTool = new BPMTool(lx, effectKnobParameters);
        midiEngine = new MidiEngine(lx, parameters, apc40Drumpad, bpmTool, null);
    }

    void registerPatternTriggerables() {
        // The 2nd parameter is the NFC tag serial number
        // Specify a blank string to only add it to the apc40 drumpad
        // The 3rd parameter is which row of the apc40 drumpad to add it to.
        // defaults to the 3rd row
        // the row parameter is zero indexed

        registerPattern(new Twister(lx));
        registerPattern(new MarkLottor(lx));
        registerPattern(new Ripple(lx));
        registerPattern(new Stripes(lx));
        registerPattern(new Lattice(lx));
        registerPattern(new Fumes(lx));
        registerPattern(new Voronoi(lx));
        registerPattern(new CandyCloud(lx));
        registerPattern(new GalaxyCloud(lx));

        registerPattern(new ColorStrobe(lx), 3);
        registerPattern(new Explosions(lx, 20), 3);
        registerPattern(new Strobe(lx), 3);
        registerPattern(new SparkleTakeOver(lx), 3);
        registerPattern(new MultiSine(lx), 3);
        registerPattern(new SeeSaw(lx), 3);
        registerPattern(new Cells(lx), 3);
        registerPattern(new Fade(lx), 3);
        registerPattern(new Pixels(lx), 3);

        registerPattern(new IceCrystals(lx), 5);
        registerPattern(new Fire(lx), 5); // Make red

        // registerPattern(new DoubleHelix(lx), "");
        registerPattern(new AcidTrip(lx));
        registerPattern(new Rain(lx));

        registerPattern(new Wisps(lx, 1, 60, 50, 270, 20, 3.5, 10)); // downward yellow wisp
        registerPattern(new Wisps(lx, 30, 210, 100, 90, 20, 3.5, 10)); // colorful wisp storm
        registerPattern(
                new Wisps(lx, 1, 210, 100, 90, 130, 3.5, 10)); // multidirection colorful wisps
        registerPattern(new Wisps(lx, 3, 210, 10, 270, 0, 3.5, 10)); // rain storm of wisps
        registerPattern(new Wisps(lx, 35, 210, 180, 180, 15, 2, 15)); // twister of wisps

        registerPattern(new Pond(lx));
        registerPattern(new Planes(lx));
    }

    void registerOneShotTriggerables() {
        registerOneShot(new Pulleys(lx));
        registerOneShot(new StrobeOneshot(lx));
        registerOneShot(new BassSlam(lx));
        registerOneShot(new Fireflies(lx, 70, 6, 180));
        registerOneShot(new Fireflies(lx, 40, 7.5f, 90));

        registerOneShot(new Fireflies(lx), 5);
        registerOneShot(new Bubbles(lx), 5);
        registerOneShot(new Lightning(lx), 5);
        registerOneShot(new Wisps(lx), 5);
        registerOneShot(new Explosions(lx), 5);
    }

    void registerOneShot(EntwinedTriggerablePattern pattern) {
        registerOneShot(pattern, 4);
    }

    void registerOneShot(EntwinedTriggerablePattern pattern, int apc40DrumpadRow) {
        registerVisual(pattern, apc40DrumpadRow);
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

        registerEffectControlParameter(speedEffect.speed, 1, 0.4);
        registerEffectControlParameter(speedEffect.speed, 1, 5);
        registerEffectControlParameter(colorEffect.rainbow);
        registerEffectControlParameter(colorEffect.mono);
        registerEffectControlParameter(colorEffect.desaturation);
        registerEffectControlParameter(colorEffect.sharp);
        registerEffectControlParameter(blurEffect.level, 0.65);
        registerEffectControlParameter(spinEffect.spin, 0.65);
        registerEffectControlParameter(ghostEffect.amount, 0, 0.16, 1);
        registerEffectControlParameter(scrambleEffect.amount, 0, 1, 1);
        registerEffectControlParameter(colorStrobeTextureEffect.amount, 0, 1, 1);
        registerEffectControlParameter(fadeTextureEffect.amount, 0, 1, 1);
        registerEffectControlParameter(acidTripTextureEffect.amount, 0, 1, 1);
        registerEffectControlParameter(candyCloudTextureEffect.amount, 0, 1, 1);
        registerEffectControlParameter(staticEffect.amount, 0, .3, 1);
        registerEffectControlParameter(candyTextureEffect.amount, 0, 1, 5);

        // These are the 8 knobs labled "Track Control" on the APC40
        parameters.effectKnobParameters =
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

    public void registerPattern(EntwinedTriggerablePattern pattern) {
        registerPattern(pattern, 2);
    }

    public void registerPattern(EntwinedTriggerablePattern pattern, int apc40DrumpadRow) {
        registerVisual(pattern, apc40DrumpadRow);
    }

    void registerVisual(EntwinedTriggerablePattern pattern, int apc40DrumpadRow) {
        Triggerable triggerable = configurePatternAsTriggerable(pattern);
        if (ConfigLoader.enableAPC40) {
            drumpadBuilder.addTriggerableToRow(apc40DrumpadRow, triggerable);
        }
    }

    Triggerable configurePatternAsTriggerable(EntwinedTriggerablePattern pattern) {
        LXChannel channel = lx.engine.mixer.addChannel(new EntwinedBasePattern[] {pattern});
        System.out.printf(
                "Added Triggerable Pattern '%s' to channel %d\n",
                pattern.getReadableName(), channel.getIndex());
        setupChannel(channel, false);

        pattern.onTriggerableModeEnabled();
        return pattern.getTriggerable();
    }

    void setupChannel(final LXChannel channel, boolean noOpWhenNotRunning) {
        // TODO(meawoppl)
        // @Slee, honestly no idea what the intention is here...
        // It looks like patterns used to have associated traditionss" have have moved to the
        // `blend` language, but I don't seen any analogue for that remaining....
        //        channel.setFaderTransition(
        //                new TreesTransition(lx, channel, model, parameters.channelTreeLevels,
        // parameters.channelShrubLevels));

        // Trying the following:
        channel.transitionBlendMode.setObjects(
                new TreesTransition[] {
                    new TreesTransition(
                            lx,
                            channel,
                            model,
                            parameters.channelTreeLevels,
                            parameters.channelShrubLevels)
                });

        channel.addListener(
                new LXChannel.Listener() {
                    LXBlend transition;

                    @Override
                    public void patternWillChange(
                            LXChannel channel, LXPattern pattern, LXPattern nextPattern) {
                        if (!channel.enabled.isOn()) {
                            // TODO(meawoppl) - IDK
                            // transition = nextPattern.getTransition();
                            //  nextPattern.setTransition(null);
                        }
                    }

                    @Override
                    public void patternDidChange(LXChannel channel, LXPattern pattern) {
                        if (transition != null) {
                            // TODO(meawoppl) IDK
                            // pattern.setTransition(transition);
                            // transition = null;
                        }
                    }
                });

        if (noOpWhenNotRunning) {
            channel.enabled.setValue(channel.fader.getValue() != 0);
            channel.fader.addListener(
                    (parameter) -> channel.enabled.setValue(channel.fader.getValue() != 0));
        }
    }

    void registerEffectControlParameter(LXListenableNormalizedParameter parameter) {
        registerEffectControlParameter(parameter, 0, 1, 0);
    }

    void registerEffectControlParameter(LXListenableNormalizedParameter parameter, double onValue) {
        registerEffectControlParameter(parameter, 0, onValue, 0);
    }

    void registerEffectControlParameter(
            LXListenableNormalizedParameter parameter, double offValue, double onValue) {
        registerEffectControlParameter(parameter, offValue, onValue, 0);
    }

    void registerEffectControlParameter(
            LXListenableNormalizedParameter parameter, double offValue, double onValue, int row) {
        ParameterTriggerableAdapter triggerable =
                new ParameterTriggerableAdapter(lx, parameter, offValue, onValue);
        drumpadBuilder.addTriggerableToRow(row, triggerable);
    }

    void registerEffectController(
            String name, LXEffect effect, LXListenableNormalizedParameter parameter) {
        ParameterTriggerableAdapter triggerable = new ParameterTriggerableAdapter(lx, parameter);
        TSEffectController effectController = new TSEffectController(name, effect, triggerable);

        engineController.effectControllers.add(effectController);
    }

    public void configureServer() {
        new AppServer(lx, engineController).start();
    }
}
