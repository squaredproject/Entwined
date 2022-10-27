package entwined.pattern.kyle_fleming;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BoundedParameter;

public class ScrambleEffect extends LXEffect {

  final BoundedParameter amount = new BoundedParameter("SCRA");
  final int offset;

  public ScrambleEffect(LX lx) {
    super(lx);
    addParameter("amount", amount);

    offset = lx.getModel().size / 4 + 5;
  }

  int getAmount() {
    return (int)(amount.getValue() * lx.getModel().size / 2);
  }

  @Override
  protected void run(double deltaMs, double amount) {
    for (LXModel tree : model.sub("TREE")) {
      for (int i = EntwinedUtils.min(tree.points.length - 1, getAmount()); i > 0; i--) {
        colors[tree.points[i].index] = colors[tree.points[(i + offset) % tree.points.length].index];
      }
    }
    for (LXModel shrub : model.sub("SHRUB")) {
        for (int i = EntwinedUtils.min(shrub.points.length - 1, getAmount()); i > 0; i--) {
          colors[shrub.points[i].index] = colors[shrub.points[(i + offset) % shrub.points.length].index];
        }
      }
  }
}
