package com.charlesgadeken.entwined;

import heronarts.lx.LXEngine;

public class FakeLXAutomationRecorder {
    FakeLXAutomationRecorder(LXEngine engine){}
    protected void onStart() {}
    public void start() {}
    public void loop(double deltaMs) {}
    public boolean isRunning() {return true;}
}
