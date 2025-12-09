package entwined.pattern.grant_patterson;

import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

/**
Testing position, theta, distance from center
*/
@LXComponent.Hidden
class GrantTest extends LXPattern {
 final DiscreteParameter whichParam = new DiscreteParameter("which", 0, 0, 23);
 final CompoundParameter angleParam = new CompoundParameter("angle", 0, 0, 2*Math.PI);
 final CompoundParameter sizeParam = new CompoundParameter("size", .5, .1, 5);
 final CompoundParameter distParam = new CompoundParameter("dist", .5, 0, 1);

 private class Grower {
   LXModel sculpture;
   float x;
   float z;
   // int index;
   // boolean isTree;
   float minR = Float.MAX_VALUE;
   float maxR = 0;

   public Grower(LXModel sculpture) {
     this.sculpture = sculpture;
     if (sculpture.tags.contains("TREE") || sculpture.tags.contains("SHRUB") ) {
       x = sculpture.cx;
       z = sculpture.cz;
     }

       /*
     isTree = sculpture instanceof Tree;
     if (sculpture instanceof Tree) {
       x = ((Tree)sculpture).x;
       z = ((Tree)sculpture).z;
       index = ((Tree)sculpture).index;
     } else if (sculpture instanceof Shrub) {
       x = ((Shrub)sculpture).x;
       z = ((Shrub)sculpture).z;
       index = ((Shrub)sculpture).index;
     }
     */

     for (LXPoint c : this.cubes()) {
       if (minR > c.r) {
         minR = c.r;
       }
       if (maxR < c.r) {
         maxR = c.r;
       }
     }
   }

   LXPoint[] cubes() {
     if (sculpture.tags.contains("TREE") || sculpture.tags.contains("SHRUB") ) {
       return sculpture.points;
     }
     return null;
   }
 }

 ArrayList<Grower> growers = new ArrayList<Grower>();
 int printedGrower = -1;

 public GrantTest(LX lx) {
   super(lx);
   addParameter("which", whichParam);
   addParameter("angle", angleParam);
   addParameter("size", sizeParam);
   addParameter("dist", distParam);

   for (LXModel tree : model.sub("TREE")) {
     growers.add(new Grower(tree));
   }
   for (LXModel shrub : model.sub("SHRUB")) {
     growers.add(new Grower(shrub));
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
 public void run(double deltaMs) {
   clearColors();

   int which = (int)whichParam.getValue();
   Grower g = growers.get(which);

   // NB - I do not have the sculpture index close at hand any more.. CSW
   //if (printedGrower != which) {
   //  System.out.println("" + g.isTree + "" + g.index + " (" + g.x + "," + g.z + ") [" + g.minR + "," + g.maxR + "]");
   //}

   for (LXPoint c : model.points) {
     if (Math.abs(c.x - g.x) < sizeParam.getValue() * 100) {
       colors[c.index] = LXColor.lightest(colors[c.index], LX.hsb(0, 100, 50));
     }
     if (Math.abs(c.z - g.z) < sizeParam.getValue() * 100) {
       colors[c.index] = LXColor.lightest(colors[c.index], LX.hsb(240, 100, 50));
     }
   }

   for (LXPoint c : g.cubes()) {
     double theta = Math.atan2(c.z-g.z, c.x-g.x) / Math.PI * 180;
     if (angleDiff(theta * Math.PI / 180, angleParam.getValue()) < Math.PI / 8
       && Math.abs(c.r - (g.minR + (g.maxR-g.minR)*distParam.getValuef())) < (g.maxR-g.minR) / 5) {
       colors[c.index] = LXColor.lightest(colors[c.index], LX.hsb(120, 100, 100));
     }
     if (printedGrower != which) {
       System.out.println(" " + c.index + " (" + c.x + "," + c.z + ")r" + c.r);
     }
   }

   //if (printedGrower != which) {
   //  System.out.println("" + g.isTree + "" + g.index + " (" + g.x + "," + g.z + ") [" + g.minR + "," + g.maxR + "]");
   //}

  printedGrower = which;
 }
}