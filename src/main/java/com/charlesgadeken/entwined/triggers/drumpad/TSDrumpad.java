package com.charlesgadeken.entwined.triggers.drumpad;

import com.charlesgadeken.entwined.triggers.Triggerable;

public class TSDrumpad implements Drumpad {
    Triggerable[][] triggerables = null;

    public void padTriggered(int row, int col, float velocity) {
        if (triggerables != null && row < triggerables.length && col < triggerables[row].length) {
            triggerables[row][col].onTriggered(velocity);
        }
    }

    public void padReleased(int row, int col) {
        if (triggerables != null && row < triggerables.length && col < triggerables[row].length) {
            triggerables[row][col].onRelease();
        }
    }
}
