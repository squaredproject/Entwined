package entwined.pattern.anon;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import entwined.utils.EntwinedUtils;

public class ColorEffect extends LXEffect {

  public final CompoundParameter desaturation = new CompoundParameter("WHT", 0);
  public final CompoundParameter hueShift = new CompoundParameter("HUE", 0, 360);
  final CompoundParameter sharp = new CompoundParameter("SHRP", 0);
  final CompoundParameter soft = new CompoundParameter("SOFT", 0);
  public final CompoundParameter mono = new CompoundParameter("MON", 0);
  final CompoundParameter rainbow = new CompoundParameter("ACID", 0);

  private final DampedParameter hueShiftd = new DampedParameter(hueShift, 180);
  private final DampedParameter rainbowd = new DampedParameter(rainbow, 1);

  // private float[] hsb = new float[3];

  // if set to a value >= 0, the effects are limited to only
  // the piece with that index (used for interactive effects)
  // XXX so how is this ever set?
  // protected int pieceIndex = -1;

  public ColorEffect(LX lx) {
    super(lx);
    addParameter("desaturation", desaturation);
    addParameter("hueShift", hueShift);
    addParameter("sharp", sharp);
    addParameter("soft", soft);
    addParameter("mono", mono);
    addParameter("rainbow", rainbow);

    addModulator(hueShiftd).start();
    addModulator(rainbowd).start();
  }

  @Override
  protected void run(double deltaMs, double enabledAmount) {
    float desatf = desaturation.getValuef();
    float huef = hueShiftd.getValuef();
    float sharpf = sharp.getValuef();
    float softf = soft.getValuef();
    float monof = mono.getValuef();
    float rainbowf = rainbowd.getValuef();
    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    if (desatf > 0 || huef > 0 || sharpf > 0 || softf > 0 || monof > 0 || rainbowf > 0) {
      float pSharp = 1/(1-.99f*sharpf);
      int componentIdx = 0;
      for (LXModel component : model.children) {
        //if (pieceIndex < 0 || componentIdx == pieceIndex) {
          for (LXPoint cube : component.points) {
            float b = LXColor.b(colors[cube.index]) / 100.f;
            float bOrig = b;
            if (sharpf > 0) {
              if (b < .5f) {
                b = EntwinedUtils.pow(b, pSharp);
              } else {
                b = 1-EntwinedUtils.pow(1-b, pSharp);
              }
            }
            if (softf > 0) {
              if (b > 0.5f) {
                b = EntwinedUtils.lerp(b, 0.5f + 2 * (b-0.5f)*(b-0.5f), softf);
              } else {
                b = EntwinedUtils.lerp(b, 0.5f * EntwinedUtils.sqrt(2*b), softf);
              }
            }

            float h = LXColor.h(colors[cube.index]);
            float bh = currentBaseHue;
            if (rainbowf > 0) {
              h = bh + (h - bh) * (1+3*rainbowf);
              h = (h + 5*360) % 360;
            }
            if (Math.abs(h - bh) > 180) {
              if (h > bh) {
                bh += 360;
              } else {
                h += 360;
              }
            }

            colors[cube.index] = LX.hsb(
              (EntwinedUtils.lerp(h, bh, monof) + huef) % 360,
              LXColor.s(colors[cube.index]) * (1 - desatf),
              100*b
            );
          }
        //}
        //componentIdx++;
      }
    }
  }
}

