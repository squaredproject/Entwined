package entwined.pattern.misko;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class ShrubRiver extends LXPattern {

  private float speedMult = 1000;
  final CompoundParameter hue = new CompoundParameter("hue", 135, 0, 360);
  final CompoundParameter treeHue = new CompoundParameter("treeHue", 135, 0, 360);
  final CompoundParameter globalTheta = new CompoundParameter("globalTheta", 1.0, 0, 1.0);
  final CompoundParameter colorSpeed = new CompoundParameter("colorSpeed", 100, 0, 200);
  final CompoundParameter speedParam = new CompoundParameter("Speed", 5, 20, .01);
  final CompoundParameter glow = new CompoundParameter("glow", 0.5, 0.1, 1.0);
  final CompoundParameter color_offset = new CompoundParameter("color_offset", 50, 0.0 , 360);
  final CompoundParameter width = new CompoundParameter("width", 360, 0.1, 1000.0);

  final SawLFO wave = new SawLFO(0, 12, 1000);
  float total_ms=0;
  private float total_length=0.0f;

  private float dist(float x, float z, float a, float b) {
      return (float)Math.sqrt(Math.pow(x-a,2f)+Math.pow(z-b,2f));
  }

  // XXX wtf is this shrub order thing? I think it's specific to the install, which is... problematic.
  // BBB this is certainly installation specific
  private int shrub_order[] = { 15, 0, 2, 1 , 4, 3, 9, 10, 13, 12, 11, 5, 6, 8, 7 , 14, 18, 19 , 17, 16 };;
  //private int shrub_order[] = { 0, 1, 2, 3, 4, 9, 10, 5, 6, 7, 8, 11, 12, 13, 14 ,15, 16, 17, 18 };;
  private float shrub_dists[]; // total dist along path to this shrub
  public ShrubRiver(LX lx) {
    super(lx);
    addModulator(wave).start();
    addParameter("treeHue", treeHue);
    addParameter("hue", hue);
    addParameter("globalTheta", globalTheta);
    addParameter("speed", speedParam);
    addParameter("colorSpeed", colorSpeed);
    addParameter("glow", glow);
    addParameter("width", width);
    addParameter("colorOffset", color_offset);
    shrub_dists=new float[shrub_order.length];
    LXModel prevShrub = null;
    LXModel firstShrub = null;
    int nShrubs = model.sub("SHRUB").size();
    int shrubIdx = 0;
    for (LXModel shrub : model.sub("SHRUB")) {
      if (firstShrub == null) {
        firstShrub = shrub;
      }
      if (prevShrub != null) {
        float d = dist(prevShrub.cx,prevShrub.cz,shrub.cx,shrub.cz);
        shrub_dists[shrub_order[(shrubIdx+1)%nShrubs]]=d+this.total_length;
        this.total_length += d;
      }
      shrubIdx++;
    }
    /*
    for (int i=0; i<shrub_order.length; i++) {
      Shrub first = model.shrubs.get(shrub_order[i]);
      Shrub second = model.shrubs.get(shrub_order[(i+1)%shrub_order.length]);
      float d = dist(first.x,first.z,second.x,second.z);
      shrub_dists[shrub_order[(i+1)%shrub_order.length]]=d+total_length;
      total_length+=d;
    }
    */
    total_length+=1;
    for (int i=0; i<shrub_order.length; i++) {
      shrub_dists[i]/=total_length;
    }

  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    wave.setPeriod(speedParam.getValuef() * speedMult / 3 );
    total_ms+=deltaMs*speedParam.getValuef();
    float time_p = (total_ms*colorSpeed.getValuef()/(10000*width.getValuef())) % 1.0f;
    for (LXModel tree : model.sub("TREE")) {
      for (LXPoint cube : tree.points) {
          colors[cube.index] = LX.hsb(treeHue.getValuef(),
              100,
              30);

      }
    }

    // shrub_order is hardcoded for a particular installation and will thus fail.
    // the get function on shrub will throw an IndexOutOfBounds exception

    for (int i=0; i<shrub_order.length; i++) {
      LXModel shrub;
      try {
        shrub = model.sub("SHRUB").get(i);
      }
      catch (Exception e) {
        // usually happens when the array is longer than the number of shrubs
        continue;
      }

      float shrub_p = shrub_dists[i];
      float dist_p = (1.0f+shrub_p-time_p)%1.0f;
      if ( (1.0f-dist_p) < dist_p ) {
        dist_p=1.0f-dist_p;
      }
      for (LXPoint shrubCube : shrub.points) {
        //float t = shrub_dists[i]/total_length;
        float h = (hue.getValuef() +
              -shrub_dists[i]*width.getValuef() +
              total_ms*colorSpeed.getValuef()/10000.0f+
            color_offset.getValuef())%360;
        //float b = Math.min(100,glow.getValuef()*100.0f+(1.0f-glow.getValuef())*diff*(100.0f/12.0f));
        float b = glow.getValuef()*100.0f + (1.0f-glow.getValuef())*dist_p*100.0f;
        //int pick = ((int)(total_ms/3000.0f))%20;
        //b = i == shrub_order[pick] ? 100 : 0;
        colors[shrubCube.index] = LX.hsb(h,
            100,
            b);
      }
    }
  }
}
