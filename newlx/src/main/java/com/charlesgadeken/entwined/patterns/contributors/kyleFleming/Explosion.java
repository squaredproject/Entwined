package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import heronarts.lx.LX;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.utils.LXUtils;
import toxi.geom.Vec2D;

public class Explosion extends MultiObject {

    static final int EXPLOSION_STATE_IMPLOSION_EXPAND = 1 << 0;
    static final int EXPLOSION_STATE_IMPLOSION_WAIT = 1 << 1;
    static final int EXPLOSION_STATE_IMPLOSION_CONTRACT = 1 << 2;
    static final int EXPLOSION_STATE_EXPLOSION = 1 << 3;

    Vec2D origin;

    float accelOfImplosion = 3000;
    Accelerator implosionRadius;
    float implosionWaitTimer = 100;
    Accelerator explosionRadius;
    LXModulator explosionFade;
    float explosionThetaOffset;

    int state = EXPLOSION_STATE_IMPLOSION_EXPAND;

    public Explosion(LX lx) {
        super(lx);
    }

    void init() {
        explosionThetaOffset = Utilities.random(360);
        implosionRadius = new Accelerator(0, 700, -accelOfImplosion);
        addModulator(implosionRadius).start();
        explosionFade = new LinearEnvelope(1, 0, 1000);
    }

    protected void advance(double deltaMs) {
        switch (state) {
            case EXPLOSION_STATE_IMPLOSION_EXPAND:
                if (implosionRadius.getVelocityf() <= 0) {
                    state = EXPLOSION_STATE_IMPLOSION_WAIT;
                    implosionRadius.stop();
                }
                break;
            case EXPLOSION_STATE_IMPLOSION_WAIT:
                implosionWaitTimer -= deltaMs;
                if (implosionWaitTimer <= 0) {
                    state = EXPLOSION_STATE_IMPLOSION_CONTRACT;
                    implosionRadius.setAcceleration(-8000);
                    implosionRadius.start();
                }
                break;
            case EXPLOSION_STATE_IMPLOSION_CONTRACT:
                if (implosionRadius.getValuef() < 0) {
                    removeModulator(implosionRadius).stop();
                    state = EXPLOSION_STATE_EXPLOSION;
                    explosionRadius = new Accelerator(0, -implosionRadius.getVelocityf(), -300);
                    addModulator(explosionRadius).start();
                    addModulator(explosionFade).start();
                }
                break;
            default:
                if (explosionFade.getValuef() <= 0) {
                    running = false;
                    removeModulator(explosionRadius).stop();
                    removeModulator(explosionFade).stop();
                }
                break;
        }
    }

    public float getBrightnessForCube(BaseCube cube) {
        Vec2D cubePointPrime = VecUtils.movePointToSamePlane(origin, cube.transformedCylinderPoint);
        float dist = origin.distanceTo(cubePointPrime);
        switch (state) {
            case EXPLOSION_STATE_IMPLOSION_EXPAND:
            case EXPLOSION_STATE_IMPLOSION_WAIT:
            case EXPLOSION_STATE_IMPLOSION_CONTRACT:
                return 100 * LXUtils.constrainf((implosionRadius.getValuef() - dist) / 10, 0, 1);
            default:
                float theta =
                        explosionThetaOffset
                                + cubePointPrime.sub(origin).heading() * 180 / Utilities.PI
                                + 360;
                return 100
                        * LXUtils.constrainf(1 - (dist - explosionRadius.getValuef()) / 10, 0, 1)
                        * LXUtils.constrainf(1 - (explosionRadius.getValuef() - dist) / 200, 0, 1)
                        * LXUtils.constrainf(
                                (1
                                        - Utilities.abs(theta % 30 - 15)
                                                / 100
                                                / Utilities.asin(20 / Utilities.max(20, dist))),
                                0,
                                1)
                        * explosionFade.getValuef();
        }
    }
}
