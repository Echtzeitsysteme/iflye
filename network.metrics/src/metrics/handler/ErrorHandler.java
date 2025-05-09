package metrics.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import metrics.HasMetric;
import metrics.manager.Context;

/**
 * Handles all metrics related to the timing.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class ErrorHandler implements HasMetric<Context> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onError(Context context) {
		HasMetric.super.onError(context);

		Set<String> exceptions = new HashSet<>();
		if (context.getLowCardinalityKeyValue("exception") != null) {
			exceptions
					.addAll(Arrays.asList(context.getLowCardinalityKeyValue("exception").getValue().split("\s*,\s*")));
		}

		Throwable e = context.getError();
		do {
			exceptions.add(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
			e = e.getCause();
		} while (e != null);

		context.addLowCardinalityKeyValue(KeyValue.of("exception", String.join(", ", exceptions)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof Context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMeterRegistry(MeterRegistry meterRegistry) {
		// noop
	}

}
