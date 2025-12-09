package entwined.pattern.mark_lottor;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;
import entwined.utils.EntwinedUtils;
import entwined.core.CubeManager;

public class MarkLottor extends LXPattern {

  // These parameters will be knobs on the UI
  final CompoundParameter p1 = new CompoundParameter("SIZ", 0.25);
  final CompoundParameter p2 = new CompoundParameter("NUM", 0.75);
  final CompoundParameter p3 = new CompoundParameter("SPD", 0.5);
  final CompoundParameter p4 = new CompoundParameter("DIM", 0.17);

  // This is an example modulator
  final SinLFO verticalPosition = new SinLFO(model.yMin, model.yMax, 5000);

  // This is an example of using cube theta
  final SawLFO anglePosition = new SawLFO(0, 360, 2000);

  int n;
  int MAXYINCHES = (50*12);  // max sculpture height in inches??
  int BALLS = 100;
  int maxballs = BALLS;
  MovObj[] balls;

  boolean isFresh = true;

  public MarkLottor(LX lx) {
    super(lx);

    // Makes the parameters have knobs in the UI
    addParameter("size", p1);
    addParameter("number", p2);
    addParameter("speed", p3);
    addParameter("dim", p4);

    // Starts the modulators
    addModulator(verticalPosition).start();
    addModulator(anglePosition).start();

    balls = new MovObj[BALLS];
    for (n = 0; n < BALLS; n++)
      balls[n] = new MovObj(-1,0,0,0,0,0,0);
  }

  // This is your run loop called every frame.
  // It's basically just like Processing's draw()
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) {
      if (!isFresh) {
        for (n = 0; n < BALLS; n++)
          balls[n] = new MovObj(-1,0,0,0,0,0,0);
        clearColors();
      }
      return;
    }

    isFresh = false;

    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    int n;
    float theta, ntheta;
    float y, ny;

    // These are the values of your knobs
    float p1v = p1.getValuef();
    float p2v = p2.getValuef();
    float p3v = p3.getValuef();
    float p4v = p4.getValuef();

    // These are the values of the LFOs  XXX CSW - not used in current implementation...
    // float vpf = verticalPosition.getValuef();
    // float apf = anglePosition.getValuef();

    maxballs = (int)(BALLS * p2v);
    // dim everything already on cube
      for (LXPoint cube : model.points) {
        colors[cube.index] = LX.hsb(
          LXColor.h(colors[cube.index]),
          LXColor.s(colors[cube.index]),
          LXColor.b(colors[cube.index]) * (1.0f-(p4v*p4v)));
      }
      /*
    // dim everything already on cube
      for (BaseCube cube : model.BaseCubes) {
        colors[cube.index] = lx.hsb(0,0,0);
      }
      */

    // add new balls if free slot and random interval
    for (n = 0; n < maxballs; n++)
    {
      if (balls[n].getposx() != -1) continue;
      if (EntwinedUtils.random(100) < 95) continue;

      // init new ball
      balls[n].setcolor(LX.hsb(currentBaseHue % 360,100,100));
      balls[n].setpos(EntwinedUtils.random(0,360),0,0);  // theta,y,n/a
      balls[n].setvel(0,EntwinedUtils.random(0.1f,0.5f),0);    // up speed
    }

    // update all ball positions
    for (n = 0; n < maxballs; n++)
    {
      if (balls[n].getposx() == -1) continue;

      // update positions
      ntheta = (balls[n].getposx() + balls[n].getvelx()) % 360;
      ny     = balls[n].getposy() + (20*p3v*balls[n].getvely());
      if (ny > MAXYINCHES)
      {
        balls[n].setpos(-1,0,0);   // ball over
        continue;
      }
      balls[n].setpos(ntheta,ny,0);  // new position
    }

    // display all balls
    for (n = 0; n < maxballs; n++)
    {
      if (balls[n].getposx() == -1) continue;

      theta = balls[n].getposx();
      y     = balls[n].getposy();

      // light up any cubes "near" this ball
     for (LXPoint cube : model.points) {
       if ((EntwinedUtils.abs(theta - CubeManager.getCube(lx, cube.index).localTheta) < (50*p1v)) &&
         (EntwinedUtils.abs(y - CubeManager.getCube(lx, cube.index).localY) < (50*p1v)))
         colors[cube.index] = balls[n].getcolor();
     }

   }

    /*
    for (Tree tree : model.trees) {
      // There will be two passes through this loop, one for each tree
      if (tree.index == 1) {
        // Make second tree rotate the other way
        apf = 360-apf;
      }

      for (Cube cube : tree.cubes) {
        // This passes through every cube in the tree
        // cubes have:
        //   .x, .y, .z (absolute position in inches)
        //   .tx, .ty, .tz (position relative to tree base, in inches)
        //   .theta (angle about the center of tree, 0-360)
        //   .size (which size cube this is, Cube.SMALL/Cube.MEDIUM/Cube.LARGE/Cube.GIANT

        // Color space for lx.hsb:
        //   h: 0-360
        //   s: 0-100
        //   b: 0-100

        colors[cube.index] = lx.hsb(
          (lx.getBaseHuef() + cube.transformedY * .3f) % 360,
          100,
          Utils.max(0, 100 - LXUtils.wrapdistf(cube.transformedTheta, apf, 360))
        );
      }
    }
    */
  }
  /* So far as I know, this is not used anywhere, so I'm commenting it out. XXX CSW
  ParameterTriggerableAdapter getParameterTriggerableAdapter() {
    return new ParameterTriggerableAdapter(lx, getChannelFade()) {
      public void onTriggered(float strength) {
        if (!isFresh) {
          for (n = 0; n < BALLS; n++)
            balls[n] = new MovObj(-1,0,0,0,0,0,0);
          clearColors();
        }
        super.onTriggered(strength);
      }
    };

  }
  */

class MovObj
{
  float posx, posy, posz;
  float velx, vely, velz;
  int co;

  MovObj(float x, float y, float z, float vx, float vy, float vz, int c)
  {
    setpos(x,y,z);
    setvel(vx,vy,vz);
    setcolor(c);
  }

  void setpos(float x, float y, float z)
  {
    posx = x;
    posy = y;
    posz = z;
  }
 void setvel(float x, float y, float z)
  {
    velx = x;
    vely = y;
    velz = z;
  }
  void setcolor(int c)
  {
    co = c;
  }

  float getposx()
    { return(posx); }
  float getposy()
    { return(posy); }
  float getposz()
    { return(posz); }

  float getvelx()
    { return(velx); }
  float getvely()
    { return(vely); }
  float getvelz()
    { return(velz); }

  int getcolor()
    { return(co); }
 }
}
