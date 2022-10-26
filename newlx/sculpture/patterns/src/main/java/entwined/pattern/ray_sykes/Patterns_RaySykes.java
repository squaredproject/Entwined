package entwined.pattern.ray_sykes;

// import org.apache.commons.lang3.ArrayUtils;

/*
class SparkleHelix extends LXPattern {
  final BoundedParameter minCoil = new BoundedParameter("MinCOIL", .02, .005, .05);
  final BoundedParameter maxCoil = new BoundedParameter("MaxCOIL", .03, .005, .05);
  final BoundedParameter sparkle = new BoundedParameter("Spark", 80, 160, 10);
  final BoundedParameter sparkleSaturation = new BoundedParameter("Sat", 50, 0, 100);
  final BoundedParameter counterSpiralStrength = new BoundedParameter("Double", 0, 0, 1);

  final SinLFO coil = new SinLFO(minCoil, maxCoil, 8000);
  final SinLFO rate = new SinLFO(6000, 1000, 19000);
  final SawLFO spin = new SawLFO(0, LX.TWO_PI, rate);
  final SinLFO width = new SinLFO(10, 20, 11000);
  int[] sparkleTimeOuts;

  SparkleHelix(LX lx) {
    super(lx);
    addParameter("minCoil", minCoil);
    addParameter("maxCoil", maxCoil);
    addParameter("sparkle", sparkle);
    addParameter("sparkleSat", sparkleSaturation);
    addParameter("double", counterSpiralStrength);
    addModulator(rate).start();
    addModulator(coil).start();
    addModulator(spin).start();
    addModulator(width).start();
    sparkleTimeOuts = new int[model.points.length];
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float coilValue = coil.getValuef();
    float widthValue = width.getValuef();
    float spinValue = spin.getValuef();
    float counterSpiralStrengthValue = counterSpiralStrength.getValuef();
    float sparkleValue = sparkle.getValuef();
    float currentBaseHue = lx.engine.palette.color.getHuef();

    for (LXModel component:  model.children) {
      for (LXPoint cube : component.points) {
        float localX = cube.x - component.cx;
        float localZ = cube.z - component.cz;
        float localTheta = (float)Math.atan2(localZ, localX); //  * 180/LX.PI; Seems to be wanted in radians here
        float compensatedWidth = (0.7f + .02f / coilValue) * widthValue;
        float wrapAngleForward   = (8*LX.TWO_PIf + spinValue + coilValue*(cube.y-component.cy)) % LX.TWO_PIf;
        float wrapAngleBackwards = (8*LX.TWO_PIf - spinValue - coilValue*(cube.y-component.cy)) % LX.TWO_PIf;
        float spiralVal = LXUtils.maxf(
            0,
            100 - (100*LX.TWO_PIf/(compensatedWidth)*LXUtils.wrapdistf(localTheta, wrapAngleForward, LX.TWO_PIf))
        );
        float counterSpiralVal = counterSpiralStrengthValue * LXUtils.maxf(
            0,
            100 - (100*LX.TWO_PIf/(compensatedWidth)*LXUtils.wrapdistf(localTheta, wrapAngleBackwards, LX.TWO_PIf))
        );
        float hueVal = (currentBaseHue + .1f*cube.y) % 360;
        if (sparkleTimeOuts[cube.index] > System.currentTimeMillis()){
          colors[cube.index] = LX.hsb(hueVal, sparkleSaturation.getValuef(), 100);
        }
        else{
          colors[cube.index] = LX.hsb(hueVal, 100, LXUtils.maxf(spiralVal, counterSpiralVal));
          if (Math.random() * LXUtils.maxf(spiralVal, counterSpiralVal) > sparkleValue){
            sparkleTimeOuts[cube.index] = (int)System.currentTimeMillis() + 100;
          }
        }
      }
    }
  }
}
*/








/*
class Lightning extends LXPattern { // TSTriggerablePattern {
  int nShrubs = model.sub("SHRUB").size();
  int nTrees = model.sub("TREES").size();
  private LightningLine[] bolts = new LightningLine[nTrees + nShrubs];
  final BoundedParameter boltAngle = new BoundedParameter("Angle", 35, 0, 55);
  final BoundedParameter propagationSpeed = new BoundedParameter("Speed", 10, 0.5, 20);
  final BoundedParameter maxBoltWidth = new BoundedParameter("Width", 60, 20, 150);
  final BoundedParameter lightningChance = new BoundedParameter("Chance", 5, 1, 10);
  final BoundedParameter forkingChance = new BoundedParameter("Fork", 3, 1, 10);
  final BooleanParameter firesOnBeat = new BooleanParameter("Beat");
  int[] randomCheckTimeOuts = new int[nTrees + nShrubs];

  Lightning(LX lx) {
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
        setCallRun(false);
      }
    }

    treeIndex = 0;
    for (LXModel tree : model.sub("TREE")){
      if (triggered) {
        if (bolts[treeIndex].isDead()) {
          if (firesOnBeat.isOn()) {
            if (lx.engine.tempo.beat()) {
              randomCheckTimeOuts[treeIndex] = (int)System.currentTimeMillis()() + 100;
              bolts[treeIndex] = makeBolt();
            }
          } else {
            if (randomCheckTimeOuts[treeIndex] < System.currentTimeMillis()){
              randomCheckTimeOuts[treeIndex] = (int)System.currentTimeMillis() + 100;
              if (Math.random() * 15 < lightningChance.getValuef()){
                bolts[treeIndex] = makeBolt();
              }
            }
          }
        }
      }
      for (LXPoint cube : tree.points) {
        float hueVal = 300;
        float localTheta = (float)Math.atan2(cube.z - tree.cz,  cube.x - tree.cx)*180/LX.PIf;
        float lightningFactor = bolts[treeIndex].getLightningFactor(cube.y, localTheta);
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
          satVal = 100 - 2 * (lightningFactor - 50);
        }
        colors[cube.index] = LX.hsb(hueVal,  satVal, brightVal);
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
        setCallRun(false);
      }
    }

    shrubIndex = nTrees;  // ditto above
    for (LXModel shrub : model.sub("SHRUB")){
      if (triggered) {
        if (bolts[shrubIndex].isDead()) {
          if (firesOnBeat.isOn()) {
            if (lx.engine.tempo.beat()) {
              randomCheckTimeOuts[shrubIndex] = (int)System.currentTimeMillis() + 100;
              bolts[shrubIndex] = makeBolt();
            }
          } else {
            if (randomCheckTimeOuts[shrubIndex] < System.currentTimeMillis()){
              randomCheckTimeOuts[shrubIndex] = (int)System.currentTimeMillis() + 100;
              if (Math.random() *15 < lightningChanceVal){
                bolts[shrubIndex] = makeBolt();
              }
            }
          }
        }
      }
      for (LXPoint cube : shrub.points) {
        float localTheta = (float)Math.atan2(cube.z - shrub.cz,  cube.x - shrub.cx)*180/LX.PIf;
        float hueVal = 300;
        float lightningFactor = bolts[shrubIndex].getLightningFactor(cube.y, localTheta);
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
          satVal = 100 - 2 * (lightningFactor - 50);
        }
        colors[cube.index] = LX.hsb(hueVal,  satVal, brightVal);
      }
      shrubIndex ++;
    }
  }
  LightningLine makeBolt(){
    float theta = 45 * (int) (Math.random() * 8);
    float boltWidth = (maxBoltWidth.getValuef() + (float)Math.random() * maxBoltWidth.getValuef()) / 2;
    return new LightningLine ((int)System.currentTimeMillis(), 550, theta, boltAngle.getValuef(), propagationSpeed.getValuef(), boltWidth, 3, forkingChance.getValuef());
  }

  public void onTriggered(float strength) {
    super.onTriggered(strength);

    propagationSpeed.setNormalized(strength);

    int treeIndex = 0;

    for (LXModel tree : model.sub("TREE")){
      if (bolts[treeIndex].isDead()){
        randomCheckTimeOuts[treeIndex] = (int)System.currentTimeMillis() + 100;
        bolts[treeIndex] = makeBolt();
      }
      treeIndex ++;
    }
    int shrubIndex = nTrees;

    for (LXModel shrub : model.sub("SHRUB")){
      if (bolts[shrubIndex].isDead()){
        randomCheckTimeOuts[shrubIndex] = (int)System.currentTimeMillis() + 100;
        bolts[shrubIndex] = makeBolt();
      }
      shrubIndex ++;
    }
  }
}


class LightningLine {
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
      y -= (25 + Math.random() * 75);
      if (y > 450){
        theta = startTheta - 20 + (float)Math.random() * 40;
      }
      else {
        straightLineTheta = startTheta + LXUtils.sinf((LX.TWO_PIf/360) * basicAngle) * (startY - y) * 0.9f;
        theta = straightLineTheta - 50 + (float)Math.random() * 100;
      }
      addKeyPoint(y, theta);
      if (recursionDepthLeft > 0 && y < 500 && Math.random() *20 < forkingChance){
        forks.add(new LightningLine(startTime + (int)((startY - y) / propagationSpeed), y, theta,(-basicAngle * (float)Math.random()*2), propagationSpeed, (lineWidth - (float)Math.random() * 2), recursionDepthLeft - 1, forkingChance));
      }
    }
  }
  public float getLightningFactor (float yToCheck, float thetaToCheck){
    float yLowerLimit = startY - (System.currentTimeMillis() - startTime) * (propagationSpeed);
    if (lifeCycleState == 0 && yLowerLimit < treeBottomY){
      lifeCycleState = 1;
      wideningStartTime = System.currentTimeMillis();
    }
    if (lifeCycleState == 1 && System.currentTimeMillis() > startTime + 2000 / propagationSpeed){
      lifeCycleState = 2;
    }
    if (lifeCycleState > 1 || yLowerLimit > yToCheck){
      return 0;
    }
    int i = 0;
    int keyPointIndex = -1;
    float result = 0;
    while (i < (yKeyPoints.length - 1)){
      if (yKeyPoints[i] > yToCheck && yKeyPoints[i + 1] <= yToCheck){
        keyPointIndex = i;
        i = yKeyPoints.length;
      }
      i++;
    }
    if (keyPointIndex >= 0){
      float targetTheta = thetaKeyPoints[keyPointIndex] + (thetaKeyPoints[keyPointIndex + 1] - thetaKeyPoints[keyPointIndex]) * (yKeyPoints[keyPointIndex] - yToCheck) /(yKeyPoints[keyPointIndex] - yKeyPoints[keyPointIndex + 1]);
      float thetaDelta = LXUtils.wrapdistf(targetTheta, thetaToCheck, 360);
      float thinnedLineWidth;
      if (lifeCycleState == 0){
        thinnedLineWidth = lineWidth / 2;
      }
      else {
        thinnedLineWidth = lineWidth / (LXUtils.maxf(1, 2 - propagationSpeed * (System.currentTimeMillis() - wideningStartTime) / 500));
      }
      result = LXUtils.maxf(0, 100 * (thinnedLineWidth - thetaDelta) / lineWidth);
    }
    for (i=0; i < forks.size(); i++){
      result = LXUtils.maxf(result, forks.get(i).getLightningFactor(yToCheck, thetaToCheck));
    }
    return result;
  }
  private void addKeyPoint(float y, float theta){
    yKeyPoints = ArrayUtils.add(yKeyPoints, y);
    thetaKeyPoints = ArrayUtils.add(thetaKeyPoints, theta);
  }
  public boolean isDead(){
    return lifeCycleState > 1;
  }
}
*/


