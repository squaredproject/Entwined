package entwined.pattern.mattaniah;

import heronarts.lx.LX;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;


public class RadialGradiant extends LXPattern {

    // Variable Declarations go here
    /* private float minz = Float.MAX_VALUE;
    private float maxz = -Float.MAX_VALUE;
    private float waveWidth = 10;
    */
    private float speedMult = 1000;


    final CompoundParameter speedParam = new CompoundParameter("Speed", 5, 20, .01);
    // final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
    final SinLFO wave360 = new SinLFO(0, 360, speedParam.getValuef() * speedMult);

    // final CompoundParameter waveSlope = new CompoundParameter("waveSlope", 360, 1, 720);
    final CompoundParameter waveSlope = new CompoundParameter("waveSlope", 0.04, 0.00001, 0.15);

    // Constructor and initial setup
    // Remember to use addParameter and addModulator if you're using Parameters or sin waves
    public RadialGradiant(LX lx) {
        super(lx);
        addModulator(wave360).start();

        addParameter("waveSlope", waveSlope);
        addParameter("speedParam", speedParam);
    }
    // This is the pattern loop, which will run continuously via LX
    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        // wave360.setPeriod(speedParam.getValuef() * speedMult);
        wave360.setPeriod( speedParam.getValuef() * speedMult);

        // Use a for loop here to set the cube colors
        for (LXPoint cube : model.points) {
            // float v = (float)( (-wave360.getValuef() + waveSlope.getValuef()) + Math.sqrt(Math.pow(cube.sx,2)+Math.pow(cube.sz,2))*5 );
            // float v = (float)( (-wave360.getValuef() + 1 ) + Math.sqrt(Math.pow(cube.x,2)+Math.pow(cube.z,2))*5 );
            float v = (float)( (wave360.getValuef() + waveSlope.getValuef() *  Math.sqrt(Math.pow(cube.x,2)+Math.pow(cube.z,2))*5  ) );


            colors[cube.index] = LX.hsb( v % 360, 100,  100);
        }
    }
}
