import java.awt.Color;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;

class RunHeadless {
  public static void main(String[] args) {
    // Force loading awt, since there's some weird race condition on mac
    // http://lists.apple.com/archives/java-dev/2008/May/msg00413.html
    new Color(0);

    if (args.length < 1) {
      System.out.println("Use run.sh");
      System.exit(1);
    }
    String rootDirectory = args[0];
    
    PureJavaEngine engine = new PureJavaEngine(rootDirectory);
    engine.start();
  }
}

class PureJavaEngine extends Engine {
  PureJavaEngine(String projectPath) {
    super(projectPath);
  }

  LX createLX() {
    return new JavaLX(model);
  }
}

class JavaLX extends LX {
  JavaLX(LXModel model) {
    super(model);
  }
}
