package entwined.pattern.adam_n_katie;

import java.util.ArrayList;

import entwined.utils.EntwinedUtils;
import entwined.utils.Vec3D;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;

/**
A flocking set of points.
*/
public class FlockingPoints extends AutographedPattern{
  // Constants
  static final int numBoids = 50;
  static final float boidWorldSize = 640;

  // Parameters (to show up on UI).
  final BoundedParameter bri = new BoundedParameter("bri", 25, 0, 100);
  final BoundedParameter rad = new BoundedParameter("rad", 10, 0, 100);
  final BoundedParameter fad = new BoundedParameter("fad", 75, 0, 100);
  final BoundedParameter bodR = new BoundedParameter("bodR", 2, 0, 25);
  final BoundedParameter maxF0To100 = new BoundedParameter("maxF", 25, 0, 100);
  final BoundedParameter maxS = new BoundedParameter("fad", 16, 0, 100);

  // Variables
  ArrayList<Boid> boids; // An ArrayList for the flock of boids.

  // Constructor and initial setup
  // Remember to use addParameter and addModulator if you're using Parameters or oscillators
  public FlockingPoints(LX lx){
    super(lx);
    addParameter("brightness", bri);
    addParameter("radius", rad);
    addParameter("fade", fad);
    addParameter("bodR", bodR);
    addParameter("maxsomething", maxF0To100);
    addParameter("maxS", maxS);

    // Make the flock of boids.
    float worldH = boidWorldSize;
    float worldW = boidWorldSize;
    createFlock(numBoids, worldH, worldW);
  }

  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);

    // Allow for the overall effect to be faded completely
    // out and do not processing.
    if (getChannel().fader.getNormalized() == 0){return;}

    float bri0To100 = bri.getValuef();
    float rad0To1 = rad.getValuef() / 100.0f;
    float fFade0To1PerS = fad.getValuef() / 100.0f;

    float fadeAmountThisFrame0To1 = deltaTimeS * fFade0To1PerS;
    fadeAmountThisFrame0To1 = EntwinedUtils.constrain(fadeAmountThisFrame0To1, 0.0f, 1.0f);
    //System.out.println("fadeAmountThisFrame0To1 = " + fadeAmountThisFrame0To1);

    // Do boid control.
    float bodRad = bodR.getValuef();
    float maxF0To1 = maxF0To100.getValuef() / 100.0f;
    float maxFor = maxF0To1 * 4.0f;
    float maxSpd = maxS.getValuef();
    setFlockBodies(bodRad, maxFor, maxSpd);

    // Update the flock.
    updateFlock(deltaTimeS);

    // Use a for loop here to set the cube colors
    for (LXPoint cube : model.points){
      // Get the color of the cube.
      int colorArrIdx = cube.index;

      // Fade the old color toward black.
      int oldColor = colors[colorArrIdx];
      int fadedOldColor =
        FadeColorTowardBlack(oldColor, fadeAmountThisFrame0To1);

      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);

      // Determine the brightness to use.
      int summedNewColors = LX.hsb(0, 0, 0);
      boolean hasNewColor = false;
      for (Boid b : boids){
        float boidPosX0To1 = b.position.x / boidWorldSize;
        float boidPosZ0To1 = b.position.y / boidWorldSize;
        float boidHue0To360 = b.colorH0To1 * 360.0f;
        int boidColor = LX.hsb(boidHue0To360, 100, bri0To100);

        float posRelToBoidX = cubePos0To1.x - boidPosX0To1;
        float posRelToBoidZ = cubePos0To1.z - boidPosZ0To1;
        float distFromBoid =
          (float)Math.sqrt(
            (posRelToBoidX * posRelToBoidX) +
            (posRelToBoidZ * posRelToBoidZ));
        if(distFromBoid <= rad0To1){
            summedNewColors = LXColor.blend(summedNewColors, boidColor, LXColor.Blend.ADD);
            hasNewColor = true;
        }
      }

      // Set the color of the cube.
      if(hasNewColor){
        colors[colorArrIdx] =
          LXColor.lightest(
            fadedOldColor,
            summedNewColors);
      }else{
        colors[colorArrIdx] = fadedOldColor;
      }
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()

  // Make the flock of boids.
  private void createFlock(int numBoidsToCreate, float worldH, float worldW){

    // Initialize the ArrayList
    // Add an initial set of boids into the system
    boids = new ArrayList<Boid>();
    for (int i = 0; i < numBoidsToCreate; i++){
      float colorH0To1 = (float)Math.random();

      float startPosX = (float)Math.random() * worldH;
      float startPosZ = (float)Math.random() * worldW;
      Boid b = new Boid(startPosX, startPosZ, colorH0To1);
      b.setBorders(worldH, worldW);
      boids.add(b);
    }
  }
  private void setFlockBodies(float bodR, float maxF, float maxS){
        // Note that since each boid must know about the flock we
    // pass the whole flock to each boid for its update.
    for (Boid b : boids){
      b.setBody(bodR, maxF, maxS);
    }
  }
  // Update the flock of boids.
  private void updateFlock(float deltaTimeS){
    // Note that since each boid must know about the flock we
    // pass the whole flock to each boid for its update.
    for (Boid b : boids){
      b.update(boids);
    }
  }
}// END class FlockingPoints extends TSPattern

