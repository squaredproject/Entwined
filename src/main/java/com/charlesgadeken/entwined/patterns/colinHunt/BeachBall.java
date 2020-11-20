package com.charlesgadeken.entwined.patterns.colinHunt;

import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.model.Tree;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Colin Hunt")
public class BeachBall extends EntwinedBasePattern {

  // Variable Declarations go here
  private float treex;
  private float treez;

  private Tree theTree;

  final BoundedParameter speed = new BoundedParameter("Speed", 5000, 20000, 1000);
  final BoundedParameter swirlMult = new BoundedParameter("Swirl", .5, 2, .1);
  final SawLFO spinner = new SawLFO(0, 360, speed);

  // Constructor
  public BeachBall(LX lx) {
    super(lx);

    addModulator(spinner).start();
    addParameter("colinHunt/beachBall/speed", speed);
    addParameter("colinHunt/beachBall/swirlMult", swirlMult);

    theTree = model.trees.get(0);
    treex = theTree.x;
    treez = theTree.z;

  }


  @Override
  public void run(double deltaMs) {
    for (BaseCube baseCube : model.baseCubes) {
      colors[baseCube.index] = lx.hsb(
          // Color is based on degrees from the center point, plus the spinner saw wave to rotate
          (float) Math.toDegrees(Math.atan2((double)(treez - baseCube.z), (double)(treex - baseCube.x))) + spinner.getValuef()
              // plus the further from the center, the more hue is added, giving a swirl effect
              - (float)(Math.hypot(treez - baseCube.z, treex - baseCube.x) * swirlMult.getValuef()),
          100.0f,
          100.0f);
    }
  }
}
