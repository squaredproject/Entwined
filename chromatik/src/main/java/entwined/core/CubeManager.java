package entwined.core;

import entwined.utils.Vec2D;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

// Additional per-cube data required for Entwined

public class CubeManager implements LX.Listener {
    // Our sculpture is broken into discrete elements, and patterns
    // are often generated using the coordinate system of those elements.
    // Cache these values so we don't have to be constantly calculating them.
    //
    CubeData[] cubes;

    private static CubeManager theCubeManager = null;

    public static void init(LX lx) {
      theCubeManager = new CubeManager(lx);
    }

    public static CubeManager getCubeManager() {
      return theCubeManager;
    }

    public static CubeData getCube(int idx) {
      // XXX - throw if out of range
      return theCubeManager.cubes[idx];
    }

    private CubeManager(LX lx) {
      lx.addListener(this);
    }

    public void modelChanged(LX lx, LXModel model) {
      this.cubes = new CubeData[model.points.length];
      System.out.println("Model changed, new size is " + model.points.length);
      for (int i=0; i<model.points.length; i++) {
        this.cubes[i] = new CubeData();
      }

      // loop through all models and points....
      for (LXModel component : model.children) {
        for (LXPoint point : component.points) {
          CubeData cube = this.cubes[point.index];
          float localX = point.x - component.cx;  // XXX - should be component origin, but don't have that yet.
          float localZ = point.z - component.cz;  // XXX ditto
          float localY = point.y;                 // XXX ditto
          float XZR    = (float)Math.sqrt(localX*localX + localZ*localZ);
          float tempTheta = (float)Math.atan2(localZ, localX);
          int ry = 0;
          if (component.meta("ry") != null) {
            ry = Integer.parseInt(component.meta("ry"));
          }
          cube.localTheta = (180 + (180/LX.PIf)*tempTheta + ry) % 360;  // All theta here in degrees
          tempTheta = (float)Math.atan2(point.z,  point.x);
          cube.theta = (180 + (180/LX.PIf)*tempTheta) % 360;       // All theta here in degrees
          cube.localPhi = (float)Math.atan2(localY, XZR);
          cube.localX   = localX;
          cube.localY   = localY;
          cube.localZ   = localZ;
          cube.localR   = (float)Math.sqrt(localX*localX + localY*localY + localZ*localZ);
          cube.cylinderPoint = new Vec2D(cube.localTheta, cube.localY);
      }
    }
  }
}
