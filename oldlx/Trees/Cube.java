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

    // CubeSizeIndex goes here
    public static final int[] PIXELS_PER_CUBE = { 1, 4, 6 }; // Tiny cubes actually have less
    public static final float[] CUBE_SIZES = { 5f, 5f, 9f };

    /**
     * Index of this cube in color buffer, colors[cube.index]
     */
    public final int index;

    /**
     * Index indicating which sculpture this cube lives inside.
     * These are only unique per pieceType.
     */
    public final int sculptureIndex;

    /**
     * Enum stating what kind of Piece this is a part of: TREE, SHRUB, FAIRY_CIRCLE
     */
    public final PieceType pieceType;

    /**
     * String for the ID of the piece, such as "medium tree" or whatever
     * Used by canopy primarily - human readable
     */
    public final String pieceId;

    /**
     * As string comparisons are too expensive to be done in the hot loop,
     * this index is available. You can find string indexes by looking in
     * the map and array which has lists of piece IDs.
     * Used by canopy.
     * This is harder to know at constructor time, regrettably.
     */
    public int pieceIndex;

    /*
    ** number of inches across the cube is for the renderer
    */

    public final float size;

    /*
    ** number of output pixels for the cube
    */
    public final int pixels;

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
     * Point of the cube in the form (theta, y) relative to (center of tree base) - global or local???
     */
    public float transformedY;
    public float transformedTheta;
    public Vec2D transformedCylinderPoint;

    BaseCube(Vec3D globalPosition, Vec3D sculpturePosition, int sculptureIdx, PieceType pieceType, String pieceId, int cubeSizeIndex ) {
        super(Arrays.asList(new LXPoint[] { new LXPoint(globalPosition.x, globalPosition.y, globalPosition.z) }));
        this.index = this.points.get(0).index;
        this.sculptureIndex = sculptureIdx;
        this.pieceType = pieceType;
        this.pieceId = pieceId;
        this.pieceIndex = -1;
        this.size = CUBE_SIZES[cubeSizeIndex];
        this.pixels = PIXELS_PER_CUBE[cubeSizeIndex];
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

enum PieceType {
  TREE,
  SHRUB,
  FAIRY_CIRCLE
}
