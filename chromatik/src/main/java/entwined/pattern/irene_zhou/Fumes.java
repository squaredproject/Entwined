package entwined.pattern.irene_zhou;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import entwined.utils.Vec2D;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

public class Fumes extends LXPattern {
  final CompoundParameter speed = new CompoundParameter("SPEED", 2, 0, 20);
  final CompoundParameter hue = new CompoundParameter("HUE", 0, 0, 360);
  final CompoundParameter sat = new CompoundParameter("SAT", 25, 0, 100);
  final int NUM_SITES = 15;
  private Site[] sites = new Site[NUM_SITES];

  private class Site {
    public float theta = 0;
    public float yPos = 0;
    public Vec2D velocity = new Vec2D(0,0);

    public Site() {
      theta = EntwinedUtils.random(0, 360);
      yPos = EntwinedUtils.random(model.yMin, model.yMax);
      velocity = new Vec2D(EntwinedUtils.random(0,1), EntwinedUtils.random(0,0.75f));
    }

    public void move(float speed) {
      theta = (theta + speed * velocity.x) % 360;
      yPos += speed * velocity.y;
      if (yPos < model.yMin - 50) {
        velocity.y *= -1;
      }
      if (yPos > model.yMax + 50) {
        yPos = model.yMin - 50;
      }
    }
  }

  public Fumes(LX lx) {
    super(lx);
    addParameter("hue", hue);
    addParameter("speed", speed);
    addParameter("saturation", sat);
    for (int i = 0; i < sites.length; ++i) {
      sites[i] = new Site();
    }
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    float minSat = sat.getValuef();
    for (LXPoint cube : model.points) {
      float minDistSq = 1000000;
      float nextMinDistSq = 1000000;
      CubeData cdata = CubeManager.getCube(lx, cube.index);
      for (int i = 0; i < sites.length; ++i) {
        if (EntwinedUtils.abs(sites[i].yPos - cdata.localY) < 150) { //restraint on calculation
          float distSq = EntwinedUtils.pow((LXUtils.wrapdistf(sites[i].theta, cdata.localTheta, 360)), 2) + EntwinedUtils.pow(sites[i].yPos - cdata.localTheta, 2);
          if (distSq < nextMinDistSq) {
            if (distSq < minDistSq) {
              nextMinDistSq = minDistSq;
              minDistSq = distSq;
            } else {
              nextMinDistSq = distSq;
            }
          }
        }
      }
      float brt = EntwinedUtils.max(0, 100 - EntwinedUtils.sqrt(nextMinDistSq));
      colors[cube.index] = LX.hsb(
        (currentBaseHue + hue.getValuef()) % 360,
        100 - EntwinedUtils.min( minSat, brt),
        brt
      );
    }
    for (Site site: sites) {
      site.move(speed.getValuef() * (float)deltaMs * 60 / 1000);
    }
  }
}

