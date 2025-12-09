
package entwined.pattern.irenedesf;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

//  @LXCategory(LXCategory.TEST)
public class TimedISDEShrubSweep extends LXPattern {

    final CompoundParameter x;
    final CompoundParameter y;
    final CompoundParameter z;
    final CompoundParameter beam;
    final CompoundParameter rot;
    private double time_elapsed = 0;
    private float time_max = 0;

    public TimedISDEShrubSweep(LX lx) {
        super(lx);
        addParameter("x", x = new CompoundParameter("X", 0, model.xMin, model.xMax));
        // the following y param should light the two shortest rods of a shrub when the beam is set to 1
        // may be useful for adjusting the rotation of the shrubs in the JSON config
//        addParameter(y = new CompoundParameter("Y", 20.8, model.yMin, model.yMax));
        addParameter("y", y = new CompoundParameter("Y", 0, model.yMin, model.yMax));
        addParameter("z", z = new CompoundParameter("Z", 0, model.zMin, model.zMax));
        addParameter("beam", beam = new CompoundParameter("beam", 12, 1, 25));
    //
        addParameter("rot", rot = new CompoundParameter( "rot",0,0,360));
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;
        double xVal = x.getValuef();
        double yVal = y.getValuef();
        double zVal = z.getValuef();
// double myrot;
        double myrot = Math.toRadians(rot.getValuef());
        time_elapsed += deltaMs;

//
       xVal =  (Math.cos(myrot) * xVal - Math.sin(myrot) * zVal);
        zVal =  (Math.sin(myrot) * xVal + Math.cos(myrot) * zVal);
      for (LXPoint cube : model.points) {
            if (Math.abs(cube.x - xVal) < beam.getValuef() ||
            Math.abs(cube.y - yVal) < beam.getValuef() ||
            Math.abs(cube.z - zVal) < beam.getValuef()) {
        colors[cube.index] = LX.hsb(300, 50, 100);
    } else {
        colors[cube.index] = LX.hsb(420, 100, 0);
    } 
      }
    }
}
//Rotates points
/* 
[newx]   [ c 0 -s ] [x]
[newy] = [ 0 1  0 ] [y]
[newz]   [ s 0  c ] [z]
where (x, y, z) are your original coordinates, 
(newx, newy, newz) are your rotated coordinates, 
and c = cos(angle) and 
s = sin(angle). Note that Java's trig functions take their parameters as radians, so 
you need to convert the angle in degrees appropriately.
If you've not used matrices before, this is equivalent to the following three expressions:
newx = c * x - s * z
newy = y
newz = s * x + c * z
float nxmin = (float) (Math.cos(rot) * xmin - Math.sin(rot) * zmin);
float nzmin = (float) (Math.sin(rot) * xmin + Math.cos(rot) * zmin);
float nxmax = (float) (Math.cos(rot) * xmax - Math.sin(rot) * zmax);
float nzmax = (float) (Math.sin(rot) * xmax + Math.cos(rot) * zmax);
*/

/*
if (time_elapsed > 4000) {
    xVal =  (Math.cos(myrot) * xVal - Math.sin(myrot) * zVal);
    zVal =  (Math.sin(myrot) * xVal + Math.cos(myrot) * zVal);
  for (LXPoint cube : model.points) {
        if (Math.abs(cube.x - xVal) < beam.getValuef() ||
        Math.abs(cube.y - yVal) < beam.getValuef() ||
        Math.abs(cube.z - zVal) < beam.getValuef()) {
    colors[cube.index] = LX.hsb(300, 50, 100);
} else {
    colors[cube.index] = LX.hsb(300, 50, 0);
}  // else
    }  // for points
}  // iftime 4s
else if (time_elapsed > 2000) {
    for (LXPoint cube : model.points) {
        if (Math.abs(cube.x - xVal) < beam.getValuef() ||
        Math.abs(cube.y - yVal) < beam.getValuef() ||
        Math.abs(cube.z - zVal) < beam.getValuef()) {
    colors[cube.index] = LX.hsb(135, 50, 100);
} else {
    colors[cube.index] = LX.hsb(135, 100, 0);
}  // else
    }  // for points
}  // iftime 2s
else 
    {
//        System.out.println("B4Timelaps: " +time_elapsed +" Myrot: " +myrot +" xval: " +xVal +" zval: " +zVal);
 //       System.out.println("B4Timelaps: " +time_elapsed +" sinMyrot: " +Math.sin(myrot) +" cosMyrot: " +Math.cos(myrot));

      xVal = (float) (Math.cos(myrot) * xVal - Math.sin(myrot) * zVal);
      zVal = (float) (Math.sin(myrot) * xVal + Math.cos(myrot) * zVal);
//      System.out.println("Timelaps: " +time_elapsed +" Myrot: " +myrot +" xval: " +xVal +" zval: " +zVal);
//      System.out.println("Timelaps: " +time_elapsed +" sinMyrot: " +Math.sin(myrot) +" cosMyrot: " +Math.cos(myrot));

        for (LXPoint cube : model.points) {
            if (Math.abs(cube.x - xVal) < beam.getValuef() ||
                    Math.abs(cube.y - yVal) < beam.getValuef() ||
                    Math.abs(cube.z - zVal) < beam.getValuef()) {
                colors[cube.index] = LX.hsb(300, 50, 100);
            } else {
                colors[cube.index] = LX.hsb(300, 50, 0);
            }  // else
        }  // for points
    } // else end
    if (time_elapsed > 5000) {
        time_elapsed = 0;
        xVal =  (Math.cos(myrot) * xVal - Math.sin(myrot) * zVal);
        zVal = (Math.sin(myrot) * xVal + Math.cos(myrot) * zVal);
  
    }


    }  // override run
}  // class timedisde
*/