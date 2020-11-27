package com.charlesgadeken.entwined.patterns.contributors.raySykes;

import com.charlesgadeken.entwined.Utilities;
import heronarts.lx.utils.LXUtils;

public class IceCrystalLine {
    protected int lifeCycleState = -1;
    private final int recursionDepth;
    private int startTime;
    private float startY;
    private float startTheta;
    private float endY;
    private float endTheta;
    private float propagationSpeed;
    private float lineLength;
    private float lineWidth;
    private int angleIndex;
    private int lifeCycleStateChangeTime;
    private final float[][] angleFactors = {
        {0, 1},
        {0.7071f, 0.7071f},
        {1, 0},
        {0.7071f, -0.7071f},
        {0, -1},
        {-0.7071f, -0.7071f},
        {-1, 0},
        {-0.7071f, 0.7071f}
    };
    private IceCrystalLine[] children = new IceCrystalLine[2];
    protected float[][] applicableRange = {{0, 0}, {0, 0}};
    private float nodeMeltRadius;
    protected boolean hasChildren = false;
    private IceCrystalSettings settings;

    IceCrystalLine(int recursionDepth, IceCrystalSettings settings) {
        this.recursionDepth = recursionDepth;
        this.settings = settings;
        if (recursionDepth < settings.maxRecursionDepth) {
            children[0] = new IceCrystalLine(recursionDepth + 1, settings);
            children[1] = new IceCrystalLine(recursionDepth + 1, settings);
        }
    }

    public void doStart(float startY, float startTheta, int angleIndex) {
        lifeCycleState = 0;
        this.angleIndex = angleIndex;
        this.startY = startY;
        this.startTheta = 360 + (startTheta % 360);
        this.propagationSpeed = settings.getPropagationSpeed(recursionDepth);
        lineLength = settings.getLineLength(recursionDepth);
        lineWidth = settings.getLineWidth(recursionDepth);
        startTime = Utilities.millis();
        doUpdate();
    }

    public void doReset() {
        lifeCycleState = -1;
        hasChildren = false;
        nodeMeltRadius = 0;
        if (recursionDepth < settings.maxRecursionDepth) {
            children[0].doReset();
            children[1].doReset();
        }
    }

    public void doUpdate() {
        switch (lifeCycleState) {
            case 0: // this line is growing
                float currentLineLength = (Utilities.millis() - startTime) * propagationSpeed / 10;
                if (currentLineLength > lineLength) {
                    currentLineLength = lineLength;
                    if (recursionDepth >= settings.totalRecursionDepth) {
                        settings.setGrowthFinished();
                        changeLifeCycleState(3);
                    } else {
                        changeLifeCycleState((endY < 0 || endY > 800) ? 3 : 1);
                    }
                }
                endTheta = startTheta + angleFactors[angleIndex][0] * currentLineLength;
                endY = startY + angleFactors[angleIndex][1] * currentLineLength;
                applicableRange[0][0] = Utilities.min(startTheta, endTheta) - lineWidth / 2;
                applicableRange[0][1] = Utilities.max(startTheta, endTheta) + lineWidth / 2;
                applicableRange[1][0] = Utilities.min(startY, endY) - lineWidth / 2;
                applicableRange[1][1] = Utilities.max(startY, endY) + lineWidth / 2;
                break;
            case 1: // creating children (wohoo!)
                children[0].doStart(endY, endTheta % 360, (8 + angleIndex - 1) % 8);
                children[1].doStart(endY, endTheta % 360, (angleIndex + 1) % 8);
                changeLifeCycleState(2);
                hasChildren = true;
                break;
            case 2: // has children that are growing
                checkRangeOfChildren();
                break;
            case 3: // frozen
                if (recursionDepth <= 3
                        && settings.growthFinished
                        && settings.growthFinishedTime
                                < (Utilities.millis() - 8000 / propagationSpeed)) {
                    changeLifeCycleState(4);
                }
                break;
            case 4: // melting
                nodeMeltRadius =
                        Utilities.pow(
                                (settings.totalRecursionDepth - recursionDepth)
                                        * (Utilities.millis() - lifeCycleStateChangeTime)
                                        * propagationSpeed
                                        / 7000,
                                2);
                applicableRange[0][0] =
                        Utilities.min(
                                applicableRange[0][0], Utilities.max(0, endTheta - nodeMeltRadius));
                applicableRange[0][1] =
                        Utilities.max(
                                applicableRange[0][1],
                                Utilities.min(720, endTheta + nodeMeltRadius));
                applicableRange[1][0] =
                        Utilities.min(
                                applicableRange[1][0], Utilities.max(100, (endY - nodeMeltRadius)));
                applicableRange[1][1] =
                        Utilities.max(
                                applicableRange[1][1], Utilities.min(700, (endY + nodeMeltRadius)));
                if (lifeCycleStateChangeTime < (Utilities.millis() - 27000 / propagationSpeed)) {
                    changeLifeCycleState(5);
                    children[0].doReset();
                    children[1].doReset();
                    hasChildren = false;
                }
                break;
            case 5: // water
                if (lifeCycleStateChangeTime < (Utilities.millis() - 8000 / propagationSpeed)) {
                    changeLifeCycleState(6);
                }
                break;
            case 6: // done
                break;
        }
        if (hasChildren && lifeCycleState >= 2 && lifeCycleState <= 4) {
            children[0].doUpdate();
            children[1].doUpdate();
            if (children[0].lifeCycleState == children[1].lifeCycleState
                    && lifeCycleState < children[0].lifeCycleState) {
                changeLifeCycleState(children[0].lifeCycleState);
            }
        }
    }

    public float getLineFactor(float yToCheck, float thetaToCheck) {
        float result = 0;
        if (lifeCycleState >= 5) {
            return 200;
        }
        if (yToCheck <= applicableRange[1][0] || yToCheck >= applicableRange[1][1]) {
            return result;
        }
        float adjustedTheta =
                thetaToCheck < applicableRange[0][0] ? thetaToCheck + 360 : thetaToCheck;
        if (!(adjustedTheta >= applicableRange[0][0] && adjustedTheta <= applicableRange[0][1])) {
            return result;
        }
        if (lifeCycleState == 4) {
            float distFromNode =
                    Utilities.sqrt(
                            Utilities.pow(Utilities.abs(endY - yToCheck), 2)
                                    + Utilities.pow(
                                            LXUtils.wrapdistf(endTheta, thetaToCheck, 360), 2));
            if (distFromNode < nodeMeltRadius) {
                result =
                        Utilities.min(
                                200, 100 + 150 * (nodeMeltRadius - distFromNode) / nodeMeltRadius);
            }
        }
        float lowestY = Utilities.min(startY, endY);
        float highestY = Utilities.max(startY, endY);
        if (Utilities.abs(angleFactors[angleIndex][1]) > 0) {
            if (yToCheck >= lowestY && yToCheck <= highestY) {
                float targetTheta =
                        startTheta
                                + (endTheta - startTheta) * (yToCheck - startY) / (endY - startY);
                float lineThetaWidth = lineWidth / (2 * Utilities.abs(angleFactors[angleIndex][1]));
                result =
                        Utilities.max(
                                result,
                                100
                                        * Utilities.max(
                                                0,
                                                (lineThetaWidth
                                                        - Utilities.abs(
                                                                LXUtils.wrapdistf(
                                                                        targetTheta,
                                                                        thetaToCheck,
                                                                        360))))
                                        / lineThetaWidth);
            }
        } else {
            float lowestTheta = Utilities.min(startTheta, endTheta);
            float highestTheta = Utilities.max(startTheta, endTheta);
            if (thetaToCheck < lowestTheta) {
                thetaToCheck += 360;
            }
            if (thetaToCheck >= lowestTheta && thetaToCheck <= highestTheta) {
                if (yToCheck <= lowestY && yToCheck >= lowestY - lineWidth / 2) {
                    result =
                            Utilities.max(
                                    result,
                                    100 * (lineWidth / 2 - (lowestY - yToCheck)) / (lineWidth / 2));
                }
                if (yToCheck >= highestY && yToCheck <= highestY + lineWidth / 2) {
                    result =
                            Utilities.max(
                                    result,
                                    100
                                            * (lineWidth / 2 - (yToCheck - highestY))
                                            / (lineWidth / 2));
                }
            }
        }
        if (lifeCycleState >= 2 && hasChildren) {
            result =
                    Utilities.max(
                            result,
                            Utilities.max(
                                    children[0].getLineFactor(yToCheck, thetaToCheck % 360),
                                    children[1].getLineFactor(yToCheck, thetaToCheck % 360)));
        }
        return result;
    }

    public void checkRangeOfChildren() {
        if (hasChildren) {
            for (int i = 0; i < children.length; i++) {
                for (int j = 0; j < 2; j++) {
                    applicableRange[j][0] =
                            Utilities.min(applicableRange[j][0], children[i].applicableRange[j][0]);
                    applicableRange[j][1] =
                            Utilities.max(applicableRange[j][1], children[i].applicableRange[j][1]);
                }
            }
        }
    }

    void changeLifeCycleState(int lifeCycleStateIn) {
        lifeCycleStateChangeTime = Utilities.millis();
        this.lifeCycleState = lifeCycleStateIn;
    }

    public boolean isDone() {
        return lifeCycleState == 6 || lifeCycleState == -1;
    }
}
