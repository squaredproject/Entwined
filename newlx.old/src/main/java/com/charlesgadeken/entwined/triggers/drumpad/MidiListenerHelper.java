package com.charlesgadeken.entwined.triggers.drumpad;

import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.MidiAftertouch;
import heronarts.lx.midi.MidiControlChange;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.MidiPitchBend;
import heronarts.lx.midi.MidiProgramChange;

public class MidiListenerHelper implements LXMidiListener {
    public MidiListenerHelper() {};

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        System.out.println("NoteOn");
        System.out.println(note);
    }

    @Override
    public void noteOffReceived(MidiNote note) {
        System.out.println("NoteOff");
        System.out.println(note);
    }

    @Override
    public void controlChangeReceived(MidiControlChange cc) {
        System.out.println("ControlChange");
        System.out.println(cc);
    }

    @Override
    public void programChangeReceived(MidiProgramChange pc) {
        System.out.println("ProgramChange");
        System.out.println(pc);
    }

    @Override
    public void pitchBendReceived(MidiPitchBend pitchBend) {
        System.out.println("PitchBend");
        System.out.println(pitchBend);
    }

    @Override
    public void aftertouchReceived(MidiAftertouch aftertouch) {
        System.out.println("AfterTouch");
        System.out.println(aftertouch);
    }
}
