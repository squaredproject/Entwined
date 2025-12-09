package entwined.pattern.misko;
import java.util.PriorityQueue;
import java.util.Iterator;
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
	static float[][] trans_mat;
	static float stay_prob=0.3f; // [0,1] , larger means lazier
	static float[][] halo_mat;
	static float[] center;
	static int[][] conn_mat; 
	static float[][] cube_xyz; 
	static int[] sprite_at_cube; 
	static float[] norms;
	static float fade[]=new float[]{0.95f,0.95f,0.95f};
	static float[][] shadow;
	private int n=14; // number of sprites
	private Sprite[] sprites;
	private int trans_c=25; // connectivity
	private int halo_c=40; // connectivity //transc must be smaller than halo c
	private int current_cube[][]; // n x 3 (RGB)
	final CompoundParameter speedParam = new CompoundParameter("Speed", 5, 20, .01);
	private int update=0;

	private void vec3_mul(float a[], float b[], float c[]) {
		for (int k=0; k<3; k++) {
			c[k]=a[k]*b[k];
		}
	}
	private void vec3_div(float a[], float b[], float c[]) {
		for (int k=0; k<3; k++) {
			c[k]=a[k]/b[k];
		}
	}
	private void vec3_addto(float a[], float b[]) {
		vec3_sum(a,b,a);
	}
	private void vec3_sum(float a[], float b[], float c[]) {
		for (int k=0; k<3; k++) {
			c[k]=a[k]+b[k];
		}
	}
	private void vec3_diff(float a[], float b[], float c[]) {
		for (int k=0; k<3; k++) {
			c[k]=a[k]-b[k];
		}
	}
	private void vec3_scale(float a[], float b, float c[]) {
		for (int k=0; k<3; k++) {
			c[k]=a[k]*b;
		}
	}
	private float vec3_abs2(float a[]) {
		float sum=0.0f;
		for (int k=0; k<3; k++) {
			sum+=Math.pow(a[k],2);
		}
		return sum;
	}
	private float vec3_abs(float a[]) {
		return (float)Math.pow(vec3_abs2(a),0.5);
	}
	private void vec3_norm(float a[], float b[]) {
		float mag=vec3_abs(a);
		for (int k=0; k<3; k++) {
			b[k]=a[k]/mag;
		}
	}
	private void vec3_clip(float a[], float b, float c[]) {
		float mag = vec3_abs(a);
		if (!Float.isFinite(mag) || mag<1e-9) {
			return; //its maybe a 0 vec or something weird?
		}
		vec3_norm(a,c); //get the norm
		vec3_scale(c,Math.min(mag,b),c);
	}

	private void printv(String tag, float a[]){ 
		System.out.println(tag + " " + a[0] + " " + a[1] + " " + a[2]);
	}
	private float vec3_dot(float a[], float b[]) {
		float sum=0.0f;
		for (int k=0; k<3; k++) {
			sum+=a[k]*b[k];
		}
		return sum;
	}


	public class Sprite  {
		float bias_xyz[];
		float rgb[];
		int current_cube;
		double ms_since_last_update;
		float color_density;
		int sprite_id;
		float velocity[];
		int stay_away;
		float inv_bias_xyz[];
		float stay_prob;
		float acceleration;
		float angular;

		public void add_ms(double ms) {

			this.ms_since_last_update+=ms;
		}


		public void shadow() {
			for (int k=0; k<3; k++) {
				shadow[this.current_cube][k]=Math.max(this.rgb[k],shadow[this.current_cube][k]);
			}
			for (int j=1; j<halo_c; j++) { // the c nearest neighbors
				int neighbor=conn_mat[this.current_cube][j];
				for (int k=0; k<3; k++) {
					shadow[neighbor][k]=Math.max(
						shadow[neighbor][k],
						Math.min(
								1.0f,
								this.rgb[k]*halo_mat[this.current_cube][j]*this.color_density)
							);

				}
			}
		}
		public Sprite(int sprite_id, float rgb[], int current_cube, float bias_xyz[], float stay_prob, float color_density, int stay_away, float angular) {
			this.velocity = new float[3];
			this.bias_xyz = new float[]{1.0f,1.0f,1.0f};
			this.inv_bias_xyz = new float[]{1.0f,1.0f,1.0f};
			this.ms_since_last_update=0;
			this.sprite_id = sprite_id;
			this.rgb=rgb.clone();
			this.stay_away=-1;
			if (current_cube==-1) {
				while (1==1) {
					current_cube=(int)(Math.random() * (model.points.length - 1) + 0);
					if (sprite_at_cube[current_cube]==-1) {
						this.current_cube=current_cube;
						break;
					}	
				}
			}
			assert(sprite_at_cube[this.current_cube]==-1);
			sprite_at_cube[this.current_cube]=this.sprite_id;
			this.current_cube=current_cube;
			this.color_density=8.0f;
			this.bias_xyz=bias_xyz.clone();
			this.stay_prob=stay_prob;
			this.velocity =  new float[3];
			this.acceleration = 1.0f;
			this.color_density = color_density;
			this.stay_away=stay_away;
			this.inv_bias_xyz=new float[]{1.0f/bias_xyz[0],1.0f/bias_xyz[1],1.0f/bias_xyz[2]};
			this.angular=angular;
			vec3_norm(this.inv_bias_xyz,this.inv_bias_xyz);
		}
		public void transistion() {
			//find out how much we want to move
			float expected_displacement[]=new float[3];
			vec3_scale(this.velocity,(float)(this.ms_since_last_update/10.0),expected_displacement);
			float pos_with_displacement[]=new float[3];
			vec3_sum(cube_xyz[this.current_cube],expected_displacement,pos_with_displacement);

			//lets recompute the transistion probabilities here
			float trans_p[] = new float[trans_c];
			float norm=0.0f;
			trans_p[0]=this.stay_prob;
			//Calculate where would be be if exhibit was continous
			float diff_to_neighbor[]=new float[3];
			float pos_diff_to_neighbor[]=new float[3];
			float pos_diff_to_neighbor_bias[]=new float[3];

			float diff_from_center[] = new float[3];
			vec3_diff(center,cube_xyz[this.current_cube],diff_from_center);
			for (int j=1; j<trans_c; j++) {
				int neighbor_cube=conn_mat[this.current_cube][j];
				if (sprite_at_cube[neighbor_cube]!=-1) {
					continue;
				}
				if (this.stay_away!=-1 && shadow[neighbor_cube][this.stay_away]>1) {
					continue;
				}
				vec3_diff(cube_xyz[neighbor_cube],cube_xyz[this.current_cube],diff_to_neighbor); //without any velocity
				if (1==1) { //|| vec3_dot(diff_to_neighbor,this.velocity)>0) { // if this is in the right direction
					vec3_diff(cube_xyz[neighbor_cube],pos_with_displacement,pos_diff_to_neighbor);
					vec3_mul(pos_diff_to_neighbor,this.bias_xyz,pos_diff_to_neighbor_bias);
					trans_p[j]=1.0f/vec3_abs(pos_diff_to_neighbor_bias);//vec3_abs2(pos_diff_to_neighbor_with_bias);
					norm+=trans_p[j];
				}
			}
			if (norm==0.0f) {
				//we have no where to go!
				for (int j=1; j<trans_c; j++) {
					int neighbor_cube=conn_mat[this.current_cube][j];
					if (sprite_at_cube[neighbor_cube]!=-1) {
						continue;
					}
					vec3_diff(cube_xyz[neighbor_cube],pos_with_displacement,pos_diff_to_neighbor);
					vec3_mul(pos_diff_to_neighbor,this.bias_xyz,pos_diff_to_neighbor_bias);
					trans_p[j]=1.0f/vec3_abs(pos_diff_to_neighbor_bias);//vec3_abs2(pos_diff_to_neighbor_with_bias);
					norm+=trans_p[j];
				}
			}

			//normalize to distribution
			for (int j=1; j<trans_c; j++) {
				trans_p[j]*=(1-trans_p[0])/norm;
			}
			//Now pick a random number and move around
			float new_p = (float)Math.random();
			float dist_travelled[]=new float[3];
			assert(sprite_at_cube[this.current_cube]==this.sprite_id);
			sprite_at_cube[this.current_cube]=-1;
			for (int j=0; j<trans_c; j++) { //connectivity
				float p = trans_p[j];
				if (new_p>p) {
					new_p-=p;
				} else {
					int neighbor_cube=conn_mat[this.current_cube][j];
					assert(sprite_at_cube[neighbor_cube]==-1);
					sprite_at_cube[neighbor_cube]=this.sprite_id;
					vec3_diff(cube_xyz[neighbor_cube],cube_xyz[this.current_cube],dist_travelled);
					if (neighbor_cube==this.current_cube) {
					} else if (vec3_dot(dist_travelled,this.velocity)<0) { //we just abrubptly changed direction
						//want to keep energy?
						float new_direction_norm[]=new float[3];
						vec3_norm(dist_travelled,new_direction_norm);
						//multiply it by mag of old
						float mag_velocity=vec3_abs(this.velocity);
						vec3_scale(new_direction_norm,mag_velocity,this.velocity);
					} else {
						float new_direction_norm[]=new float[3];
						vec3_norm(dist_travelled,new_direction_norm);
						vec3_scale(new_direction_norm,this.acceleration,new_direction_norm);
						vec3_addto(this.velocity,new_direction_norm);
					}
					vec3_clip(this.velocity,10,this.velocity);
					vec3_mul(this.velocity,this.inv_bias_xyz,this.velocity);
					this.current_cube=conn_mat[this.current_cube][j];
					break;
				}
			}
			if (this.angular!=0.0f) {
				diff_from_center = new float[3];
				vec3_diff(center,cube_xyz[this.current_cube],diff_from_center);
				float angular_vel[]=new float[3];
				angular_vel[0]=-diff_from_center[2]; //-z-0.2f*x;
				angular_vel[1]=0.0f;
				angular_vel[2]=diff_from_center[0];//x-0.2f*z;
				vec3_norm(angular_vel,angular_vel);
				vec3_scale(angular_vel,angular,angular_vel);
				vec3_addto(this.velocity,angular_vel);
			}
			this.ms_since_last_update=0;
		}
	}


	private class PDCube implements Comparable<PDCube> {
		float diffs[];
		int cube;
		float distance;
		public PDCube(float diffs[], int cube) {
			this.diffs=diffs.clone();
			this.cube=cube;
			this.distance=vec3_abs(this.diffs);
		}
		@Override public int compareTo(PDCube a)
		{
			if (this.distance<a.distance) {
				return 1;
			}
			return -1;
		}
	}	



	public Stringy(LX lx) {
		super(lx);
		assert(trans_c<=halo_c);
		addParameter("speedParam", speedParam);

		center = new float[3];
		trans_mat = new float[model.points.length][trans_c];
		sprite_at_cube = new int[model.points.length];
		halo_mat = new float[model.points.length][halo_c];
		conn_mat = new int[model.points.length][halo_c];
		cube_xyz = new float[model.points.length][3];
		shadow = new float[model.points.length][3];
		sprites = new Sprite[n];

		for (int i=0; i<model.points.length; i++) {
			LXPoint cubei = model.points[i];
			sprite_at_cube[i]=-1;
			cube_xyz[i][0]=cubei.x;
			cube_xyz[i][1]=cubei.y;
			cube_xyz[i][2]=cubei.z;
			vec3_addto(center,cube_xyz[i]);
		}
		vec3_scale(center,1.0f/model.points.length,center);
	
		for (int i=0; i<model.points.length; i++) {
			shadow[i][0]=0;
			shadow[i][1]=0;
			shadow[i][2]=0;
			LXPoint cubei = model.points[i];
			PriorityQueue<PDCube> pQueue = new PriorityQueue<PDCube>();
			float diff[]=new float[3];
			for (int j=0; j<model.points.length; j++) {
				LXPoint cubej = model.points[j];
				if (i!=j) {
					vec3_diff(cube_xyz[j],cube_xyz[i],diff);
					pQueue.add( new PDCube(diff,j)); // add it 
					while (pQueue.size()>=halo_c) { // remove the smallest probabilities
						pQueue.poll();
					}
				}
			}
			Iterator<PDCube> it = pQueue.iterator();
			trans_mat[i][0]=stay_prob; // default stay probability
			halo_mat[i][0]=0.0f; // default stay probability
			conn_mat[i][0]=i;

			//lets fill out the mats 
			int j=halo_c-1;
  			while (it.hasNext()) {
				PDCube pc = pQueue.poll(); // gonna pop the smallest prop, so go backwards
				if (j<trans_c) {
					trans_mat[i][j]=1.0f/pc.distance;
				}
				halo_mat[i][j]=1.0f/pc.distance;
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

	
		float bias_to_y[]=new float[]{10.0f,1.0f,10.0f};
		vec3_norm(bias_to_y,bias_to_y);

		float bias_to_none[]=new float[]{1.0f,1.0f,1.0f};
		vec3_norm(bias_to_none,bias_to_none);

		float bias_to_notz[]=new float[]{1.0f,10.0f,1.0f};
		vec3_norm(bias_to_notz,bias_to_notz);
	

		for (int i=0; i<(n/2-1); i++) {
			sprites[i*2]=new Sprite(i*2,
					new float[]{1.0f,0.0f,0.0f},
					-1,
					bias_to_y, 0.01f, 1.0f, -1,0.0f);
			sprites[i*2+1]=new Sprite(i*2+1,
					new float[]{0.0f,1.0f,0.0f},
					-1,
					bias_to_notz, 0.01f, 1.0f, 2,0.0f);
		}
		sprites[n-2]=new Sprite(n-2,
				new float[]{0.0f,0.0f,1.0f},
				-1,
				bias_to_none, 0.1f, 10.0f, 1,0.05f);
		sprites[n-1]=new Sprite(n-1,
				new float[]{0.0f,0.0f,1.0f},
				-1,
				bias_to_none, 0.1f, 10.0f, 1,0.05f);
	}


	private boolean hits_cube(int query, int cubes[]) {
		for (int i=0; i<n; i++) {
			if (query==cubes[i]) {
				return true;
			}
		}
		return false;
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
			if (total_ms2>100) {
				//fade the tails
				for (int i=0; i<model.points.length; i++ ) {
					for (int k=0; k<3; k++) {
						shadow[i][k]*=fade[k];
					}
				}
				//update shadows
				for (int i=0; i<n; i++) { // for each sprite lets see which cubes need updates
					sprites[i].shadow();
				}
				total_ms2=0;
			}
			for (int i=0; i<n; i++) {
				sprites[i].add_ms(deltaMs);
			}
			//move the sprites to a new cube based on probability
			if (total_ms1>speedParam.getValuef()*10) {
				//add an impulse to seperate sprites
				float d[]=new float[3];
				for (int i=0; i<n; i++) {
					if (sprites[i].stay_away==-1) {
						continue;
					}
					float a[]=cube_xyz[sprites[i].current_cube];
					for (int l=0; l<n; l++) {
						if (sprites[l].rgb[sprites[i].stay_away]==0.0f) {
							continue;
						}
						float b[]=cube_xyz[sprites[l].current_cube];
						vec3_diff(a,b,d);
						//vec3_mul(d,sprites[i].bias_xyz,d);
						float mag=vec3_abs(d);
						//if (mag<100) {
						vec3_scale(d,500.0f/(mag*mag),d);
						vec3_mul(d,sprites[i].inv_bias_xyz,d);
						vec3_addto(sprites[i].velocity,d);
						//}
					}
				}

				//transition to new cube
				for (int i=0; i<n; i++) {
					//sprites[(update)%n].transistion();
					sprites[i].transistion();
				}
				total_ms1=0;

			}
		}
}
