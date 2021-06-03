package metrics.manager;

import metrics.RuntimeDetailedMetric;

/**
 * Global metrics manager. This class can be used to check in metrics and get them back later on.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class GlobalMetricsManager {

  static RuntimeDetailedMetric rt;

  private GlobalMetricsManager() {}

  public static void startRuntime() {
    if (rt != null) {
      throw new UnsupportedOperationException("RuntimeDetailedMetric was already created!");
    }

    rt = new RuntimeDetailedMetric();
  }

  public static RuntimeDetailedMetric getRuntime() {
    return rt;
  }

  public static void startIlpTime() {
    if (rt != null) {
      rt.startIlpTime();
    }
  }

  public static void endIlpTime() {
    if (rt != null) {
      rt.endIlpTime();
    }
  }

  public static void startPmTime() {
    if (rt != null) {
      rt.startPmTime();
    }
  }

  public static void endPmTime() {
    if (rt != null) {
      rt.endPmTime();
    }
  }

  public static void stopRuntime() {
    if (rt != null) {
      rt.stop();
    }
  }

}
