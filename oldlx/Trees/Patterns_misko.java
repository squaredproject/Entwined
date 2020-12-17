import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.Click;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import toxi.geom.Vec2D;
import org.apache.commons.lang3.ArrayUtils;
import toxi.math.noise.PerlinNoise;
import toxi.math.noise.SimplexNoise;

class Circles extends TSPattern {

  // Variable Declarations go here
  private float minz = Float.MAX_VALUE;
  private float maxz = -Float.MAX_VALUE;
  private float waveWidth = 1;
  private float speedMult = 1000;

  final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SawLFO wave100 = new SawLFO(0, 100, speedParam.getValuef() * speedMult);

  // add speed, wave width

  // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using Parameters or sin waves
  Circles(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter(waveSlope);
    addParameter(speedParam);

    for (BaseCube cube : model.baseCubes) {
      if (cube.z < minz) {minz = cube.z;}
      if (cube.z > maxz) {maxz = cube.z;}
    }
  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    wave360.setPeriod(speedParam.getValuef() * speedMult);
    wave100.setPeriod(speedParam.getValuef() * speedMult);

      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
	float v = (float)( (-wave360.getValuef() + waveSlope.getValuef()) + Math.sqrt(Math.pow(cube.sx,2)+Math.pow(cube.sz,2))*5 );
        colors[cube.index] = lx.hsb( v % 360, 100,  100);
        //colors[cube.index] = lx.hsb( (float)( Math.sqrt(Math.pow(cube.sx,2)+Math.pow(cube.sz,2)) % 360), 100, 100);
      }
  }
}

class LineScan extends TSPattern {
  // Variable Declarations go here
  private float waveWidth = 1;
  private float speedMult = 1000;

  private float nx = 0;
  private float nz = 0;
  private float n = 0;
  private double total_ms =0.0;
  final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
  final BasicParameter theta = new BasicParameter("theta", 45, 0, 360);
  final BasicParameter hue = new BasicParameter("hue", 45, 0, 360);
  final BasicParameter wave_width = new BasicParameter("waveWidth", 500, 10, 1500);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SinLFO wave100 = new SinLFO(0, 100, speedParam.getValuef() * speedMult);

    LineScan(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter(waveSlope);
    addParameter(speedParam);
    addParameter(theta);
    addParameter(hue);
    addParameter(wave_width);


  }
  private float dist(float  x, float z) {
	return (nx*x+nz*z)/n;
  }
  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0)       return;

    float theta_rad = (float)Math.toRadians((int)theta.getValuef());
    nx = (float)Math.sin(theta_rad);
    nz = (float)Math.cos(theta_rad);
    n = (float)Math.sqrt(Math.pow(nx,2)+Math.pow(nz,2));
    wave360.setPeriod(speedParam.getValuef() * speedMult);
    wave100.setPeriod(speedParam.getValuef() * speedMult);

      float field_len=7000;
      total_ms+=deltaMs;
      // Use a for loop here to set the ube colors
      for (BaseCube cube : model.baseCubes) {
	double d = Math.abs(dist(cube.x,cube.z)-speedParam.getValuef()*(total_ms%field_len-field_len/2)/10);
        if (d<wave_width.getValuef()) {
        	//float d = (float)(50.0*(Math.sin(dist(cube.x,cube.z)/(wave_width.getValuef()) + speedParam.getValuef()*total_ms/1000.0)+1.0));
        	colors[cube.index] = lx.hsb( hue.getValuef()  , 100, (float)((1.0-d/wave_width.getValuef())*100.0 ));
	} else {
        	colors[cube.index] = lx.hsb( hue.getValuef()  , 100, 0);
	}
      }
  }
}

class Stringy extends TSPattern {
  // Variable Declarations go here
  private float waveWidth = 1;
  private float speedMult = 1000;

  private double total_ms1 =0.0;
  private double total_ms2 =0.0;
  private float p[][];
  private float d[][];
  private float shadow[][];
  private int n=3;
  private int current_cube_r[];
  private int current_cube_g[];
  private int current_cube_b[];
  final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SinLFO wave100 = new SinLFO(0, 100, speedParam.getValuef() * speedMult);

  private float dist(float  x, float y, float z, float a, float b, float c) {
	//return (float)Math.sqrt(Math.pow(x-a,2)+0.5*Math.pow(y-b,2)+Math.pow(z-c,2));
	return (float)(Math.pow(x-a,2)+Math.pow(y-b,2)+Math.pow(z-c,2));
	//return (float)(Math.pow(x-a,2)+Math.pow(z-b,2));
  }
  Stringy(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter(waveSlope);
    addParameter(speedParam);

    
    p = new float[model.baseCubes.size()][model.baseCubes.size()];
    d = new float[model.baseCubes.size()][model.baseCubes.size()];
    shadow = new float[model.baseCubes.size()][3];
    
    for (int i=0; i<model.baseCubes.size(); i++) {
        shadow[i][0]=(float)0;
        shadow[i][1]=(float)0;
        shadow[i][2]=(float)0;
        BaseCube cubei = model.baseCubes.get(i);
        float norm=0;
    	for (int j=0; j<model.baseCubes.size(); j++) {
           BaseCube cubej = model.baseCubes.get(j);
           if (i==j) {
		p[i][j]=0;
           } else {
	   	float dd = dist(cubei.x,cubei.y,cubei.z,cubej.x,cubej.y,cubej.z);
	   	d[i][j]=(float)1.0/dd;
		if (Float.isInfinite(d[i][j])) {
		   d[i][j]=(float)1000000;
		}
           	norm+=d[i][j];
           }
	}
    	for (int j=0; j<model.baseCubes.size(); j++) {
		p[i][j]=(float)d[i][j]/norm;
	}
        
    }
    current_cube_r = new int[n];
    current_cube_g = new int[n];
    current_cube_b = new int[n];
    for (int i=0; i<n; i++) {
	current_cube_r[i]=(int)(Math.random() * (model.baseCubes.size() - 0 + 1) + 0);
	current_cube_g[i]=(int)(Math.random() * (model.baseCubes.size() - 0 + 1) + 0);
	current_cube_b[i]=(int)(Math.random() * (model.baseCubes.size() - 0 + 1) + 0);
    }
  }


  private float dmax(int query, int cubes[]) {
	int x = cubes[0];
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
	float m_shadow = (float)(old_shadow);	
	if (d_shadow>m_shadow) {
		return (float)(d_shadow);
	}
	if (m_shadow>0.9) {
		return (float)(m_shadow*0.99);
	} else if (m_shadow>0.3) {
		return (float)(m_shadow*0.90);
	} else if (m_shadow>0.1) { 
		return (float)(m_shadow*0.8);
	}
	return (float)m_shadow*0.99f;
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
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    //wave360.setPeriod(speedParam.getValuef() * speedMult);
    //wave100.setPeriod(speedParam.getValuef() * speedMult);
      total_ms1+=deltaMs;
      total_ms2+=deltaMs;
      // Use a for loop here to set the ube colors
      if (total_ms2>50) {
	      for (int i=0; i<model.baseCubes.size(); i++ ) {
		      BaseCube cube=model.baseCubes.get(i);

		      if (hits_cube(i,current_cube_r)) {
			      shadow[i][0]=(float)1;
			      shadow[i][1]=(float)0;
			      shadow[i][2]=(float)0;
		      } else if (hits_cube(i,current_cube_g)) {
			      shadow[i][0]=(float)0;
			      shadow[i][1]=(float)1;
			      shadow[i][2]=(float)0;
		      } else if (hits_cube(i,current_cube_b)) {
			      shadow[i][0]=(float)0;
			      shadow[i][1]=(float)0;
			      shadow[i][2]=(float)1;
		      } else {
			      shadow[i][0]=(float)Math.min(1,new_shadow(shadow[i][0],dmax(i,current_cube_r)));
			      shadow[i][1]=(float)Math.min(1,new_shadow(shadow[i][1],dmax(i,current_cube_g)));
			      shadow[i][2]=(float)Math.min(1,new_shadow(shadow[i][2],dmax(i,current_cube_b)));
		      }
		      float norm =shadow[i][0]*2+shadow[i][1]+shadow[i][2];
		      float h = (360*shadow[i][0]*2+120*shadow[i][1]+240*shadow[i][2])/norm;
		      float v = (shadow[i][0]+shadow[i][1]+shadow[i][2])*100;
		      colors[cube.index] = lx.hsb( h  , 100, Math.min(100,v));
	      }
		total_ms2=0;
      }
      if (total_ms1>10*speedParam.getValuef()) {

	      //transistion to new cube

	      float new_p;
	      for (int j=0; j<n; j++) {
		      new_p = (float)Math.random();
		      for (int i=0; i<model.baseCubes.size(); i++) {
			      if (new_p>0.0) {
				      new_p-=p[current_cube_r[j]][i];
			      } else {
				      current_cube_r[j]=i;
				      break;
			      }
		      }
		      new_p = (float)Math.random();
		      for (int i=0; i<model.baseCubes.size(); i++) {
			      if (new_p>0.0) {
				      new_p-=p[current_cube_g[j]][i];
			      } else {
				      current_cube_g[j]=i;
				      break;
			      }
		      }
		      new_p = (float)Math.random();
		      for (int i=0; i<model.baseCubes.size(); i++) {
			      if (new_p>0.0) {
				      new_p-=p[current_cube_b[j]][i];
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

class WaveScan extends TSPattern {
  // Variable Declarations go here
  private float waveWidth = 1;
  private float speedMult = 1000;

  private float nx = 0;
  private float nz = 0;
  private float n = 0;
  private double total_ms =0.0;
  final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
  final BasicParameter theta = new BasicParameter("theta", 45, 0, 360);
  final BasicParameter hue = new BasicParameter("hue", 45, 0, 360);
  final BasicParameter wave_width = new BasicParameter("waveWidth", 20, 1, 50);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SinLFO wave100 = new SinLFO(0, 100, speedParam.getValuef() * speedMult);

    WaveScan(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter(waveSlope);
    addParameter(speedParam);
    addParameter(theta);
    addParameter(hue);
    addParameter(wave_width);


  }
  private float dist(float  x, float z) {
	return (nx*x+nz*z)/n;
  }
  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    float theta_rad = (float)Math.toRadians((int)theta.getValuef());
    nx = (float)Math.sin(theta_rad);
    nz = (float)Math.cos(theta_rad);
    n = (float)Math.sqrt(Math.pow(nx,2)+Math.pow(nz,2));
    wave360.setPeriod(speedParam.getValuef() * speedMult);
    wave100.setPeriod(speedParam.getValuef() * speedMult);
      total_ms+=deltaMs;
      // Use a for loop here to set the ube colors
      for (BaseCube cube : model.baseCubes) {
        float d = (float)(50.0*(Math.sin(dist(cube.x,cube.z)/(wave_width.getValuef()) + speedParam.getValuef()*total_ms/1000.0)+1.0));
        colors[cube.index] = lx.hsb( hue.getValuef()  , 100, d);
      }
  }
}

class RainbowWaveScan extends TSPattern {
  // Variable Declarations go here
  private float waveWidth = 1;
  private float speedMult = 1000;

  private float nx = 0;
  private float nz = 0;
  private float n = 0;
  private double total_ms =0.0;
  final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
  final BasicParameter theta = new BasicParameter("theta", 45, 0, 360);
  final BasicParameter hue = new BasicParameter("hue", 45, 0, 360);
  final BasicParameter wave_width = new BasicParameter("waveWidth", 20, 1, 50);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SinLFO wave100 = new SinLFO(0, 100, speedParam.getValuef() * speedMult);

    RainbowWaveScan(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter(waveSlope);
    addParameter(speedParam);
    addParameter(theta);
    addParameter(hue);
    addParameter(wave_width);


  }
  private float dist(float  x, float z) {
	return (nx*x+nz*z)/n;
  }
  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    float theta_rad = (float)Math.toRadians((int)theta.getValuef());
    nx = (float)Math.sin(theta_rad);
    nz = (float)Math.cos(theta_rad);
    n = (float)Math.sqrt(Math.pow(nx,2)+Math.pow(nz,2));
    wave360.setPeriod(speedParam.getValuef() * speedMult);
    wave100.setPeriod(speedParam.getValuef() * speedMult);
      total_ms+=deltaMs;
      // Use a for loop here to set the ube colors
      for (BaseCube cube : model.baseCubes) {
        float d = (float)(50.0*(Math.sin(dist(cube.x,cube.z)/(wave_width.getValuef()) + speedParam.getValuef()*total_ms/1000.0)+1.0));
        colors[cube.index] = lx.hsb( wave360.getValuef()  , 100, d);
      }
  }
}

class SyncSpinner extends TSPattern {
   
    private float speedMult = 1000;
    final BasicParameter hue = new BasicParameter("hue", 135, 0, 360);
    final BasicParameter globalTheta = new BasicParameter("globalTheta", 1.0, 0, 1.0);
    final BasicParameter colorSpeed = new BasicParameter("colorSpeed", 100, 0, 200);
    final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
    final BasicParameter glow = new BasicParameter("glow", 0.1, 0.0, 1.0);
    final SawLFO wave = new SawLFO(0, 12, 1000);
    float total_ms=0; 
    int shrub_offset[];
    
    SyncSpinner(LX lx) {
        super(lx);
        addModulator(wave).start();
    	addParameter(hue);
    	addParameter(globalTheta);
    	addParameter(speedParam);
    	addParameter(colorSpeed);
    	addParameter(glow);
        
    }
    
    public void run(double deltaMs) {
      if (getChannel().getFader().getNormalized() == 0) return;

    	wave.setPeriod(speedParam.getValuef() * speedMult / 3 );
        total_ms+=deltaMs*speedParam.getValuef();
        for (Shrub shrub : model.shrubs) {
		int shrub_offset = (int)(-shrub.ry/30+24)%12;
		for (ShrubCube shrubCube : shrub.cubes) {
			//System.out.format("%f %d %d | %d\n",shrub.ry,shrub_offset,shrubCube.config.clusterIndex,(shrubCube.config.clusterIndex+shrub_offset)%12);
                        float diff = (12.0f+(wave.getValuef() - (shrubCube.config.clusterIndex+shrub_offset))%12.0f)%12.0f;
			if (diff<0) {
				System.out.println(diff);
			}
			float h = (360+(hue.getValuef() +
                                                 globalTheta.getValuef()*shrubCube.globalTheta +
                                                 total_ms*colorSpeed.getValuef()/10000)%360)%360;
			float b = Math.min(100,glow.getValuef()*100.0f+(1.0f-glow.getValuef())*diff*(100.0f/12.0f));
			colors[shrubCube.index] = lx.hsb(h,
						100, 
						b);    
		}
	}
    }
}


class LightHouse extends TSPattern {
   
    private float speedMult = 1000;
    final BasicParameter hue = new BasicParameter("hue", 50, 0, 360);
    final BasicParameter width = new BasicParameter("width", 45, 0, 100);
    final BasicParameter globalTheta = new BasicParameter("globalTheta", 1.0, 0, 1.0);
    final BasicParameter colorSpeed = new BasicParameter("colorSpeed", 0, 0, 200);
    final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
    final BasicParameter glow = new BasicParameter("glow", 0.1, 0.0, 1.0);
    final SawLFO wave = new SawLFO(0, 360, 1000);
    float total_ms=0; 
    int shrub_offset[];
    
    LightHouse(LX lx) {
        super(lx);
        addModulator(wave).start();
    	addParameter(hue);
    	addParameter(globalTheta);
    	addParameter(speedParam);
    	addParameter(colorSpeed);
    	addParameter(glow);
    	addParameter(width);
        
    }
    
    public void run(double deltaMs) {
      if (getChannel().getFader().getNormalized() == 0) return;

    	wave.setPeriod(speedParam.getValuef() * speedMult  );
        total_ms+=deltaMs*speedParam.getValuef();
	float offset = (wave.getValuef()+360.0f)%360.0f;
	for (BaseCube cube : model.baseCubes) {
		float diff = (360.0f+(wave.getValuef() - cube.globalTheta)%360.0f)%360.f; // smallest postive representation modulo 360
		if ((360-diff)<diff) {
			diff=360-diff;
		}
		float b = diff<width.getValuef() ? 100.0f : 0.0f;
		float h = (360+(hue.getValuef() +
					 total_ms*colorSpeed.getValuef()/10000)%360)%360;
		colors[cube.index] = lx.hsb(h,
					100, 
					b);    
	}
    }
}


class ShrubRiver extends TSPattern {
   
    private float speedMult = 1000;
    final BasicParameter hue = new BasicParameter("hue", 135, 0, 360);
    final BasicParameter treeHue = new BasicParameter("treeHue", 135, 0, 360);
    final BasicParameter globalTheta = new BasicParameter("globalTheta", 1.0, 0, 1.0);
    final BasicParameter colorSpeed = new BasicParameter("colorSpeed", 100, 0, 200);
    final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
    final BasicParameter glow = new BasicParameter("glow", 0.5, 0.1, 1.0);
    final BasicParameter color_offset = new BasicParameter("color_offset", 50, 0.0 , 360);
    final BasicParameter width = new BasicParameter("width", 360, 0.1, 1000.0);
    
    final SawLFO wave = new SawLFO(0, 12, 1000);
    float total_ms=0;
    private float total_length=0.0f; 
 
    private float dist(float x, float z, float a, float b) {
	return (float)Math.sqrt(Math.pow(x-a,2f)+Math.pow(z-b,2f));
    }

    private int shrub_order[] = { 15, 0, 2, 1 , 4, 3, 9, 10, 13, 12, 11, 5, 6, 8, 7 , 14, 18, 19 , 17, 16 };;
    //private int shrub_order[] = { 0, 1, 2, 3, 4, 9, 10, 5, 6, 7, 8, 11, 12, 13, 14 ,15, 16, 17, 18 };;
    private float shrub_dists[]; // total dist along path to this shrub
    ShrubRiver(LX lx) {
        super(lx);
        addModulator(wave).start();
    	addParameter(treeHue);
    	addParameter(hue);
    	addParameter(globalTheta);
    	addParameter(speedParam);
    	addParameter(colorSpeed);
    	addParameter(glow);
    	addParameter(width);
    	addParameter(color_offset);
	shrub_dists=new float[shrub_order.length];
	for (int i=0; i<shrub_order.length; i++) {
		Shrub first = model.shrubs.get(shrub_order[i]);
		Shrub second = model.shrubs.get(shrub_order[(i+1)%shrub_order.length]);
		float d = dist(first.x,first.z,second.x,second.z);
		shrub_dists[shrub_order[(i+1)%shrub_order.length]]=d+total_length;
		total_length+=d;
	}
	total_length+=1;
	for (int i=0; i<shrub_order.length; i++) {
		shrub_dists[i]/=total_length;
	}
        
    }
    
    public void run(double deltaMs) {
      if (getChannel().getFader().getNormalized() == 0) return;

	    wave.setPeriod(speedParam.getValuef() * speedMult / 3 );
	    total_ms+=deltaMs*speedParam.getValuef();
	    float time_p = (total_ms*colorSpeed.getValuef()/(10000*width.getValuef())) % 1.0f;
	    for (Tree tree : model.trees) {
		for (BaseCube cube : tree.cubes) {
			colors[cube.index] = lx.hsb(treeHue.getValuef(),
						    100, 
						    30);    

		}
	    }
	    
	    for (int i=0; i<shrub_order.length; i++) {
			Shrub shrub = model.shrubs.get(i);
			float shrub_p = shrub_dists[i];
                        float dist_p = (1.0f+shrub_p-time_p)%1.0f;
			if ( (1.0f-dist_p) < dist_p ) {
				dist_p=1.0f-dist_p;
			} 
			    for (ShrubCube shrubCube : shrub.cubes) {
			            float t = shrub_dists[i]/total_length;
				    float h = (hue.getValuef() +
							    -shrub_dists[i]*width.getValuef() +
							    total_ms*colorSpeed.getValuef()/10000.0f+
								color_offset.getValuef())%360;
				    //float b = Math.min(100,glow.getValuef()*100.0f+(1.0f-glow.getValuef())*diff*(100.0f/12.0f));
				    float b = glow.getValuef()*100.0f + (1.0f-glow.getValuef())*dist_p*100.0f;
				    //int pick = ((int)(total_ms/3000.0f))%20;
				    //b = i == shrub_order[pick] ? 100 : 0;
				    colors[shrubCube.index] = lx.hsb(h,
						    100, 
						    b);    
			    }
	    }
    }
}

class ColorBlast extends TSPattern {
   
    private float speedMult = 1000;
    final BasicParameter hue = new BasicParameter("hue", 135, 0, 360);
    final BasicParameter width = new BasicParameter("width", 0.15, 0, 2);
    final BasicParameter globalTheta = new BasicParameter("globalTheta", 1.0, 0, 1.0);
    final BasicParameter colorSpeed = new BasicParameter("colorSpeed", 85, 0, 200);
    final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
    final BasicParameter glow = new BasicParameter("glow", 0.1, 0.0, 1.0);
    final SawLFO wave = new SawLFO(0, 360, 1000);
    float total_ms=0; 
    int shrub_offset[];
    
    ColorBlast(LX lx) {
        super(lx);
        addModulator(wave).start();
    	addParameter(hue);
    	addParameter(globalTheta);
    	addParameter(speedParam);
    	addParameter(colorSpeed);
    	addParameter(glow);
    	addParameter(width);
        
    }
    private float dist(float x, float y, float z) {
        return (float)Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
    }
    public void run(double deltaMs) {
      if (getChannel().getFader().getNormalized() == 0) return;

    	wave.setPeriod(speedParam.getValuef() * speedMult  );
      total_ms+=deltaMs*speedParam.getValuef();
      float offset = (wave.getValuef()+360.0f)%360.0f;
    	for (BaseCube cube : model.baseCubes) {
    		float b = 100.0f; //diff<width.getValuef() ? 100.0f : 0.0f;
    		float h = (hue.getValuef() +
    				dist(cube.x,cube.y,cube.z)*width.getValuef()+
    					 -total_ms*colorSpeed.getValuef()/10000)%360;
    		colors[cube.index] = lx.hsb(h,
    					100, 
    					b);    
    	}
    }
}


class Vertigo extends TSPattern {
   
    private float speedMult = 1000;
    final BasicParameter hue = new BasicParameter("hue", 135, 0, 360);
    final BasicParameter width = new BasicParameter("width", 45, 0, 100);
    final BasicParameter globalTheta = new BasicParameter("globalTheta", 1.0, 0, 1.0);
    final BasicParameter colorSpeed = new BasicParameter("colorSpeed", 0, 0, 200);
    final BasicParameter speedParam = new BasicParameter("Speed", 1.5, 3, .01);
    final BasicParameter glow = new BasicParameter("glow", 0.1, 0.0, 1.0);
    final SawLFO wave = new SawLFO(0, 360, 1000);
    float total_ms=0; 
    int shrub_offset[];
    private float max_height=0.0f;
    
    Vertigo(LX lx) {
        super(lx);
        addModulator(wave).start();
    	addParameter(hue);
    	addParameter(globalTheta);
    	addParameter(speedParam);
    	addParameter(colorSpeed);
    	addParameter(glow);
    	addParameter(width);
	max_height=0.0f;
	for (BaseCube cube : model.baseCubes) {
		if (max_height<cube.y) {
			max_height=cube.y;
		}
	}
        
    }
    
    public void run(double deltaMs) {
      if (getChannel().getFader().getNormalized() == 0) return;

    	wave.setPeriod(speedParam.getValuef() * speedMult  );
      total_ms+=deltaMs*speedParam.getValuef();
    	float offset = (wave.getValuef()+360.0f)%360.0f;
    	for (BaseCube cube : model.baseCubes) {
                    float h = hue.getValuef();
    		float b =  ((10.0f-cube.y/max_height + (total_ms/3000.0f))%1.0f)*100.0f ; //? 100.0f : 0.0f;
    		colors[cube.index] = lx.hsb(h,
    					100.0f, 
    					b);    
    	}
    }
}


/**
* A template pattern to get ya started.
*/
/*
class PatternTemplate extends TSPattern {

  // Variable declarations, parameters, and modulators go here
  float minValue;
  float maxValue;
  float startValue;
  float period;
  final BasicParameter parameterName = new BasicParameter("parameterName", startValue, minValue, maxValue);
  final SawLFO modulatorName = new SawLFO(minValue, maxValue, period);

  // Constructor
  PatternTemplate(LX lx) {
    super(lx);

    // Add any needed modulators or parameters here
    addModulator(modulatorName).start();
    addParameter(parameterName);

  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
      if (getChannel().getFader().getNormalized() == 0) return;

      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
        colors[cube.index] = lx.hsb( , , );
      }
  }
}
*/
