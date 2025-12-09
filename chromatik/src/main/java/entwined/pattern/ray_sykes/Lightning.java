package entwined.pattern.ray_sykes;

import java.util.ArrayList;

import entwined.core.CubeManager;
import entwined.core.TSTriggerablePattern;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.ModelBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.utils.LXUtils;


public class Lightning extends TSTriggerablePattern {
  int nShrubs = model.sub("SHRUB").size();
  int nTrees = model.sub("TREE").size();
  private LightningLine[] bolts = new LightningLine[nTrees + nShrubs];
  final CompoundParameter boltAngle = new CompoundParameter("Angle", 35, 0, 55);
  final CompoundParameter propagationSpeed = new CompoundParameter("Speed", 10, 0.5, 20);
  final CompoundParameter maxBoltWidth = new CompoundParameter("Width", 60, 20, 150);
  final CompoundParameter lightningChance = new CompoundParameter("Chance", 5, 1, 10);
  final CompoundParameter forkingChance = new CompoundParameter("Fork", 3, 1, 10);
  final BooleanParameter firesOnBeat = new BooleanParameter("Beat");
  int[] randomCheckTimeOuts = new int[nTrees + nShrubs];
  private final ModelBuffer myBuffer = new ModelBuffer(lx, LXColor.BLACK);


  public Lightning(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;
    for (int i=0; i < (nTrees + nShrubs); i++){
      bolts[i] = makeBolt();
      randomCheckTimeOuts[i] = 0;
    }
    addParameter("boltAngle", boltAngle);
    addParameter("propagationSpeed", propagationSpeed);
    addParameter("maxBoltWidth", maxBoltWidth);
    addParameter("lightningChance", lightningChance);
    addParameter("forkingChance", forkingChance);
    addParameter("firesOnBeat", firesOnBeat);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    this.myBuffer.copyTo(getBuffer());  // deal with the fact that we don't touch all the pixels


    int treeIndex = 0;

    float lightningChanceVal = lightningChance.getValuef();

    if (!triggered) {
      boolean running = false;
      for (LXModel tree : model.sub("TREE")) {
        if (!bolts[treeIndex].isDead()) {
          running = true;
          break;
        }
        treeIndex++;
      }
      if (!running) {
        enabled.setValue(false);
        // setCallRun(false);
      }
    }

    treeIndex = 0;
    for (LXModel tree : model.sub("TREE")){
      if (triggered) {
        if (bolts[treeIndex].isDead()) {
          if (firesOnBeat.isOn()) {
            if (lx.engine.tempo.beat()) {
              randomCheckTimeOuts[treeIndex] = EntwinedUtils.millis() + 100;
              bolts[treeIndex] = makeBolt();
            }
          } else {
            if (randomCheckTimeOuts[treeIndex] < EntwinedUtils.millis()){
              randomCheckTimeOuts[treeIndex] = EntwinedUtils.millis() + 100;
              if (EntwinedUtils.random(15) < lightningChance.getValuef()){
                bolts[treeIndex] = makeBolt();
              }
            }
          }
        }
      }
      for (LXPoint cube : tree.points) {
        float localTheta = CubeManager.getCube(lx, cube.index).localTheta;
        float localY = CubeManager.getCube(lx, cube.index).localY;
        float hueVal = 300;
        float lightningFactor = bolts[treeIndex].getLightningFactor(localY, localTheta);
        float brightVal = lightningFactor;
        float satVal;
        if (lightningFactor < 20){
          hueVal = 300;
          satVal = 100;
        }
        else if (lightningFactor < 50){
          hueVal = 280;
          satVal = 100;
        }
        else {
          hueVal = 280;
          satVal = (100 - 2 * (lightningFactor - 50)) % 100;
        }
        colors[cube.index] = LX.hsb(hueVal,  satVal, brightVal % 100);
      }
      treeIndex ++;
    }

    int shrubIndex = nTrees;  // Shrubs are indexed after trees
    if (!triggered) {
      boolean running = false;
      for (LXModel shrub : model.sub("SHRUB")) {
        if (!bolts[shrubIndex].isDead()) {
          running = true;
          break;
        }
        shrubIndex++;
      }
      if (!running) {
        enabled.setValue(false);
        // setCallRun(false);
      }
    }

    shrubIndex = nTrees;  // ditto above
    for (LXModel shrub : model.sub("SHRUB")){
      if (triggered) {
        if (bolts[shrubIndex].isDead()) {
          if (firesOnBeat.isOn()) {
            if (lx.engine.tempo.beat()) {
              randomCheckTimeOuts[shrubIndex] = EntwinedUtils.millis() + 100;
              bolts[shrubIndex] = makeBolt();
            }
          } else {
            if (randomCheckTimeOuts[shrubIndex] < EntwinedUtils.millis()){
              randomCheckTimeOuts[shrubIndex] = EntwinedUtils.millis() + 100;
              if (EntwinedUtils.random(15) < lightningChanceVal){
                bolts[shrubIndex] = makeBolt();
              }
            }
          }
        }
      }
      for (LXPoint cube : shrub.points) {
        float localTheta = CubeManager.getCube(lx, cube.index).localTheta;
        float localY = CubeManager.getCube(lx, cube.index).localY;
        float hueVal = 300;
        float lightningFactor = bolts[shrubIndex].getLightningFactor(localY, localTheta);
        float brightVal = lightningFactor;
        float satVal;
        if (lightningFactor < 20){
          hueVal = 300;
          satVal = 100;
        }
        else if (lightningFactor < 50){
          hueVal = 280;
          satVal = 100;
        }
        else {
          hueVal = 280;
          satVal = (100 - 2 * (lightningFactor - 50)) % 100;
        }
        colors[cube.index] = LX.hsb(hueVal,  satVal, brightVal % 100);
      }
      shrubIndex ++;
    }
    this.myBuffer.copyFrom(getBuffer());
  }

  LightningLine makeBolt(){
    float maxBoltWidthVal = maxBoltWidth.getValuef();
    float theta = 45 * (int) EntwinedUtils.random(8);
    float boltWidth = (maxBoltWidthVal + EntwinedUtils.random(maxBoltWidthVal)) / 2;
    return new LightningLine (EntwinedUtils.millis(), 550, theta, boltAngle.getValuef(), propagationSpeed.getValuef(), boltWidth, 3, forkingChance.getValuef());
  }

  @Override
  public void onTriggered() {
    super.onTriggered();

    propagationSpeed.setNormalized(1.0);  // XXX this used to be strength, from the drumpad strength. Giving up on that for now.

    int treeIndex = 0;

    for (LXModel tree : model.sub("TREE")){
      if (bolts[treeIndex].isDead()){
        randomCheckTimeOuts[treeIndex] = EntwinedUtils.millis() + 100;
        bolts[treeIndex] = makeBolt();
      }
      treeIndex ++;
    }
    int shrubIndex = nTrees;

    for (LXModel shrub : model.sub("SHRUB")){
      if (bolts[shrubIndex].isDead()){
        randomCheckTimeOuts[shrubIndex] = EntwinedUtils.millis() + 100;
        bolts[shrubIndex] = makeBolt();
      }
      shrubIndex ++;
    }
  }
}


class LightningLine {
  private final float treeBottomY = 100;
  // private float[] yKeyPoints = {};
  private ArrayList<Float> yKeyPoints = new ArrayList<Float>();
  //private float[] thetaKeyPoints = {};
  private ArrayList<Float> thetaKeyPoints = new ArrayList<Float>();
  private int lifeCycleState = 0;
  private final int startTime;
  private final float startY;
  private final float propagationSpeed;
  private final float lineWidth;
  private float wideningStartTime = 0;

  private ArrayList<LightningLine> forks = new ArrayList<LightningLine>();

  LightningLine(int startTime, float startY, float startTheta, float basicAngle, float propagationSpeed, float lineWidth, int recursionDepthLeft, float forkingChance){
    this.propagationSpeed = propagationSpeed;
    this.lineWidth = lineWidth;
    this.startY = startY;
    this.startTime = startTime;
    float y = startY;
    float theta = startTheta;
    float straightLineTheta;
    addKeyPoint(y, theta);
    while (y > treeBottomY){
      y -= (25 + EntwinedUtils.random(75));
      if (y > 450){
        theta = startTheta - 20 + EntwinedUtils.random(40);
      }
      else {
        straightLineTheta = startTheta + EntwinedUtils.sin((LX.TWO_PIf/360) * basicAngle) * (startY - y) * 0.9f;
        theta = straightLineTheta - 50 + EntwinedUtils.random(100);
      }
      addKeyPoint(y, theta);
      if (recursionDepthLeft > 0 && y < 500 && EntwinedUtils.random(20) < forkingChance){
        forks.add(new LightningLine(startTime + (int)((startY - y) / propagationSpeed), y, theta, (-basicAngle * EntwinedUtils.random(2)), propagationSpeed, (lineWidth - EntwinedUtils.random(2)), recursionDepthLeft - 1, forkingChance));
      }
    }
  }
  public float getLightningFactor (float yToCheck, float thetaToCheck){
    float yLowerLimit = startY - (EntwinedUtils.millis() - startTime) * (propagationSpeed);
    if (lifeCycleState == 0 && yLowerLimit < treeBottomY){
      lifeCycleState = 1;
      wideningStartTime = EntwinedUtils.millis();
    }
    if (lifeCycleState == 1 && EntwinedUtils.millis() > startTime + 2000 / propagationSpeed){
      lifeCycleState = 2;
    }
    if (lifeCycleState > 1 || yLowerLimit > yToCheck){
      return 0;
    }
    int i = 0;
    int keyPointIndex = -1;
    float result = 0;
    while (i < (yKeyPoints.size() - 1)){
      if (yKeyPoints.get(i) > yToCheck && yKeyPoints.get(i + 1) <= yToCheck){
        keyPointIndex = i;
        i = yKeyPoints.size();
      }
      i++;
    }
    if (keyPointIndex >= 0){
      float targetTheta = thetaKeyPoints.get(keyPointIndex) + (thetaKeyPoints.get(keyPointIndex + 1) - thetaKeyPoints.get(keyPointIndex)) * (yKeyPoints.get(keyPointIndex) - yToCheck) /(yKeyPoints.get(keyPointIndex) - yKeyPoints.get(keyPointIndex + 1));
      float thetaDelta = LXUtils.wrapdistf(targetTheta % 360, thetaToCheck % 360, 360);
      float thinnedLineWidth;
      if (lifeCycleState == 0){
        thinnedLineWidth = lineWidth / 2;
      }
      else {
        thinnedLineWidth = lineWidth / (LXUtils.maxf(1, 2 - propagationSpeed * (EntwinedUtils.millis()- wideningStartTime) / 500));
      }
      result = LXUtils.maxf(0, 100 * (thinnedLineWidth - thetaDelta) / lineWidth);
    }
    for (i=0; i < forks.size(); i++){
      result = LXUtils.maxf(result, forks.get(i).getLightningFactor(yToCheck, thetaToCheck));
    }
    return result;
  }
  private void addKeyPoint(float y, float theta){
    // yKeyPoints = ArrayUtils.add(yKeyPoints, y);
    yKeyPoints.add(y);
    thetaKeyPoints.add(theta);
  }
  public boolean isDead(){
    return lifeCycleState > 1;
  }
}



