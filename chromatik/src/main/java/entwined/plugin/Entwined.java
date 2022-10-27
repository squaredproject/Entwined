package entwined.plugin;

import java.io.File;

import heronarts.lx.LX;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.LXStudio.UI;

public class Entwined implements LXStudio.Plugin {

  public static void log(String str) {
    LX.log("[ENTWINED] " + str);
  }
  
  @Override
  public void initialize(LX lx) {
    log("Entwined.initialize()");
    
    // NOTE(mcslee): start up things here like the Server, ServerController, CanopyServer
    // global stuff can go directly in the initialize method.
    
    
    lx.addProjectListener(new LX.ProjectListener() {
      @Override
      public void projectChanged(File file, Change change) {
        if (change == LX.ProjectListener.Change.OPEN) {
          log("Entwined.projectChanged(OPEN)");
          // NOTE(mcslee): a new project file has been opened! may want to
          // initialize or re-initialize things that depend upon project state
          // here
        }
      }
    });
  }

  @Override
  public void initializeUI(LXStudio lx, UI ui) {
    // NOTE(mcslee): probably nothing to do here
    log("Entwined.initializeUI");
    
  }

  @Override
  public void onUIReady(LXStudio lx, UI ui) {
    // NOTE(mcslee): potentially something here if we ever want custom UI components, but
    // most likely they are also not needed
    log("Entwined.onUIReady");
  }

}
