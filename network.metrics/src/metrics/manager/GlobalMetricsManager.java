package metrics.manager;

import metrics.MetricConfig;
import metrics.memory.MemoryDetailedMetric;
import metrics.time.RuntimeDetailedMetric;

/**
 * Global metrics manager. This class can be used to check in metrics and get them back later on.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class GlobalMetricsManager {

  /**
   * Global time measurement array.
   */
  private static final double[] globalTimeMeasurement = new double[5];

  /**
   * Global runtime detailed metric.
   */
  static RuntimeDetailedMetric rt;

  /**
   * Global memory detailed metric.
   */
  static MemoryDetailedMetric mm;

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
   * Sets an end point for the deploy time measurement and increments the global deploy time value.
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

  /**
   * Resets the global runtime measurement.
   */
  public static void resetRuntime() {
    globalTimeMeasurement[0] += rt.getValue();
    globalTimeMeasurement[1] += rt.getPmValue();
    globalTimeMeasurement[2] += rt.getIlpValue();
    globalTimeMeasurement[3] += rt.getDeployValue();
    globalTimeMeasurement[4] += rt.getRestValue();
    rt = null;
  }

  /**
   * Returns the global captured time array.
   * 
   * @return Global captured time array.
   */
  public static double[] getGlobalTimeArray() {
    return globalTimeMeasurement;
  }

  /**
   * Triggers a memory measurement.
   * 
   * @return Index of the new measurement.
   */
  public static int measureMemory() {
    if (mm == null) {
      mm = new MemoryDetailedMetric();
    }

    if (!MetricConfig.ENABLE_MEMORY) {
      return -1;
    }

    return mm.capture();
  }

  public static int dummyMemory() {
    if (mm == null) {
      mm = new MemoryDetailedMetric();
    }

    if (!MetricConfig.ENABLE_MEMORY) {
      return -1;
    }

    return mm.dummy();
  }

  /**
   * Resets the global memory measurement.
   */
  public static void resetMemory() {
    mm = null;
  }

  /**
   * Returns the captured memory detailed metric.
   * 
   * @return Captured memory detailed metric.
   */
  public static MemoryDetailedMetric getMemory() {
    return mm;
  }

}
