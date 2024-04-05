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
public class zisdeAnimateMe1 extends LXPattern {
    // Declarations
    // Variable Declarations go here
    private float xwidth = 0;
    private float xstartloc = 0;
    private float zwidth = 0;
    private float zstartloc = 0;
    private double time_elapsed = 0;
    private float time_max = 0;
    private float dummy =1;

   // final double spd;
    
    CompoundParameter colorOfCubes = new CompoundParameter("Mycolor", 300, 300, 420);

    // Constructor
    public zisdeAnimateMe1(LX lx) {
        // This makes lx work
        super(lx);
        // Kick off your patterns, modulators, etc. here.
        addParameter("Mycolor", colorOfCubes);
  //      addParameter("spd", spd = new BoundedParameter( "spd",0,0,20));
    }
    // THE LOOP.
    @Override
    public void run(double deltaMs) {
        time_elapsed += deltaMs;
        double myspd = 1;
//  double myspd = spd.getValuef();
        xwidth = 30;
        xstartloc = 0;
        zwidth = -100;
        zstartloc = -180;

        if (time_elapsed > 28000/myspd) {
            for (LXPoint cubeOutsides : model.points) {
                if ((cubeOutsides.x < -80 || cubeOutsides.x > 80)
                    && (cubeOutsides.y < 30 || cubeOutsides.y > 150)
                    && (cubeOutsides.z < -20 || cubeOutsides.z > 20)) {
                    // colors[cubeOutsides.index] = LX.hsb(30, 75, 100);}
                    colors[cubeOutsides.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
        } else if (time_elapsed > 27000/myspd) {
            for (LXPoint ln315 : model.points) {   // angle line 315 degrees
                if (ln315.x > -130 && ln315.x < 0 && ln315.y > 40 && ln315.y < 400 && ln315.z > -0 && ln315.z < 100
                  && (Math.abs(ln315.x)-Math.abs(ln315.z)<10)  && Math.abs(ln315.z) > 25 && Math.abs(ln315.x) > 25 ) {
               // dummy = 0; 
                 colors[ln315.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              } 
              for (LXPoint ln225 : model.points) {   // angle line 315 degrees
               if (ln225.x > -130 && ln225.x < 0 && ln225.y > 40 && ln225.y < 400 && ln225.z > -1800 && ln225.z < 0
                   && (Math.abs(ln225.x)-Math.abs(ln225.z)<10)  && Math.abs(ln225.z) > 25 && Math.abs(ln225.x) > 25 ) {
                  // dummy = 0; 
               colors[ln225.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
               } 
              for (LXPoint ln045 : model.points) {  // angle line 45 degrees
                 if (ln045.x > 0 && ln045.x < 130 && ln045.y > 40 && ln045.y < 400 && ln045.z > -40 && ln045.z < 100
                  && (Math.abs(ln045.x)-Math.abs(ln045.z)<10)  && Math.abs(ln045.z) > 25 && Math.abs(ln045.x) > 25 ) {
                      // dummy = 0;
                        colors[ln045.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
              for (LXPoint ln135 : model.points) {  // angle line 45 degrees
                  if (ln135.x > 0 && ln135.x < 130 && ln135.y > 40 && ln135.y < 400 && ln135.z > -180 && ln135.z < 0
                  && (Math.abs(ln135.x)-Math.abs(ln135.z)<10)  && Math.abs(ln135.z) > 25 && Math.abs(ln135.x) > 25  ) {
                      // dummy = 0;
                        colors[ln135.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
  
        }
        else if (time_elapsed > 26000/myspd) {
            for (LXPoint cubeOutsides : model.points) {
                if ((cubeOutsides.x < -80 || cubeOutsides.x > 80)
                    && (cubeOutsides.y < 30 || cubeOutsides.y > 150)
                    && (cubeOutsides.z < -20 || cubeOutsides.z > 20)) {
                    // colors[cubeOutsides.index] = LX.hsb(30, 75, 100);}
                    colors[cubeOutsides.index] = LX
                        .hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
        } else if (time_elapsed > 25000/myspd) {
            for (LXPoint ln090 : model.points) {  // x+ only
                if (ln090.x > 0 && ln090.x < 130 && ln090.y > 40 && ln090.y < 400 && ln090.z > -40 && ln090.z < 20) {
                    // dummy = 0;
                      colors[ln090.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln270 : model.points) {   // x only
                if (ln270.x > -130 && ln270.x < 0 && ln270.y > 40 && ln270.y < 400 && ln270.z > -20 && ln270.z < 20) {
                    dummy = 0;
                   colors[ln270.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }

        } else if (time_elapsed > 24000/myspd) {
            for (LXPoint ln000 : model.points) {  // z+ only
                if (ln000.x > -40 && ln000.x < 20 && ln000.y > 40 && ln000.y < 400 && ln000.z > 0 && ln000.z < 130) {
                 dummy = 0 ;
                 //   colors[ln000.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln180 : model.points) { // z only
                if (ln180.x > -20 && ln180.x < 30  && ln180.y > 40 && ln180.y < 400 && ln180.z > -180 && ln180.z < 0) {
                    // dummy = 0;
                   colors[ln180.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }

        } else if (time_elapsed > 23000/myspd) {
            for (LXPoint ln315 : model.points) {   // angle line 315 degrees
                if (ln315.x > -130 && ln315.x < 0 && ln315.y > 40 && ln315.y < 400 && ln315.z > -0 && ln315.z < 100
                  && (Math.abs(ln315.x)-Math.abs(ln315.z)<10)  && Math.abs(ln315.z) > 25 && Math.abs(ln315.x) > 25 ) {
               // dummy = 0; 
                 colors[ln315.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              } 
              for (LXPoint ln225 : model.points) {   // angle line 315 degrees
               if (ln225.x > -130 && ln225.x < 0 && ln225.y > 40 && ln225.y < 400 && ln225.z > -1800 && ln225.z < 0
                   && (Math.abs(ln225.x)-Math.abs(ln225.z)<10)  && Math.abs(ln225.z) > 25 && Math.abs(ln225.x) > 25 ) {
                  // dummy = 0; 
               colors[ln225.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
               } 
              for (LXPoint ln045 : model.points) {  // angle line 45 degrees
                 if (ln045.x > 0 && ln045.x < 130 && ln045.y > 40 && ln045.y < 400 && ln045.z > -40 && ln045.z < 100
                  && (Math.abs(ln045.x)-Math.abs(ln045.z)<10)  && Math.abs(ln045.z) > 25 && Math.abs(ln045.x) > 25 ) {
                      // dummy = 0;
                        colors[ln045.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
              for (LXPoint ln135 : model.points) {  // angle line 45 degrees
                  if (ln135.x > 0 && ln135.x < 130 && ln135.y > 40 && ln135.y < 400 && ln135.z > -180 && ln135.z < 0
                  && (Math.abs(ln135.x)-Math.abs(ln135.z)<10)  && Math.abs(ln135.z) > 25 && Math.abs(ln135.x) > 25  ) {
                      // dummy = 0;
                        colors[ln135.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
  
        } else if (time_elapsed > 22000/myspd) {
            for (LXPoint ln090 : model.points) {  // x+ only
                if (ln090.x > 0 && ln090.x < 130 && ln090.y > 40 && ln090.y < 400 && ln090.z > -40 && ln090.z < 20) {
                    // dummy = 0;
                  colors[ln090.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln270 : model.points) {   // x only
                if (ln270.x > -130 && ln270.x < 0 && ln270.y > 40 && ln270.y < 400 && ln270.z > -20 && ln270.z < 20) {
                 //   dummy = 0;
                   colors[ln270.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln000 : model.points) {  // z+ only
                if (ln000.x > -40 && ln000.x < 20 && ln000.y > 40 && ln000.y < 400 && ln000.z > 0 && ln000.z < 130) {
                 // dummy = 0 ;
                    colors[ln000.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln180 : model.points) { // z only
                if (ln180.x > -20 && ln180.x < 30  && ln180.y > 40 && ln180.y < 400 && ln180.z > -180 && ln180.z < 0) {
                    // dummy = 0;
                   colors[ln180.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
    
        } else if (time_elapsed > 21000/myspd) {
            for (LXPoint ln315 : model.points) {   // angle line 315 degrees
                if (ln315.x > -130 && ln315.x < 0 && ln315.y > 40 && ln315.y < 400 && ln315.z > -0 && ln315.z < 100
                  && (Math.abs(ln315.x)-Math.abs(ln315.z)<10)  && Math.abs(ln315.z) > 25 && Math.abs(ln315.x) > 25 ) {
               // dummy = 0; 
                 colors[ln315.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              } 
              for (LXPoint ln225 : model.points) {   // angle line 315 degrees
               if (ln225.x > -130 && ln225.x < 0 && ln225.y > 40 && ln225.y < 400 && ln225.z > -1800 && ln225.z < 0
                   && (Math.abs(ln225.x)-Math.abs(ln225.z)<10)  && Math.abs(ln225.z) > 25 && Math.abs(ln225.x) > 25 ) {
                  // dummy = 0; 
               colors[ln225.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
               } 
  
        } else if (time_elapsed > 20000/myspd) {
            for (LXPoint ln045 : model.points) {  // angle line 45 degrees
                if (ln045.x > 0 && ln045.x < 130 && ln045.y > 40 && ln045.y < 400 && ln045.z > -40 && ln045.z < 100
                 && (Math.abs(ln045.x)-Math.abs(ln045.z)<10)  && Math.abs(ln045.z) > 25 && Math.abs(ln045.x) > 25 ) {
                     // dummy = 0;
                       colors[ln045.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                 } //
             }
             for (LXPoint ln135 : model.points) {  // angle line 45 degrees
                 if (ln135.x > 0 && ln135.x < 130 && ln135.y > 40 && ln135.y < 400 && ln135.z > -180 && ln135.z < 0
                 && (Math.abs(ln135.x)-Math.abs(ln135.z)<10)  && Math.abs(ln135.z) > 25 && Math.abs(ln135.x) > 25  ) {
                     // dummy = 0;
                       colors[ln135.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                 } //
             }
 
        } else if (time_elapsed > 19000/myspd) {
            for (LXPoint ln000 : model.points) {  // z+ only
                if (ln000.x > -40 && ln000.x < 20 && ln000.y > 40 && ln000.y < 400 && ln000.z > 0 && ln000.z < 130) {
                //   dummy = 0 ;
                   colors[ln000.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln180 : model.points) { // z only
                if (ln180.x > -20 && ln180.x < 30  && ln180.y > 40 && ln180.y < 400 && ln180.z > -180 && ln180.z < 0) {
                    // dummy = 0;
                   colors[ln180.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }

        } else if (time_elapsed > 18000/myspd) {
            for (LXPoint ln315 : model.points) {   // angle line 315 degrees
                if (ln315.x > -130 && ln315.x < 0 && ln315.y > 40 && ln315.y < 400 && ln315.z > -0 && ln315.z < 100
                  && (Math.abs(ln315.x)-Math.abs(ln315.z)<10)  && Math.abs(ln315.z) > 25 && Math.abs(ln315.x) > 25 ) {
               // dummy = 0; 
                 colors[ln315.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              } 
              for (LXPoint ln225 : model.points) {   // angle line 315 degrees
               if (ln225.x > -130 && ln225.x < 0 && ln225.y > 40 && ln225.y < 400 && ln225.z > -1800 && ln225.z < 0
                   && (Math.abs(ln225.x)-Math.abs(ln225.z)<10)  && Math.abs(ln225.z) > 25 && Math.abs(ln225.x) > 25 ) {
                  // dummy = 0; 
               colors[ln225.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
               } 
        } else if (time_elapsed > 17000/myspd) {
            for (LXPoint ln090 : model.points) {  // x+ only
                if (ln090.x > 0 && ln090.x < 130 && ln090.y > 40 && ln090.y < 400 && ln090.z > -40 && ln090.z < 20) {
                    // dummy = 0;
                  colors[ln090.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln270 : model.points) {   // x only
                if (ln270.x > -130 && ln270.x < 0 && ln270.y > 40 && ln270.y < 400 && ln270.z > -20 && ln270.z < 20) {
                  //  dummy = 0;
                   colors[ln270.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }

        } else if (time_elapsed > 16000/myspd) {
            for (LXPoint ln045 : model.points) {  // angle line 45 degrees
                if (ln045.x > 0 && ln045.x < 130 && ln045.y > 40 && ln045.y < 400 && ln045.z > -40 && ln045.z < 100
                 && (Math.abs(ln045.x)-Math.abs(ln045.z)<10)  && Math.abs(ln045.z) > 25 && Math.abs(ln045.x) > 25 ) {
                     // dummy = 0;
                       colors[ln045.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                 } //
             }
             for (LXPoint ln135 : model.points) {  // angle line 45 degrees
                 if (ln135.x > 0 && ln135.x < 130 && ln135.y > 40 && ln135.y < 400 && ln135.z > -180 && ln135.z < 0
                 && (Math.abs(ln135.x)-Math.abs(ln135.z)<10)  && Math.abs(ln135.z) > 25 && Math.abs(ln135.x) > 25  ) {
                     // dummy = 0;
                       colors[ln135.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                 } //
             }

        } else if (time_elapsed > 15000/myspd) {
            for (LXPoint ln315 : model.points) {   // angle line 315 degrees
                if (ln315.x > -130 && ln315.x < 0 && ln315.y > 40 && ln315.y < 400 && ln315.z > -0 && ln315.z < 100
                  && (Math.abs(ln315.x)-Math.abs(ln315.z)<10)  && Math.abs(ln315.z) > 25 && Math.abs(ln315.x) > 25 ) {
               // dummy = 0; 
                 colors[ln315.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              } 
              for (LXPoint ln225 : model.points) {   // angle line 315 degrees
               if (ln225.x > -130 && ln225.x < 0 && ln225.y > 40 && ln225.y < 400 && ln225.z > -1800 && ln225.z < 0
                   && (Math.abs(ln225.x)-Math.abs(ln225.z)<10)  && Math.abs(ln225.z) > 25 && Math.abs(ln225.x) > 25 ) {
                  // dummy = 0; 
               colors[ln225.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
               } 
              for (LXPoint ln045 : model.points) {  // angle line 45 degrees
                 if (ln045.x > 0 && ln045.x < 130 && ln045.y > 40 && ln045.y < 400 && ln045.z > -40 && ln045.z < 100
                  && (Math.abs(ln045.x)-Math.abs(ln045.z)<10)  && Math.abs(ln045.z) > 25 && Math.abs(ln045.x) > 25 ) {
                      // dummy = 0;
                        colors[ln045.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
              for (LXPoint ln135 : model.points) {  // angle line 45 degrees
                  if (ln135.x > 0 && ln135.x < 130 && ln135.y > 40 && ln135.y < 400 && ln135.z > -180 && ln135.z < 0
                  && (Math.abs(ln135.x)-Math.abs(ln135.z)<10)  && Math.abs(ln135.z) > 25 && Math.abs(ln135.x) > 25  ) {
                      // dummy = 0;
                        colors[ln135.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
        } else if (time_elapsed > 14000/myspd) {
            for (LXPoint ln090 : model.points) {  // x+ only
                if (ln090.x > 0 && ln090.x < 130 && ln090.y > 40 && ln090.y < 400 && ln090.z > -40 && ln090.z < 20) {
                    // dummy = 0;
                  colors[ln090.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln270 : model.points) {   // x only
                if (ln270.x > -130 && ln270.x < 0 && ln270.y > 40 && ln270.y < 400 && ln270.z > -20 && ln270.z < 20) {
                  //  dummy = 0;
                   colors[ln270.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln000 : model.points) {  // z+ only
                if (ln000.x > -40 && ln000.x < 20 && ln000.y > 40 && ln000.y < 400 && ln000.z > 0 && ln000.z < 130) {
                //   dummy = 0 ;
                   colors[ln000.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln180 : model.points) { // z only
                if (ln180.x > -20 && ln180.x < 30  && ln180.y > 40 && ln180.y < 400 && ln180.z > -180 && ln180.z < 0) {
                    // dummy = 0;
                   colors[ln180.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }

        } else if (time_elapsed > 13000/myspd) {
            for (LXPoint cubeOutsides : model.points) {
                if ((cubeOutsides.x < -80 || cubeOutsides.x > 80)
                    && (cubeOutsides.y < 30 || cubeOutsides.y > 150)
                    && (cubeOutsides.z < -20 || cubeOutsides.z > 20)) {
                    // colors[cubeOutsides.index] = LX.hsb(30, 75, 100);}
                    colors[cubeOutsides.index] = LX
                        .hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
        } else if (time_elapsed > 12000/myspd) {
            for (LXPoint ln315 : model.points) {   // angle line 315 degrees
                if (ln315.x > -130 && ln315.x < 0 && ln315.y > 40 && ln315.y < 400 && ln315.z > -0 && ln315.z < 100
                  && (Math.abs(ln315.x)-Math.abs(ln315.z)<10)  && Math.abs(ln315.z) > 25 && Math.abs(ln315.x) > 25 ) {
               // dummy = 0; 
                 colors[ln315.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              } 
              for (LXPoint ln225 : model.points) {   // angle line 315 degrees
               if (ln225.x > -130 && ln225.x < 0 && ln225.y > 40 && ln225.y < 400 && ln225.z > -1800 && ln225.z < 0
                   && (Math.abs(ln225.x)-Math.abs(ln225.z)<10)  && Math.abs(ln225.z) > 25 && Math.abs(ln225.x) > 25 ) {
                  // dummy = 0; 
               colors[ln225.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
               } 
              for (LXPoint ln045 : model.points) {  // angle line 45 degrees
                 if (ln045.x > 0 && ln045.x < 130 && ln045.y > 40 && ln045.y < 400 && ln045.z > -40 && ln045.z < 100
                  && (Math.abs(ln045.x)-Math.abs(ln045.z)<10)  && Math.abs(ln045.z) > 25 && Math.abs(ln045.x) > 25 ) {
                      // dummy = 0;
                        colors[ln045.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
              for (LXPoint ln135 : model.points) {  // angle line 45 degrees
                  if (ln135.x > 0 && ln135.x < 130 && ln135.y > 40 && ln135.y < 400 && ln135.z > -180 && ln135.z < 0
                  && (Math.abs(ln135.x)-Math.abs(ln135.z)<10)  && Math.abs(ln135.z) > 25 && Math.abs(ln135.x) > 25  ) {
                      // dummy = 0;
                        colors[ln135.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
        } else if (time_elapsed > 11000/myspd) {
            for (LXPoint cubeOutsides : model.points) {
                if ((cubeOutsides.x < -80 || cubeOutsides.x > 80)
                    && (cubeOutsides.y < 30 || cubeOutsides.y > 150)
                    && (cubeOutsides.z < -20 || cubeOutsides.z > 20)) {
                    // colors[cubeOutsides.index] = LX.hsb(30, 75, 100);}
                    colors[cubeOutsides.index] = LX
                        .hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
        } else if (time_elapsed > 10000/myspd) {
            for (LXPoint ln000 : model.points) {  // z+ only
                if (ln000.x > -40 && ln000.x < 20 && ln000.y > 40 && ln000.y < 400 && ln000.z > 0 && ln000.z < 130) {
                //   dummy = 0 ;
                   colors[ln000.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln180 : model.points) { // z only
                if (ln180.x > -20 && ln180.x < 30  && ln180.y > 40 && ln180.y < 400 && ln180.z > -180 && ln180.z < 0) {
                    // dummy = 0;
                   colors[ln180.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
        } else if (time_elapsed > 9000/myspd) {
            for (LXPoint cubeOutsides : model.points) {
                if ((cubeOutsides.x < -80 || cubeOutsides.x > 80)
                    && (cubeOutsides.y < 30 || cubeOutsides.y > 150)
                    && (cubeOutsides.z < -20 || cubeOutsides.z > 20)) {
                    // colors[cubeOutsides.index] = LX.hsb(30, 75, 100);}
                    colors[cubeOutsides.index] = LX
                        .hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
        } else if (time_elapsed > 8000/myspd) {
            for (LXPoint ln090 : model.points) {  // x+ only
                if (ln090.x > 0 && ln090.x < 130 && ln090.y > 40 && ln090.y < 400 && ln090.z > -40 && ln090.z < 20) {
                    // dummy = 0;
                  colors[ln090.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln270 : model.points) {   // x only
                if (ln270.x > -130 && ln270.x < 0 && ln270.y > 40 && ln270.y < 400 && ln270.z > -20 && ln270.z < 20) {
                  //  dummy = 0;
                   colors[ln270.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
        } else if (time_elapsed > 7000/myspd) {
            for (LXPoint cubeOutsides : model.points) {
                if ((cubeOutsides.x < -80 || cubeOutsides.x > 80)
                    && (cubeOutsides.y < 30 || cubeOutsides.y > 150)
                    && (cubeOutsides.z < -20 || cubeOutsides.z > 20)) {
                    // colors[cubeOutsides.index] = LX.hsb(30, 75, 100);}
                    colors[cubeOutsides.index] = LX
                        .hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
        } else if (time_elapsed > 6000/myspd) {
            for (LXPoint ln090 : model.points) {  // x+ only
                if (ln090.x > 0 && ln090.x < 130 && ln090.y > 40 && ln090.y < 400 && ln090.z > -40 && ln090.z < 20) {
                    // dummy = 0;
                  colors[ln090.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln270 : model.points) {   // x only
                if (ln270.x > -130 && ln270.x < 0 && ln270.y > 40 && ln270.y < 400 && ln270.z > -20 && ln270.z < 20) {
                  //  dummy = 0;
                   colors[ln270.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln000 : model.points) {  // z+ only
                if (ln000.x > -40 && ln000.x < 20 && ln000.y > 40 && ln000.y < 400 && ln000.z > 0 && ln000.z < 130) {
                //   dummy = 0 ;
                   colors[ln000.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln180 : model.points) { // z only
                if (ln180.x > -20 && ln180.x < 30  && ln180.y > 40 && ln180.y < 400 && ln180.z > -180 && ln180.z < 0) {
                    // dummy = 0;
                   colors[ln180.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }

        } else if (time_elapsed > 5000/myspd) {
            for (LXPoint cubeOutsides : model.points) {
                if ((cubeOutsides.x < -80 || cubeOutsides.x > 80)
                    && (cubeOutsides.y < 30 || cubeOutsides.y > 150)
                    && (cubeOutsides.z < -20 || cubeOutsides.z > 20)) {
                    // colors[cubeOutsides.index] = LX.hsb(30, 75, 100);}
                    colors[cubeOutsides.index] = LX
                        .hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
        } else if (time_elapsed > 4000/myspd) {
            for (LXPoint ln315 : model.points) {   // angle line 315 degrees
                if (ln315.x > -130 && ln315.x < 0 && ln315.y > 40 && ln315.y < 400 && ln315.z > -0 && ln315.z < 100
                  && (Math.abs(ln315.x)-Math.abs(ln315.z)<10)  && Math.abs(ln315.z) > 25 && Math.abs(ln315.x) > 25 ) {
               // dummy = 0; 
                 colors[ln315.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              } 
              for (LXPoint ln225 : model.points) {   // angle line 315 degrees
               if (ln225.x > -130 && ln225.x < 0 && ln225.y > 40 && ln225.y < 400 && ln225.z > -1800 && ln225.z < 0
                   && (Math.abs(ln225.x)-Math.abs(ln225.z)<10)  && Math.abs(ln225.z) > 25 && Math.abs(ln225.x) > 25 ) {
                  // dummy = 0; 
               colors[ln225.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
               } 
              for (LXPoint ln045 : model.points) {  // angle line 45 degrees
                 if (ln045.x > 0 && ln045.x < 130 && ln045.y > 40 && ln045.y < 400 && ln045.z > -40 && ln045.z < 100
                  && (Math.abs(ln045.x)-Math.abs(ln045.z)<10)  && Math.abs(ln045.z) > 25 && Math.abs(ln045.x) > 25 ) {
                      // dummy = 0;
                        colors[ln045.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
              for (LXPoint ln135 : model.points) {  // angle line 45 degrees
                  if (ln135.x > 0 && ln135.x < 130 && ln135.y > 40 && ln135.y < 400 && ln135.z > -180 && ln135.z < 0
                  && (Math.abs(ln135.x)-Math.abs(ln135.z)<10)  && Math.abs(ln135.z) > 25 && Math.abs(ln135.x) > 25  ) {
                      // dummy = 0;
                        colors[ln135.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
  
        } else if (time_elapsed > 3000/myspd) {
            for (LXPoint ln090 : model.points) {  // x+ only
                if (ln090.x > 0 && ln090.x < 130 && ln090.y > 40 && ln090.y < 400 && ln090.z > -40 && ln090.z < 20) {
                    // dummy = 0;
                  colors[ln090.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln270 : model.points) {   // x only
                if (ln270.x > -130 && ln270.x < 0 && ln270.y > 40 && ln270.y < 400 && ln270.z > -20 && ln270.z < 20) {
                  //  dummy = 0;
                   colors[ln270.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
        } else if (time_elapsed > 2000/myspd) {
            for (LXPoint ln315 : model.points) {   // angle line 315 degrees
                if (ln315.x > -130 && ln315.x < 0 && ln315.y > 40 && ln315.y < 400 && ln315.z > -0 && ln315.z < 100
                  && (Math.abs(ln315.x)-Math.abs(ln315.z)<10)  && Math.abs(ln315.z) > 25 && Math.abs(ln315.x) > 25 ) {
               // dummy = 0; 
                 colors[ln315.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              } 
              for (LXPoint ln225 : model.points) {   // angle line 315 degrees
               if (ln225.x > -130 && ln225.x < 0 && ln225.y > 40 && ln225.y < 400 && ln225.z > -1800 && ln225.z < 0
                   && (Math.abs(ln225.x)-Math.abs(ln225.z)<10)  && Math.abs(ln225.z) > 25 && Math.abs(ln225.x) > 25 ) {
                  // dummy = 0; 
               colors[ln225.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
               } 
              for (LXPoint ln045 : model.points) {  // angle line 45 degrees
                 if (ln045.x > 0 && ln045.x < 130 && ln045.y > 40 && ln045.y < 400 && ln045.z > -40 && ln045.z < 100
                  && (Math.abs(ln045.x)-Math.abs(ln045.z)<10)  && Math.abs(ln045.z) > 25 && Math.abs(ln045.x) > 25 ) {
                      // dummy = 0;
                        colors[ln045.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
              for (LXPoint ln135 : model.points) {  // angle line 45 degrees
                  if (ln135.x > 0 && ln135.x < 130 && ln135.y > 40 && ln135.y < 400 && ln135.z > -180 && ln135.z < 0
                  && (Math.abs(ln135.x)-Math.abs(ln135.z)<10)  && Math.abs(ln135.z) > 25 && Math.abs(ln135.x) > 25  ) {
                      // dummy = 0;
                        colors[ln135.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                  } //
              }
  
        } else if (time_elapsed > 1000/myspd) {
            for (LXPoint ln000 : model.points) {  // z+ only
                if (ln000.x > -40 && ln000.x < 20 && ln000.y > 40 && ln000.y < 400 && ln000.z > 0 && ln000.z < 130) {
                //   dummy = 0 ;
                   colors[ln000.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln180 : model.points) { // z only
                if (ln180.x > -20 && ln180.x < 30  && ln180.y > 40 && ln180.y < 400 && ln180.z > -180 && ln180.z < 0) {
                    // dummy = 0;
                   colors[ln180.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }

        } else {
            for (LXPoint ln090 : model.points) {  // x+ only
                if (ln090.x > 0 && ln090.x < 130 && ln090.y > 40 && ln090.y < 400 && ln090.z > -40 && ln090.z < 20) {
                    // dummy = 0;
                  colors[ln090.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln270 : model.points) {   // x only
                if (ln270.x > -130 && ln270.x < 0 && ln270.y > 40 && ln270.y < 400 && ln270.z > -20 && ln270.z < 20) {
                  //  dummy = 0;
                   colors[ln270.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln000 : model.points) {  // z+ only
                if (ln000.x > -40 && ln000.x < 20 && ln000.y > 40 && ln000.y < 400 && ln000.z > 0 && ln000.z < 130) {
                //   dummy = 0 ;
                   colors[ln000.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }
            for (LXPoint ln180 : model.points) { // z only
                if (ln180.x > -20 && ln180.x < 30  && ln180.y > 40 && ln180.y < 400 && ln180.z > -180 && ln180.z < 0) {
                    // dummy = 0;
                   colors[ln180.index] = LX.hsb(colorOfCubes.getValuef(), 50, 100);
                } //
            }

        } // else end
        if (time_elapsed > 30000/myspd) {
            time_elapsed = 0;
        }

    }
}