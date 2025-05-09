package metrics.handler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import metrics.HasMetric;
import metrics.MetricTransformer;
import metrics.manager.Context;
import metrics.reporter.NotionReporter;
import metrics.reporter.NotionReporter.PropertyFormat;
import metrics.reporter.TextSummaryReporter;
import metrics.reporter.TextSummaryReporter.Aggregation;

/**
 * Handles all metrics related to the timing.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class TimingHandler implements HasMetric<Context> {

	/**
	 * The {@link MeterRegistry} to register the metrics to.
	 */
	private MeterRegistry meterRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<MetricTransformer> getProvidedMeters() {
		class TimestampMeter implements MetricTransformer, NotionReporter.NotionMeter {
			@Override
			public Map<String, Object> toEntry(Meter meter, Map<String, Object> unmodifiableEntry) {
				DistributionSummary timestampMeter = (DistributionSummary) meter;
				Double timestamp = Double.valueOf(timestampMeter.max());
				Map<String, Object> entry = new HashMap<>();
				entry.put(meter.getId().getName(), epochSecondsToOffsetDateTime(timestamp));
				return entry;
			}

			public OffsetDateTime epochSecondsToOffsetDateTime(double timestamp) {
				Instant instant = Instant.ofEpochMilli((long) timestamp);
				ZoneId zoneId = ZoneId.systemDefault(); // Use the system default time zone
				LocalDateTime dateTime = instant.atZone(zoneId).toLocalDateTime();

				return ZonedDateTime.of(dateTime, ZoneOffset.systemDefault()).toOffsetDateTime();
			}

			@Override
			public boolean supportsMeter(Meter meter) {
				return meter instanceof DistributionSummary && meter.getId().getName().equals("timestamp");
			}

			@Override
			public boolean shouldResetMeter(Meter meter) {
				return true;
			}

			@Override
			public PropertyFormat getNotionPropertyFormat(Meter meter, String key, Object value) {
				return NotionReporter.PROPERTY_TYPE.DATE;
			}
		}
		class TimeMeter implements TextSummaryReporter.AggregatingMeter, NotionReporter.NotionMeter {
			@Override
			public Map<String, Object> toEntry(Meter meter, Map<String, Object> unmodifiableEntry) {
				Timer timer = (Timer) meter;
				Map<String, Object> entry = new HashMap<>();
				entry.put(meter.getId().getName(), (Object) timer.mean(TimeUnit.SECONDS));
				return entry;
			}

			@Override
			public boolean supportsMeter(Meter meter) {
				return meter instanceof Timer && meter.getId().getName().startsWith("time_");
			}

			@Override
			public boolean shouldResetMeter(Meter meter) {
				return true;
			}

			@Override
			public Aggregation getAggregationType(Meter meter, String key, Object value) {
				return TextSummaryReporter.AGGREGATION_TYPE.SUM;
			}

			@Override
			public PropertyFormat getNotionPropertyFormat(Meter meter, String key, Object value) {
				return NotionReporter.PROPERTY_TYPE.NUMBER;
			}
		}
		return List.of(new TimestampMeter(), new TimeMeter());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart(Context context) {
		context.put(Timer.Sample.class, Timer.start(meterRegistry));

		if (context instanceof Context.VnetEmbeddingContext) {
			OffsetDateTime now = OffsetDateTime.now();
			context.put("timestamp", now);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStop(Context context) {
		final String suffix = getSuffix(context);

		Timer.Sample sample = context.getRequired(Timer.Sample.class);
		if (sample != null) {
			sample.stop(Timer.builder("time_" + suffix).tags(createTags(context)).register(meterRegistry));
		}

		if (context.containsKey("timestamp")) {
			OffsetDateTime now = context.getRequired("timestamp");
			meterRegistry.summary("timestamp", createTags(context))
					.record(Double.valueOf(now.toInstant().toEpochMilli()));
		}
	}

	/**
	 * Returns the suffix for the metric name based on the context.
	 * 
	 * @param context The context to get the suffix for.
	 * @return The suffix for the metric name.
	 */
	public String getSuffix(Context context) {
		if (context instanceof Context.PhaseContext) {
			return ((Context.PhaseContext) context).getPhase();
		}

		return "total";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof Context.PhaseContext || context instanceof Context.VnetEmbeddingContext;
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
