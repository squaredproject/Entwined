package entwined.pattern.eric_gauderman;

import entwined.utils.EntwinedUtils;
import entwined.utils.PerlinNoise;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;


public class CounterSpin extends LXPattern {
    float time = 0;
    final PerlinNoise perlinNoise = new PerlinNoise();
    // Each value will count down from one to zero.
    final double[] treesSwirlProgress;
    final double[] treesColorOffset;
    final double[] shrubsSwirlProgress;
    final double[] shrubsColorOffset;
    final double[] fairyCirclesSwirlProgress;
    final double[] fairyCirclesColorOffset;
    final double swirlDurationMs = 2000;
    final double progressSpeed = 1 / swirlDurationMs;

    // XXX - should have an on model changed handler here, or this is going to
    // vomit when we add an element.
    public CounterSpin(LX lx) {
        super(lx);
        int nTrees = model.sub("TREE").size();
        int nShrubs = model.sub("SHRUB").size();
        int nFairyCircles = model.sub("FAIRY_CIRCLE").size();
        treesSwirlProgress = new double[nTrees];
        treesColorOffset = new double[nTrees];
        shrubsSwirlProgress = new double[nShrubs];
        shrubsColorOffset = new double[nShrubs];
        fairyCirclesSwirlProgress = new double[nFairyCircles];
        fairyCirclesColorOffset = new double[nFairyCircles];

        for (int treeIdx = 0; treeIdx < model.sub("TREE").size(); treeIdx++) {
            treesColorOffset[treeIdx] = EntwinedUtils.random(360);
        }
        for (int shrubIdx = 0; shrubIdx < model.sub("SHRUB").size(); shrubIdx++) {
            shrubsColorOffset[shrubIdx] = EntwinedUtils.random(360);
        }
        for (int fairyCircleIdx = 0; fairyCircleIdx < model.sub("FAIRY_CIRCLE").size(); fairyCircleIdx++) {
            fairyCirclesColorOffset[fairyCircleIdx] = EntwinedUtils.random(360);
        }
    }

    @Override
    protected void run(double deltaMs) {
        time += deltaMs;
        int treeIdx = 0;
        for (LXModel tree : model.sub("TREE")) {
            // float a = perlinNoise.noise(tree.x, tree.z, (float) time);
            if (Math.random() > 0.99) {
                treesSwirlProgress[treeIdx] = 1;
            } else {
                treesSwirlProgress[treeIdx] = Math.max(0, treesSwirlProgress[treeIdx] - deltaMs * progressSpeed);
            }
            for (LXPoint cube : tree.points) {
                colors[cube.index] = getColors(cube, treesSwirlProgress[treeIdx], treesColorOffset[treeIdx]);
            }
            treeIdx++;
        }
        int shrubIdx = 0;
        for (LXModel shrub : model.sub("SHRUB")) {
            if (Math.random() > 0.99) {
                shrubsSwirlProgress[shrubIdx] = 1;
            } else {
                shrubsSwirlProgress[shrubIdx] = Math.max(0,
                        shrubsSwirlProgress[shrubIdx] - deltaMs * progressSpeed);
            }
            for (LXPoint cube : shrub.points) {
                colors[cube.index] = getColors(cube, shrubsSwirlProgress[shrubIdx], shrubsColorOffset[shrubIdx]);
            }
            shrubIdx++;
        }
        int fairyCircleIdx = 0;
        for (LXModel fairyCircle : model.sub("FAIRY_CIRCLE")) {
            if (Math.random() > 0.99) {
                fairyCirclesSwirlProgress[fairyCircleIdx] = 1;
            } else {
                fairyCirclesSwirlProgress[fairyCircleIdx] = Math.max(0,
                        fairyCirclesSwirlProgress[fairyCircleIdx] - deltaMs * progressSpeed);
            }
            for (LXPoint cube : fairyCircle.points) {
                colors[cube.index] = getColors(cube, fairyCirclesSwirlProgress[fairyCircleIdx],
                        fairyCirclesColorOffset[fairyCircleIdx]);
            }
            fairyCircleIdx++;
        }
    }

    // XXX - watch it with theta here!! Should be the local theta I believe...
    int getColors(LXPoint cube, double swirlProgress, double colorOffset) {
        return LX.hsb(
                (float) (cube.theta + colorOffset - time / 7000 * 360),
                100,
                50 + 50 * (float)Math.sin(
                        time / 2000 * LX.TWO_PI + cube.theta * LX.TWO_PI / 360));

        // + Utils.map(cube.y, model.yMin, model.yMax) * Utils.PI
        // +
        // (float) fairyCirclesSwirlProgress[shrub.index] * Utils.TWO_PI
        // (float) (100 * fairyCirclesSwirlProgress[fairyCircle.index]));
    }
}




