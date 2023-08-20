package entwined.pattern.bbulkow;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.pattern.LXPattern;

import entwined.core.CubeData;
import entwined.core.CubeManager;

// Use Jcodec to read frames from a file
// Would be better to replace with ... just about anything faster...
// but doing something that works at all is nice
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.containers.mp4.demuxer.MP4DemuxerTrack;
import org.jcodec.scale.AWTUtil;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
In order to be like a flag, have multiple colors on the sculpture
*/
public class VideoPlayer extends LXPattern {

  final StringParameter dirName = new StringParameter("VIDEODIR", "C:\\Users\\bbulk\\Downloads");
  final StringParameter fileName = new StringParameter("VIDEOFILE", "entwined.mp4");
  // todo: we'll probably want float parameters for the projection

  // let's declare a global that will be the array of RGB. The thread with frames can fill it,
  // the pattern loop can display it
 // final int vidWidth;
  //final int vidHeight;
  //final int vidFrameRate;
  //final long vidFrameCount;
  //final double vidDuration;


  //
  private File vidFile = null;
  //
  int  vidFrameNumCur = 0;
  //
  BufferedImage vidBufferedImage = null;
  //
  Thread readerThread = null;
  VideoReader videoReader = null;

  // Constructor and initial setup
  public VideoPlayer(LX lx) {
    super(lx);
    addParameter("file",fileName);
    addParameter("dir",dirName);

    // System.out.println(" VIDEO PLAYER INIT: user dir "+System.getProperty("user.dir"));

    String fn = dirName.getString() + System.getProperty("file.separator") + fileName.getString();
    try {
      vidFile = new File(fn);
      System.out.println("opened file "+fn);
    }
    catch (Exception ex) {
      System.out.println(" unable to open file  "+fn+" "+ex.getMessage());
      return;
    }

    // todo: consider: pick out or validate the frame rate, compression type, etc

    // let's try this for now. Would be better to start only when you have a file or something
    videoReader = new VideoReader(vidFile);
    readerThread = new Thread(videoReader);
    readerThread.start();

  } // VideoPlayer constructur

  @Override
  protected void onActive() {
    super.onActive();
    videoReader.onActive();
  }

  @Override
  protected void onInactive() {
    super.onInactive();
    videoReader.onInactive();
  }



  class VideoReader implements Runnable {

    boolean active;
    final File vidFile;

    public VideoReader(File file) {
      vidFile = file;
      active = false;
    }

    void onActive() {
      active = true;
      //System.out.println(" VideoReader: now active");
    }

    void onInactive() {
      active = false;
      //System.out.println(" VideoREader: now inactive");
    }

    public void run() {

      try {
        if (vidFile == null) {
          System.out.println("VideoReader: vidfile is null");
          return;
        }

        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(vidFile));

        if (grab == null) {
          System.out.println("VideoReader: could not create Grab object");
          return;
        }

        while (true) {

          long delay_ms = 100;

          if (active) {

              long start_ms = System.currentTimeMillis();

              // this gets the next one. 
              Picture picture = grab.getNativeFrame();
              if (picture == null) {
                grab.seekToFramePrecise(0);
                picture = grab.getNativeFrame();
                System.out.println(" VideoReader: reached end of file, seeking to start ");
              }

              // System.out.println(" picture: height "+picture.getHeight()+ " width "+picture.getWidth());

              // picture has getData() which returns an array of bytes. That is uncompressed,
              // but it'll be in some particular color space. If you know the colorspace
              // because you're always using the same file type youll remove an abstraction
              // by not converting to a BufferedImage
              // It def. has getWidth() and getHeight().
              // You can check its color space too

              BufferedImage bImage = AWTUtil.toBufferedImage(picture);
              vidBufferedImage = bImage;

              vidFrameNumCur++;

              delay_ms -= System.currentTimeMillis() - start_ms;
              if (delay_ms < 0) delay_ms = 0;

          } // if active

          Thread.sleep(delay_ms,0);

        } // loop

      } catch (Exception ex) {
        System.out.println(" VideoReader: Exception in VideoReader runnable: FrameGrab or getNative "+ex.getMessage()+" "+ex.toString() );
        return;
      }
    } // run

  } // VideoReader

  // This is the pattern loop, which will run continuously via LX
  // VideoPlayer run method
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    // not yet enabled? This seems to happen for half a dozen frames :-P
    if (vidBufferedImage == null) {
      //System.out.println(" VideoReader: being run without being enabled");
      return;
    }

    // take the current one, in case it gets replaced by the other thread
    BufferedImage bi = vidBufferedImage;
    float width = (float) bi.getWidth();
    float height = (float) (bi.getHeight() - 1);

    float yMax = model.yMax;
    float yMin = model.yMin;
    float ySize = yMax - yMin;
    //System.out.println(" yMax "+yMax+" yMin "+yMin);

    for (LXPoint cube : model.points) {
        // find the XYZ of the point
      CubeData cubeData = CubeManager.getCube(lx, cube.index);
      //System.out.println(" got cube");
      //System.out.println(" calculate y: ySize "+ySize+" localY "+cubeData.localY+" yMin "+yMin+" height "+height);
      // find the X,Y within the video
      int x = (int) ((cubeData.localTheta / 360.0) * width);
      int y = (int) (((cubeData.localY - yMin) / ySize) * height);
      //System.out.println(" map to vid: x "+x+" y "+y);

      // pixel will be ARGB - split it out
      int pixel = bi.getRGB(x,y);
      //System.out.println("vid color: "+ String.format("0x%08x",pixel));

      // LX has a very similar structure for its RGB.
/* PROPERLY ABSTRACT THING
      //System.out.println(" VIDRGB pixel: "+String.format("%x",pixel));

      int b = pixel & 0xff;
      pixel = pixel >> 8;
      int g = pixel & 0xff;
      pixel = pixel >> 8;
      int r = pixel & 0xff;
      pixel = pixel >> 8;
      int a = pixel & 0xff;

      // set the leds
      colors[cube.index] = LX.rgb(r,g,b);
      //System.out.println(" LX RGB color: "+String.format("%x",LX.rgb(r,g,b)));
*/ 
      // when using the default ARGB color space, it's the same as LX's ARGB color space,
      // so we can just copy an integer :-)
      colors[cube.index] = pixel;


    }

  } // run VideoPlayer

} // VideoReader


