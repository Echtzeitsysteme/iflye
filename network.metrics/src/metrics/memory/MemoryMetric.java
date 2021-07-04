package metrics.memory;

import metrics.IMetric;
import metrics.MetricConsts;

/**
 * Memory metric implementation.
 * 
 * Please keep in mind that this metric will slow down your program, as it has to explicitly trigger
 * the garbage collector.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class MemoryMetric implements IMetric {

  /**
   * Measured value of maximum used RAM in kiB.
   */
  private final long memory;

  /**
   * Creates a new instance of this memory metric and measures the value of used RAM.
   */
  public MemoryMetric() {
    final Runtime rt = Runtime.getRuntime();
    rt.gc();
    memory = rt.totalMemory() - rt.freeMemory();
  }

  @Override
  public double getValue() {
    return 1.0 * memory / MetricConsts.MEBIBYTE;
  }

}
