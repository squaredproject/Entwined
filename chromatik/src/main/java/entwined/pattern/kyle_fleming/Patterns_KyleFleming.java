package entwined.pattern.kyle_fleming;

import java.util.ArrayList;
import java.util.Iterator;

import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.utils.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.LXLayer;

import entwined.utils.Vec2D;
import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;

import entwined.utils.PerlinNoise;
import entwined.utils.SimplexNoise;







/*
class RotationEffect extends ModelTransform {

  final BoundedParameter rotation = new BoundedParameter("ROT", 0, 0, 360);

  RotationEffect(LX lx) {
    super(lx);
  }

  void transform(LXModel model) {
    if (rotation.getValue() > 0) {
      float rotationTheta = rotation.getValuef();
      for (LXPoint cube : model.points) {
        cube.transformedTheta = (cube.transformedTheta + 360 - rotationTheta) % 360;
      }
    }
  }
}
*/

