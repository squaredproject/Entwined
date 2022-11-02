package entwined.pattern.anon;




/*

class Twister extends TSPattern {

  final SinLFO spin = new SinLFO(0, 5*360, 16000);

  float coil(float basis) {
    return Utils.sin(basis*Utils.TWO_PI - Utils.PI);
  }

  Twister(LX lx) {
    super(lx);
    addModulator(spin).start();
  }

  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    float spinf = spin.getValuef();
    float coilf = 2*coil(spin.getBasisf());
    for (LXPoint cube: model.points) {
      float wrapdist = LXUtils.wrapdistf(cube.transformedTheta, spinf + (model.yMax - cube.transformedY)*coilf, 360);
      float yn = (cube.transformedY / model.yMax);
      float width = 10 + 30 * yn;
      float df = Utils.max(0, 100 - (100 / 45) * Utils.max(0, wrapdist-width));
      colors[cube.index] = lx.hsb(
        (lx.getBaseHuef() + .2f*cube.transformedY - 360 - wrapdist) % 360,
        Utils.max(0, 100 - 500*Utils.max(0, yn-.8f)),
        df
      );
    }
  }
}
*/












// Three settings (?):
// which hue to use as the center
// how "wide" to make it
// consider: "squish" vs "push"

// Problem: it would be nice, especially on certain holidays, to have a filter that gives the entire sculpture
// a certain color. Valentine's day, St Patrics Day, Halloween, Christmas.... all have colors
// One option today is to put a Solid Color and Multiply - but this means the entire sculpture becomes
// that one color. We'd rather have white come through as white - which is why doing the filter in HSV would give
// a better result. Next question is how sophisticated to get - you could convolve with a sin curve (which)
//
// Need a way to enable and disable the effect. Not sure how to do that yet.
//
// First: let's just set the hue to a color, period. That should leave white and black where they are, because
// they aren't touched by Hue.
// Second: let's try having an "angle" (like 15 degrees) and everything outside that gets nailed to the hue in that range
// artist seems OK with the concept of "hue" and "angle", which gives us two real controls.
// We want DEG to be "transparent" at 0, and most at 180 (which is kinda backward)
// Author: Brian Bulkowski 2021 brian@bulkowsk.org




