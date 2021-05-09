package metrics;

/**
 * Runtime metric implementation. This one has a start and an end time stamp and calculates the
 * amount of time between them.
 * 
 * Please keep in mind that this implementation adds around 0.05 to 0.1 milliseconds of latency to
 * the measurement. Therefore, it is not well suited for capturing very small time frames.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class RuntimeMetric implements IMetric {

  /**
   * Start time stamp.
   */
  private final long start;

  /**
   * End time stamp.
   */
  private long end = 0;

  /**
   * Creates a new instance of this metric. The time of creation will be used as starting time
   * stamp.
   */
  public RuntimeMetric() {
    this.start = System.nanoTime();
  }

  /**
   * Triggers the end of the time capturing.
   */
  public void stop() {
    this.end = System.nanoTime();
  }

  @Override
  public double getValue() {
    if (end == 0) {
      throw new UnsupportedOperationException("Time measurement was not stopped before.");
    }

    return end - start;
  }

  /**
   * Returns the captured time window in milliseconds.
   * 
   * @return Captured time window in milliseconds.
   */
  public double getMilliSeconds() {
    return getValue() / 1_000_000;
  }

}
