package entwined.pattern.kyle_fleming;

import entwined.core.MultiObject;
import entwined.core.MultiObjectPattern;
import entwined.utils.EntwinedUtils;
import entwined.utils.Vec2D;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.utils.LXUtils;

public class Wisps extends MultiObjectPattern<Wisp> {

  final CompoundParameter baseColor = new CompoundParameter("COLR", 210, 360);
  final CompoundParameter colorVariability = new CompoundParameter("CVAR", 10, 180);
  final CompoundParameter direction = new CompoundParameter("DIR", 90, 360);
  final CompoundParameter directionVariability = new CompoundParameter("DVAR", 20, 180);
  final CompoundParameter thickness = new CompoundParameter("WIDT", 3.5f, 1, 20).setExponent(2);
  final CompoundParameter speed = new CompoundParameter("SPEE", 10, 1, 20).setExponent(2);
  // Possible other parameters:
  //  Distance
  //  Distance variability
  //  width variability
  //  Speed variability
  //  frequency variability
  //  Fade time

  public Wisps(LX lx) {
    this(lx, .5f, 210, 10, 90, 20, 3.5f, 10);
  }

  public Wisps(LX lx, double initial_frequency, double initial_color,
        double initial_colorVariability, double initial_direction,
        double initial_directionVariability, double initial_thickness,
        double initial_speed) {
    super(lx, initial_frequency);

    addParameter("color", baseColor);
    addParameter("colorVariabilty", colorVariability);
    addParameter("direction", direction);
    addParameter("directionVariablity", directionVariability);
    addParameter("thickness", thickness);
    addParameter("speed", speed);

    baseColor.setValue(initial_color);
    colorVariability.setValue(initial_colorVariability);
    direction.setValue(initial_direction);
    directionVariability.setValue(initial_directionVariability);
    thickness.setValue(initial_thickness);
    speed.setValue(initial_speed);

  };

  @Override
  protected Wisp generateObject(float strength) {
    Wisp wisp = new Wisp(lx);
    wisp.setRunningTimerEnd(5000 / speed.getValuef());
    float pathDirection = (float)(direction.getValuef()
      + LXUtils.random(-directionVariability.getValuef(), directionVariability.getValuef())) % 360;
    float pathDist = EntwinedUtils.random(200, 400);
    float startTheta = EntwinedUtils.random(360);
    float startY = EntwinedUtils.random(EntwinedUtils.max(model.yMin, model.yMin - pathDist * EntwinedUtils.sin(LX.PIf * pathDirection / 180)),
      EntwinedUtils.min(model.yMax, model.yMax - pathDist * EntwinedUtils.sin(LX.PIf * pathDirection / 180)));
    wisp.startPoint = new Vec2D(startTheta, startY);
    wisp.endPoint = Vec2D.fromTheta(pathDirection * LX.PIf / 180);
    wisp.endPoint.scaleSelf(pathDist);
    wisp.endPoint.addSelf(wisp.startPoint);
    wisp.setHue((int)(baseColor.getValuef()
      + LXUtils.random(-colorVariability.getValuef(), colorVariability.getValuef())) % 360);
    wisp.setThickness(10 * thickness.getValuef() + EntwinedUtils.random(-3, 3));

    return wisp;
  }
}

class Wisp extends MultiObject {

  Vec2D startPoint;
  Vec2D endPoint;

  Wisp(LX lx) {
    super(lx);
  }

  @Override
  public void onProgressChanged(float progress) {
    currentPoint = startPoint.interpolateTo(endPoint, progress);
  }
}
