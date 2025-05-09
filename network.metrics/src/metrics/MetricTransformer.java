package metrics;

import java.util.Map;

import io.micrometer.core.instrument.Meter;

/**
 * Interface for transforming metrics into a report output format.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public interface MetricTransformer {

	/**
	 * @param meter The meter to be transformed.
	 * @return If the given meter is supported by this transformer.
	 */
	public boolean supportsMeter(Meter meter);

	/**
	 * @param meter             The meter to be transformed.
	 * @param unmodifiableEntry The entry up-to-now where the meter should be put
	 *                          into.
	 * @return The transformed entry.
	 */
	public Map<String, Object> toEntry(Meter meter, Map<String, Object> unmodifiableEntry);

	/**
	 * If the given meter should be reset after the transformation.
	 * 
	 * @param meter The meter that was transformed.
	 * @return True if the meter should be reset after the transformation, false
	 *         otherwise.
	 */
	default public boolean shouldResetMeter(Meter meter) {
		return false;
	}

}