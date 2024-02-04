
package entwined.pattern.irenedesf;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

//  @LXCategory(LXCategory.TEST)
public class TimedISDEShrubSweep extends LXPattern {

    final BoundedParameter x;
    final BoundedParameter y;
    final BoundedParameter z;
    final BoundedParameter beam;
    final BoundedParameter rot;
    private double time_elapsed = 0;
    private float time_max = 0;

    public TimedISDEShrubSweep(LX lx) {
        super(lx);
        addParameter("x", x = new BoundedParameter("X", 0, model.xMin, model.xMax));
        // the following y param should light the two shortest rods of a shrub when the beam is set to 1
        // may be useful for adjusting the rotation of the shrubs in the JSON config
//        addParameter(y = new BoundedParameter("Y", 20.8, model.yMin, model.yMax));
        addParameter("y", y = new BoundedParameter("Y", 0, model.yMin, model.yMax));
        addParameter("z", z = new BoundedParameter("Z", 0, model.zMin, model.zMax));
        addParameter("beam", beam = new BoundedParameter("beam", 12, 1, 25));
    //
        addParameter("rot", rot = new BoundedParameter( "rot",0,0,360));
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;
        float xVal = x.getValuef();
        float yVal = y.getValuef();
        float zVal = z.getValuef();
// double myrot;
        double myrot = Math.toRadians(rot.getValuef());
        time_elapsed += deltaMs;

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




        for (LXPoint cube : model.points) {
            if (Math.abs(cube.x - xVal) < beam.getValuef() ||
                    Math.abs(cube.y - yVal) < beam.getValuef() ||
                    Math.abs(cube.z - zVal) < beam.getValuef()) {
                colors[cube.index] = LX.hsb(135, 100, 100);
            } else {
                colors[cube.index] = LX.hsb(135, 100, 0);
            }
        }
    }
}