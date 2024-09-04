package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.Utilities;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import toxi.geom.Vec3D;

public class EntwinedBranch {
    /**
     * This defines the available mounting points on a given branch variation. The variable names
     * and ratios for the keypoints reflect what is in the CAD drawings for the branches
     */
    public List<Vec3D> availableMountingPoints;

    private static final int NUM_KEYPOINTS = 5;
    private double[] xKeyPoints = new double[NUM_KEYPOINTS];
    private double[] yKeyPoints = new double[NUM_KEYPOINTS];
    private double[] zKeyPoints = new double[NUM_KEYPOINTS];
    private static final double holeSpacing = 8;

    EntwinedBranch(int canopyMajorLength, int rotationalPosition, int layerBaseHeight) {
        int rotationIndex =
                rotationalPosition > 4 ? 4 - rotationalPosition % 4 : rotationalPosition;
        float canopyScaling = canopyMajorLength / 180;
        double branchLengthRatios[] = {0.37, 0.41, 0.50, 0.56, 0.63};
        double heightAdjustmentFactors[] = {1.0, 0.96, 0.92, 0.88, 0.85};
        double branchLength = canopyMajorLength * branchLengthRatios[rotationIndex];
        xKeyPoints[4] = branchLength;
        xKeyPoints[3] = branchLength * 0.917;
        xKeyPoints[2] = branchLength * 0.623;
        xKeyPoints[1] = branchLength * 0.315;
        xKeyPoints[0] = canopyScaling * 12;
        yKeyPoints[4] = 72 * heightAdjustmentFactors[rotationIndex];
        yKeyPoints[3] = 72 * 0.914 * heightAdjustmentFactors[rotationIndex];
        yKeyPoints[2] = 72 * 0.793 * heightAdjustmentFactors[rotationIndex];
        yKeyPoints[1] = (72 * 0.671 + 6) * heightAdjustmentFactors[rotationIndex];
        yKeyPoints[0] = (72 * 0.455 + 8) * heightAdjustmentFactors[rotationIndex];
        zKeyPoints[4] = branchLength * 0.199;
        zKeyPoints[3] = branchLength * 0.13;
        zKeyPoints[2] = 0;
        zKeyPoints[1] = branchLength * (-0.08);
        zKeyPoints[0] = branchLength * (-0.05);
        List<Vec3D> _availableMountingPoints = new ArrayList<Vec3D>();
        LXTransform transform = new LXTransform();
        transform.rotateY(rotationalPosition * 45 * (Utilities.PI / 180));
        double newX = xKeyPoints[0] + 2;
        while (newX < xKeyPoints[NUM_KEYPOINTS - 1]) {
            int keyPointIndex = 0;
            while (xKeyPoints[keyPointIndex] < newX && keyPointIndex < NUM_KEYPOINTS) {
                keyPointIndex++;
            }
            if (keyPointIndex < NUM_KEYPOINTS) {
                double ratio =
                        (newX - xKeyPoints[keyPointIndex - 1])
                                / (xKeyPoints[keyPointIndex] - xKeyPoints[keyPointIndex - 1]);
                double newY =
                        yKeyPoints[keyPointIndex - 1]
                                + ratio
                                        * (yKeyPoints[keyPointIndex]
                                                - yKeyPoints[keyPointIndex - 1])
                                + layerBaseHeight;
                double newZ =
                        zKeyPoints[keyPointIndex - 1]
                                + ratio
                                        * (zKeyPoints[keyPointIndex]
                                                - zKeyPoints[keyPointIndex - 1]);
                transform.push();
                transform.translate((float) newX, (float) newY, (float) newZ);
                _availableMountingPoints.add(
                        new Vec3D(transform.x(), transform.y(), transform.z()));
                transform.pop();
                transform.push();
                transform.translate((float) newX, (float) newY, (float) (-newZ));
                _availableMountingPoints.add(
                        new Vec3D(transform.x(), transform.y(), transform.z()));
                transform.pop();
            }
            newX += holeSpacing;
        }
        this.availableMountingPoints = Collections.unmodifiableList(_availableMountingPoints);
    }
}
