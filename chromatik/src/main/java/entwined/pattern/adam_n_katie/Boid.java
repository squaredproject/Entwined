package entwined.pattern.adam_n_katie;

import java.util.ArrayList;

import entwined.utils.Vec2D;
import heronarts.lx.LX;

/**
Boids are agents that move like flocking, herding, schooling, swarming
creatures.
Boids are often drawn as a triangle rotated in the direction of the boid's
velocity.
*/
class Boid {
  // Flocking control.
  static final float neighbordist = 50;
  static final float desiredseparation = 25.0f;
  static final float sepWeight = 1.5f;// Separation
  static final float aliWeight = 1.0f;// Alignment
  static final float cohWeight = 1.0f;// Cohesion

  // Boid movement control.
  float bodRadius = 2.0f;
  float maxforce = 0.24f;   // Maximum steering force
  float maxspeed = 16.0f;    // Maximum speed

  // World control.
  float worldWidth = 640.0f;
  float worldHeight = 640.0f;

  // Variables
  float colorH0To1;
  Vec2D position;
  Vec2D vel;
  Vec2D acceleration;

  Boid(float xPos, float yPos, float hueTo1_in){
    position = new Vec2D(xPos, yPos);
    colorH0To1 = hueTo1_in;

    acceleration = new Vec2D(0, 0);
    float dirRads0ToTwoPi = (float)Math.random() * LX.TWO_PIf;
    vel = new Vec2D((float)Math.cos(dirRads0ToTwoPi), (float)Math.sin(dirRads0ToTwoPi));
  }

  void setBody(float bodR, float maxF, float maxS)
  {
    bodRadius = bodR;
    maxforce = maxF;
    maxspeed = maxS;
  }

  void setBorders(float worldW, float worldH){
    worldWidth = worldW;
    worldHeight = worldH;
  }

  void update(ArrayList<Boid> boids){
    // Reset acceleration to 0 each cycle
    acceleration.scaleSelf(0);

    // Update the acceleration.
    Vec2D steer = getFlockingForces(boids);
    steer.limit(maxforce);  // Limit to maximum steering force
    acceleration.addSelf(steer);

    // Update vel, but limit the speed.
    vel.addSelf(acceleration);
    vel.limit(maxspeed);

    // Update position with wraparound the space.
    position.addSelf(vel);
    if (position.x < -bodRadius) position.x = worldWidth+bodRadius;
    if (position.y < -bodRadius) position.y = worldHeight+bodRadius;
    if (position.x > worldWidth+bodRadius) position.x = -bodRadius;
    if (position.y > worldHeight+bodRadius) position.y = -bodRadius;
  }

  // We accumulate a new acceleration each time based on three
  // rules (described below).
  // To know how to move means knowing about all the other boids.
  Vec2D getFlockingForces(ArrayList<Boid> boids){
    // Determine the flocking forces.
    Vec2D sep = separate(boids);   // Separation
    Vec2D ali = align(boids);      // Alignment
    Vec2D coh = cohesion(boids);   // Cohesion

    // Arbitrarily weight these forces
    sep.scaleSelf(sepWeight);
    ali.scaleSelf(aliWeight);
    coh.scaleSelf(cohWeight);

    // Add the force vectors to acceleration
    // We could add mass here if we want A = F / M
    Vec2D steer = new Vec2D(0,0);
    steer.addSelf(sep);
    steer.addSelf(ali);
    steer.addSelf(coh);

    return steer;
  }

  // A method that calculates and applies a steering force towards a target.
  Vec2D seek(Vec2D target){
    // A vector pointing from the position to the target.
    Vec2D desiredPosDelta= target.sub(position);

    // Scale to maximum speed.
    Vec2D desiredVel = new Vec2D(desiredPosDelta);
    desiredVel.normalize();
    desiredVel.scaleSelf(maxspeed);

    // Implement Reynolds: Steering = Desired - Vel
    Vec2D steer = desiredVel.sub(vel);
    return steer;
  }

  // Separation
  // Method checks for nearby boids and steers away
  Vec2D separate (ArrayList<Boid> boids){
    Vec2D steer = new Vec2D(0, 0);
    int count = 0;
    // For every boid in the system, check if it's too close
    for (Boid other : boids){
      float d = position.distanceTo(other.position);
      // If the distance is greater than 0 and less than an arbitrary amount
      // (0 when you are yourself)
      if ((d > 0) && (d < desiredseparation)){
        // Calculate vector pointing away from neighbor
        Vec2D diff = position.sub(other.position);
        diff.normalize();
        diff.scaleSelf(1.0f/d);        // Weight by distance
        steer.addSelf(diff);
        count++;            // Keep track of how many
      }
    }
    // Average -- divide by how many
    if (count > 0){
      steer.scaleSelf(1.0f/count);
    }

    // As long as the vector is greater than 0
    if (steer.magnitude() > 0){
      // Implement Reynolds: Steering = Desired - Vel
      steer.normalize();
      steer.scaleSelf(maxspeed);
      steer.subSelf(vel);
    }
    return steer;
  }

  // Alignment
  // For every nearby boid in the system, calculate the average vel
  Vec2D align (ArrayList<Boid> boids){

    Vec2D averageVel = new Vec2D(0, 0);
    int count = 0;
    for (Boid other : boids){
      float d = position.distanceTo(other.position);
      if (d < neighbordist){
        averageVel.addSelf(other.vel);
        count++;
      }
    }
    if (count <= 0){
      return new Vec2D(0, 0);// Return a zero steering.
    }
    averageVel.scaleSelf(1.0f/count);

    // Implement Reynolds: Steering = DesiredVel - Vel
    Vec2D desiredVel = new Vec2D(averageVel);
    desiredVel.normalize();
    desiredVel.scaleSelf(maxspeed);
    Vec2D steer = desiredVel.sub(vel);
    return steer;
  }

  // Cohesion
  // For the average position (i.e. center) of all nearby boids, calculate steering vector towards that position
  Vec2D cohesion (ArrayList<Boid> boids){
    Vec2D averagePos = new Vec2D(0, 0);   // Start with empty vector to accumulate all positions
    int count = 0;
    for (Boid other : boids){
      float d = position.distanceTo(other.position);
      if (d < neighbordist){
        averagePos.addSelf(other.position); // Add position
        count++;
      }
    }
    if (count <= 0){
      return new Vec2D(0, 0);// Return a zero steering.
    }
    averagePos.scaleSelf(1.0f/count);

    // Steer towards the average position.
    return seek(averagePos);
  }
}
