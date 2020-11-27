package com.charlesgadeken.entwined.patterns.contributors.raySykes;

import com.charlesgadeken.entwined.Utilities;
import heronarts.lx.utils.LXUtils;
import java.util.ArrayList;
import org.apache.commons.lang3.ArrayUtils; // Only dep of apache commons is this file!

public class LightningLine {
    private final float treeBottomY = 100;
    private float[] yKeyPoints = {};
    private float[] thetaKeyPoints = {};
    private int lifeCycleState = 0;
    private final int startTime;
    private final float startY;
    private final float propagationSpeed;
    private final float lineWidth;
    private float wideningStartTime = 0;

    @SuppressWarnings("unchecked")
    private ArrayList<LightningLine> forks = new ArrayList();

    LightningLine(
            int startTime,
            float startY,
            float startTheta,
            float basicAngle,
            float propagationSpeed,
            float lineWidth,
            int recursionDepthLeft,
            float forkingChance) {
        this.propagationSpeed = propagationSpeed;
        this.lineWidth = lineWidth;
        this.startY = startY;
        this.startTime = startTime;
        float y = startY;
        float theta = startTheta;
        float straightLineTheta;
        addKeyPoint(y, theta);
        while (y > treeBottomY) {
            y -= (25 + Utilities.random(75));
            if (y > 450) {
                theta = startTheta - 20 + Utilities.random(40);
            } else {
                straightLineTheta =
                        startTheta
                                + Utilities.sin((Utilities.TWO_PI / 360) * basicAngle)
                                        * (startY - y)
                                        * 0.9f;
                theta = straightLineTheta - 50 + Utilities.random(100);
            }
            addKeyPoint(y, theta);
            if (recursionDepthLeft > 0 && y < 500 && Utilities.random(20) < forkingChance) {
                forks.add(
                        new LightningLine(
                                startTime + (int) ((startY - y) / propagationSpeed),
                                y,
                                theta,
                                (-basicAngle * Utilities.random(2)),
                                propagationSpeed,
                                (lineWidth - Utilities.random(2)),
                                recursionDepthLeft - 1,
                                forkingChance));
            }
        }
    }

    public float getLightningFactor(float yToCheck, float thetaToCheck) {
        float yLowerLimit = startY - (Utilities.millis() - startTime) * (propagationSpeed);
        if (lifeCycleState == 0 && yLowerLimit < treeBottomY) {
            lifeCycleState = 1;
            wideningStartTime = Utilities.millis();
        }
        if (lifeCycleState == 1 && Utilities.millis() > startTime + 2000 / propagationSpeed) {
            lifeCycleState = 2;
        }
        if (lifeCycleState > 1 || yLowerLimit > yToCheck) {
            return 0;
        }
        int i = 0;
        int keyPointIndex = -1;
        float result = 0;
        while (i < (yKeyPoints.length - 1)) {
            if (yKeyPoints[i] > yToCheck && yKeyPoints[i + 1] <= yToCheck) {
                keyPointIndex = i;
                i = yKeyPoints.length;
            }
            i++;
        }
        if (keyPointIndex >= 0) {
            float targetTheta =
                    thetaKeyPoints[keyPointIndex]
                            + (thetaKeyPoints[keyPointIndex + 1] - thetaKeyPoints[keyPointIndex])
                                    * (yKeyPoints[keyPointIndex] - yToCheck)
                                    / (yKeyPoints[keyPointIndex] - yKeyPoints[keyPointIndex + 1]);
            float thetaDelta = LXUtils.wrapdistf(targetTheta, thetaToCheck, 360);
            float thinnedLineWidth;
            if (lifeCycleState == 0) {
                thinnedLineWidth = lineWidth / 2;
            } else {
                thinnedLineWidth =
                        lineWidth
                                / (Utilities.max(
                                        1,
                                        2
                                                - propagationSpeed
                                                        * (Utilities.millis() - wideningStartTime)
                                                        / 500));
            }
            result = Utilities.max(0, 100 * (thinnedLineWidth - thetaDelta) / lineWidth);
        }
        for (i = 0; i < forks.size(); i++) {
            result = Utilities.max(result, forks.get(i).getLightningFactor(yToCheck, thetaToCheck));
        }
        return result;
    }

    private void addKeyPoint(float y, float theta) {
        yKeyPoints = ArrayUtils.add(yKeyPoints, y);
        thetaKeyPoints = ArrayUtils.add(thetaKeyPoints, theta);
    }

    public boolean isDead() {
        return lifeCycleState > 1;
    }
}
