package entwined.pattern.eric_gauderman;

import entwined.utils.SimplexNoise;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

public class FreeFall extends LXPattern {

    float height = 0.0f;
    final BoundedParameter velocityParam = new BoundedParameter("VEL", 10f,
        -100f, 100f);
    final BoundedParameter blobWidthParam = new BoundedParameter("WID", 90f,
        10f, 400f);
    final BoundedParameter blobHeightParam = new BoundedParameter("HGT", 60f,
        10f, 400f);
    // Size of simplex noise blobs. Number from 0 to 2.
    final BoundedParameter fillParam = new BoundedParameter("FIL", 25f, 0.001f,
        100f);

    public FreeFall(LX lx) {
        super(lx);
        addParameter("velocity", velocityParam);
        addParameter("blob_width", blobWidthParam);
        addParameter("blob_height", blobHeightParam);
        addParameter("fill", fillParam);
    }

    @Override
    protected void run(double deltaMs) {
        float velocity = velocityParam.getValuef() / 200f;
        float fill = fillParam.getValuef() / 50f;
        float blobWidth = blobWidthParam.getValuef();
        float blobHeight = blobHeightParam.getValuef();

        height -= deltaMs * velocity / blobHeight;

        float baseline = Math.max(0f, 1f - fill);
        float boost = Math.max(0f, fill - 1f);

        for (LXPoint point : model.points) {
            float noise1 = 0.5f
                + (float) SimplexNoise.noise(point.x / blobWidth,
                    point.z / blobWidth, height + point.y / blobHeight) / 2f;

            float cutoffNoise1 = Math.min(1.0f, Math.max(0.0f,
                (noise1 + boost - baseline) / (1.0f - boost - baseline)));

            colors[point.index] = LX.hsb(150, 100, cutoffNoise1 * 100f);
        }
    }
}
