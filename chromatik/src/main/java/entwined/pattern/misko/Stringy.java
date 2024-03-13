package entwined.pattern.misko;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class Stringy extends LXPattern {
  // Variable Declarations go here
  // private float waveWidth = 1;
  private float speedMult = 1000;

  private double total_ms1 =0.0;
  private double total_ms2 =0.0;
  static float[][] d;
  static float[] norms;
  static float[][] shadow;
  private int n=3;
  private int current_cube_r[];
  private int current_cube_g[];
  private int current_cube_b[];
  final CompoundParameter speedParam = new CompoundParameter("Speed", 5, 20, .01);
  final CompoundParameter waveSlope = new CompoundParameter("wvSlope", 360, 1, 720);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SinLFO wave100 = new SinLFO(0, 100, speedParam.getValuef() * speedMult);

  private float dist(float  x, float y, float z, float a, float b, float c) {
  //return (float)Math.sqrt(Math.pow(x-a,2)+0.5*Math.pow(y-b,2)+Math.pow(z-c,2));
  return (float)(Math.pow(x-a,2)+Math.pow(y-b,2)+Math.pow(z-c,2));
  //return (float)(Math.pow(x-a,2)+Math.pow(z-b,2));
  }

  private float get_p(int i, int j) {
    return d[i][j]/norms[i];
  }

  public Stringy(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter("waveSlope", waveSlope);
    addParameter("speedParam", speedParam);

    d = new float[model.points.length][model.points.length];
    norms = new float[model.points.length];
    shadow = new float[model.points.length][3];

    for (int i=0; i<model.points.length; i++) {
      shadow[i][0]=0;
      shadow[i][1]=0;
      shadow[i][2]=0;
      LXPoint cubei = model.points[i];
      float norm=0.0f;
      for (int j=0; j<model.points.length; j++) {
        LXPoint cubej = model.points[j];
        if (i==j) {
          d[i][j]=0.0f;
        } else {
          float dd = dist(cubei.x,cubei.y,cubei.z,cubej.x,cubej.y,cubej.z);
          d[i][j]=(float)1.0/dd;
          if (Float.isInfinite(d[i][j])) {
            d[i][j]=1000000;
          }
          norm+=d[i][j];
        }
      }
      norms[i]=norm;
    }
    current_cube_r = new int[n];
    current_cube_g = new int[n];
    current_cube_b = new int[n];
    for (int i=0; i<n; i++) {
      current_cube_r[i]=(int)(Math.random() * (model.points.length - 0 + 1) + 0);
      current_cube_g[i]=(int)(Math.random() * (model.points.length - 0 + 1) + 0);
      current_cube_b[i]=(int)(Math.random() * (model.points.length - 0 + 1) + 0);
    }
  }


  private float dmax(int query, int cubes[]) {
  // int x = cubes[0];
  float dmax=d[query][cubes[0]];
  for (int i=1; i<n; i++) {
    if (dmax<d[query][cubes[i]]) {
      dmax=d[query][cubes[i]];
    }
  }
  return dmax;
  }
  private boolean hits_cube(int query, int cubes[]) {
  for (int i=0; i<n; i++) {
    if (query==cubes[i]) {
      return true;
    }
  }
  return false;
  }
  private float new_shadow(float old_shadow, float current_d) {
  float d_shadow = (float)(current_d*current_d/(0.0005*0.0005))*0.1f;
  float m_shadow = (old_shadow);
  if (d_shadow>m_shadow) {
    return (d_shadow);
  }
  if (m_shadow>0.9) {
    return (float)(m_shadow*0.99);
  } else if (m_shadow>0.3) {
    return (float)(m_shadow*0.90);
  } else if (m_shadow>0.1) {
    return (float)(m_shadow*0.8);
  }
  return m_shadow*0.99f;
  //return (float)Math.max(old_shadow*0.90,(current_d*current_d/(0.0005*0.0005))*0.3);
       /* if (old_shadow>0.98) {
    return (float)(old_shadow*0.99+current_d*3/n);
  } else if (old_shadow>0.3) {
    return (float)(old_shadow*0.95+current_d*2/n);
  }
  if (current_d>0.0005) {
    return (float)0.9;
  }
  return (float)(old_shadow*0.999+current_d/n);*/
  }
  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    //wave360.setPeriod(speedParam.getValuef() * speedMult);
    //wave100.setPeriod(speedParam.getValuef() * speedMult);
      total_ms1+=deltaMs;
      total_ms2+=deltaMs;
      // Use a for loop here to set the ube colors
      if (total_ms2>50) {
        for (int i=0; i<model.points.length; i++ ) {
          LXPoint cube=model.points[i];

          if (hits_cube(i,current_cube_r)) {
            shadow[i][0]=1;
            shadow[i][1]=0;
            shadow[i][2]=0;
          } else if (hits_cube(i,current_cube_g)) {
            shadow[i][0]=0;
            shadow[i][1]=1;
            shadow[i][2]=0;
          } else if (hits_cube(i,current_cube_b)) {
            shadow[i][0]=0;
            shadow[i][1]=0;
            shadow[i][2]=1;
          } else {
            shadow[i][0]=Math.min(1,new_shadow(shadow[i][0],dmax(i,current_cube_r)));
            shadow[i][1]=Math.min(1,new_shadow(shadow[i][1],dmax(i,current_cube_g)));
            shadow[i][2]=Math.min(1,new_shadow(shadow[i][2],dmax(i,current_cube_b)));
          }
          float norm =shadow[i][0]*2+shadow[i][1]+shadow[i][2];
          float h = (360*shadow[i][0]*2+120*shadow[i][1]+240*shadow[i][2])/norm;
          float v = (shadow[i][0]+shadow[i][1]+shadow[i][2])*100;
          colors[cube.index] = LX.hsb( h  , 100, Math.min(100,v));
        }
        total_ms2=0;
      }
      if (total_ms1>10*speedParam.getValuef()) {

        //transition to new cube

        float new_p;
        for (int j=0; j<n; j++) {
          new_p = (float)Math.random();
          for (int i=0; i<model.points.length; i++) {
            if (new_p>0.0) {
              new_p-=get_p(current_cube_r[j],i);
            } else {
              current_cube_r[j]=i;
              break;
            }
          }
          new_p = (float)Math.random();
          for (int i=0; i<model.points.length; i++) {
            if (new_p>0.0) {
              new_p-=get_p(current_cube_g[j],i);
            } else {
              current_cube_g[j]=i;
              break;
            }
          }
          new_p = (float)Math.random();
          for (int i=0; i<model.points.length; i++) {
            if (new_p>0.0) {
              new_p-=get_p(current_cube_b[j],i);
            } else {
              current_cube_b[j]=i;
              break;
            }
          }
        }
        total_ms1=0;
      }
  }
}
