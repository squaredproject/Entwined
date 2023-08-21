// Make sure your package is here (name of your folder path)
package entwined.pattern.colin_hunt;

// Some imports
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.parameter.CompoundParameter;

// The CubeManager gives us access to the individual cubes as we go through our for loop later on. 
// This is very useful. Look at alchemy/Zebra.java to see it used in action
import entwined.core.CubeManager;

/*
In this example we'll be making a simple pattern using one CompoundParameter that controls the color of the cubes. It is boring, I hope yours is better.
*/
public class Example extends LXPattern {

    // Declarations
    CompoundParameter colorOfCubes =  new CompoundParameter ("Color", 20, 0, 360);

    // Constructor
    public Example(LX lx) {
        // This makes lx work
        super(lx); 

        // Kick off your patterns, modulators, etc. here.
        addParameter("color", colorOfCubes);
      }

    // THE LOOP.
    @Override
    public void run(double deltaMs) {

        // This is where the colors happen. 
        for (LXPoint cube : model.points) {
            colors[cube.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
        }

    }
}