package entwined.pattern.misko;
import java.util.PriorityQueue;
import java.util.Iterator;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;



public class Stringy extends LXPattern {
	// Variable Declarations go here
	// private float waveWidth = 1;
	private float speedMult = 1000;

	private double total_ms1 =0.0;
	private double total_ms2 =0.0;
	static float[][] trans_mat;
	static float stay_prob=0.05f;
	static float[][] halo_mat;
	static float[][] dist_mat;
	static float[][][] three_dist_mat;
	static int[][] conn_mat; 
	static float[] norms;
	static float[][] shadow;
	private int n=5; // number of sprites
	private Sprite[] sprites;
	private int trans_c=10; // connectivity
	private int halo_c=30; // connectivity //transc must be smaller than halo c
	private int current_cube[][]; // n x 3 (RGB)
	final BoundedParameter speedParam = new BoundedParameter("Speed", 5, 20, .01);
	final BoundedParameter waveSlope = new BoundedParameter("wvSlope", 360, 1, 720);
	final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
	final SinLFO wave100 = new SinLFO(0, 100, speedParam.getValuef() * speedMult);
	private int update=0;

	private class Sprite {
		float rgb[];
		int current_cube;
		
		public void transistion() {
			float new_p = (float)Math.random();
			for (int j=0; j<trans_c; j++) { //connectivity
				float p = trans_mat[this.current_cube][j];
				if (new_p>p) {
					new_p-=p;
				} else {
					this.current_cube=conn_mat[this.current_cube][j];
					break;
				}
			}
		}

		public Sprite(float rgb[], int current_cube) {
			this.rgb=rgb.clone();
			this.current_cube=current_cube;
		}

		public void shadow() {
			for (int k=0; k<3; k++) {
				shadow[this.current_cube][k]=Math.max(this.rgb[k],shadow[this.current_cube][k]);
			}
			for (int j=1; j<halo_c; j++) { // the c nearest neighbors
				int neighbor=conn_mat[this.current_cube][j];
				for (int k=0; k<3; k++) {
					shadow[neighbor][k]=Math.min(
						1,
						new_shadow(
							shadow[neighbor][k],this.rgb[k]*halo_mat[this.current_cube][j]));
					//shadow[neighbor][k]=this.rgb[k];
				}
			}
		}
	}

	public class SpriteCompute extends Sprite {
		public SpriteCompute(float rgb[], int current_cube) {
			super(rgb, current_cube);
		}
		public void transistion() {
			//lets recompute the transistion probabilities here
			float trans_p[] = new float[trans_c];
			float norm=0.0f;
			trans_p[0]=stay_prob;
			for (int j=1; j<trans_c; j++) {
				trans_p[j]=diffs_to_dist(three_dist_mat[this.current_cube][j]);
				norm+=trans_p[j];
			}
			for (int j=1; j<trans_c; j++) {
				trans_p[j]*=(1-trans_p[0])/norm;
			}
			float new_p = (float)Math.random();
			for (int j=0; j<trans_c; j++) { //connectivity
				float p = trans_p[j];
				if (new_p>p) {
					new_p-=p;
				} else {
					this.current_cube=conn_mat[this.current_cube][j];
					break;
				}
			}
		}
	}

	private float diffs_to_dist(float diffs[]) {
		float sum=0.0f;
		for (int i=0; i<3; i++){ 
			sum+=Math.pow(diffs[i],2);
		}
		return (float)(Math.pow(sum,0.5));
	}

	private class PDCube implements Comparable<PDCube> {
		float diffs[];
		int cube;
		float distance;
		public PDCube(float diffs[], int cube) {
			this.diffs=diffs.clone();
			this.cube=cube;
			this.distance=diffs_to_dist(this.diffs);
		}
		@Override public int compareTo(PDCube a)
		{
			if (this.distance<a.distance) {
				return 1;
			}
			return -1;
		}
	}	

	private float dist(float  x, float y, float z, float a, float b, float c) {
		//return (float)Math.sqrt(Math.pow(x-a,2)+0.5*Math.pow(y-b,2)+Math.pow(z-c,2));
		return (float)(Math.pow(x-a,2)+Math.pow(y-b,2)+Math.pow(z-c,2));
		//return (float)(Math.pow(x-a,2)+Math.pow(z-b,2));
	}

	private float get_p(int i, int j) {
		return trans_mat[i][j]; ///(norms[i]+0.000001f);
	}

	public Stringy(LX lx) {
		super(lx);
		assert(trans_c<=halo_c);
		addModulator(wave360).start();
		addModulator(wave100).start();
		addParameter("waveSlope", waveSlope);
		addParameter("speedParam", speedParam);

		trans_mat = new float[model.points.length][trans_c];
		halo_mat = new float[model.points.length][halo_c];
		dist_mat = new float[model.points.length][halo_c];
		three_dist_mat = new float[model.points.length][halo_c][3];
		conn_mat = new int[model.points.length][halo_c];
		shadow = new float[model.points.length][3];
		sprites = new Sprite[n];

		for (int i=0; i<model.points.length; i++) {
			shadow[i][0]=0;
			shadow[i][1]=0;
			shadow[i][2]=0;
			LXPoint cubei = model.points[i];
			PriorityQueue<PDCube> pQueue = new PriorityQueue<PDCube>();
			for (int j=0; j<model.points.length; j++) {
				LXPoint cubej = model.points[j];
				if (i!=j) {
					float d = dist(cubei.x,cubei.y,cubei.z,cubej.x,cubej.y,cubej.z);
					assert(d!=0.0f);
					assert(Float.isInfinite(1.0f/d));
					pQueue.add( new PDCube(
								new float[]{cubei.x-cubej.x,
								cubei.y-cubej.y,
								cubei.z-cubej.z},j)); // add it 
					while (pQueue.size()>=halo_c) { // remove the smallest probabilities
						pQueue.poll();
					}
				}
			}
			Iterator<PDCube> it = pQueue.iterator();
			trans_mat[i][0]=stay_prob; // default stay probability
			halo_mat[i][0]=0.0f; // default stay probability
			dist_mat[i][0]=0.0f;
			conn_mat[i][0]=i;

			//lets fill out the mats 
			int j=halo_c-1;
  			while (it.hasNext()) {
				PDCube pc = pQueue.poll(); // gonna pop the smallest prop, so go backwards
				if (j<trans_c) {
					trans_mat[i][j]=1.0f/pc.distance;
				}
				halo_mat[i][j]=1.0f/pc.distance;
				dist_mat[i][j]=pc.distance;
				for (int k=0; k<3; k++) {
					three_dist_mat[i][j][k]=pc.diffs[k];
				}
				conn_mat[i][j--]=pc.cube;
			}
			for (j=2; j<halo_c; j++) {
				assert(halo_mat[i][j-1]>=halo_mat[i][j]);
			}

			float trans_norm=trans_mat[i][0];
			for (j=1; j<trans_c; j++) {
				trans_norm+=trans_mat[i][j];
			}
			for (j=1; j<trans_c; j++) {
				trans_mat[i][j]*=(1-trans_mat[i][0])/trans_norm; // keep the original stay probability
			}

			float halo_norm=trans_mat[i][0];
			for (j=0; j<halo_c; j++) {
				halo_norm+=halo_mat[i][j];
			}
			for (j=0; j<halo_c; j++) {
				halo_mat[i][j]/=trans_norm;
			}
		}

		for (int i=0; i<n; i++) {
			sprites[i]=new SpriteCompute(new float[]{1.0f,0.2f,0.2f},(int)(Math.random() * (model.points.length - 0 + 1) + 0));
		}
	}


	private boolean hits_cube(int query, int cubes[]) {
		for (int i=0; i<n; i++) {
			if (query==cubes[i]) {
				return true;
			}
		}
		return false;
	}

	//decay the shadow on this cube
	private float new_shadow(float old_shadow, float current_p) {
		float d_shadow=Math.max(0.1f,current_p);
		float m_shadow = (old_shadow);
		if (d_shadow>m_shadow) {
			return (d_shadow);
		}
		if (m_shadow>0.9) {
			return (float)(m_shadow*0.99);
		} else if (m_shadow>0.3) {
			return (float)(m_shadow*0.90);
		} else if (m_shadow>0.1) {
			return (float)(m_shadow*0.80);
		}
		return m_shadow*0.99f;
	}
	// This is the pattern loop, which will run continuously via LX
	@Override
		public void run(double deltaMs) {
			if (getChannel().fader.getNormalized() == 0) return;

			total_ms1+=deltaMs;
			total_ms2+=deltaMs;
			// Render shadow / tail updates
			for (int i=0; i<model.points.length; i++ ) {
				LXPoint cube=model.points[i];
				float norm =shadow[i][0]*2+shadow[i][1]+shadow[i][2];
				float h = (360*shadow[i][0]*2+120*shadow[i][1]+240*shadow[i][2])/(norm+0.0001f);
				float v = (shadow[i][0]+shadow[i][1]+shadow[i][2])*100;
				colors[cube.index] = LX.hsb( h  , 100, Math.min(100,v));
			}
			if (total_ms2>10) {
				//fade the tails
				for (int i=0; i<model.points.length; i++ ) {
					for (int k=0; k<3; k++) {
						shadow[i][k]*=0.99f;
					}
				}
				//update shadows
				for (int i=0; i<n; i++) { // for each sprite lets see which cubes need updates
					sprites[i].shadow();
				}
				total_ms2=0;
			}
			//move the sprites to a new cube based on probability
			if (total_ms1>speedParam.getValuef()) {
				//transition to new cube
				sprites[(update++)%n].transistion();
				total_ms1=0;
			}
		}
}
