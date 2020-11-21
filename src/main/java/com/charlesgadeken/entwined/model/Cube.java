package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.model.config.TreeCubeConfig;
import toxi.geom.Vec3D;

public class Cube extends BaseCube {
    // Tiny cubes actually have less, but for Entwined we want to tell the NDB that everything is 6
    public static final int[] PIXELS_PER_CUBE = {6, 6, 6, 12, 12};
    public static final float[] CUBE_SIZES = {4f, 7.5f, 11.25f, 15f, 16.5f};
    /** Size of this cube, one of SMALL/MEDIUM/LARGE/GIANT */
    public final float size;

    public final int pixels;
    public TreeCubeConfig config = null;

    Cube(Vec3D globalPosition, Vec3D treePosition, TreeCubeConfig config) {
        super(globalPosition, treePosition);
        this.size = CUBE_SIZES[config.cubeSizeIndex];
        this.pixels = PIXELS_PER_CUBE[config.cubeSizeIndex];
        this.config = config;
    }
}
