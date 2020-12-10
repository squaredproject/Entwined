import heronarts.lx.LX;


class AADebuggingPattern extends TSPattern {
    public AADebuggingPattern(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().getFader().getNormalized() == 0) return;

        //DoubleSummaryStatistics stats = model.baseCubes.stream().mapToDouble(BaseCube::getTransformedTheta).summaryStatistics();
        //System.out.println(stats.getMin());
       // System.out.println(stats.getMax());

        for (BaseCube cube : model.baseCubes) {
            // float ndTT = (float) ((cube.getTransformedTheta() - stats.getMin()) / (stats.getMax() - stats.getMin()));
            colors[cube.index] = LX.hsb((float) cube.transformedTheta, 100, 100);
        }
    }
}
