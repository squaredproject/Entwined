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
public class zisdetest1 extends LXPattern {
    // Declarations
CompoundParameter colorOfCubes =  new CompoundParameter ("Mycolor", 300, 300, 420);
    // Constructor
    public zisdetest1(LX lx) {
        // This makes lx work
        super(lx); 
        // Kick off your patterns, modulators, etc. here.
        addParameter("Mycolor", colorOfCubes);
      }
    // THE LOOP.
    @Override
    public void run(double deltaMs) {
        // This is where the colors happen. 
        for (LXPoint cube : model.points) {
              if (cube.x > 30 && cube.x < 60 
              && cube.y > 40 && cube.y < 400 
              && cube.z > 30 && cube.z < 130 
              // && !(cube.y > 80 && cube.y < 100 && cube.z > 60 && cube.z < 80)
              )  { 
//  colors[cube.index] = LX.hsb(30, 75, 100);}
           colors[cube.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
               } // 
        }
    }
}