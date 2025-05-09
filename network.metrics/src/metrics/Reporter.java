package metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;

/**
 * An interface for all classes, usually {@link MeterRegistry} implementations,
 * that are used to report metrics.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public interface Reporter {

	/**
	 * Hook that is called after all classes are initialized but before the first
	 * operations.
	 */
	default public void initialized() {
		// noop
	}

	/**
	 * Hook that is called after a set of operations once all related metrics are
	 * available.
	 */
	default public void flush() {
		// noop
	}

	/**
	 * Hook that is called after all operations are finished.
	 */
	default public void conclude() {
		// noop
	}

	/**
	 * Returns the {@link MeterRegistry} that is used to report metrics.
	 */
	default public MeterRegistry getMeterRegistry() {
		if (this instanceof MeterRegistry) {
			return (MeterRegistry) this;
		}

		throw new IllegalStateException(
				this.getClass().getSimpleName() + " is not a " + MeterRegistry.class.getSimpleName()
						+ " and therefore needs to implement " + Reporter.class.getSimpleName() + "#getMeterRegistry!");
	}

	/**
	 * Informs the reporter about a new {@link HasMetric} that is registered.
	 * 
	 * @param meterProvider The {@link HasMetric} that is registered.
	 */
	default public void registerMeterProvider(HasMetric<? extends Observation.Context> meterProvider) {
		// noop
	}
}
