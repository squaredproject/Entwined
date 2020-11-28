package com.charlesgadeken.entwined.triggers.drumpad;

import heronarts.lx.LX;
import heronarts.lx.midi.*;
import heronarts.lx.midi.surface.LXMidiSurface;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.parameter.LXParameter;
import org.apache.commons.lang3.tuple.Pair;

import javax.sound.midi.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class APC40 extends LXMidiSurface {
    public static final int ANY_CHANNEL = -1;
    public static final int NOTE_VELOCITY = -1;
    public static final int CC_VALUE = -1;
    public static final int DIRECT = 1;
    public static final int TOGGLE = 2;


    public static final int VOLUME = 7;
    public static final int MASTER_FADER = 14;
    public static final int CUE_LEVEL = 47;
    public static final int DEVICE_CONTROL = 16;
    public static final int DEVICE_CONTROL_LED_MODE = 24;
    public static final int TRACK_CONTROL = 48;
    public static final int TRACK_CONTROL_LED_MODE = 56;
    public static final int CROSSFADER = 15;
    public static final int CLIP_LAUNCH = 53;
    public static final int SCENE_LAUNCH = 82;
    public static final int CLIP_STOP = 52;
    public static final int STOP_ALL_CLIPS = 81;
    public static final int TRACK_SELECTION = 51;
    public static final int MASTER_TRACK = 80;
    public static final int ACTIVATOR = 50;
    public static final int SOLO_CUE = 49;
    public static final int RECORD_ARM = 48;
    public static final int PAN = 87;
    public static final int SEND_A = 88;
    public static final int SEND_B = 89;
    public static final int SEND_C = 90;
    public static final int SHIFT = 98;
    public static final int BANK_UP = 94;
    public static final int BANK_DOWN = 95;
    public static final int BANK_RIGHT = 96;
    public static final int BANK_LEFT = 97;
    public static final int TAP_TEMPO = 99;
    public static final int NUDGE_PLUS = 100;
    public static final int NUDGE_MINUS = 101;
    public static final int CLIP_TRACK = 58;
    public static final int DEVICE_ON_OFF = 59;
    public static final int LEFT_ARROW = 60;
    public static final int RIGHT_ARROW = 61;
    public static final int DETAIL_VIEW = 62;
    public static final int REC_QUANTIZATION = 63;
    public static final int MIDI_OVERDUB = 64;
    public static final int METRONOME = 65;
    public static final int PLAY = 91;
    public static final int STOP = 92;
    public static final int REC = 93;
    public static final int OFF = 0;
    public static final int GREEN = 1;
    public static final int GREEN_BLINK = 2;
    public static final int RED = 3;
    public static final int RED_BLINK = 4;
    public static final int YELLOW = 5;
    public static final int YELLOW_BLINK = 6;
    public static final int LED_MODE_OFF = 0;
    public static final int LED_MODE_SINGLE = 1;
    public static final int LED_MODE_VOLUME = 2;
    public static final int LED_MODE_PAN = 3;
    public static final byte GENERIC = 64;
    public static final byte MODE_ABLETON = 65;
    public static final byte MODE_ALTERNATE_ABLETON = 66;
    public static final int NUM_TRACK_CONTROL_KNOBS = 8;
    public static final int NUM_DEVICE_CONTROL_KNOBS = 8;
    public static final String DEVICE_NAME = "APC40";
    private LXChannel deviceControlChannel;

    private Consumer<MidiNoteOn> onNoteOn;
    private Consumer<MidiControlChange> onControlChange;

    public static LXMidiInput matchInput(LX lx) {
        return lx.engine.midi.matchInput("APC40");
    }

    public static LXMidiOutput matchOutput(LX lx) { return lx.engine.midi.matchOutput("APC40"); }

    private Map<Pair<Integer, Integer>, LXParameter> noteBindings;

    public void setOnNoteOn(Consumer<MidiNoteOn> onNoteOn){
        this.onNoteOn = onNoteOn;
    }

    public void setOnControlChange(Consumer<MidiControlChange> cc){
        this.onControlChange = cc;
    }

    public static APC40 getAPC40(LX lx) {
        return new APC40(lx, matchInput(lx), matchOutput(lx));
    }

    public APC40(LX lx, LXMidiInput input, LXMidiOutput output){
        super(lx, input, output);
        noteBindings = new HashMap<>();
    }

    public APC40(LX lx){
        super(lx, APC40.matchInput(lx), APC40.matchOutput(lx));
    }

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        super.noteOnReceived(note);
        if(onNoteOn != null){
            onNoteOn.accept(note);
        }

        Pair<Integer, Integer> channelNote = Pair.of(note.getChannel(), note.getPitch());
        if(noteBindings.containsKey(channelNote)){
            LXParameter parameter = noteBindings.get(channelNote);
            parameter.setValue(1);
        }
    }

    @Override
    public void noteOffReceived(MidiNote note) {
        super.noteOffReceived(note);

        Pair<Integer, Integer> channelNote = Pair.of(note.getChannel(), note.getPitch());
        if(noteBindings.containsKey(channelNote)){
            LXParameter parameter = noteBindings.get(channelNote);
            parameter.setValue(0);
        }
    }

    @Override
    public void controlChangeReceived(MidiControlChange cc) {
        super.controlChangeReceived(cc);

        if( onControlChange != null) {
            onControlChange.accept(cc);
        }

    }

    public void bindNote(LXParameter parameter, int channel, int note) {
        noteBindings.put(Pair.of(channel, note), parameter);
    }

    public void bindNotes(LXParameter parameter, int[] channels, int note){
        for (int channel: channels) {
            bindNote(parameter, channel, note);
        }
    }


// This is theproblem with the APC40 that it requires a special SYSEX to say we're
// ableton. In older versions of this code, we used a library called 'mmj' that works
// only on the mac and is obsolete, we've switched to CoreMidi which seems more modern
// here in 2018 and seems to play nicely on multiple platforms
//

    static void setAPC40Mode() throws Exception {
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

        //System.out.println(" APC40 2 Mode --- for non-mac ");
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        System.out.println(" length of array is "+infos.length);

        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {

            System.out.println(" looking for the APC40:: "+info.toString());

            // Note: on Macs, there are often multiple devices that will claim to be
            // APC40, but some will have receivers and some will not. Only try
            // to send on the ones that have receivers. Sending this sysex to the wrong
            // device seems somewhat harmless
            if (info.toString().contains("APC40")) {

                System.out.println(" Found APC40 - try to send send sysex");
                try {
                    SysexMessage sysMsg = new SysexMessage(  );
                    sysMsg.setMessage(APC_MODE_SYSEX, APC_MODE_SYSEX.length);

                    MidiDevice dev = MidiSystem.getMidiDevice(info);

                    dev.open();
                    Receiver r = dev.getReceiver();

                    r.send(sysMsg, -1);
                    dev.close();
                }
                catch ( InvalidMidiDataException e ) {
                    System.out.println("InvalidMidiDataException: sysex send " + e.getMessage());
                }
                catch ( MidiUnavailableException e ) {
                    System.out.println("MidiUnavailableException: sysex send " + e.getMessage());
                }
            }
        }
    }
}
