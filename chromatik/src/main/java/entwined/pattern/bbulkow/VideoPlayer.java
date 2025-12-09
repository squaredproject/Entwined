package entwined.pattern.bbulkow;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.pattern.LXPattern;

import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.nio.file.DirectoryStream;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
In order to be like a flag, have multiple colors on the sculpture
*/
public class VideoPlayer extends LXPattern implements UIDeviceControls<VideoPlayer> {

  // this ends up being in the Chromatik directory, a good place
  final StringParameter dirName = new StringParameter("VIDEODIR", "Videos");
  // I don't think I can run a function here to attempt to figure out the actual number
  final DiscreteParameter fileNumber = new DiscreteParameter("FNUM", 1, 1, 10);

  final ObjectParameter<String> filePicker = 
      new ObjectParameter<String>("Files", 
          new String[] { "One 1", "Two 2", "Three" } );

  // let's declare a global that will be the array of RGB. The thread with frames can fill it,
  // the pattern loop can display it
 // final int vidWidth;
  //final int vidHeight;
  //final int vidFrameRate;
  //final long vidFrameCount;
  //final double vidDuration;


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
    addParameter("FNUM",fileNumber);
    addParameter("Files",this.filePicker );

    // set the max parameter to the number of files in the directory
    long dfc = getDirectoryFileCount( dirName.getString());
    //System.out.println(" +VideoPlayer+: number of directory files: "+dfc);
    fileNumber.setRange(1,(int)dfc+1);

/*
    List<String> filenames = getDirectoryFilenames(dirName.getString());
    System.out.print(" +VideoPlayer: the files are");
    System.out.println(filenames);
*/

    // todo: consider: pick out or validate the frame rate, compression type, etc

    // let's try this for now. Would be better to start only when you have a file or something
    videoReader = new VideoReader();
    readerThread = new Thread(videoReader);
    readerThread.start();

  } // VideoPlayer constructur

// gets called when the specific instance goes active and inactive
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

// picklist UI helper
  @Override
  public void buildDeviceControls(UI ui, UIDevice uiDevice, VideoPlayer device) {
    uiDevice.setLayout(UIDevice.Layout.HORIZONTAL, 4);
    addColumn(uiDevice, "Column A",newDropMenu(device.filePicker)).setChildSpacing(6);
  }

// Some file helpers
  List<String> getDirectoryFilenames(String directory) {
    List<String> s_s = new ArrayList<String>();
    Path dir = Paths.get(directory);

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
        for (Path entry : stream) {
          if (Files.isRegularFile(entry)) {
            s_s.add(entry.toString());
          }
        }
    } catch (IOException e) {
        e.printStackTrace();
        return(null);
    }

    Collections.sort(s_s);
    return(s_s);
  }

  long getDirectoryFileCount(String directory) {

    long ret = -1;
    try {
        ret = Files.list(Paths.get(directory))
                              .filter(Files::isRegularFile)  // This ensures we're counting only files, not subdirectories
                              .count();

    } catch (IOException e) {
        e.printStackTrace();
    }
    return(ret);
  }


  // Separate thread. It will open a file and read into a shared buffer
  // which is used in the run loop. It is controlled by a discrete parameters
  // which is set to the number of files in the directory, and at the 
  // end of a given file, will close and open the new file if necessary
  // It would probably be better to implement a parameter listener
  class VideoReader implements Runnable {

    boolean active;
    File vidFile;
    int vidFileNumber;

    public VideoReader() {
      vidFile = null;
      active = false;
      vidFileNumber = -1;
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

        File vidFile = null;
        FrameGrab grab = null;
        Picture picture = null;

        while (true) {

          // most videos are 30 fps. Should actually load this with the real fps
          long delay_ms = 33;

          if (active) {

            try { // big try for most of the loop

              if (vidFile == null) {
                List<String> filenames = getDirectoryFilenames(dirName.getString());
                vidFileNumber = fileNumber.getValuei();
                String fn = filenames.get( vidFileNumber - 1);
                try {
                  vidFile = new File(fn);
                  System.out.println("VideoPlayer: opened file "+fn);
                }
                catch (Exception ex) {
                  System.out.println("VideoPlayer: unable to open file  "+fn+" "+ex.getMessage());
                  continue;
                }

                grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(vidFile));
              }

              long start_ms = System.currentTimeMillis();

              // this gets the next one. 
              picture = grab.getNativeFrame();
              // null means end of file, loop
              if (picture == null) {
                // only refresh the file and grab if the file number changed, for efficiency
                if (vidFileNumber != fileNumber.getValuei()) {
                  vidFile = null;
                  grab = null;
                  continue;
                }
                else { // loop to beginning
                  grab.seekToFramePrecise(0);
                  picture = grab.getNativeFrame();
                }
              }

              BufferedImage bImage = AWTUtil.toBufferedImage(picture);
              if (bImage != null)    vidBufferedImage = bImage;

              vidFrameNumCur++;

              delay_ms -= System.currentTimeMillis() - start_ms;
              if (delay_ms < 0) delay_ms = 0;

            }
            catch (Exception ex) {
              System.out.println("VideoPlayer: unknown exception "+ex.getMessage());
              continue;
            }

            // System.out.println(" picture: height "+picture.getHeight()+ " width "+picture.getWidth());

            // picture has getData() which returns an array of bytes. That is uncompressed,
            // but it'll be in some particular color space. If you know the colorspace
            // because you're always using the same file type youll remove an abstraction
            // by not converting to a BufferedImage
            // It def. has getWidth() and getHeight().
            // You can check its color space too

          } // if active
          else {
            // not active, close files and whatnot
            if (grab != null) { grab = null; }
            if (vidFile != null) { vidFile = null; }
          }

          try{
            Thread.sleep(delay_ms,0);
          } catch (InterruptedException ignored) { }

        } // loop

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
    //System.out.println(" yMax ",yMax," yMin ",yMin);

    for (LXPoint cube : model.points) {
        // find the XYZ of the point
      CubeData cubeData = CubeManager.getCube(lx, cube.index);

      // Cylendrical projection 
      // TODO: add limits and scaling

      //System.out.println(" calculate y: ySize ",ySize," localY ",cubeData.localY," yMin ",yMin," height ",height);
      // find the X,Y within the video
      // NOTE: Y in the sculpture, 0 is low, Y in video, 0 is high...
      int x = (int) ((cubeData.localTheta / 360.0) * width);
      int y = (int) (((yMax - cubeData.localY) / ySize) * height);
      //System.out.println(" map to vid: x ",x," y ",y);

      // pixel will be ARGB - split it out
      int pixel = bi.getRGB(x,y);
      //System.out.println("vid color: ", String.format("0x%08x",pixel));

      // LX has a very similar structure for its RGB.
/* PROPERLY ABSTRACT THING
      //System.out.println(" VIDRGB pixel: ", String.format("%x",pixel));

      int b = pixel & 0xff;
      pixel = pixel >> 8;
      int g = pixel & 0xff;
      pixel = pixel >> 8;
      int r = pixel & 0xff;
      pixel = pixel >> 8;
      int a = pixel & 0xff;

      // set the leds
      colors[cube.index] = LX.rgb(r,g,b);
      //System.out.println(" LX RGB color: ",String.format("%x",LX.rgb(r,g,b)));
*/ 
      // when using the default ARGB color space, it's the same as LX's ARGB color space,
      // so we can just copy an integer :-)
      colors[cube.index] = pixel;


    }

  } // run VideoPlayer

} // VideoReader


