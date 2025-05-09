package metrics.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import metrics.HasMetric;
import metrics.MetricTransformer;
import metrics.manager.Context;
import metrics.reporter.NotionReporter;
import metrics.reporter.NotionReporter.PropertyFormat;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.observation.Observation;

/**
 * Put a counter to the given level of the embedding.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class CounterHandler implements HasMetric<Context.VnetEmbeddingContext> {

	/**
	 * The {@link MeterRegistry} to register the metrics to.
	 */
	private MeterRegistry meterRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<MetricTransformer> getProvidedMeters() {
		class CounterMeter implements MetricTransformer, NotionReporter.NotionMeter {
			@Override
			public Map<String, Object> toEntry(Meter meter, Map<String, Object> unmodifiableEntry) {
				if (meter instanceof Counter) {
					return new HashMap<>();
				}

				DistributionSummary counter = (DistributionSummary) meter;

				Map<String, Object> entry = new HashMap<>();
				entry.put(meter.getId().getName(), counter.max());
				return entry;
			}

			@Override
			public boolean supportsMeter(Meter meter) {
				return meter instanceof DistributionSummary && meter.getId().getName().startsWith("level_")
						&& meter.getId().getName().endsWith(".counter")
						|| meter instanceof Counter && meter.getId().getName().equals("embedding_counter");
			}

			@Override
			public boolean shouldResetMeter(Meter meter) {
				return meter instanceof DistributionSummary;
			}

			@Override
			public PropertyFormat getNotionPropertyFormat(Meter meter, String key, Object value) {
				return NotionReporter.PROPERTY_TYPE.NUMBER;
			}
		}
		return List.of(new CounterMeter());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart(Context.VnetEmbeddingContext context) {

		// Reset all counters of nested levels
		meterRegistry.find("embedding_counter").tag("embedding_level", (v) -> Integer.parseInt(v) > context.getLevel())
				.counters().forEach(this.meterRegistry::remove);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStop(Context.VnetEmbeddingContext context) {

		for (int i = 0; i <= context.getLevel(); i++) {
			Counter counter = meterRegistry.find("embedding_counter").tag("embedding_level", String.valueOf(i))
					.counter();
			if (counter == null) {
				counter = meterRegistry.counter("embedding_counter", Tags.of("embedding_level", String.valueOf(i)));
			}

			meterRegistry.summary("level_" + i + ".counter", createTags(context)).record(counter.count());

			if (i == context.getLevel()) {
				counter.increment();
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof Context.VnetEmbeddingContext;
	}

	/**
	 * Transform the context key-values into a set of Tags to register with the
	 * meters.
	 * 
	 * @param context The context to transform.
	 * @return The tags to register with the meters.
	 */
	private List<Tag> createTags(Context context) {
		return context.getLowCardinalityKeyValues().stream().map(kv -> Tag.of(kv.getKey(), kv.getValue())).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMeterRegistry(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

}
