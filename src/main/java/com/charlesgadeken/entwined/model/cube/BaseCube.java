package com.charlesgadeken.entwined.model.cube;

import com.charlesgadeken.entwined.Utilities;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import java.awt.geom.Point2D;
import java.util.Arrays;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

public class BaseCube extends LXModel {

    /** Index of this cube in color buffer, colors[cube.index] */
    public final int index;

    /** Size of this cube, one of SMALL/MEDIUM/LARGE/GIANT */
    // public final float size;
    //
    // public final int pixels;

    /** Global x-position of center of cube */
    public final float x;

    /** Global y-position of center of cube */
    public final float y;

    /** Global z-position of center of cube */
    public final float z;

    /** Pitch of cube, in degrees, relative to cluster */
    public final float rx;

    /** Yaw of cube, in degrees, relative to cluster, after pitch */
    public final float ry;

    /** Roll of cube, in degrees, relative to cluster, after pitch+yaw */
    public final float rz;

    /** x-position of cube, relative to center of tree base */
    public final float sx;

    /** y-position of cube, relative to center of tree base */
    public final float sy;

    /** z-position of cube, relative to center of tree base */
    public final float sz;

    /** Radial distance from cube center to center of tree in x-z plane */
    public final float r;

    /** Angle in degrees from cube center to center of tree in x-z plane */
    public final float theta;

    /** Point of the cube in the form (theta, y) relative to center of tree base */
    public float transformedY;

    public float transformedTheta;
    public Vec2D transformedCylinderPoint;
    // public CubeConfig config = null;

    public BaseCube(Vec3D globalPosition, Vec3D sculpturePosition) {
        super(
                Arrays.asList(
                        new LXPoint[] {
                            new LXPoint(globalPosition.x, globalPosition.y, globalPosition.z)
                        }));
        this.index = this.points[0].index;
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
        this.theta =
                180
                        + 180
                                / Utilities.PI
                                * Utilities.atan2(sculpturePosition.z, sculpturePosition.x);
    }

    void resetTransform() {
        transformedTheta = theta;
        transformedY = y;
    }

    void didTransform() {
        transformedCylinderPoint = new Vec2D(transformedTheta, transformedY);
    }
}
