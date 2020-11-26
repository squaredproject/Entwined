package com.charlesgadeken.entwined;

import heronarts.lx.LX;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.DDPDatagram;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntwinedGuiTest {
  @Test
  void setup() {
      LX.Flags flags = EntwinedGui.headlessInit(null);
      flags.initialize.initialize(new LX());
  }
}
