package com.charlesgadeken.entwined.triggers.drumpad;

public interface Drumpad {
    void padTriggered(int row, int col, float velocity);
    void padReleased(int row, int col);
}
