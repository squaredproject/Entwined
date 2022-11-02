package entwined.pattern.irene_zhou;

//class MirageEffect extends ModelTransform {
//  final BoundedParameter amplitude  = new BoundedParameter("AMP", 0, 0, 0.5);
//  final SinLFO ripple = new SinLFO(0, 1, 300);
//  final SawLFO rotate = new SawLFO(0, 360, 6000);
//
//  MirageEffect(LX lx) {
//    super(lx);
//    addModulator(ripple.start());
//  }
//
//  void transform(Model model) {
//    for (BaseCube cube: model.baseCubes) {
//      cube.transformedY = cube.transformedY * ( 1 - ripple.getValuef() * amplitude.getValuef() * Utils.sin((cube.transformedTheta + rotate) / 30 * Utils.PI ));
//    }
//  }
//}



// class Ripple extends LXPattern {
//   final BoundedParameter speed = new BoundedParameter("Speed", 15000, 25000, 8000);
//   final BoundedParameter baseBrightness = new BoundedParameter("Bright", 0, 0, 100);
//   final SawLFO rippleAge = new SawLFO(0, 100, speed);
//   float hueVal;
//   float brightVal;
//   boolean resetDone = false;
//   float yCenter;
//   float thetaCenter;
//   Ripple(LX lx) {
//     super(lx);
//     addParameter(speed);
//     addParameter(baseBrightness);
//     addModulator(rippleAge.start());
//   }

//   public void run(double deltaMs) {
//     if (getChannel().getFader().getNormalized() == 0) return;

//     if (rippleAge.getValuef() < 5){
//       if (!resetDone){
//         yCenter = 150 + Utils.random(300);
//         thetaCenter = Utils.random(360);
//         resetDone = true;
//       }
//     }
//     else {
//       resetDone = false;
//     }
//     float radius = Utils.pow(rippleAge.getValuef(), 2) / 3;
//     for (BaseCube cube : model.baseCubes) {
//       float distVal = Utils.sqrt(Utils.pow((LXUtils.wrapdistf(thetaCenter, cube.transformedTheta, 360)) * 0.8f, 2) + Utils.pow(yCenter - cube.transformedY, 2));
//       float heightHueVariance = 0.1f * cube.transformedY;
//       if (distVal < radius){
//         float rippleDecayFactor = (100 - rippleAge.getValuef()) / 100;
//         float timeDistanceCombination = distVal / 20 - rippleAge.getValuef();
//         hueVal = (lx.getBaseHuef() + 40 * Utils.sin(Utils.TWO_PI * (12.5f + rippleAge.getValuef() )/ 200) * rippleDecayFactor * Utils.sin(timeDistanceCombination) + heightHueVariance + 360) % 360;
//         brightVal = Utils.constrain((baseBrightness.getValuef() + rippleDecayFactor * (100 - baseBrightness.getValuef()) + 80 * rippleDecayFactor * Utils.sin(timeDistanceCombination + Utils.TWO_PI / 8)), 0, 100);
//       }
//       else {
//         hueVal = (lx.getBaseHuef() + heightHueVariance) % 360;
//         brightVal = baseBrightness.getValuef();
//       }
//       colors[cube.index] = lx.hsb(hueVal,  100, brightVal);
//     }
//   }
// }

// class Ripples extends LXPattern {
//   Ripples(LX lx) {
//     super(lx);
//   }

//   public void run(double deltaMs) {

//   }
// }
