package metrics.memory;

import metrics.IMetric;

/**
 * Memory metric implementation.
 * 
 * Please keep in mind that this metric will slow down your program, as it has to explicitly trigger
 * the garbage collector.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class MemoryMetric implements IMetric {

  private final long memory;
  private static final long MEGABYTE = 1024L * 1024L;

  public MemoryMetric() {
    final Runtime rt = Runtime.getRuntime();
    rt.gc();
    memory = rt.totalMemory() - rt.freeMemory();
  }

  @Override
  public double getValue() {
    return 1.0 * memory / MEGABYTE;
  }

}
