package com.charlesgadeken.entwined.effects.original;

import com.charlesgadeken.entwined.EntwinedCategory;
import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedBaseEffect;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory(EntwinedCategory.ORIGINAL)
public class ColorEffect extends EntwinedBaseEffect {

    final BoundedParameter desaturation = new BoundedParameter("WHT", 0);
    final BoundedParameter hueShift = new BoundedParameter("HUE", 0, 360);
    final BoundedParameter sharp = new BoundedParameter("SHRP", 0);
    final BoundedParameter soft = new BoundedParameter("SOFT", 0);
    final BoundedParameter mono = new BoundedParameter("MON", 0);
    final BoundedParameter rainbow = new BoundedParameter("ACID", 0);

    private final DampedParameter hueShiftd = new DampedParameter(hueShift, 180);
    private final DampedParameter rainbowd = new DampedParameter(rainbow, 1);

    private float[] hsb = new float[3];

    public ColorEffect(LX lx) {
      super(lx);
      addParameter(desaturation);
      addParameter(hueShift);
      addParameter(sharp);
      addParameter(soft);
      addParameter(mono);
      addParameter(rainbow);

      addModulator(hueShiftd).start();
      addModulator(rainbowd).start();
    }

    @Override
    public void run(double deltaMs, double enabledAmount) {
      float desatf = desaturation.getValuef();
      float huef = hueShiftd.getValuef();
      float sharpf = sharp.getValuef();
      float softf = soft.getValuef();
      float monof = mono.getValuef();
      float rainbowf = rainbowd.getValuef();
      if (desatf > 0 || huef > 0 || sharpf > 0 || softf > 0 || monof > 0 || rainbowf > 0) {
        float pSharp = 1/(1-.99f*sharpf);
        for (int i = 0; i < colors.length; ++i) {
          float b = LXColor.b(colors[i]) / 100.f;
          float bOrig = b;
          if (sharpf > 0) {
            if (b < .5f) {
              b = Utilities.pow(b, pSharp);
            } else {
              b = 1-Utilities.pow(1-b, pSharp);
            }
          }
          if (softf > 0) {
            if (b > 0.5f) {
              b = Utilities.lerp(b, 0.5f + 2 * (b-0.5f)*(b-0.5f), softf);
            } else {
              b = Utilities.lerp(b, 0.5f * Utilities.sqrt(2*b), softf);
            }
          }

          float h = LXColor.h(colors[i]);
          float bh = lx.engine.palette.getHuef();
          if (rainbowf > 0) {
            h = bh + (h - bh) * (1+3*rainbowf);
            h = (h + 5*360) % 360;
          }
          if (Utilities.abs(h - bh) > 180) {
            if (h > bh) {
              bh += 360;
            } else {
              h += 360;
            }
          }

          colors[i] = LX.hsb(
              (Utilities.lerp(h, bh, monof) + huef) % 360,
              LXColor.s(colors[i]) * (1 - desatf),
              100*b
          );
        }
      }
    }
}
