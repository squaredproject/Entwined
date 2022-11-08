package entwined.pattern.interactive;

import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;

public class InteractiveHSVEffect extends LXEffect {

  final BoundedParameter hueSet[];
  final BoundedParameter hueSetAmount[]; // need to expose this because 0 means none

  final BoundedParameter hueShift[];

  final BoundedParameter saturation[];

  final BoundedParameter brightness[];

  final int nPieces;
  final Map<String, Integer> pieceIdMap = new HashMap<String, Integer>();

  public InteractiveHSVEffect(LX lx) {
    super(lx);

    // Need to know the different pieces that exist, and be able to look them up by name
    //
    int nPieces = model.children.length;
    this.nPieces = nPieces;
    int componentIdx = 0;
    for (LXModel component : model.children) {
      this.pieceIdMap.put(component.metaData.get("name"), componentIdx);  // XXX this can throw!
      componentIdx++;
    }


    hueSet = new BoundedParameter[nPieces];
    hueSetAmount = new BoundedParameter[nPieces];
    hueShift = new BoundedParameter[nPieces];
    saturation = new BoundedParameter[nPieces];
    brightness = new BoundedParameter[nPieces];
    for (int i=0;i<nPieces;i++) {
      String pieceId = model.children[i].metaData.get("name");  // XXX I have no name associated with this component. Metadata? Maybe.
      hueSet[i] = new BoundedParameter("Inter"+"HueSet"+pieceId, 0, 360);
      hueSetAmount[i] = new BoundedParameter("Inter"+"HueSetVal"+pieceId,0, 180);

      // hue shift
      hueShift[i] = new BoundedParameter("Inter"+"HueShift"+pieceId,0,360);
      //
      saturation[i] = new BoundedParameter("Inter"+"Sat"+pieceId,0,100);
      //
      brightness[i] = new BoundedParameter("Inter"+"Bri"+pieceId,0,100);
      // set initial values
      resetPiece(i);
    }
  }

  static float norm360(float i) {
    while (i < 0.0f) {
      i += 360.0f;
    }
    while (i > 360.0f) {
      i -= 360.0f;
    }
    return(i);
  }

  // distance between a and b in degrees, absolute
  static float absdist360(float a, float b) {
    float r = Math.abs( a - b );
    if (r < 180.0f) return(r);
    return( 360.0f - r );
  }

  // distance between a and b in degrees, negative means a is counterclockwise
  // so for example a = 0, b = 190, the distance is 170, but positive, because the short path is clockwise
  static float dist360(float a, float b) {
    float r = a - b;
    if (Math.abs(r) <= 180.0f) return(r);
    if (r < 0.0f) return( r + 360.0f );
    return( r - 360.0f );
  }


  // quick bit of math: interpolate on the hue circle
  // but do so with a limit. Make sure the color is never outside of
  // a certain number of degrees. There's some subtlety in when/how to clip the
  // edges, so will try a few things.
  // Interesting, having a limitdeg of 180 means no effect, that's the blend (basically)
  static float hueBlend(float src, float dst, float limitDeg) {
    float r;
    float dist = dist360(src,dst);
    r = dst + ( (dist / 180.0f) * limitDeg );
    r = norm360(r);

    // test: output should be within the limit distance of dst
    if ( absdist360(r, dst) > limitDeg ) {
      System.out.println("InteractiveHSVBlendFail: src "+src+" dst "+dst+" res "+r+" limit "+limitDeg);
    }

    return(r);
  }

  @Override
  protected void run(double deltaMs, double amount) {

    // iterate over Cubes not Colors because Cubes have index into colors, not the other way around
    int componentIdx = 0;
    for (LXModel component : model.children) {
      for (LXPoint cube : component.points) {

        float hueSetf = hueSet[componentIdx].getValuef();
        float hueSetAmountf = hueSetAmount[componentIdx].getValuef();
        float hueShiftf = hueShift[componentIdx].getValuef();
        float brightnessf = brightness[componentIdx].getValuef();
        float saturationf = saturation[componentIdx].getValuef();

        if ( hueShiftf > 0.0f || hueSetAmountf < 180.0f ||
             brightnessf != 50.0f || saturationf != 50.0f ) {

          int i = cube.index;
          float h = LXColor.h(colors[i]); // 0-360
          float s = LXColor.s(colors[i]); // 0-100
          float b = LXColor.b(colors[i]); // 0-100

          // first squash if set
          if (hueSetAmountf < 180.0f) {
            h = hueBlend(h, hueSetf, hueSetAmountf);
          }

          // shift if shifting
          if (hueShiftf > 0.0f) {
            h += hueShiftf;
            if (h > 360.0f) h -= 360.0f;
          }

          // brightness of 50 is same, > 50 is brighter, < 50 is dimmer
          if (brightnessf != 50.0f) {
            b = (brightnessf / 50.0f) * b;
            if (b > 100.0f) b = 100.0f;
          }

          // saturation of 50 is same, > 50 is brighter, < 50 is dimmer
          if (saturationf != 50.0f) {
            s = (saturationf / 50.0f) * s;
            if (s > 100.0f) s = 100.0f;
          }

          colors[i] = LX.hsb( h, s, b );
        }
      }
      componentIdx++;
    }
  }


  // set all parameters to values that say nothing
  public void resetAll() {
    System.out.println(" disable all pieces ");
    for (int i=0;i<hueSet.length;i++) {
      resetPiece(i);
    }
  }

  private int getPieceIndex(String pieceId) {
    Integer pieceIndex_o = pieceIdMap.get(pieceId);
    if (pieceIndex_o == null) {
      // this is hit if the piece has no string
      //System.out.println(" pieceId not found in map2: "+pieceId);
      return(-1);
    }
    return( pieceIndex_o );
  }

  public void resetPiece(int pieceIndex) {
      hueSet[pieceIndex].setValue(0f);
      hueSetAmount[pieceIndex].setValue(180.0f);
      hueShift[pieceIndex].setValue(0f);
      brightness[pieceIndex].setValue(50.0f);
      saturation[pieceIndex].setValue(50.0f);
  }

  public void resetPiece(String pieceId) {

      int pieceIndex = getPieceIndex(pieceId);
      if (pieceIndex == -1) return;
      resetPiece(pieceIndex);
  }


  // Value is from 0 to 360.0, where 0 means no shift
  public void setPieceHueShift(String pieceId, float value) {

    //System.out.println("SetShrubHue: "+pieceId+" hue "+hue);

    int pieceIndex = getPieceIndex(pieceId);
    if (pieceIndex == -1) return;

    hueShift[pieceIndex].setValue(value);
  }


  // Note: this needs a parallel to disable the hueSet alone,
  // but we don't have it wired in yet from Canopy. If we like "hueSet" we'll
  // wire it in
  public void setPieceHueSet(String pieceId, float hue) {
    //System.out.println("SetPieceIdHue: "+pieceId+" hue "+hue);
    int pieceIndex = getPieceIndex(pieceId);
    if (pieceIndex == -1) return;

    hueSet[pieceIndex].setValue(hue);
    hueSetAmount[pieceIndex].setValue(30f); // a good angle to squash to
  }

  // Value is from 0 to 100, where 50 means no change
  public void setPieceBrightness(String pieceId, float value) {
    //System.out.println("SetPieceBrightness: "+pieceId+" hue "+hue);
    int pieceIndex = getPieceIndex(pieceId);
    if (pieceIndex == -1) return;

    brightness[pieceIndex].setValue(value);
  }

  // Value is from 0 to 100, where 50 means no change
  public void setPieceSaturation(String pieceId, float value) {
    //System.out.println("SetPieceSaturation: "+pieceId+" hue "+hue);
    int pieceIndex = getPieceIndex(pieceId);
    if (pieceIndex == -1) return;

    saturation[pieceIndex].setValue(value);
  }

}


