package metrics.memory;

import java.util.LinkedList;
import java.util.List;

/**
 * Detailed memory metric implementation. This one can be used to capture
 * various memory metric values.
 *
 * Please keep in mind that this metric will slow down your program, as it has
 * to explicitly trigger the garbage collector.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class MemoryDetailedMetric extends MemoryMetric {

	/**
	 * List of memory metrics to use during multiple measurements.
	 */
	final List<MemoryMetric> values;

	/**
	 * Creates a new instance of this detailed memory metric.
	 */
	public MemoryDetailedMetric() {
		values = new LinkedList<>();
	}

	/**
	 * Adds a dummy entry to the measurement. This is used for algorithms, that,
	 * e.g., do not have a dedicated ILP time (as the TAF algorithm).
	 *
	 * @return Index of the newly created measurement.
	 */
	public int dummy() {
		values.add(null);
		return values.size() - 1;
	}

	/**
	 * Measures the amount of free memory and saves it to the list.
	 *
	 * @return Index of the newly created measurement.
	 */
	public int capture() {
		values.add(new MemoryMetric());
		return values.size() - 1;
	}

	/**
	 * Returns the measured value for the given index or -1 if no such value exists.
	 *
	 * @param index Index to return value for.
	 * @return Measured value for the given index.
	 */
	public double getValue(final int index) {
		if (values == null || values.isEmpty() || index >= getSize() || values.get(index) == null) {
			return -1;
		}

		return values.get(index).getValue();
	}

	/**
	 * Returns the size of all measured memory values.
	 *
	 * @return Size of all measured memory values.
	 */
	public int getSize() {
		return values.size();
	}

	@Override
	public double getValue() {
		if (values == null || values.isEmpty()) {
			return -1;
		}

		return getValue(getSize() - 1);
	}

}
