package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedBaseEffect;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;
import java.util.ArrayList;
import java.util.Iterator;

public class GhostEffect extends EntwinedBaseEffect {
    final BoundedParameter amount =
            new BoundedParameter(
                    "GHOS", 0, 0, 1); // TODO(meawoppl), BoundedParameter.Scaling.QUAD_IN);

    public GhostEffect(LX lx) {
        super(lx);
        addLayer(new GhostEffectsLayer(lx));
    }

    protected void run(double deltaMs, double unused) {}

    class GhostEffectsLayer extends Layer {

        GhostEffectsLayer(LX lx) {
            super(lx);
            addParameter(amount);
        }

        float timer = 0;
        ArrayList<GhostEffectLayer> ghosts = new ArrayList<GhostEffectLayer>();

        public void run(double deltaMs) {
            if (amount.getValue() != 0) {
                timer += deltaMs;
                float lifetime = (float) amount.getValue() * 2000;
                if (timer >= lifetime) {
                    timer = 0;
                    GhostEffectLayer ghost = new GhostEffectLayer(lx);
                    ghost.lifetime = lifetime * 3;
                    addLayer(ghost);
                    ghosts.add(ghost);
                }
            }
            if (ghosts.size() > 0) {
                Iterator<GhostEffectLayer> iter = ghosts.iterator();
                while (iter.hasNext()) {
                    GhostEffectLayer ghost = iter.next();
                    if (!ghost.running) {
                        layers.remove(ghost);
                        iter.remove();
                    }
                }
            }
        }

        public void onParameterChanged(LXParameter parameter) {
            if (parameter == amount && parameter.getValue() == 0) {
                timer = 0;
            }
        }
    }

    class GhostEffectLayer extends Layer {

        float lifetime;
        boolean running = true;

        private int[] ghostColors = null;
        float timer = 0;

        GhostEffectLayer(LX lx) {
            super(lx);
        }

        public void run(double deltaMs) {
            if (running) {
                timer += (float) deltaMs;
                if (timer >= lifetime) {
                    running = false;
                } else {
                    if (ghostColors == null) {
                        ghostColors = new int[colors.length];
                        for (int i = 0; i < colors.length; i++) {
                            ghostColors[i] = colors[i];
                        }
                    }

                    for (int i = 0; i < colors.length; i++) {
                        ghostColors[i] =
                                LXColor.blend(
                                        ghostColors[i],
                                        lx.hsb(
                                                0,
                                                0,
                                                100
                                                        * Utilities.max(
                                                                0,
                                                                (float) (1 - deltaMs / lifetime))),
                                        LXColor.Blend.MULTIPLY);
                        blendColor(i, ghostColors[i], LXColor.Blend.LIGHTEST);
                    }
                }
            }
        }
    }
}
