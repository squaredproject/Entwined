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

public class Cells extends LXPattern {
  final CompoundParameter speed = new CompoundParameter("SPEED", 1, 0, 5);
  final CompoundParameter width = new CompoundParameter("WIDTH", 0.75, 0.5, 1.25);
  final CompoundParameter hue = new CompoundParameter("HUE", 0, 0, 360);
  final int NUM_SITES = 15;
  private Site[] sites = new Site[NUM_SITES];

  private class Site {
    public float theta = 0;
    public float yPos = 0;
    public Vec2D velocity = new Vec2D(0,0);

    public Site() {
      theta = EntwinedUtils.random(0, 360);
      yPos = EntwinedUtils.random(model.yMin, model.yMax);
      velocity = new Vec2D(EntwinedUtils.random(-1,1), EntwinedUtils.random(-1,1));
    }

    public void move(float speed) {
      theta = (theta + speed * velocity.x) % 360;
      yPos += speed * velocity.y;
      if ((yPos < model.yMin - 20) || (yPos > model.yMax + 20)) {
        velocity.y *= -1;
      }
    }
  }

  public Cells(LX lx) {
    super(lx);
    addParameter("speed", speed);
    addParameter("width", width);
    addParameter("hue", hue);
    for (int i = 0; i < sites.length; ++i) {
      sites[i] = new Site();
    }
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    for (LXPoint cube : model.points) {
      float minDistSq = 1000000;
      float nextMinDistSq = 1000000;
      CubeData cdata = CubeManager.getCube(lx, cube.index);
      for (int i = 0; i < sites.length; ++i) {
        if (EntwinedUtils.abs(sites[i].yPos - cdata.localY) < 150) { //restraint on calculation
          float distSq = EntwinedUtils.pow((LXUtils.wrapdistf(sites[i].theta, cdata.localTheta, 360)), 2) + EntwinedUtils.pow(sites[i].yPos - cdata.localY, 2);
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
      colors[cube.index] = LX.hsb(
        (currentBaseHue + hue.getValuef()) % 360,
        100,
        EntwinedUtils.max(0, EntwinedUtils.min(100, 100 - EntwinedUtils.sqrt(nextMinDistSq - 2 * minDistSq)))
      );
    }
    for (Site site: sites) {
      site.move(speed.getValuef() * (float)deltaMs * 60 / 1000);
    }
  }
}


