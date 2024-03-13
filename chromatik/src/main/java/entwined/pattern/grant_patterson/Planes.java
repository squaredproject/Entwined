package entwined.pattern.grant_patterson;

import entwined.utils.SimplexNoise;
import entwined.utils.Vec3D;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

import entwined.utils.Plane;

/**
Planes rotating through the space
*/
public class Planes extends LXPattern {
 // Random seed for our noise functions so it's different on every run
 double seed;
 // These offsets increase relative to deltaMs and speed parameters
 double positionOffset = 0;
 double rotationOffset = 0;
 double colorOffset = 0;

 // Number of planes
 final DiscreteParameter countParam = new DiscreteParameter("count", 3, 1, 10);
 // Rate of change of position, rotation, and color
 final CompoundParameter positionSpeedParam = new CompoundParameter("posSpd", 0.2, 0.01, 1);
 final CompoundParameter rotationSpeedParam = new CompoundParameter("rotSpd", 0.1, 0, 1);
 final CompoundParameter colorSpeedParam = new CompoundParameter("clrSpd", 0.2, 0.01, 1);
 // Width of each rendered plane
 final CompoundParameter sizeParam = new CompoundParameter("size", .5, .1, 5);
 // How different each plane is from the others in position, rotation, and color
 // (0 means all planes have the same position/rotation/color)
 final CompoundParameter positionVarianceParam = new CompoundParameter("posVar", 0.5, 0, 0.5);
 final CompoundParameter rotationVarianceParam = new CompoundParameter("rotVar", 0.5, 0, 0.5);
 final CompoundParameter colorVarianceParam = new CompoundParameter("clrVar", 0.3, 0, 0.3);

 public Planes(LX lx) {
   super(lx);
   addParameter("count", countParam);
   addParameter("positionSpeed", positionSpeedParam);
   addParameter("rotationSpeed", rotationSpeedParam);
   addParameter("colorSpeed", colorSpeedParam);
   addParameter("size", sizeParam);
   addParameter("positionVariance", positionVarianceParam);
   addParameter("rotationVariance", rotationVarianceParam);
   addParameter("colorVariance", colorVarianceParam);

   seed = Math.random() * 1000;
 }

 @Override
 public void run(double deltaMs) {
   // Increase each offset based on time since last run() and speed param values
   positionOffset += deltaMs * positionSpeedParam.getValuef() / 1000;
   rotationOffset += deltaMs * rotationSpeedParam.getValuef() / 2000;
   colorOffset += deltaMs * colorSpeedParam.getValuef() / 1000;
   float positionVariance = positionVarianceParam.getValuef();
   float rotationVariance = rotationVarianceParam.getValuef();
   float colorVariance = colorVarianceParam.getValuef();

   // Black out all cubes and add colors from each plane
   clearColors();
   int countValue = (int)countParam.getValue();
   for (int i = 0; i < countValue; i++) {
     // For each plane we want to display, compute position, rotation, and color from SimplexNoise function
     float x = (float)(model.cx + SimplexNoise.noise(i * positionVariance, positionOffset, seed, 0) * model.xRange / 2.0);
     float y = (float)(model.cy + SimplexNoise.noise(i * positionVariance, positionOffset, seed, 100) * model.yRange / 2.0);
     float z = (float)(model.cz + SimplexNoise.noise(i * positionVariance, positionOffset, seed, 200) * model.zRange / 2.0);
     float yrot = (float)(SimplexNoise.noise(i * rotationVariance, rotationOffset, seed, 300) * Math.PI);
     float zrot = (float)(SimplexNoise.noise(i * rotationVariance, rotationOffset, seed, 400) * Math.PI);
     Plane plane = new Plane(new Vec3D(x, y, z), new Vec3D(1, 0, 0).rotateY(yrot).rotateZ(zrot));
     // Noise hovers around 0 between -1 and 1; double the hue range so we actually get red sometimes.
     int hue = (int)((SimplexNoise.noise(i * colorVariance, colorOffset, seed, 500) + 1) * 360) % 360;
     // Here we want full saturation most of the time, so turn 0 into full and -1 or 1 into none.
     // But take the square root to curve a little back towards less saturation.
     int saturation = (int)((1.0 - Math.sqrt(Math.abs(SimplexNoise.noise(i * colorVariance, colorOffset, seed, 600)))) * 100);
     for (LXPoint cube : model.points) {
       double distance = plane.getDistanceToPoint(new Vec3D(cube.x, cube.y, cube.z));
       colors[cube.index] = LXColor.lightest(colors[cube.index], LX.hsb(hue, saturation, (float)Math.max(0, 100 - distance / sizeParam.getValuef())));
     }
   }
 }
}
