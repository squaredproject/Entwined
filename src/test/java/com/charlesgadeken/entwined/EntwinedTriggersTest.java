package com.charlesgadeken.entwined;

import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntwinedTriggersTest {
    @Test
    public void testEntwinedTriggers(){
        LX lx = new LX();
        BasicParameterProxy bp = new BasicParameterProxy(1);
        EntwinedTriggers et = new EntwinedTriggers(lx, bp);
        et.configureMIDI();
    }

}