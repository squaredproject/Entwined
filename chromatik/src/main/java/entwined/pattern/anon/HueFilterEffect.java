package entwined.pattern.anon;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.CompoundParameter;

public class HueFilterEffect extends LXEffect {

  public final CompoundParameter hueFilter = new CompoundParameter("HUEF", 0, 360); // 0 to 360 starting at 0
  public final CompoundParameter amount = new CompoundParameter("HDEG", 0, 180);

  //private float[] hsb = new float[3];

  public HueFilterEffect(LX lx) {
    super(lx);
    addParameter("hue_filter", hueFilter);
    addParameter("amount", amount);
  }

  static float norm360(float i) {
    while (i < 0.0f) {
      i += 360.0f;
    }
    while (i > 360.0f) {
      i -= 360.0f;
    }
    return(i);
  }


  // distance between a and b in degrees, absolute
  static float absdist360(float a, float b) {
    float r = Math.abs( a - b );
    if (r < 180.0f) return(r);
    return( 360.0f - r );
  }

  // distance between a and b in degrees, negative means a is counterclockwise
  // so for example a = 0, b = 190, the distance is 170, but postive, because the short path is clockwise
  static float dist360(float a, float b) {
    float r = a - b;
    if (Math.abs(r) <= 180.0f) return(r);
    if (r < 0.0f) return( r + 360.0f );
    return( r - 360.0f );
  }


  // quick bit of math: interpolate on the hue circle
  // but do so with a limit. Make sure the color is never outside of
  // a certain number of degress. There's some sublty in when/how to clip the
  // edges, so will try a few things.
  // Interesting, having a limitdeg of 180 means no effect, that's the blend (basically)
  static float hueBlend(float src, float dst, float limitDeg) {
    float r;
    float dist = dist360(src,dst);
    r = dst + ( (dist / 180.0f) * limitDeg );
    r = norm360(r);

    // test: output should be within the limit distance of dst
    if ( absdist360(r, dst) > limitDeg ) {
      System.out.println("HueFilterEffect: hueBlendFail: src "+src+" dst "+dst+" res "+r+" limit "+limitDeg);
    }

    return(r);
  }

  @Override
  protected void run(double deltaMs, double enabledAmount) {  // XXX would be the amountf?
    float huef = hueFilter.getValuef();
    float amountf = amount.getValuef();
    amountf = Math.abs(amountf - 180f);

    // todo: if enabled

    for (int i = 0; i < colors.length; ++i) {
        // float h = lerp360(LXColor.h(colors[i]), huef, amountf);

        float h = hueBlend(LXColor.h(colors[i]), huef, amountf);

        colors[i] = LX.hsb( h, LXColor.s(colors[i]), LXColor.b(colors[i]) );
    }
  }
}


