package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.config.TreeOrShrub;
import com.google.common.collect.Lists;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import java.awt.geom.Point2D;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

/**
 * The parent Cube class, which is extended by the Tree and Shrub Cubes. This allows us to use a
 * single for loop to run through both sets of cubes. Cube configuration info is left out of the
 * parent Cube and left for child cubes to handle.
 */
public class BaseCube extends LXModel {
    /** Index of this cube in color buffer, colors[cube.index] */
    public final int index;

    /** Index indicating which sculpture this cube lives inside. */
    public final int sculptureIndex;

    /** Enum stating whether this cube is part of a TREE or SHRUB */
    public final TreeOrShrub treeOrShrub;

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

    /** Radial distance from sculpture center to field center (0, 0) in x-z plane */
    public final float r;

    /** Angle in degrees from cube center to center of tree in x-z plane */
    public final float theta;

    /**
     * Global radial distance from cube center to center of the field (0, 0) also the center of main
     * tree. x-z plane
     */
    public final float gr;

    /**
     * Global angle in degrees from cube center to center of the field (0, 0) also the center of
     * main tree. x-z plane
     */
    public final float globalTheta;

    /** Point of the cube in the form (theta, y) relative to center of tree base */
    public float transformedY;

    private float transformedTheta;
    public Vec2D transformedCylinderPoint;

    BaseCube(
            Vec3D globalPosition,
            Vec3D sculpturePosition,
            int sculptureIdx,
            TreeOrShrub treeOrShrub) {
        super(
                Lists.newArrayList(
                        new LXPoint(globalPosition.x, globalPosition.y, globalPosition.z)));
        this.index = this.points[0].index;
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
        this.theta =
                180
                        + 180
                                / Utilities.PI
                                * Utilities.atan2(sculpturePosition.z, sculpturePosition.x);
        this.gr = (float) Point2D.distance(this.x, this.z, 0, 0);
        // System.out.println("gr: " + this.gr);
        this.globalTheta =
                (float) Math.toDegrees(Math.atan2((double) (0 - this.z), (double) (0 - this.x)));
    }

    void resetTransform() {
        transformedTheta = theta;
        transformedY = y;
    }

    public void setTransformedTheta(float theta) {
        System.out.printf("θ set to %f\n", theta);
        transformedTheta = theta;
    }

    public float getTransformedTheta() {
        return this.transformedTheta;
    }

    void didTransform() {
        transformedCylinderPoint = new Vec2D(transformedTheta, transformedY);
    }
}
