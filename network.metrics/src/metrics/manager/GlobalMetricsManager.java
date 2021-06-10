package metrics.manager;

import metrics.RuntimeDetailedMetric;

/**
 * Global metrics manager. This class can be used to check in metrics and get them back later on.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class GlobalMetricsManager {

  /**
   * Global runtime detailed metric.
   */
  static RuntimeDetailedMetric rt;

  /**
   * Private constructor ensures no instantiation of this class.
   */
  private GlobalMetricsManager() {}

  /**
   * Starts the complete runtime measurement.
   */
  public static void startRuntime() {
    if (rt != null) {
      throw new UnsupportedOperationException("RuntimeDetailedMetric was already created!");
    }

    rt = new RuntimeDetailedMetric();
  }

  /**
   * Returns the runtime metric.
   * 
   * @return Runtime metric.
   */
  public static RuntimeDetailedMetric getRuntime() {
    return rt;
  }

  /**
   * Sets a start point for the ILP time measurement.
   */
  public static void startIlpTime() {
    if (rt != null) {
      rt.startIlpTime();
    }
  }

  /**
   * Sets an end point for the ILP time measurement and increments the global ILP time value.
   */
  public static void endIlpTime() {
    if (rt != null) {
      rt.endIlpTime();
    }
  }

  /**
   * Sets a start point for the PM time measurement.
   */
  public static void startPmTime() {
    if (rt != null) {
      rt.startPmTime();
    }
  }

  /**
   * Sets an end point for the PM time measurement and increments the global PM time value.
   */
  public static void endPmTime() {
    if (rt != null) {
      rt.endPmTime();
    }
  }

  /**
   * Sets a start point for the deploy time measurement.
   */
  public static void startDeployTime() {
    if (rt != null) {
      rt.startDeployTime();
    }
  }

  /**
   * Sets an end point for the deploy time mesaurement and increments the global deploy time value.
   */
  public static void endDeployTime() {
    if (rt != null) {
      rt.endDeployTime();
    }
  }

  /**
   * Stops the global runtime measurement.
   */
  public static void stopRuntime() {
    if (rt != null) {
      rt.stop();
    }
  }



}
