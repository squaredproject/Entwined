package com.charlesgadeken.entwined.model.shrub;

import heronarts.lx.transform.LXTransform;
import toxi.geom.Vec3D;

public class Rod {
    public Vec3D mountingPoint;
    private double xKeyPoint;
    private double yKeyPoint;
    private double zKeyPoint;

    public Rod(int rodPosition, int clusterMaxRodLength, int clusterIndex) {
        int rodIndex = rodPosition;
        zKeyPoint = (clusterMaxRodLength - (rodIndex * 6));

        switch (rodPosition) {
            case 0:
                xKeyPoint = .75 * zKeyPoint;
                yKeyPoint = .75 * zKeyPoint - 1;
                break;
            case 1:
                xKeyPoint = .75 * zKeyPoint;
                yKeyPoint = .75 * zKeyPoint + 1;
                break;
            case 2:
                xKeyPoint = .75 * zKeyPoint - 2;
                yKeyPoint = .75 * zKeyPoint - 2;
                break;
            case 3:
                xKeyPoint = .75 * zKeyPoint - 2;
                yKeyPoint = .75 * zKeyPoint + 2;
                break;
            case 4:
                xKeyPoint = .75 * zKeyPoint - 2;
                yKeyPoint = .75 * zKeyPoint;
                break;
            default:
                break;
        }

        LXTransform transform = new LXTransform();
        // clockwise, starting at the longest left-most cluster

        // A -> 0, 1
        // B -> 2, 3, 10, 11
        // C -> 4, 5, 8, 9
        // D -> 6, 7

        transform.rotateY(clusterIndex * 0.5236);

        transform.push();
        transform.translate((float) xKeyPoint, (float) yKeyPoint, (float) zKeyPoint);
        this.mountingPoint = new Vec3D(transform.x(), transform.y(), transform.z());
        transform.pop();
    }
}
