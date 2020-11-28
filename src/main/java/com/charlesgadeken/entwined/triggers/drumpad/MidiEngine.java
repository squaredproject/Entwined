package com.charlesgadeken.entwined.triggers.drumpad;

import com.charlesgadeken.entwined.EntwinedParameters;
import com.charlesgadeken.entwined.TreesTransition;
import com.charlesgadeken.entwined.bpm.BPMTool;
import com.charlesgadeken.entwined.config.ConfigLoader;
import heronarts.lx.LX;
import heronarts.lx.midi.*;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.parameter.*;
import javax.sound.midi.*;

public class MidiEngine {
    private final LX lx;
    // private final LXAutomationRecorder[] automation;

    private final EntwinedParameters parameters;

    public MidiEngine(
            final LX lx,
            EntwinedParameters parameters,
            final TSDrumpad apc40Drumpad,
            final BPMTool bpmTool,
            final InterfaceController uiDeck
            // LXAutomationRecorder[] automation,
            ) {

        this.lx = lx;
        this.parameters = parameters;
        // this.automation = automation;

        try {
            setAPC40Mode();
        } catch (java.lang.UnsatisfiedLinkError e) {
            System.out.println(
                    "could not set up APC40: unsatisfied link error, caused by mmj on non mac");
            return;
        }

        LXMidiInput apcInput = lx.engine.midi.matchInput(APC40mk1.DEVICE_NAME);
        LXMidiOutput apcOutput = lx.engine.midi.matchOutput(APC40mk1.DEVICE_NAME);

        apcInput.enabled.setValue(true);
        apcOutput.enabled.setValue(true);
        System.out.println("MIDI Setup...");
        if (apcInput != null) {

            // Add this input to the midi engine so that events are recorded
            if (!lx.engine.midi.inputs.contains(apcInput)) {
                throw new RuntimeException("Expected APC40 in inputs");
            }

            lx.engine.midi.addListener(new MidiListenerHelper());

            lx.engine.midi.addListener(
                    new LXAbstractMidiListener() {
                        public void noteOnReceived(MidiNoteOn note) {
                            int channel = note.getChannel();
                            int pitch = note.getPitch();
                            switch (pitch) {
                                case APC40mk1.CLIP_LAUNCH:
                                case APC40mk1.CLIP_LAUNCH + 1:
                                case APC40mk1.CLIP_LAUNCH + 2:
                                case APC40mk1.CLIP_LAUNCH + 3:
                                case APC40mk1.CLIP_LAUNCH + 4:
                                    apc40Drumpad.padTriggered(
                                            pitch - APC40mk1.CLIP_LAUNCH,
                                            channel,
                                            parameters.drumpadVelocity.getValuef());
                                    break;
                                case APC40mk1.CLIP_STOP:
                                    apc40Drumpad.padTriggered(
                                            5, channel, parameters.drumpadVelocity.getValuef());
                                    break;
                                case APC40mk1.SCENE_LAUNCH:
                                case APC40mk1.SCENE_LAUNCH + 1:
                                case APC40mk1.SCENE_LAUNCH + 2:
                                case APC40mk1.SCENE_LAUNCH + 3:
                                case APC40mk1.SCENE_LAUNCH + 4:
                                    apc40Drumpad.padTriggered(
                                            pitch - APC40mk1.SCENE_LAUNCH,
                                            8,
                                            parameters.drumpadVelocity.getValuef());
                                    break;
                                case APC40mk1.STOP_ALL_CLIPS:
                                    apc40Drumpad.padTriggered(
                                            5, 8, parameters.drumpadVelocity.getValuef());
                                    break;
                            }
                        }

                        public void noteOffReceived(MidiNote note) {
                            int channel = note.getChannel();
                            int pitch = note.getPitch();
                            switch (pitch) {
                                case APC40mk1.CLIP_LAUNCH:
                                case APC40mk1.CLIP_LAUNCH + 1:
                                case APC40mk1.CLIP_LAUNCH + 2:
                                case APC40mk1.CLIP_LAUNCH + 3:
                                case APC40mk1.CLIP_LAUNCH + 4:
                                    apc40Drumpad.padReleased(pitch - APC40mk1.CLIP_LAUNCH, channel);
                                    break;
                                case APC40mk1.CLIP_STOP:
                                    apc40Drumpad.padReleased(5, channel);
                                    break;
                                case APC40mk1.SCENE_LAUNCH:
                                case APC40mk1.SCENE_LAUNCH + 1:
                                case APC40mk1.SCENE_LAUNCH + 2:
                                case APC40mk1.SCENE_LAUNCH + 3:
                                case APC40mk1.SCENE_LAUNCH + 4:
                                    apc40Drumpad.padReleased(pitch - APC40mk1.SCENE_LAUNCH, 8);
                                    break;
                                case APC40mk1.STOP_ALL_CLIPS:
                                    apc40Drumpad.padReleased(5, 8);
                                    break;
                            }
                        }
                    });

            final APC40mk1 apc40Mk1 =
                    new APC40mk1(apcInput, apcOutput) {
                        protected void noteOn(MidiNoteOn note) {
                            int channel = note.getChannel();
                            switch (note.getPitch()) {
                                case APC40mk1.SOLO_CUE:
                                    if (parameters.previewChannels[channel].isOn()
                                            && channel != focusedChannel()) {
                                        lx.engine.mixer.focusedChannel.setValue(channel);
                                    }
                                    break;

                                case APC40mk1.SEND_A:
                                    bpmTool.beatType.increment();
                                    break;
                                case APC40mk1.SEND_B:
                                    bpmTool.tempoLfoType.increment();
                                    break;

                                case APC40mk1.MASTER_TRACK:
                                case APC40mk1.SHIFT:
                                    if (uiDeck != null) uiDeck.select();
                                    break;
                                case APC40mk1.BANK_UP:
                                    if (uiDeck != null) uiDeck.scroll(-1);
                                    break;
                                case APC40mk1.BANK_DOWN:
                                    if (uiDeck != null) uiDeck.scroll(1);
                                    break;
                                case APC40mk1.BANK_RIGHT:
                                    lx.engine.mixer.focusedChannel.increment();
                                    break;
                                case APC40mk1.BANK_LEFT:
                                    lx.engine.mixer.focusedChannel.decrement();
                                    break;
                            }
                        }

                        protected void controlChange(MidiControlChange controller) {
                            switch (controller.getCC()) {
                                case APC40mk1.CUE_LEVEL:
                                    if (uiDeck != null) uiDeck.knob(controller.getValue());
                                    break;
                            }
                        }
                    };

            lx.engine.midi.addListener(apc40Mk1);

            // Breadcrumb: there was some code here to init the NFC subsystem.
            // We aren't using NFC anymore, but if you want to revive it, go look
            // at earlier versions of this file in the code repo.
            // this section is all about NFC which we're removing,
            // which means this function doesn't need the apc40Drumpad anymore

            int[] channelIndices = new int[ConfigLoader.NUM_CHANNELS];
            for (int i = 0; i < ConfigLoader.NUM_CHANNELS; ++i) {
                channelIndices[i] = i;
            }

            // Track selection
            apc40Mk1.bindNotes(
                    lx.engine.mixer.focusedChannel, channelIndices, APC40mk1.TRACK_SELECTION);

            for (int i = 0; i < ConfigLoader.NUM_CHANNELS; i++) {
                // Cue activators
                apc40Mk1.bindNote(
                        parameters.previewChannels[i],
                        i,
                        APC40mk1.SOLO_CUE,
                        LXMidiDeviceBackport.TOGGLE);
                apc40Mk1.bindController(
                        lx.engine.mixer.getChannel(i).fader,
                        i,
                        APC40mk1.VOLUME,
                        LXMidiDeviceBackport.TakeoverMode.PICKUP);
            }

            for (int i = 0; i < 8; ++i) {
                apc40Mk1.sendController(
                        0, APC40mk1.TRACK_CONTROL_LED_MODE + i, APC40mk1.LED_MODE_VOLUME);
                apc40Mk1.sendController(
                        0, APC40mk1.DEVICE_CONTROL_LED_MODE + i, APC40mk1.LED_MODE_VOLUME);
            }

            // Master fader
            apc40Mk1.bindController(
                    parameters.outputBrightness,
                    0,
                    APC40mk1.MASTER_FADER,
                    LXMidiDeviceBackport.TakeoverMode.PICKUP);

            apc40Mk1.bindController(parameters.drumpadVelocity, 0, APC40mk1.CROSSFADER);

            // Effect knobs + buttons
            for (int i = 0; i < parameters.effectKnobParameters.length; ++i) {
                if (parameters.effectKnobParameters[i] != null) {
                    apc40Mk1.bindController(
                            parameters.effectKnobParameters[i], 0, APC40mk1.TRACK_CONTROL + i);
                }
            }

            // Pattern control
            apc40Mk1.bindDeviceControlKnobs(lx.engine);
            lx.engine.mixer.focusedChannel.addListener(
                    (LXParameter parameter) -> {
                        apc40Mk1.bindNotes(
                                getFaderTransition((LXChannel) lx.engine.mixer.getFocusedChannel())
                                        .blendMode,
                                0,
                                new int[] {
                                    APC40mk1.CLIP_TRACK,
                                    APC40mk1.DEVICE_ON_OFF,
                                    APC40mk1.LEFT_ARROW,
                                    APC40mk1.RIGHT_ARROW
                                });
                    });

            // Tap Tempo
            apc40Mk1.bindNote(
                    new BooleanParameter("ANON", false), 0, APC40mk1.SEND_A, APC40mk1.DIRECT);
            apc40Mk1.bindNote(
                    new BooleanParameter("ANON", false), 0, APC40mk1.SEND_B, APC40mk1.DIRECT);
            apc40Mk1.bindNote(bpmTool.addTempoLfo, 0, APC40mk1.PAN, APC40mk1.DIRECT);
            apc40Mk1.bindNote(bpmTool.clearAllTempoLfos, 0, APC40mk1.SEND_C, APC40mk1.DIRECT);
            apc40Mk1.bindNote(bpmTool.tapTempo, 0, APC40mk1.TAP_TEMPO, APC40mk1.DIRECT);
            apc40Mk1.bindNote(bpmTool.nudgeUpTempo, 0, APC40mk1.NUDGE_PLUS, APC40mk1.DIRECT);
            apc40Mk1.bindNote(bpmTool.nudgeDownTempo, 0, APC40mk1.NUDGE_MINUS, APC40mk1.DIRECT);

            apc40Mk1.bindNotes(
                    getFaderTransition((LXChannel) lx.engine.mixer.getFocusedChannel()).blendMode,
                    0,
                    new int[] {
                        APC40mk1.CLIP_TRACK,
                        APC40mk1.DEVICE_ON_OFF,
                        APC40mk1.LEFT_ARROW,
                        APC40mk1.RIGHT_ARROW
                    });
            apc40Mk1.bindNotes(
                    parameters.automationSlot,
                    0,
                    new int[] {
                        APC40mk1.DETAIL_VIEW,
                        APC40mk1.REC_QUANTIZATION,
                        APC40mk1.MIDI_OVERDUB,
                        APC40mk1.METRONOME
                    });
            parameters.automationSlot.addListener((parameter) -> setAutomation(apc40Mk1));
            setAutomation(apc40Mk1);
        }
    }

    void setAutomation(APC40mk1 apc40) {
        // TODO(meawoppl)?
        //        LXAutomationRecorder auto = automation[automationSlot.getValuei()];
        //        apc40.bindNoteOn(auto.isRunning, 0, APC40.PLAY, LXMidiDevice.TOGGLE);
        //        apc40.bindNoteOn(auto.armRecord, 0, APC40.REC, LXMidiDevice.TOGGLE);
        //        apc40.bindNote(
        //                automationStop[automationSlot.getValuei()], 0, APC40mk1.STOP,
        // LXMidiDeviceBackport.DIRECT);
    }

    // This is theproblem with the APC40 that it requires a special SYSEX to say we're
    // ableton. In older versions of this code, we used a library called 'mmj' that works
    // only on the mac and is obsolete, we've switched to CoreMidi which seems more modern
    // here in 2018 and seems to play nicely on multiple platforms
    void setAPC40Mode() {
        final byte[] APC_MODE_SYSEX = {
            (byte) 0xf0, // sysex start
            (byte) 0x47, // manufacturers id
            (byte) 0x00, // device id
            (byte) 0x73, // product model id
            (byte) 0x60, // message
            (byte) 0x00, // bytes MSB
            (byte) 0x04, // bytes LSB
            (byte) 0x42, // ableton mode 2
            (byte) 0x08, // version maj
            (byte) 0x01, // version min
            (byte) 0x01, // version bugfix
            (byte) 0xf7, // sysex end
        };

        boolean sentSysEx = false;
        int i = 0;

        // System.out.println(" APC40 2 Mode --- for non-mac ");
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        // System.out.println(" length of array is "+infos.length);

        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {

            // System.out.println(" looking for the APC40:: "+info.toString());

            // Note: on Macs, there are often multiple devices that will claim to be
            // APC40, but some will have receivers and some will not. Only try
            // to send on the ones that have receivers. Sending this sysex to the wrong
            // device seems somewhat harmless
            if (info.toString().contains("APC40")) {

                // System.out.println(" Found APC40 - try to send send sysex");
                try {
                    SysexMessage sysMsg = new SysexMessage();
                    sysMsg.setMessage(APC_MODE_SYSEX, APC_MODE_SYSEX.length);

                    MidiDevice dev = MidiSystem.getMidiDevice(info);

                    dev.open();
                    Receiver r = dev.getReceiver();

                    r.send(sysMsg, -1);
                    sentSysEx = true;
                    dev.close();
                } catch (InvalidMidiDataException e) {
                    // System.out.println("InvalidMidiDataException: sysex send " + e.getMessage());
                } catch (MidiUnavailableException e) {
                    // System.out.println("MidiUnavailableException: sysex send " + e.getMessage());
                }

                // just send to all of them
            }
            i++;
        }
    }

    int focusedChannel() {
        return lx.engine.mixer.focusedChannel.getValuei();
    }

    TreesTransition getFaderTransition(LXChannel channel) {
        return (TreesTransition) channel.transitionBlendMode.getObject();
    }
}
