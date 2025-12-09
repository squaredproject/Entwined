package entwined.pattern.irenedesf;

// Some imports
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
// Let's us use the Sin Wave
// import heronarts.lx.modulator.SinLFO;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
// The CubeManager gives us access to the individual cubes as we go through our for loop later on. 
// This is very useful. Look at alchemy/Zebra.java to see it used in action
import entwined.core.CubeManager;

/*
In this example we'll be making a simple pattern using one CompoundParameter that controls the color of the cubes. It is boring, I hope yours is better.
*/
public class zisdetestAll4 extends LXPattern {
    // Declarations
    // Variable Declarations go here
    private float xwidth = 0;
    private float xstartloc = 0;
    private float zwidth = 0;
    private float zstartloc = 0;
    private double time_elapsed = 0;
    private float time_max = 0;

    CompoundParameter colorOfCubes = new CompoundParameter("Mycolor", 300, 300,
        420);

    // Constructor
    public zisdetestAll4(LX lx) {
        // This makes lx work
        super(lx);
        // Kick off your patterns, modulators, etc. here.
        addParameter("Mycolor", colorOfCubes);
    }

    // THE LOOP.
    @Override
    public void run(double deltaMs) {
        time_elapsed += deltaMs;

        xwidth = 30;
        xstartloc = 0;
        zwidth = -100;
        zstartloc = -180;

        if (time_elapsed > 9000) {
            for (LXPoint cube : model.points) {
                if (cube.x > -130 && cube.x < 0 && cube.y > 40 && cube.y < 400
                    && cube.z > -20 && cube.z < 20) {
                    // colors[cube.index] = LX.hsb(30, 75, 100);}
                    // BB: there was a 'cube1' here. Cube scopes, so there's no reason to use 'cube1'
                    colors[cube.index] = LX.hsb(colorOfCubes.getValuef(), 50,
                        100);
                } //
            }
            for (LXPoint cube2 : model.points) {
                if (cube2.x > 0 && cube2.x < 130 && cube2.y > 40
                    && cube2.y < 400 && cube2.z > -20 && cube2.z < 20) {
                    // colors[cube2.index] = LX.hsb(30, 75, 100);}
                    colors[cube2.index] = LX.hsb(colorOfCubes.getValuef(), 50,
                        100);
                } //
            }
            for (LXPoint cube3 : model.points) {
                if (cube3.x > xstartloc && cube3.x < xstartloc + xwidth
                    && cube3.y > 40 && cube3.y < 400 && cube3.z > -180
                    && cube3.z < 0) {
                    // colors[cube3.index] = LX.hsb(30, 75, 100);}
                    colors[cube3.index] = LX.hsb(colorOfCubes.getValuef(), 50,
                        100);
                } //
            }
        }
        else if (time_elapsed > 8000) {
            // This is where the colors happen.
            for (LXPoint cube : model.points) {
                if (cube.x > -130 && cube.x < 0 && cube.y > 40 && cube.y < 400
                    && cube.z > -20 && cube.z < 20) {
                    // colors[cube.index] = LX.hsb(30, 75, 100);}
                    colors[cube.index] = LX.hsb(colorOfCubes.getValuef(), 50,
                        100);
                } //
            }
        } /* time3000 */
        else if (time_elapsed > 5000) {
            for (LXPoint cube1 : model.points) {
                if (cube1.x > -30 && cube1.x < 30 && cube1.y > 40
                    && cube1.y < 400 && cube1.z > 30 && cube1.z < 130) {
                    // colors[cube1.index] = LX.hsb(30, 75, 100);}
                    colors[cube1.index] = LX.hsb(colorOfCubes.getValuef(), 50,
                        100);
                } //
            }
        } else if (time_elapsed > 2000) {
            for (LXPoint cube2 : model.points) {
                if (cube2.x > 0 && cube2.x < 130 && cube2.y > 40
                    && cube2.y < 400 && cube2.z > -20 && cube2.z < 20) {
                    // colors[cube2.index] = LX.hsb(30, 75, 100);}
                    colors[cube2.index] = LX.hsb(colorOfCubes.getValuef(), 50,
                        100);
                } //
            }
        } else {
            for (LXPoint cube3 : model.points) {
                if (cube3.x > xstartloc && cube3.x < xstartloc + xwidth
                    && cube3.y > 40 && cube3.y < 400 && cube3.z > -180
                    && cube3.z < 0) {
                    // colors[cube3.index] = LX.hsb(30, 75, 100);}
                    colors[cube3.index] = LX.hsb(colorOfCubes.getValuef(), 50,
                        100);
                } //
            }
        } // else end
        if (time_elapsed > 12000) {
            time_elapsed = 0;


        }

    }
}