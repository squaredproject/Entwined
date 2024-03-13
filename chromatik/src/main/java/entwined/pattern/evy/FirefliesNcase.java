package entwined.pattern.evy;

import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import entwined.core.TSBufferedPattern;
import entwined.utils.EntwinedUtils;

//inspired by https://ncase.me/fireflies/
//TODO in the future if we add interactivity that supports this,
// maybe someone could click a button on a shrub and that would make
// that shrub light up (by changing the shrub's lightUpOffset)
public class FirefliesNcase extends TSBufferedPattern {

  final CompoundParameter hue = new CompoundParameter("HUE", 52, 0, 360);
  final SawLFO cycle = new SawLFO(0, 100, 3000); // fireflies flash every 3 seconds
  // neighboring fireflies must be within this distance to have their clock move forward
  final CompoundParameter flyRadius = new CompoundParameter("FLY_RADIUS", 1000, 0, 2000);

  // maps the shrub id to the offset from `cycle`, that determines when it uniquely lights up
  static final Map<Integer, Float> lightUpOffset = new HashMap<Integer, Float>();
  // keeps track of if a shrub is lit up, so we can change nearby shrubs' offsets during transitions
  static final Map<Integer, Boolean> isLitUp = new HashMap<Integer, Boolean>();


  // To reset approximately every `resetInterval` ms, we run a saw wave over that time period.
  // If `reset == false` and `resetTimer < 0` then we switch `reset = true` and reset.
  // Once `resetTimer > 0` we flip `reset` back to false, to be ready for the next cycle.
  // (If anyone has any ideas for cleaner ways to do this I'd love to hear them, since this feels convoluted.)
  final CompoundParameter resetInterval = new CompoundParameter("RESET_INTERVAL", 10000, 0, 30000);
  final SawLFO resetTimer = new SawLFO(-1, 1, resetInterval);
  static Boolean reset = true; // start with true since we run `reset` in the constructor

  void reset() {
   for (int shrubIdx=0; shrubIdx<model.sub("SHRUB").size(); ++shrubIdx) {
     lightUpOffset.put(shrubIdx, EntwinedUtils.random(0, 100));
     isLitUp.put(shrubIdx, false);
   }
  }

  public FirefliesNcase(LX lx) {
   super(lx);
   addModulator(cycle).start();
   addParameter("hue", hue);
   addParameter("resetInterval", resetInterval);
   addModulator("resetTimer", resetTimer).start();
   reset();
  }

  Boolean closeEnough(float fromX, float toX, float fromZ, float toZ){
   float dx = fromX - toX;
   float dz = fromZ - toZ;
   float dist = dx*dx + dz*dz;
   return (dist < flyRadius.getValuef()*flyRadius.getValuef());
  }

  // This is the pattern loop, which will run continuously via LX
  @Override
  public void bufferedRun(double deltaMs) {
     // see above comment for how reset works
     if (reset == false && resetTimer.getValuef() < 0) {
       reset();
       reset = true;
     }
     else if (reset == true && resetTimer.getValuef() > 0) {
       reset = false;
     }

     int shrubIdx = 0;
     for (LXModel shrub : model.sub("SHRUB")) {
       for (LXPoint cube : shrub.points) {
         float sculptureCycleValue = (cycle.getValuef() + lightUpOffset.get(shrubIdx)) % 100;

         // shrubs are lit when the cycle value is between 0 and 40
         // (influenced by both their own random number and the cycling modulator)

         if (0 < sculptureCycleValue && sculptureCycleValue < 40) {
           colors[cube.index] = LX.hsb(hue.getValuef(), 100, (40 - sculptureCycleValue)/40 * 100);

           // When a shrub transitions to being lit up, loop through all shrubs and move the
           // clocks of close ones forward.
           // I copied this from https://github.com/ncase/fireflies/blob/gh-pages/js/index.js#L234
           // and though its logic seems sort of sketchy, it does look cool, so... seems fine.
           if(!isLitUp.get(shrubIdx)) {
             isLitUp.put(shrubIdx, true);
             int subShrubIdx = 0;
             for (LXModel subShrub : model.sub("SHRUB")) {
               if (subShrubIdx == shrubIdx) continue;
               if (closeEnough(subShrub.cx, cube.x, subShrub.cz, cube.z)) {
                 float newOffset = (lightUpOffset.get(subShrubIdx) + 5  * lightUpOffset.get(subShrubIdx)/100);
                 if (newOffset > 100) {
                   newOffset = 100; // this feels like cheating, but it also doesn't work without it :shrug:
                 }
                 lightUpOffset.put(subShrubIdx, newOffset);
               }
               subShrubIdx++;
             }
           }
         }
         // this one's simpler - if their shrub isn't lit up, then it's dark
         else {
           colors[cube.index] = LX.hsb(0, 0, 0);
           if (isLitUp.get(shrubIdx)) {
             isLitUp.put(shrubIdx, false);
           }
         }
       }
       shrubIdx++;
     }
   }
}



