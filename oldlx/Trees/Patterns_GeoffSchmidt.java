import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BasicParameter;

class PixelState {
  LX lx;
  double when; // time last triggered (possibly zero)
  float h, s, life; // parameters when last triggered
  
  PixelState(LX _lx) {
    lx = _lx;
    when = -1000 * 60 * 60; // arbitrary time far in the past
    h = s = life = 0;
  }
  
  public void fire(double now, float _life, float _h, float _s) {
    when = now;
    life = _life;
    h = _h;
    s = _s;
  }
  
  public int currentColor(double now) {
    double age = (life - (now - when)) / life;
    if (age < 0)
      age = 0;
    return LXColor.hsb(h * 360, s * 100, age * 100);
  }
}

class Pixels extends TSPattern {
  final BasicParameter pSpeed = new BasicParameter("SPD", 2.0/15.0);
  final BasicParameter pLifetime = new BasicParameter("LIFE", 3.0/15.0);
  final BasicParameter pHue = new BasicParameter("HUE", 0.5);
  final BasicParameter pSat = new BasicParameter("SAT", 0.5);
  final SawLFO hueLFO = new SawLFO(0.0, 1.0, 1000);

  PixelState[] pixelStates;
  double now = 0;
  double lastFireTime = 0;
  
  Pixels(LX lx) {
    super(lx);
    
    addParameter(pSpeed);
    addParameter(pLifetime);
    addParameter(pSat);
    addParameter(pHue);
    addModulator(hueLFO).start();

    int numCubes = model.cubes.size();
    pixelStates = new PixelState[numCubes];
    for (int n = 0; n < numCubes; n++)
      pixelStates[n] = new PixelState(lx);
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    now += deltaMs;
    
    float vSpeed = pSpeed.getValuef();
    float vLifetime = pLifetime.getValuef();
    float vHue = pHue.getValuef();
    float vSat = pSat.getValuef();

    hueLFO.setPeriod(vHue * 30000 + 1000);
    
    float minFiresPerSec = 5;
    float maxFiresPerSec = 2000;
    float firesPerSec = minFiresPerSec + vSpeed * (maxFiresPerSec - minFiresPerSec);
    float timeBetween = 1000 / firesPerSec;
    while (lastFireTime + timeBetween < now) {
      int which = (int)Utils.random(0, model.cubes.size());
      pixelStates[which].fire(now, vLifetime * 1000 + 10, hueLFO.getValuef(), (1 - vSat));
      lastFireTime += timeBetween;
    } 
    
    int i = 0;
    for (i = 0; i < model.cubes.size(); i++) {
      colors[i] = pixelStates[i].currentColor(now);
    }
  }
}

///////////////////////////////////////////////////////////////////////////////

class Wedges extends TSPattern {
  final BasicParameter pSpeed = new BasicParameter("SPD", .52);
  final BasicParameter pCount = new BasicParameter("COUNT", 4.0/15.0);
  final BasicParameter pSat = new BasicParameter("SAT", 5.0/15.0);
  final BasicParameter pHue = new BasicParameter("HUE", .5);
  double rotation = 0; // degrees

  Wedges(LX lx) {
    super(lx);
    
    addParameter(pSpeed);
    addParameter(pCount);
    addParameter(pSat);
    addParameter(pHue);
    rotation = 0;
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    float vSpeed = pSpeed.getValuef();
    float vCount = pCount.getValuef();
    float vSat = pSat.getValuef();
    float vHue = pHue.getValuef();

    rotation += deltaMs/1000.0f * (2 * (vSpeed - .5f) * 360.0f * 1.0f);
    rotation = rotation % 360.0f;

    double sections = Math.floor(1.0f + vCount * 10.0f);
    double quant = 360.0f/sections;

    for (Cube cube : model.cubes) {
      colors[cube.index] = LXColor.hsb(
        Math.floor((rotation - cube.transformedTheta) / quant) * quant + vHue * 360.0f,
        (1 - vSat) * 100,
        100);
    }     
  } 
}

///////////////////////////////////////////////////////////////////////////////

class ColorBar {
  double s, b;
  double startTime;
  double velocity;
  double barHeight;
  
  ColorBar(double now) {
    startTime = now;
    velocity = Utils.random(6.0f, 12.0f*25.0f); // upward velocity, inches per second
    s = Utils.random(0.1f, 1.0f) * 100;
    b = Utils.random(0.1f, 1.0f) * 100;
    barHeight = Utils.random(12.0f, 10.0f*12.0f);
  }
  
  public boolean intersects(double now, double y) {
    y -= (now - startTime)/1000.0f * velocity;
    return y < 0 && y > -barHeight;
  }
  
  public boolean offscreen(double now) {
    return (velocity * (now - startTime)/1000.0f) - barHeight > 70*12;
  }
  
  public int getColor(double h) {
    return LXColor.hsb(h, s, b);
  }
}

class Parallax extends TSPattern {
  final BasicParameter pHue = new BasicParameter("HUE", 0.5);
  final BasicParameter pSpeed = new BasicParameter("SPD", 0);
  final BasicParameter pCount = new BasicParameter("BARS", .25);
  final BasicParameter pBounceMag = new BasicParameter("BNC", 0);
  final SinLFO bounceLFO = new SinLFO(-1.0, 1.0, 750);
  ColorBar[] colorBars;
  double now = 0;

  Parallax(LX lx) {
    super(lx);
    addParameter(pHue);
    addParameter(pSpeed);
    addParameter(pCount);
    addParameter(pBounceMag);
    addModulator(bounceLFO).start();
    colorBars = new ColorBar[0];
  }
  
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;
    
    int targetCount = (int)(pCount.getValuef() * 20) + 1;
    
    if (targetCount != colorBars.length) {
      // If I knew any Java, I might know how to resize an array
      ColorBar[] newColorBars = new ColorBar[targetCount];
      for (int i = 0; i < targetCount; i++) {
        newColorBars[i] = i < colorBars.length ? colorBars[i] : null;
      }
      colorBars = newColorBars;
    }
    
    now += deltaMs * (pSpeed.getValuef() * 2.0f + .5f);
    double bouncedNow = now + bounceLFO.getValuef() * pBounceMag.getValuef() * 1000.0f;
    
    for (int i = 0; i < colorBars.length; i++) {
      if (colorBars[i] == null || colorBars[i].offscreen(now))
        colorBars[i] = new ColorBar(now);
    }

    for (Cube cube : model.cubes) {
      colors[cube.index] = lx.hsb(0, 0, 0);

      for (ColorBar colorBar : colorBars) {
        if (colorBar.intersects(bouncedNow, cube.transformedY)) {
          colors[cube.index] = colorBar.getColor(pHue.getValuef() * 360);
          break;
        }
      }
    }     
  } 
}

