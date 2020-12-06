package com.charlesgadeken.entwined.triggers.drumpad;

import heronarts.lx.LX;
import heronarts.lx.midi.*;
import heronarts.lx.mixer.LXChannel;

import javax.annotation.Nullable;

/** @Slee please forgive my explicit theft of these from an earlier version of LX */
public class APC40mk1 extends LXMidiDeviceBackport {
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

    public final LXMidiInput input;
    public final LXMidiOutput output;

    public static boolean hasACP40(LX lx){
        return (matchInput(lx) != null) && (matchOutput(lx) != null);
    }

    public static @Nullable LXMidiInput matchInput(LX lx) {
        return lx.engine.midi.matchInput("APC40");
    }

    public static @Nullable LXMidiOutput matchOutput(LX lx) {
        return lx.engine.midi.matchOutput("APC40");
    }

    public APC40mk1(LXMidiInput input, LXMidiOutput output) {
        super(input, output);
        this.input = input;
        this.output = output;
    }
}
