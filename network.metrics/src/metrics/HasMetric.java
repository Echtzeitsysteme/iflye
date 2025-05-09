package metrics;

import java.util.Collection;
import java.util.List;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;

/**
 * An interface for all {@link ObservationHandler}s that provide a {@link Meter}
 * to give further instructions on how to handle the Meter in the
 * {@link Reporter}.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public interface HasMetric<T extends Observation.Context> extends ObservationHandler<T> {

	/**
	 * @return The {@link MetricTransformer} that is responsible for the given
	 *         {@link Meter} or null if no such transformer exists.
	 * 
	 * @param meter The {@link Meter} to be transformed.
	 */
	default public MetricTransformer getProvidedMeter(Meter meter) {
		for (MetricTransformer transformer : getProvidedMeters()) {
			if (transformer.supportsMeter(meter)) {
				return transformer;
			}
		}

		return null;
	}

	/**
	 * @return A collection of {@link MetricTransformer}s that are provided by this
	 *         {@link ObservationHandler}.
	 */
	default public Collection<MetricTransformer> getProvidedMeters() {
		return List.of();
	}

	/**
	 * Set the currently used {@link MeterRegistry} for this {@link HasMetric}.
	 * 
	 * @param meterRegistry The {@link MeterRegistry} to be set.
	 * @throws IllegalArgumentException If the given {@link MeterRegistry} is null.
	 */
	public void setMeterRegistry(final MeterRegistry meterRegistry);

}
