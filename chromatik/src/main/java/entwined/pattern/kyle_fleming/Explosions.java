package entwined.pattern.kyle_fleming;

import java.util.ArrayList;

import entwined.core.CubeManager;
import entwined.core.MultiObject;
import entwined.core.MultiObjectPattern;
import entwined.utils.EntwinedUtils;
import entwined.utils.Vec2D;
import entwined.utils.VecUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.utils.LXUtils;

public class Explosions extends MultiObjectPattern<Explosion> {

  ArrayList<Explosion> explosions;

  public Explosions(LX lx) {
    this(lx, 0.5f);
  }

  public Explosions(LX lx, double speed) {
    super(lx, false);

    explosions = new ArrayList<Explosion>();
    frequency.setValue(speed);
  }

  @Override
  protected BoundedParameter getFrequencyParameter() {
    return new BoundedParameter("FREQ", .50, .1, 20).setExponent(2);
  }

  @Override
  protected Explosion generateObject(float strength) {
    Explosion explosion = new Explosion(lx);
    explosion.origin = new Vec2D(EntwinedUtils.random(360), (float)LXUtils.random(model.yMin + 50, model.yMax - 50));
    explosion.setHue((int) EntwinedUtils.random(360));
    return explosion;
  }
}

class Explosion extends MultiObject {

  final static int EXPLOSION_STATE_IMPLOSION_EXPAND = 1 << 0;
  final static int EXPLOSION_STATE_IMPLOSION_WAIT = 1 << 1;
  final static int EXPLOSION_STATE_IMPLOSION_CONTRACT = 1 << 2;
  final static int EXPLOSION_STATE_EXPLOSION = 1 << 3;

  Vec2D origin;

  float accelOfImplosion = 3000;
  Accelerator implosionRadius;
  float implosionWaitTimer = 100;
  Accelerator explosionRadius;
  LXModulator explosionFade;
  float explosionThetaOffset;

  int state = EXPLOSION_STATE_IMPLOSION_EXPAND;

  Explosion(LX lx) {
    super(lx);
  }

  @Override
  public void init() {
    explosionThetaOffset = EntwinedUtils.random(360);
    implosionRadius = new Accelerator(0, 700, -accelOfImplosion);
    addModulator(implosionRadius).start();
    explosionFade = new LinearEnvelope(1, 0, 1000);
  }

  @Override
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

  @Override
  public float getBrightnessForCube(LXPoint cube) {
    Vec2D cubePointPrime = VecUtils.movePointToSamePlane(origin, CubeManager.getCube(lx, cube.index).cylinderPoint);
    float dist = origin.distanceTo(cubePointPrime);
    switch (state) {
      case EXPLOSION_STATE_IMPLOSION_EXPAND:
      case EXPLOSION_STATE_IMPLOSION_WAIT:
      case EXPLOSION_STATE_IMPLOSION_CONTRACT:
        return 100 * LXUtils.constrainf((implosionRadius.getValuef() - dist) / 10, 0, 1);
      default:
        float theta = explosionThetaOffset + cubePointPrime.sub(origin).heading() * 180 / LX.PIf + 360;
        return 100
            * LXUtils.constrainf(1 - (dist - explosionRadius.getValuef()) / 10, 0, 1)
            * LXUtils.constrainf(1 - (explosionRadius.getValuef() - dist) / 200, 0, 1)
            * LXUtils.constrainf((1 - EntwinedUtils.abs(theta % 30 - 15) / 100 / EntwinedUtils.asin(20 / EntwinedUtils.max(20, dist))), 0, 1)
            * explosionFade.getValuef();
    }
  }

}
