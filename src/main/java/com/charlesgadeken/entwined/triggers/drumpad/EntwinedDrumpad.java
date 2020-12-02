package com.charlesgadeken.entwined.triggers.drumpad;

import com.charlesgadeken.entwined.triggers.Triggerable;

public class EntwinedDrumpad implements Drumpad {
    private final Triggerable[][] triggerables;

    public EntwinedDrumpad(Triggerable[][] triggerables) {
        this.triggerables = triggerables;
        for (int i = 0; i < triggerables.length; i++) {
            for (int j = 0; j < triggerables[i].length; j++) {
                Triggerable t = triggerables[i][j];
                if (t == null) {
                    continue;
                }

                System.out.printf("Triggerable %s registered to drumpad (%d, %d)\n", t, i, j);
            }
        }
    }

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
