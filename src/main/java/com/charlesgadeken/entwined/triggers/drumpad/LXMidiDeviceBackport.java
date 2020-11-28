package com.charlesgadeken.entwined.triggers.drumpad;

import heronarts.lx.LXEngine;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.LXMidiOutput;
import heronarts.lx.midi.MidiAftertouch;
import heronarts.lx.midi.MidiControlChange;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.MidiPitchBend;
import heronarts.lx.midi.MidiProgramChange;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;
import java.util.Iterator;

public class LXMidiDeviceBackport implements LXMidiListener {
    private static final int MIDI_RANGE = 128;
    private static final int MIDI_CHANNELS = 16;
    private static final int NUM_BINDINGS = 2048;
    private static final int MIDI_MAX = 127;
    public static final int ANY_CHANNEL = -1;
    public static final int NOTE_VELOCITY = -1;
    public static final int CC_VALUE = -1;
    private static final int OFF = -1;
    public static final int DIRECT = 1;
    public static final int TOGGLE = 2;
    private static final int DISCRETE = 3;
    private static final int DISCRETE_OFF = 4;
    private boolean logEvents;
    private final LXMidiDeviceBackport.NoteBinding[] noteOnBindings;
    private final LXMidiDeviceBackport.NoteBinding[] noteOffBindings;
    private final LXMidiDeviceBackport.ControllerBinding[] controllerBindings;
    private final LXMidiInput input;
    private final LXMidiOutput output;

    public LXMidiDeviceBackport(LXMidiInput var1) {
        this(var1, (LXMidiOutput) null);
    }

    public LXMidiDeviceBackport(LXMidiOutput var1) {
        this((LXMidiInput) null, var1);
    }

    public LXMidiDeviceBackport(LXMidiInput var1, LXMidiOutput var2) {
        this.logEvents = false;
        this.input = var1;
        this.output = var2;
        if (this.input != null) {
            this.input.addListener(this);
        }

        this.noteOnBindings = new LXMidiDeviceBackport.NoteBinding[2048];
        this.noteOffBindings = new LXMidiDeviceBackport.NoteBinding[2048];
        this.controllerBindings = new LXMidiDeviceBackport.ControllerBinding[2048];

        for (int var3 = 0; var3 < 2048; ++var3) {
            this.noteOnBindings[var3] = null;
            this.noteOffBindings[var3] = null;
            this.controllerBindings[var3] = null;
        }

        this.deviceControlChannel = null;
        this.deviceControlListener =
                new LXChannel.Listener() {
                    public void patternDidChange(LXChannel var1, LXPattern var2) {
                        bindDeviceControlKnobs(var2);
                    }
                };
    }

    public LXMidiInput getInput() {
        return this.input;
    }

    public LXMidiOutput getOutput() {
        return this.output;
    }

    public LXMidiDeviceBackport bindNote(LXParameter var1, int var2) {
        return this.bindNote(var1, -1, var2);
    }

    public LXMidiDeviceBackport bindNote(LXParameter var1, int var2, int var3) {
        return this.bindNote(var1, var2, var3, 1);
    }

    public LXMidiDeviceBackport bindNote(LXParameter var1, int var2, int var3, int var4) {
        return this.bindNote(var1, var2, var3, var4, -1);
    }

    public LXMidiDeviceBackport bindNote(LXParameter var1, int var2, int var3, int var4, int var5) {
        this.bindNoteOn(var1, var2, var3, var4, var5);
        if (var4 == 1) {
            this.bindNoteOff(var1, var2, var3);
        }

        return this;
    }

    public LXMidiDeviceBackport bindNoteOn(LXParameter var1, int var2) {
        return this.bindNoteOn(var1, -1, var2);
    }

    public LXMidiDeviceBackport bindNoteOn(LXParameter var1, int var2, int var3) {
        return this.bindNoteOn(var1, var2, var3, 1);
    }

    public LXMidiDeviceBackport bindNoteOn(LXParameter var1, int var2, int var3, int var4) {
        return this.bindNoteOn(var1, var2, var3, var4, -1);
    }

    public LXMidiDeviceBackport bindNoteOn(
            LXParameter var1, int var2, int var3, int var4, int var5) {
        return this.bindNoteOn(var1, var2, var3, var4, var5, 0);
    }

    private LXMidiDeviceBackport bindNoteOn(
            LXParameter var1, int var2, int var3, int var4, int var5, int var6) {
        int var7;
        if (var2 == -1) {
            for (var7 = 0; var7 < 16; ++var7) {
                this.bindNoteOn(var1, var7, var3, var4, var5, var6);
            }
        } else {
            this.unbindNoteOn(var2, var3);
            var7 = this.index(var2, var3);
            this.noteOnBindings[var7] =
                    new LXMidiDeviceBackport.NoteBinding(var1, var2, var3, var4, var5, var6);
        }

        return this;
    }

    public LXMidiDeviceBackport bindNoteOff(LXParameter var1, int var2) {
        return this.bindNoteOff(var1, -1, var2);
    }

    public LXMidiDeviceBackport bindNoteOff(LXParameter var1, int var2, int var3) {
        int var4;
        if (var2 == -1) {
            for (var4 = 0; var4 < 16; ++var4) {
                this.bindNoteOff(var1, var4, var3);
            }
        } else {
            this.unbindNoteOff(var2, var3);
            var4 = this.index(var2, var3);
            this.noteOffBindings[var4] =
                    new LXMidiDeviceBackport.NoteBinding(var1, var2, var3, -1, 0);
        }

        return this;
    }

    public LXMidiDeviceBackport bindController(LXParameter var1, int var2) {
        return this.bindController(var1, -1, var2);
    }

    public LXMidiDeviceBackport bindController(
            LXParameter var1, int var2, LXMidiDeviceBackport.TakeoverMode var3) {
        return this.bindController(var1, -1, var2, var3);
    }

    public LXMidiDeviceBackport bindController(LXParameter var1, int var2, int var3) {
        return this.bindController(var1, var2, var3, -1);
    }

    public LXMidiDeviceBackport bindController(
            LXParameter var1, int var2, int var3, LXMidiDeviceBackport.TakeoverMode var4) {
        return this.bindController(var1, var2, var3, -1, var4);
    }

    public LXMidiDeviceBackport bindController(LXParameter var1, int var2, int var3, int var4) {
        return this.bindController(
                var1, var2, var3, var4, LXMidiDeviceBackport.TakeoverMode.TAKEOVER);
    }

    public LXMidiDeviceBackport bindController(
            LXParameter var1,
            int var2,
            int var3,
            int var4,
            LXMidiDeviceBackport.TakeoverMode var5) {
        int var6;
        if (var2 == -1) {
            for (var6 = 0; var6 < 16; ++var6) {
                this.bindController(var1, var6, var3, var4);
            }
        } else {
            this.unbindController(var2, var3);
            var6 = this.index(var2, var3);
            this.controllerBindings[var6] =
                    new LXMidiDeviceBackport.ControllerBinding(var1, var2, var3, var4, var5);
        }

        return this;
    }

    public LXMidiDeviceBackport bindNotes(DiscreteParameter var1, int var2, int[] var3) {
        for (int var4 = 0; var4 < var3.length; ++var4) {
            this.bindNoteOn(var1, var2, var3[var4], 3, var1.getMinValue() + var4);
        }

        return this;
    }

    public LXMidiDeviceBackport bindNotes(DiscreteParameter var1, int var2, int[] var3, int var4) {
        for (int var5 = 0; var5 < var3.length; ++var5) {
            this.bindNoteOn(var1, var2, var3[var5], 4, var1.getMinValue() + var5, var4);
        }

        return this;
    }

    public LXMidiDeviceBackport bindNotes(DiscreteParameter var1, int[] var2, int var3) {
        for (int var4 = 0; var4 < var2.length; ++var4) {
            this.bindNoteOn(var1, var2[var4], var3, 3, var1.getMinValue() + var4);
        }

        return this;
    }

    public LXMidiDeviceBackport bindNotes(DiscreteParameter var1, int[] var2, int var3, int var4) {
        for (int var5 = 0; var5 < var2.length; ++var5) {
            this.bindNoteOn(var1, var2[var5], var3, 4, var1.getMinValue() + var5, var4);
        }

        return this;
    }

    public LXMidiDeviceBackport unbindNote(int var1) {
        return this.unbindNote(-1, var1);
    }

    public LXMidiDeviceBackport unbindNote(int var1, int var2) {
        this.unbindNoteOn(var1, var2);
        this.unbindNoteOff(var1, var2);
        return this;
    }

    public LXMidiDeviceBackport unbindNoteOn(int var1) {
        return this.unbindNoteOn(-1, var1);
    }

    public LXMidiDeviceBackport unbindNoteOn(int var1, int var2) {
        int var3;
        if (var1 == -1) {
            for (var3 = 0; var3 < 16; ++var3) {
                this.unbindNoteOn(var1, var2);
            }
        } else {
            var3 = this.index(var1, var2);
            if (this.noteOnBindings[var3] != null) {
                this.noteOnBindings[var3].unbind();
                this.noteOnBindings[var3] = null;
            }
        }

        return this;
    }

    public LXMidiDeviceBackport unbindNoteOff(int var1) {
        return this.unbindNoteOff(-1, var1);
    }

    public LXMidiDeviceBackport unbindNoteOff(int var1, int var2) {
        int var3;
        if (var1 == -1) {
            for (var3 = 0; var3 < 16; ++var3) {
                this.unbindNoteOff(var1, var2);
            }
        } else {
            var3 = this.index(var1, var2);
            if (this.noteOffBindings[var3] != null) {
                this.noteOffBindings[var3].unbind();
                this.noteOffBindings[var3] = null;
            }
        }

        return this;
    }

    public LXMidiDeviceBackport unbindController(int var1) {
        return this.unbindController(-1, var1);
    }

    public LXMidiDeviceBackport unbindController(int var1, int var2) {
        int var3;
        if (var1 == -1) {
            for (var3 = 0; var3 < 16; ++var3) {
                this.unbindNoteOff(var1, var2);
            }
        } else {
            var3 = this.index(var1, var2);
            if (this.controllerBindings[var3] != null) {
                this.controllerBindings[var3].unbind();
                this.controllerBindings[var3] = null;
            }
        }

        return this;
    }

    private int index(int var1, int var2) {
        return var1 * 128 + var2;
    }

    public LXMidiDeviceBackport sendNoteOn(int var1, int var2, int var3) {
        if (this.output != null) {
            this.output.sendNoteOn(var1, var2, var3);
        }

        return this;
    }

    public LXMidiDeviceBackport sendNoteOff(int var1, int var2) {
        return this.sendNoteOff(var1, var2, 0);
    }

    public LXMidiDeviceBackport sendNoteOff(int var1, int var2, int var3) {
        if (this.output != null) {
            this.output.sendNoteOff(var1, var2, var3);
        }

        return this;
    }

    public LXMidiDeviceBackport sendController(int var1, int var2, int var3) {
        if (this.output != null) {
            this.output.sendControlChange(var1, var2, var3);
        }

        return this;
    }

    public LXMidiDeviceBackport sendSysex(byte[] var1) {
        if (this.output != null) {
            this.output.sendSysex(var1);
        }

        return this;
    }

    public final LXMidiDeviceBackport logEvents(boolean var1) {
        this.logEvents = var1;
        return this;
    }

    public final void noteOnReceived(MidiNoteOn var1) {
        if (this.logEvents) {
            System.out.println(
                    this.input.getName()
                            + ":noteOn:"
                            + var1.getChannel()
                            + ":"
                            + var1.getPitch()
                            + ":"
                            + var1.getVelocity());
        }

        int var2 = this.index(var1.getChannel(), var1.getPitch());
        if (this.noteOnBindings[var2] != null) {
            this.noteOnBindings[var2].noteOnReceived(var1);
        }

        this.noteOn(var1);
    }

    public final void noteOffReceived(MidiNote var1) {
        if (this.logEvents) {
            System.out.println(
                    this.input.getName()
                            + ":noteOff:"
                            + var1.getChannel()
                            + ":"
                            + var1.getPitch()
                            + ":"
                            + var1.getVelocity());
        }

        int var2 = this.index(var1.getChannel(), var1.getPitch());
        if (this.noteOffBindings[var2] != null) {
            this.noteOffBindings[var2].noteOffReceived(var1);
        }

        this.noteOff(var1);
    }

    public final void controlChangeReceived(MidiControlChange var1) {
        if (this.logEvents) {
            System.out.println(
                    this.input.getName()
                            + ":controllerChange:"
                            + var1.getChannel()
                            + ":"
                            + var1.getCC()
                            + ":"
                            + var1.getValue());
        }

        int var2 = this.index(var1.getChannel(), var1.getCC());
        if (this.controllerBindings[var2] != null) {
            this.controllerBindings[var2].controlChangeReceived(var1);
        }

        this.controlChange(var1);
    }

    public final void programChangeReceived(MidiProgramChange var1) {
        if (this.logEvents) {
            System.out.println(
                    this.input.getName()
                            + ":programChange:"
                            + var1.getChannel()
                            + ":"
                            + var1.getProgram());
        }

        this.programChange(var1);
    }

    public final void pitchBendReceived(MidiPitchBend var1) {
        if (this.logEvents) {
            System.out.println(
                    this.input.getName()
                            + ":pitchBend:"
                            + var1.getChannel()
                            + ":"
                            + var1.getPitchBend());
        }

        this.pitchBend(var1);
    }

    public final void aftertouchReceived(MidiAftertouch var1) {
        if (this.logEvents) {
            System.out.println(
                    this.input.getName()
                            + ":aftertouch:"
                            + var1.getChannel()
                            + ":"
                            + var1.getAftertouch());
        }

        this.aftertouch(var1);
    }

    protected void noteOn(MidiNoteOn var1) {}

    protected void noteOff(MidiNote var1) {}

    protected void controlChange(MidiControlChange var1) {}

    protected void programChange(MidiProgramChange var1) {}

    protected void pitchBend(MidiPitchBend var1) {}

    protected void aftertouch(MidiAftertouch var1) {}

    private class ControllerBinding extends LXMidiDeviceBackport.Binding {
        private final int channel;
        private final int cc;
        private final int value;
        private LXMidiDeviceBackport.TakeoverMode takeoverMode;
        private boolean isDirty;
        private double pickupDirection;
        private double lastValueSet;

        private ControllerBinding(
                LXParameter var2,
                int var3,
                int var4,
                int var5,
                LXMidiDeviceBackport.TakeoverMode var6) {
            super(var2);
            this.isDirty = true;
            this.pickupDirection = 0.0D;
            this.lastValueSet = 0.0D;
            this.channel = var3;
            this.cc = var4;
            this.value = var5;
            this.takeoverMode = var6;
            this.assertChannel(var3);
            this.assertValue(var4);
            if (this.isListening) {
                this.onParameterChanged(this.parameter);
            }
        }

        private double valueDelta(int var1, double var2) {
            return this.parameter instanceof LXNormalizedParameter
                    ? var2 - ((LXNormalizedParameter) this.parameter).getNormalized()
                    : (double) var1 - this.parameter.getValue();
        }

        private void controlChangeReceived(MidiControlChange var1) {
            int var2 = var1.getValue();
            double var3 = (double) var2 / 127.0D;
            if (this.takeoverMode == LXMidiDeviceBackport.TakeoverMode.PICKUP) {
                double var5 = this.valueDelta(var2, var3);
                if (this.isDirty) {
                    if (this.pickupDirection == 0.0D) {
                        if (Math.abs(var5) < 0.04D) {
                            this.isDirty = false;
                        } else {
                            this.pickupDirection = var5;
                        }
                    }

                    if (var5 == 0.0D || var5 > 0.0D != this.pickupDirection > 0.0D) {
                        this.isDirty = false;
                    }
                } else if (this.parameter.getValue() != this.lastValueSet) {
                    this.pickupDirection = var5;
                    this.isDirty = true;
                }

                if (this.isDirty) {
                    return;
                }
            }

            if (var2 == 0) {
                if (this.parameter instanceof LXNormalizedParameter) {
                    ((LXNormalizedParameter) this.parameter).setNormalized(0.0D);
                } else {
                    this.parameter.setValue(0.0D);
                }
            } else {
                switch (this.value) {
                    case -1:
                        if (this.parameter instanceof LXNormalizedParameter) {
                            ((LXNormalizedParameter) this.parameter).setNormalized(var3);
                        } else {
                            this.parameter.setValue((double) var2);
                        }
                        break;
                    default:
                        this.parameter.setValue((double) this.value);
                }
            }

            this.lastValueSet = this.parameter.getValue();
        }

        public void onParameterChanged(LXParameter var1) {
            if (LXMidiDeviceBackport.this.output != null) {
                double var2 = this.parameter.getValue();
                if (this.parameter instanceof LXNormalizedParameter) {
                    double var4 = ((LXNormalizedParameter) this.parameter).getNormalized();
                    var2 = 127.0D * var4;
                }

                if (var2 == 0.0D) {
                    LXMidiDeviceBackport.this.output.sendControlChange(this.channel, this.cc, 0);
                } else {
                    int var6;
                    switch (this.value) {
                        case -1:
                            var6 = (int) LXUtils.constrain(var2, 0.0D, 127.0D);

                            break;
                        default:
                            var6 = LXUtils.constrain(this.value, 0, 127);
                    }

                    LXMidiDeviceBackport.this.output.sendControlChange(this.channel, this.cc, var6);
                }
            }
        }
    }

    private class NoteBinding extends LXMidiDeviceBackport.Binding {
        private final int channel;
        private final int number;
        private final int mode;
        private final int value;
        private final int secondary;

        private NoteBinding(LXParameter var2, int var3, int var4, int var5, int var6) {
            this(var2, var3, var4, var5, var6, 0);
        }

        private NoteBinding(LXParameter var2, int var3, int var4, int var5, int var6, int var7) {
            super(var2);
            this.channel = var3;
            this.number = var4;
            this.mode = var5;
            this.value = var6;
            this.secondary = var7;
            this.assertChannel(var3);
            this.assertValue(var4);
            switch (this.mode) {
                case -1:
                case 1:
                    break;
                case 0:
                default:
                    throw new IllegalArgumentException("Invalid NoteBinding mode: " + var5);
                case 2:
                    if (!(var2 instanceof LXNormalizedParameter)) {
                        throw new IllegalArgumentException(
                                "TOGGLE mode requires LXNormalizedParameter");
                    }
                    break;
                case 3:
                case 4:
                    if (!(var2 instanceof DiscreteParameter)) {
                        throw new IllegalArgumentException(
                                "DISCRETE mode requires DiscreteParameter");
                    }
            }

            if (this.isListening) {
                this.onParameterChanged(this.parameter);
            }
        }

        private void noteOnReceived(MidiNoteOn var1) {
            switch (this.mode) {
                case 1:
                    switch (this.value) {
                        case -1:
                            if (this.parameter instanceof LXNormalizedParameter) {
                                double var4 = (double) var1.getVelocity() / 127.0D;
                                ((LXNormalizedParameter) this.parameter).setNormalized(var4);
                            } else {
                                this.parameter.setValue((double) var1.getVelocity());
                            }

                            return;
                        default:
                            this.parameter.setValue((double) this.value);
                            return;
                    }
                case 2:
                    LXNormalizedParameter var2 = (LXNormalizedParameter) this.parameter;
                    if (this.parameter instanceof BooleanParameter) {
                        ((BooleanParameter) this.parameter).toggle();
                    } else if (var2.getNormalized() > 0.0D) {
                        var2.setNormalized(0.0D);
                    } else {
                        var2.setNormalized(1.0D);
                    }
                    break;
                case 3:
                case 4:
                    DiscreteParameter var3 = (DiscreteParameter) this.parameter;
                    if (this.mode == 4 && this.value == var3.getValuei()) {
                        var3.setValue((double) this.secondary);
                    } else {
                        var3.setValue((double) this.value);
                    }
            }
        }

        private void noteOffReceived(MidiNote var1) {
            switch (this.mode) {
                case -1:
                    if (this.parameter instanceof BooleanParameter) {
                        ((BooleanParameter) this.parameter).setValue(false);
                    } else if (this.parameter instanceof LXNormalizedParameter) {
                        ((LXNormalizedParameter) this.parameter).setNormalized(0.0D);
                    } else {
                        this.parameter.setValue(0.0D);
                    }
                default:
            }
        }

        public void onParameterChanged(LXParameter var1) {
            if (LXMidiDeviceBackport.this.output != null) {
                double var2 = this.parameter.getValue();
                if (this.parameter instanceof LXNormalizedParameter) {
                    var2 = 127.0D * ((LXNormalizedParameter) this.parameter).getNormalized();
                }

                switch (this.mode) {
                    case -1:
                        if (var2 == 0.0D) {
                            LXMidiDeviceBackport.this.output.sendNoteOff(
                                    this.channel, this.number, 0);
                        }
                    case 0:
                    default:
                        break;
                    case 1:
                    case 2:
                        if (var2 == 0.0D) {
                            if (this.mode == 2) {
                                LXMidiDeviceBackport.this.output.sendNoteOff(
                                        this.channel, this.number, 0);
                            }
                        } else if (this.value == -1) {
                            LXMidiDeviceBackport.this.output.sendNoteOn(
                                    this.channel,
                                    this.number,
                                    (int) LXUtils.constrain(var2, 0.0D, 127.0D));
                        } else {
                            LXMidiDeviceBackport.this.output.sendNoteOn(
                                    this.channel, this.number, this.value);
                        }
                        break;
                    case 3:
                    case 4:
                        DiscreteParameter var4 = (DiscreteParameter) this.parameter;
                        if (var4.getValuei() == this.value) {
                            LXMidiDeviceBackport.this.output.sendNoteOn(
                                    this.channel, this.number, 127);
                        } else {
                            LXMidiDeviceBackport.this.output.sendNoteOff(
                                    this.channel, this.number, 0);
                        }
                }
            }
        }
    }

    private abstract class Binding implements LXParameterListener {
        protected final LXParameter parameter;
        protected final boolean isListening;

        private Binding(LXParameter var2) {
            if (var2 == null) {
                throw new IllegalArgumentException("Cannot bind to null parameter");
            } else {
                this.parameter = var2;
                if (LXMidiDeviceBackport.this.output != null
                        && var2 instanceof LXListenableParameter) {
                    ((LXListenableParameter) var2).addListener(this);
                    this.isListening = true;
                } else {
                    this.isListening = false;
                }
            }
        }

        protected void unbind() {
            if (LXMidiDeviceBackport.this.output != null
                    && this.parameter instanceof LXListenableParameter) {
                ((LXListenableParameter) this.parameter).removeListener(this);
            }
        }

        protected void assertChannel(int var1) {
            if (var1 < 0 || var1 >= 16) {
                throw new IllegalArgumentException("Invalid MIDI channel: " + var1);
            }
        }

        protected void assertValue(int var1) {
            if (var1 < 0 || var1 >= 128) {
                throw new IllegalArgumentException("Invalid MIDI value: " + var1);
            }
        }
    }

    public static enum TakeoverMode {
        TAKEOVER,
        PICKUP;

        private TakeoverMode() {}
    }

    // Stolen from olde version APC40 VV
    private LXChannel deviceControlChannel = null;
    private final LXChannel.Listener deviceControlListener;

    public LXMidiDeviceBackport bindDeviceControlKnobs(final LXEngine var1) {
        var1.mixer.focusedChannel.addListener(
                new LXParameterListener() {
                    public void onParameterChanged(LXParameter var1x) {
                        bindDeviceControlKnobs((LXChannel) var1.mixer.getFocusedChannel());
                    }
                });
        bindDeviceControlKnobs((LXChannel) var1.mixer.getFocusedChannel());
        return this;
    }

    public LXMidiDeviceBackport bindDeviceControlKnobs(LXChannel var1) {
        if (this.deviceControlChannel != var1) {
            if (this.deviceControlChannel != null) {
                this.deviceControlChannel.removeListener(this.deviceControlListener);
            }

            this.deviceControlChannel = var1;
            this.deviceControlChannel.addListener(this.deviceControlListener);
        }

        bindDeviceControlKnobs(var1.getActivePattern());
        return this;
    }

    public LXMidiDeviceBackport bindDeviceControlKnobs(LXPattern var1) {
        int var2 = 0;
        Iterator var3 = var1.getParameters().iterator();

        while (var3.hasNext()) {
            LXParameter var4 = (LXParameter) var3.next();
            if (var4 instanceof LXListenableNormalizedParameter) {
                this.bindController(var4, 0, 16 + var2);
                ++var2;
                if (var2 >= 8) {
                    break;
                }
            }
        }

        while (var2 < 8) {
            this.unbindController(0, 16 + var2);
            this.sendController(0, 16 + var2, 0);
            ++var2;
        }

        return this;
    }
}
