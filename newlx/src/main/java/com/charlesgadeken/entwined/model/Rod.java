package com.charlesgadeken.entwined.model;

import heronarts.lx.transform.LXTransform;
import toxi.geom.Vec3D;

public class Rod {
    public Vec3D mountingPoint;
    private double xKeyPoint;
    private double yKeyPoint;
    private double zKeyPoint;

    Rod(int rodPosition, int clusterMaxRodLength, int clusterIndex) {
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

        //            double ratio = (newX - xKeyPoint[keyPointIndex - 1]) /
        // (xKeyPoint[keyPointIndex] - xKeyPoint[keyPointIndex - 1]);
        //            double newY = yKeyPoint[keyPointIndex - 1] + ratio * (yKeyPoint[keyPointIndex]
        // - yKeyPoint[keyPointIndex - 1])
        //                    + clusterBaseHeight;
        //            double newZ = zKeyPoint[keyPointIndex - 1] + ratio * (zKeyPoint[keyPointIndex]
        // - zKeyPoint[keyPointIndex - 1]);
        //            transform.push();
        //            transform.translate((float) newX, (float) newY, (float) newZ);

        transform.push();
        transform.translate((float) xKeyPoint, (float) yKeyPoint, (float) zKeyPoint);
        this.mountingPoint = new Vec3D(transform.x(), transform.y(), transform.z());
        transform.pop();

        //        List<Vec3D> _availableMountingPoints = new ArrayList<Vec3D>();
        //        LXTransform transform = new LXTransform();
        //        transform.rotateY(rotationalPosition * 45 * (Utils.PI / 180));
        //        double newX = xKeyPoints[0] + 2;
        //        while (newX < xKeyPoints[NUM_KEYPOINTS - 1]) {
        //            int keyPointIndex = 0;
        //            while (xKeyPoints[keyPointIndex] < newX && keyPointIndex < NUM_KEYPOINTS) {
        //                keyPointIndex++;
        //            }
        //            if (keyPointIndex < NUM_KEYPOINTS) {
        //                double ratio = (newX - xKeyPoints[keyPointIndex - 1]) /
        // (xKeyPoints[keyPointIndex] - xKeyPoints[keyPointIndex - 1]);
        //                double newY = yKeyPoints[keyPointIndex - 1] + ratio *
        // (yKeyPoints[keyPointIndex] - yKeyPoints[keyPointIndex - 1])
        //                        + layerBaseHeight;
        //                double newZ = zKeyPoints[keyPointIndex - 1] + ratio *
        // (zKeyPoints[keyPointIndex] - zKeyPoints[keyPointIndex - 1]);
        //                transform.push();
        //                transform.translate((float) newX, (float) newY, (float) newZ);
        //                _availableMountingPoints.add(new Vec3D(transform.x(), transform.y(),
        // transform.z()));
        //                transform.pop();
        //                transform.push();
        //                transform.translate((float) newX, (float) newY, (float) (-newZ));
        //                _availableMountingPoints.add(new Vec3D(transform.x(), transform.y(),
        // transform.z()));
        //                transform.pop();
        //            }
        //            newX += holeSpacing;
        //        }
        //        this.availableMountingPoints =
        // Collections.unmodifiableList(_availableMountingPoints);

    }
}
