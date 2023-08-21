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
	static float[][] halo_mat;
	static float[][] dist_mat;
	static int[][] conn_mat; 
	static float[] norms;
	static float[][] shadow;
	private int n=5; // number of sprites
	private int trans_c=10; // connectivity
	private int halo_c=10; // connectivity //transc must be smaller than halo c
	private int current_cube[][]; // n x 3 (RGB)
	final BoundedParameter speedParam = new BoundedParameter("Speed", 5, 20, .01);
	final BoundedParameter waveSlope = new BoundedParameter("wvSlope", 360, 1, 720);
	final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
	final SinLFO wave100 = new SinLFO(0, 100, speedParam.getValuef() * speedMult);

	private class PDCube implements Comparable<PDCube> {
		float probability;
		float distance;
		int cube;
		public PDCube(float probability, float distance, int cube) {
			this.probability=probability;
			this.distance=distance;
			this.cube=cube;
		}
		@Override public int compareTo(PDCube a)
		{
			if (this.probability>a.probability) {
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
		conn_mat = new int[model.points.length][halo_c];
		shadow = new float[model.points.length][3];

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
					pQueue.add( new PDCube(1.0f/d,d,j)); // add it 
					while (pQueue.size()>=halo_c) { // remove the smallest probabilities
						pQueue.poll();
					}
				}
			}
			Iterator<PDCube> it = pQueue.iterator();
			trans_mat[i][0]=0.2f; // default stay probability
			halo_mat[i][0]=0.0f; // default stay probability
			dist_mat[i][0]=0.0f;
			conn_mat[i][0]=i;

			//lets fill out the mats 
			int j=halo_c-1;
  			while (it.hasNext()) {
				PDCube pc = pQueue.poll(); // gonna pop the smallest prop, so go backwards
				if (j<trans_c) {
					trans_mat[i][j]=pc.probability;
				}
				halo_mat[i][j]=pc.probability;
				dist_mat[i][j]=pc.distance;
				conn_mat[i][j--]=pc.cube;
			}
			for (j=2; j<halo_c; j++) {
				assert(halo_mat[i][j-1]>=halo_mat[i][j]);
			}

			float trans_norm=trans_mat[i][0];
			for (j=0; j<trans_c; j++) {
				trans_norm+=trans_mat[i][j];
			}
			for (j=0; j<trans_c; j++) {
				trans_mat[i][j]/=trans_norm;
			}

			float halo_norm=trans_mat[i][0];
			for (j=0; j<halo_c; j++) {
				halo_norm+=halo_mat[i][j];
			}
			for (j=0; j<halo_c; j++) {
				halo_mat[i][j]/=trans_norm;
			}
		}
		current_cube = new int[n][3];
		for (int i=0; i<n; i++) {
			current_cube[i][0]=(int)(Math.random() * (model.points.length - 0 + 1) + 0);
			current_cube[i][1]=(int)(Math.random() * (model.points.length - 0 + 1) + 0);
			current_cube[i][2]=(int)(Math.random() * (model.points.length - 0 + 1) + 0);
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
		//float d_shadow = (float)(current_d*current_d/(0.0005*0.0005))*0.1f; //d_shadow proportional to distance to sprite
		//float d_shadow = Math.min(1.0f,1.0f/current_d);
		//if (current_d==0.0) {
		//	d_shadow=1.0f;
		//}
		//System.out.println(""+current_d+" " +d_shadow);
		float d_shadow=Math.max(0.4f,current_p);
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
				//low brightness
				for (int i=0; i<model.points.length; i++ ) {
					for (int k=0; k<3; k++) {
						shadow[i][k]*=0.99f;
					}
					//colors[cube.index] = LX.hsb( 50,50,50);
				}
				for (int i=0; i<n; i++) { // for each sprite lets see which cubes need updates
					for (int k=0; k<3; k++) { // RGB
						int cc=current_cube[i][k];
						shadow[cc][k]=1; // full shadow because its a hit!
						for (int j=1; j<halo_c; j++) { // the c nearest neighbors
							int neighbor=conn_mat[cc][j];
							shadow[neighbor][k]=Math.min(1,new_shadow(shadow[neighbor][k],halo_mat[cc][j]));
						}
					}
				}
				total_ms2=0;
			}
			//move the sprites to a new cube based on probability
			if (total_ms1>10*speedParam.getValuef()) {

				//transition to new cube
				for (int i=0; i<n; i++) { //sprite 
					for (int k=0; k<3; k++) { //channel
						int cc = current_cube[i][k];
						float new_p = (float)Math.random();
						for (int j=0; j<trans_c; j++) { //connectivity
							float p = trans_mat[cc][j];
							if (new_p>p) {
								new_p-=p;
							} else {
								current_cube[i][k]=conn_mat[cc][j];
								break;
							}
						}
					}
				}
				total_ms1=0;
			}
		}
}
