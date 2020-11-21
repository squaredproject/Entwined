import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

import heronarts.lx.LX;
import heronarts.lx.LXLayer;
import heronarts.lx.LXLoopTask;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

/**
* The parent Cube class, which is extended by the Tree and Shrub Cubes.
* This allows us to use a single for loop to run through both sets of cubes.
* Cube configuration info is left out of the parent Cube and left for child cubes to handle.
*/
class BaseCube extends LXModel {

    // public static final int[] PIXELS_PER_CUBE = { 6, 6, 6, 12, 12 }; // Tiny cubes actually have less, but for Entwined we want to
    //                                                                  // tell the NDB that everything is 6
    // public static final float[] CUBE_SIZES = { 4f, 7.5f, 11.25f, 15f, 16.5f };

    /**
     * Index of this cube in color buffer, colors[cube.index]
     */
    public final int index;

    /**
     * Index indicating which sculpture this cube lives inside.
     */
    public final int sculptureIndex;

    /**
     * Enum stating whether this cube is part of a TREE or SHRUB
     */
    public final TreeOrShrub treeOrShrub;

    /**
     * Global x-position of center of cube
     */
    public final float x;

    /**
     * Global y-position of center of cube
     */
    public final float y;

    /**
     * Global z-position of center of cube
     */
    public final float z;

    /**
     * Pitch of cube, in degrees, relative to cluster
     */
    public final float rx;

    /**
     * Yaw of cube, in degrees, relative to cluster, after pitch
     */
    public final float ry;

    /**
     * Roll of cube, in degrees, relative to cluster, after pitch+yaw
     */
    public final float rz;

    /**
     * x-position of cube, relative to center of tree base
     */
    public final float sx;

    /**
     * y-position of cube, relative to center of tree base
     */
    public final float sy;

    /**
     * z-position of cube, relative to center of tree base
     */
    public final float sz;

    /**
     * Radial distance from sculpture center to field center (0, 0) in x-z plane
     */
    public final float r;

    /**
     * Angle in degrees from cube center to center of tree in x-z plane
     */
    public final float theta;

    /**
     * Global radial distance from cube center to center of the field (0, 0) also the center of main tree. x-z plane
     */
    public final float gr;

    /**
     * Global angle in degrees from cube center to center of the field (0, 0) also the center of main tree. x-z plane
     */
    public final float globalTheta;

    /**
     * Point of the cube in the form (theta, y) relative to center of tree base
     */
    public float transformedY;
    public float transformedTheta;
    public Vec2D transformedCylinderPoint;
    //public TreeCubeConfig config = null;

    BaseCube(Vec3D globalPosition, Vec3D sculpturePosition, int sculptureIdx, TreeOrShrub treeOrShrub) {
        super(Arrays.asList(new LXPoint[] { new LXPoint(globalPosition.x, globalPosition.y, globalPosition.z) }));
        this.index = this.points.get(0).index;
        this.sculptureIndex = sculptureIdx;
        this.treeOrShrub = treeOrShrub;
        this.rx = 0;
        this.ry = 0;
        this.rz = 0;
        this.x = globalPosition.x;
        this.y = globalPosition.y;
        this.z = globalPosition.z;
        this.sx = sculpturePosition.x;
        this.sy = sculpturePosition.y;
        this.sz = sculpturePosition.z;
        this.r = (float) Point2D.distance(sculpturePosition.x, sculpturePosition.z, 0, 0);
        this.theta = 180 + 180 / Utils.PI * Utils.atan2(sculpturePosition.z, sculpturePosition.x);
        this.gr = (float) Point2D.distance(this.x, this.z, 0, 0);
        System.out.println("gr: " + this.gr);
        this.globalTheta = (float) Math.toDegrees(Math.atan2((double)(0 - this.z), (double)(0 - this.x)));
    }

    void resetTransform() {

        transformedTheta = theta;
        transformedY = y;
    }

    void didTransform() {
        transformedCylinderPoint = new Vec2D(transformedTheta, transformedY);
    }
}

enum TreeOrShrub {
  TREE,
  SHRUB
}
