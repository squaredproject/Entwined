package entwined.pattern.misko;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

public class ShrubRiver extends LXPattern {

  private float speedMult = 1000;
  final BoundedParameter hue = new BoundedParameter("hue", 135, 0, 360);
  final BoundedParameter treeHue = new BoundedParameter("treeHue", 135, 0, 360);
  final BoundedParameter globalTheta = new BoundedParameter("globalTheta", 1.0, 0, 1.0);
  final BoundedParameter colorSpeed = new BoundedParameter("colorSpeed", 100, 0, 200);
  final BoundedParameter speedParam = new BoundedParameter("Speed", 5, 20, .01);
  final BoundedParameter glow = new BoundedParameter("glow", 0.5, 0.1, 1.0);
  final BoundedParameter color_offset = new BoundedParameter("color_offset", 50, 0.0 , 360);
  final BoundedParameter width = new BoundedParameter("width", 360, 0.1, 1000.0);

  final SawLFO wave = new SawLFO(0, 12, 1000);
  float total_ms=0;
  private float total_length=0.0f;

  private float dist(float x, float z, float a, float b) {
      return (float)Math.sqrt(Math.pow(x-a,2f)+Math.pow(z-b,2f));
  }

  // XXX wtf is this shrub order thing? I think it's specific to the install, which is... problematic.
  private int shrub_order[] = { 15, 0, 2, 1 , 4, 3, 9, 10, 13, 12, 11, 5, 6, 8, 7 , 14, 18, 19 , 17, 16 };;
  //private int shrub_order[] = { 0, 1, 2, 3, 4, 9, 10, 5, 6, 7, 8, 11, 12, 13, 14 ,15, 16, 17, 18 };;
  private float shrub_dists[]; // total dist along path to this shrub
  public ShrubRiver(LX lx) {
    super(lx);
    addModulator(wave).start();
    addParameter("shrubRiverTreeHue", treeHue);
    addParameter("shrubRiverHue", hue);
    addParameter("shrubRiverGlobalTheta", globalTheta);
    addParameter("shrubRiverSpeed", speedParam);
    addParameter("shrubRiverColorSpeed", colorSpeed);
    addParameter("shrubRiverGlow", glow);
    addParameter("shrubRiverWidth", width);
    addParameter("shrubRiverColorOffset", color_offset);
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

    for (int i=0; i<shrub_order.length; i++) {
      LXModel shrub = model.sub("SHRUB").get(i);
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
