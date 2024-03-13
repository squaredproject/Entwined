package entwined.pattern.ray_sykes;

import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

public class IceCrystals extends LXPattern {
  private IceCrystalLine crystal;
  final CompoundParameter propagationSpeed = new CompoundParameter("Speed", 5, 1, 20);
  final CompoundParameter lineWidth = new CompoundParameter("Width", 60, 20, 150);
  final DiscreteParameter recursionDepth = new DiscreteParameter("Danger", 7, 12);
  final IceCrystalSettings settingsObj;

  public IceCrystals(LX lx) {
    super(lx);
    addParameter("speed", propagationSpeed);
    addParameter("lineWidth", lineWidth);
    addParameter("recursionDepth", recursionDepth);
    recursionDepth.setRange(5, 14);
    settingsObj = new IceCrystalSettings(14);
    crystal = new IceCrystalLine(0, settingsObj);
  }
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) {
      if (crystal.lifeCycleState != -1) {
        crystal.doReset();
      }
      return;
    }

    if (crystal.isDone()){
      startCrystal();
    }
    crystal.doUpdate();

    for (LXPoint cube : model.points) {
      float localTheta = CubeManager.getCube(lx, cube.index).localTheta;
      float lineFactor = crystal.getLineFactor(cube.y, localTheta);
      if (lineFactor > 110) {
        lineFactor = 200 - lineFactor;
      }
      float hueVal;
      float satVal;
      float brightVal = LXUtils.minf(100, 20 + lineFactor);
      if (lineFactor > 100){
        brightVal = 100;
        hueVal = 180;
        satVal = 0;
      }
      else if (lineFactor < 20){
        hueVal = 220;
        satVal = 100;
      }
      else if (lineFactor < 50){
        hueVal = 240;
        satVal = 60;
      }
      else {
        hueVal = 240;
        satVal = 60 - 60 * (lineFactor / 100);
      }
      colors[cube.index] = LX.hsb(hueVal,  satVal, brightVal);
    }
  }
  void startCrystal(){
    crystal.doReset();
    settingsObj.doSettings(recursionDepth.getValuei(), lineWidth.getValuef(), 150, propagationSpeed.getValuef());
    crystal.doStart(100, EntwinedUtils.random(360), (7 + ((int)EntwinedUtils.random(2.9f))) % 8);
  }

  /*
   * XXX - this does not appear to be used - seems like ice crystals aren't triggerable in this way
  ParameterTriggerableAdapter getParameterTriggerableAdapter() {
    return new ParameterTriggerableAdapter(lx, getChannelFade()) {
      public void onTriggered(float strength) {
        startCrystal();
        super.onTriggered(strength);
      }
    };
  }
  */
}


class IceCrystalSettings {
  protected int totalRecursionDepth;
  protected float baseLineWidth;
  protected float baseLineLength;
  protected float basePropagationSpeed;
  protected float[] lineLengths;
  protected boolean growthFinished = false;
  protected int growthFinishedTime = 0;
  protected final int maxRecursionDepth;
  IceCrystalSettings(int maxRecursionDepth){
    this.maxRecursionDepth = maxRecursionDepth;
  }
  public void doSettings(int totalRecursionDepth, float baseLineWidth, float baseLineLength, float basePropagationSpeed) {
    this.totalRecursionDepth = totalRecursionDepth;
    this.baseLineWidth = baseLineWidth;
    this.baseLineLength = baseLineLength;
    this.basePropagationSpeed = basePropagationSpeed;
    growthFinishedTime = 0;
    growthFinished = false;
    lineLengths = new float[totalRecursionDepth + 1];
    for (int i=0; i <= totalRecursionDepth; i++){
      lineLengths[i] =  EntwinedUtils.pow(0.9f, i) * (0.5f + EntwinedUtils.random(1)) * baseLineLength;
    }
  }

  public float getLineWidth(int recursionDepth){
    return baseLineWidth * (float)Math.pow(0.9f, recursionDepth);
  }

  public float getLineLength(int recursionDepth){
    return lineLengths[recursionDepth];
  }

  public float getPropagationSpeed(int recursionDepth){
    return basePropagationSpeed * (float)Math.pow(0.8f, recursionDepth);
  }

  public void setGrowthFinished(){
    if (!growthFinished){
      growthFinishedTime = EntwinedUtils.millis();
    }
    growthFinished = true;
  }
}


class IceCrystalLine {
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
  private final float[][] angleFactors = {{0, 1}, {0.7071f, 0.7071f}, {1, 0}, {0.7071f, -0.7071f}, {0, -1}, {-0.7071f, -0.7071f}, {-1, 0}, {-0.7071f, 0.7071f}};
  private IceCrystalLine[] children = new IceCrystalLine[2];
  protected float[][] applicableRange = {{0, 0}, {0, 0}};
  private float nodeMeltRadius;
  protected boolean hasChildren = false;
  private IceCrystalSettings settings;

  IceCrystalLine(int recursionDepth, IceCrystalSettings settings){
    this.recursionDepth = recursionDepth;
    this.settings = settings;
    if (recursionDepth < settings.maxRecursionDepth){
      children[0] = new IceCrystalLine(recursionDepth + 1,  settings);
      children[1] = new IceCrystalLine(recursionDepth + 1,  settings);
    }
  }

  public void doStart(float startY, float startTheta, int angleIndex){
    lifeCycleState = 0;
    this.angleIndex = angleIndex;
    this.startY = startY;
    this.startTheta = 360 + (startTheta % 360);
    this.propagationSpeed = settings.getPropagationSpeed(recursionDepth);
    lineLength = settings.getLineLength(recursionDepth);
    lineWidth = settings.getLineWidth(recursionDepth);
    startTime = EntwinedUtils.millis();
    doUpdate();
  }

  public void doReset(){
    lifeCycleState = -1;
    hasChildren = false;
    nodeMeltRadius = 0;
    if (recursionDepth < settings.maxRecursionDepth){
      children[0].doReset();
      children[1].doReset();
    }
  }

  public void doUpdate(){
    switch(lifeCycleState){
      case 0: //this line is growing
        float currentLineLength = (EntwinedUtils.millis() - startTime) * propagationSpeed / 10;
        if (currentLineLength > lineLength) {
          currentLineLength = lineLength;
          if (recursionDepth >= settings.totalRecursionDepth){
            settings.setGrowthFinished();
            changeLifeCycleState(3);
          }
          else {
            changeLifeCycleState((endY < 0 || endY >  800) ? 3 : 1);
          }
        }
        endTheta = startTheta + angleFactors[angleIndex][0] * currentLineLength;
        endY = startY + angleFactors[angleIndex][1] * currentLineLength;
        applicableRange[0][0] = LXUtils.minf(startTheta, endTheta) - lineWidth / 2;
        applicableRange[0][1] = LXUtils.maxf(startTheta, endTheta) + lineWidth / 2;
        applicableRange[1][0] = LXUtils.minf(startY, endY) - lineWidth / 2;
        applicableRange[1][1] = LXUtils.maxf(startY, endY) + lineWidth / 2;
      break;
      case 1: // creating children (wohoo!)
        children[0].doStart(endY, endTheta % 360, (8 + angleIndex - 1) % 8);
        children[1].doStart(endY, endTheta % 360, (angleIndex + 1) % 8);
        changeLifeCycleState(2);
        hasChildren = true;
      break;
      case 2: //has children that are growing
        checkRangeOfChildren();
      break;
      case 3: // frozen
        if (recursionDepth <= 3 && settings.growthFinished && settings.growthFinishedTime < (EntwinedUtils.millis() - 8000 / propagationSpeed)){
          changeLifeCycleState(4);
        }
      break;
      case 4: // melting
        nodeMeltRadius = (float) Math.pow((settings.totalRecursionDepth - recursionDepth) * (EntwinedUtils.millis() - lifeCycleStateChangeTime) * propagationSpeed  / 7000, 2);
        applicableRange[0][0] = LXUtils.minf(applicableRange[0][0], LXUtils.maxf(0, endTheta - nodeMeltRadius));
        applicableRange[0][1] = LXUtils.maxf(applicableRange[0][1], LXUtils.minf(720, endTheta + nodeMeltRadius));
        applicableRange[1][0] = LXUtils.minf(applicableRange[1][0], LXUtils.maxf(100, (endY - nodeMeltRadius)));
        applicableRange[1][1] = LXUtils.maxf(applicableRange[1][1], LXUtils.minf(700, (endY + nodeMeltRadius)));
        if (lifeCycleStateChangeTime < (EntwinedUtils.millis() - 27000 / propagationSpeed)){
          changeLifeCycleState(5);
          children[0].doReset();
          children[1].doReset();
          hasChildren = false;
        }
      break;
      case 5: //water
        if (lifeCycleStateChangeTime < (EntwinedUtils.millis() - 8000 / propagationSpeed)){
          changeLifeCycleState(6);
        }
      break;
      case 6: // done
      break;
    }
    if (hasChildren && lifeCycleState >= 2 && lifeCycleState <= 4){
      children[0].doUpdate();
      children[1].doUpdate();
      if (children[0].lifeCycleState == children[1].lifeCycleState && lifeCycleState < children[0].lifeCycleState){
        changeLifeCycleState(children[0].lifeCycleState);
      }
    }
  }

  public float getLineFactor (float yToCheck, float thetaToCheck){
    float result = 0;
    if (lifeCycleState >= 5){
      return 200;
    }
    if (yToCheck <= applicableRange[1][0] || yToCheck >= applicableRange[1][1]){
      return result;
    }
    float adjustedTheta = thetaToCheck < applicableRange[0][0] ? thetaToCheck + 360 : thetaToCheck;
    if (!(adjustedTheta >= applicableRange[0][0] && adjustedTheta <= applicableRange[0][1])){
      return result;
    }
    if (lifeCycleState == 4){
      float distFromNode = (float)Math.sqrt(Math.pow(Math.abs(endY - yToCheck), 2) + Math.pow(LXUtils.wrapdistf(endTheta%360, thetaToCheck%360, 360), 2));
      if (distFromNode < nodeMeltRadius){
        result = LXUtils.minf(200, 100 + 150 * (nodeMeltRadius - distFromNode) / nodeMeltRadius);
      }
    }
    float lowestY = LXUtils.minf(startY, endY);
    float highestY = LXUtils.maxf(startY, endY);
    if (Math.abs(angleFactors[angleIndex][1]) > 0){
     if (yToCheck >= lowestY && yToCheck <= highestY){
        float targetTheta = startTheta + (endTheta - startTheta) * (yToCheck - startY) / (endY - startY);
        float lineThetaWidth = lineWidth / (2 * Math.abs(angleFactors[angleIndex][1]));
        result = LXUtils.maxf(result, 100 * LXUtils.maxf(0, (lineThetaWidth - Math.abs(LXUtils.wrapdistf(targetTheta%360, thetaToCheck%360, 360)))) / lineThetaWidth);
      }
    }
    else {
      float lowestTheta = LXUtils.minf(startTheta, endTheta);
      float highestTheta = LXUtils.maxf(startTheta, endTheta);
      if (thetaToCheck < lowestTheta) {
        thetaToCheck += 360;
      }
      if (thetaToCheck >= lowestTheta && thetaToCheck <= highestTheta){
        if (yToCheck <= lowestY && yToCheck >= lowestY - lineWidth / 2){
          result = LXUtils.maxf(result, 100 * (lineWidth / 2 - (lowestY - yToCheck)) / (lineWidth / 2));
        }
        if (yToCheck >= highestY && yToCheck <= highestY + lineWidth / 2){
          result = LXUtils.maxf(result, 100 * (lineWidth / 2 - (yToCheck - highestY)) / (lineWidth / 2));
        }
      }
    }
    if (lifeCycleState >= 2 && hasChildren){
      result = LXUtils.maxf(result, LXUtils.maxf(children[0].getLineFactor(yToCheck, thetaToCheck % 360), children[1].getLineFactor(yToCheck, thetaToCheck % 360)));
    }
    return result;
  }

  public void checkRangeOfChildren(){
    if (hasChildren){
      for (int i = 0; i < children.length; i++){
        for (int j = 0; j < 2; j++){
          applicableRange[j][0] = LXUtils.minf(applicableRange[j][0], children[i].applicableRange[j][0]);
          applicableRange[j][1] = LXUtils.maxf(applicableRange[j][1], children[i].applicableRange[j][1]);
        }
      }
    }
  }

  void changeLifeCycleState(int lifeCycleStateIn){
    lifeCycleStateChangeTime = EntwinedUtils.millis();
    this.lifeCycleState = lifeCycleStateIn;
  }

  public boolean isDone(){
    return lifeCycleState == 6 || lifeCycleState == -1;
  }
}
