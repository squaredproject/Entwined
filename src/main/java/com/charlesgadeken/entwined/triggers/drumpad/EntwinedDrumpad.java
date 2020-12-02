package com.charlesgadeken.entwined.triggers.drumpad;

import com.charlesgadeken.entwined.triggers.Triggerable;
import java.util.ArrayList;

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

    public static class Builder {
        ArrayList<Triggerable>[] apc40DrumpadTriggerablesLists;

        public Builder() {
            apc40DrumpadTriggerablesLists =
                    new ArrayList[] {
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>()
                    };
        }

        public void addTriggerableToRow(int row, Triggerable triggerable) {
            apc40DrumpadTriggerablesLists[row].add(triggerable);
        }

        public EntwinedDrumpad build() {
            Triggerable[][] apc40DrumpadTriggerables =
                    new Triggerable[apc40DrumpadTriggerablesLists.length][];
            for (int i = 0; i < apc40DrumpadTriggerablesLists.length; i++) {
                ArrayList<Triggerable> triggerablesList = apc40DrumpadTriggerablesLists[i];
                apc40DrumpadTriggerables[i] = triggerablesList.toArray(new Triggerable[0]);
            }

            return new EntwinedDrumpad(apc40DrumpadTriggerables);
        }
    }
}
