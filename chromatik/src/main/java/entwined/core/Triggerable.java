package entwined.core;

public interface Triggerable {
  public boolean isTriggered();
  public void onTriggered();
  public void onReleased();
  public void onTimeout();
  //public void addOutputTriggeredListener(LXParameterListener listener);
}