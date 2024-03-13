package entwined.pattern.grant_patterson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

import entwined.core.TSBufferedPattern;
import entwined.utils.SimplexNoise;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

/**
Interconnected growth
*/
public class Growth extends TSBufferedPattern {

 final CompoundParameter growthSpeedParam = new CompoundParameter("spd", 2, .1, 10);
 final CompoundParameter lifeSpeedParam = new CompoundParameter("life", 5, 0, 10);
 final CompoundParameter fertilityParam = new CompoundParameter("fert", 1.15, 1, 3);
 final CompoundParameter angleParam = new CompoundParameter("angl", 60, 30, 180);

 private class Root {
   // Width of fade at start/end of growing/dying root
   final static double fadeDist = 30;

   Grower src;
   Grower dest;
   double angle;
   double srcHue;
   double srcSat;
   double hueDelta;
   double satDelta;
   // <0: pre-life; 0=>1: growing; 1=>(lifeSpeedParam): changing; (lifeSpeedParam)=>(lifeSpeedParam+1): dying
   double age = 0;
   boolean dead = false;
   float srcMinR = Float.MAX_VALUE;
   float srcMaxR = 0;
   float destMinR = Float.MAX_VALUE;
   float destMaxR = 0;
   public Root(Grower src, Grower dest, Root parent){
     this.src = src;
     this.dest = dest;
     src.roots.add(this);
     // Add a random amount of delay before we grow.
     //age = -1 * Math.random() * (1 + lifeSpeedParam.getValue());
     if (parent != null) {
       srcHue = parent.srcHue + parent.hueDelta;
       srcSat = parent.srcSat + parent.satDelta;
     } else {
       srcHue = Math.random() * 360;
       srcSat = Math.sqrt(Math.random()) * 100;
     }
     // Keep hue increasing so we don't always float around some hue
     hueDelta = Math.random() * 60;
     // Vary saturation by 25 (out of 100) on each root
     satDelta = Math.max(0, Math.min(100, srcSat + (Math.random() - 0.5) * 50)) - srcSat;

     for (LXPoint c : src.cubes()) {
       if (c.r < srcMinR) {
         srcMinR = c.r;
       }
       if (c.r > srcMaxR) {
         srcMaxR = c.r;
       }
     }
     if (dest != null) {
       for (LXPoint c : dest.cubes()) {
         if (c.r < destMinR) {
           destMinR = c.r;
         }
         if (c.r > destMaxR) {
           destMaxR = c.r;
         }
       }
       dest.roots.add(this);
       angle = (Math.atan2(dest.z - src.z, dest.x - src.x) + 2*Math.PI) % (2*Math.PI);
     } else {
       destMinR = 0;
       destMaxR = 0;
       angle = Math.random() * 2 * Math.PI;
     }
   }

   boolean shouldRenderCube(Grower g, LXPoint c) {
     return angleDiff(Math.atan2(c.z-g.z, c.x-g.x), (angle + (g == src ? 0 : Math.PI)) % (2*Math.PI)) < angleParam.getValue() / 2 * Math.PI / 180;
   }

   void runGrower(Grower g) {
     if (age < 0) {
       return;
     }
     double deathStartAge = lifeSpeedParam.getValue() + 1;
     // Total "length" of this root (we ignore the space between src and dest)
     double range = (srcMaxR - srcMinR) + (destMaxR - destMinR);
     for (LXPoint c : g.cubes()) {
       // If this cube is within the angle cutoff for this root,
       if (shouldRenderCube(g, c)) {
         // Cube's position within length of the root [0, 1]
         double cubePos = (g == src ? c.r - srcMinR : range - (c.r - destMinR)) / range;
         float hue = (float)(srcHue + hueDelta * cubePos) % 360;
         float sat = (float)(srcSat + satDelta * cubePos);
         double bright = 75 + SimplexNoise.noise(angle, age - cubePos) * 25;
         if (age < 1 || age >= deathStartAge) {
           // The position of the "wipe" which matches cube position [0, 1] but may be <0 if we're just
           // starting to grow the root or >1 if we're almost done killing it.
           double wipePos;
           if (age < 1) {
             // map age [0, 1] to [-fadeDist/range, 1]
             wipePos = age * (fadeDist/range + 1) - fadeDist/range;
           } else {
             // map (age-deathStartAge) [0, 1] to [0, 1+fadeDist/range]
             wipePos = (age-deathStartAge) * (fadeDist/range + 1);
           }
           // This cube's distance from the wipe
           double distance;
           // Growing: Everything behind the wipe is full on.
           if ((age < 1 && cubePos <= wipePos) ||
               // Dying: everything in front of the wipe is full on.
               (age >= deathStartAge && cubePos >= wipePos)) {
             distance = 0;
           } else {
             distance = Math.abs(cubePos - wipePos);
           }
           // I worked out this math...I think it's right...
           double brightChange = 100 * distance / fadeDist * range;
           bright = Math.max(0, bright - brightChange);
         }
         colors[c.index] = LXColor.screen(colors[c.index], LX.hsb(hue, sat, (float)bright));
       }
     }
   }

   public void run(double deltaMs) {
     runGrower(src);
     if (dest != null) {
       runGrower(dest);
     }

     double ageDelta = deltaMs * growthSpeedParam.getValue() / 1000;

     age += ageDelta;
     if (age >= 1 && age - ageDelta < 1 && dest != null) {
       // This root just entered its living phase! Tell dest to grow more.
       dest.growRoots(this);
     }
     dead = age > lifeSpeedParam.getValue() + 2;
     if (dead) {
       src.roots.remove(this);
       if (dest != null) {
         dest.roots.remove(this);
       }
     }
   }
 }

 private class Grower {
   LXModel sculpture;
   float x;
   float z;
   boolean isTree;
   ArrayList<Root> roots = new ArrayList<Root>();
   ArrayList<Grower> neighbors;

   public Grower(LXModel sculpture) {
     this.sculpture = sculpture;
     isTree = sculpture.tags.contains("TREE");
     if (isTree) {
       x = sculpture.cx;
       x = sculpture.cx;
     } else if (sculpture.tags.contains("SHRUB")) {
       x = sculpture.cx;
       z = sculpture.cz;
     }
   }

   LXPoint[] cubes() {
     if (sculpture.tags.contains("TREE")) {
       return sculpture.points;
     } else if (sculpture.tags.contains("SHRUB")) {
       return sculpture.points;
     }
     return null;
   }

   void growRoots(Root parent) {
     if (allRoots.size() > growers.size() * 10) {
       return;
     }
     double fertility = fertilityParam.getValue();
     int numNew = (int)Math.floor(fertility);
     if (Math.random() < fertility - numNew) {
       numNew++;
     }
     numNew = 1;
     for (int i = 0; numNew > 0 && i < neighbors.size(); i++) {
       // Sometimes skip a nearest neighbor, so we don't always grow to the same place
       /*if (Math.random() > .5) {
         continue;
       }
       if (Math.random() < 0.1) {
         // Sometimes grow a root in a random direction to nowhere
         allRoots.add(new Root(this, null, parent));
         numNew--;
         continue;
       }*/
       Grower g = neighbors.get(i);
       boolean alreadyLinked = false;
       // Trying this simpler metric: only draw roots to neighbors with no roots.
       alreadyLinked = g.roots.size() > 0;
       /*
       // Search for a root that already links this grower and its neighbor.
       for (Root r : g.roots) {
         if (r.src == this || r.dest == this) {
           // These growers are already linked; don't grow another root between them.
           alreadyLinked = true;
           break;
         }
       }*/
       if (!alreadyLinked) {
         allRoots.add(new Root(this, g, parent));
         numNew--;
         // Nothing below this, but put this here for completeness
         continue;
       }
     }
   }
 }

 public double distance(Grower a, Grower b) {
   return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.z - b.z, 2));
 }

 class SortByDistance implements Comparator<Grower> {
   Grower target;
   public SortByDistance(Grower target) {
     this.target = target;
   }
   public int compare(Grower a, Grower b) {
     return (int)(distance(target, a) - distance(target, b));
   }
 }

 ArrayList<Grower> growers = new ArrayList<Grower>();
 ArrayList<Root> allRoots = new ArrayList<Root>();

 public Growth(LX lx) {
   super(lx);
   addParameter("growthSpeed", growthSpeedParam);
   addParameter("lifeSpeed", lifeSpeedParam);
   addParameter("fertility", fertilityParam);
   addParameter("angle", angleParam);

   for (LXModel tree : model.sub("TREE")) {
     if (tree.points.length == 0) {
       continue;
     }
     growers.add(new Grower(tree));
   }
   for (LXModel shrub : model.sub("SHRUB")) {
     if (shrub.points.length == 0) {
       continue;
     }
     growers.add(new Grower(shrub));
   }
   for (Grower g : growers) {
     g.neighbors = new ArrayList<Grower>(growers);
     Collections.sort(g.neighbors, new SortByDistance(g));
     g.neighbors.remove(0);
   }
 }

 double angleDiff(double a1, double a2) {
   double diff = Math.abs(a1 - a2);
   if (diff > Math.PI) {
     diff = 2*Math.PI - diff;
   }
   return diff;
 }

 @Override
 public void bufferedRun(double deltaMs) {
   clearColors();

   for (int i = 0; i < allRoots.size(); i++) {
     allRoots.get(i).run(deltaMs);
   }

   ListIterator<Root> iter = allRoots.listIterator();
   while (iter.hasNext()) {
     Root r = iter.next();
     if (r.dead) {
       iter.remove();
     }
   }

   if (allRoots.size() == 0) {
     for (int i = 0; i < Math.ceil(fertilityParam.getValue()); i++) {
       growers.get((int)Math.floor(Math.random() * growers.size())).growRoots(null);
     }
   }
 }
}
