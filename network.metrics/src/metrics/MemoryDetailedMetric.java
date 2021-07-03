package metrics;

import java.util.LinkedList;
import java.util.List;

/**
 * Detailed memory metric implementation. This one can be used to capture various memory metric
 * values.
 * 
 * Please keep in mind that this metric will slow down your program, as it has to explicitly trigger
 * the garbage collector.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class MemoryDetailedMetric extends MemoryMetric {

  // TODO: Documentation.

  final List<MemoryMetric> values;

  public MemoryDetailedMetric() {
    values = new LinkedList<MemoryMetric>();
  }

  public int capture() {
    values.add(new MemoryMetric());
    return values.size() - 1;
  }

  public double getValue(final int index) {
    return values.get(index).getValue();
  }

}
