package metrics.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.observation.Observation;
import metrics.HasMetric;
import metrics.MetricTransformer;
import metrics.manager.Context;
import metrics.reporter.NotionReporter;
import metrics.reporter.NotionReporter.PropertyFormat;
import metrics.reporter.TextSummaryReporter;
import metrics.reporter.TextSummaryReporter.Aggregation;

/**
 * Handles all metrics related to the memory usage.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class MemoryHandler implements HasMetric<Context>, AutoCloseable {

	/**
	 * The {@link MeterRegistry} to register the metrics to.
	 */
	private MeterRegistry meterRegistry;

	/**
	 * The {@link ScheduledExecutorService} to sample the memory usage.
	 */
	private final ScheduledExecutorService executor;

	/**
	 * The {@link Map} of active contexts.
	 */
	private final Map<String, TrackingContext> activeContexts = new ConcurrentHashMap<>();

	/**
	 * Start a new {@link MemoryHandler} and schedule the memory sampling.
	 */
	public MemoryHandler() {
		this.executor = Executors
				.newSingleThreadScheduledExecutor(new SampleThreadFactory("observer-memory-sampler-%d-%d"));

		executor.scheduleAtFixedRate(this::sampleMemory, 0, 200, TimeUnit.MILLISECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<MetricTransformer> getProvidedMeters() {
		class MemoryAvgMeter implements TextSummaryReporter.AggregatingMeter, NotionReporter.NotionMeter {
			@Override
			public Map<String, Object> toEntry(Meter meter, Map<String, Object> unmodifiableEntry) {
				DistributionSummary memory = (DistributionSummary) meter;
				String name = meter.getId().getName().replace(".sampled", "");

				Map<String, Object> entry = new HashMap<>();
				entry.put(name + ".avg", memory.mean());
				entry.put(name + ".max", memory.max());
				entry.put(name + ".count", (double) memory.count());
				return entry;
			}

			@Override
			public boolean supportsMeter(Meter meter) {
				String name = meter.getId().getName();
				return meter instanceof DistributionSummary && name.startsWith("memory_") && name.endsWith(".sampled");
			}

			@Override
			public boolean shouldResetMeter(Meter meter) {
				return true;
			}

			@Override
			public Aggregation getAggregationType(Meter meter, String key, Object value) {
				return key.endsWith(".max") ? TextSummaryReporter.AGGREGATION_TYPE.MAX : null;
			}

			@Override
			public PropertyFormat getNotionPropertyFormat(Meter meter, String key, Object value) {
				return NotionReporter.PROPERTY_TYPE.NUMBER;
			}
		}
		class MemoryMeter implements NotionReporter.NotionMeter {
			@Override
			public Map<String, Object> toEntry(Meter meter, Map<String, Object> unmodifiableEntry) {
				DistributionSummary memory = (DistributionSummary) meter;

				Map<String, Object> entry = new HashMap<>();
				entry.put(meter.getId().getName(), memory.mean());
				return entry;
			}

			@Override
			public boolean supportsMeter(Meter meter) {
				String name = meter.getId().getName();
				return meter instanceof DistributionSummary && name.startsWith("memory_")
						&& (name.endsWith(".begin") || name.endsWith(".end"));
			}

			@Override
			public boolean shouldResetMeter(Meter meter) {
				return true;
			}

			@Override
			public PropertyFormat getNotionPropertyFormat(Meter meter, String key, Object value) {
				return NotionReporter.PROPERTY_TYPE.NUMBER;
			}
		}
		return List.of(new MemoryAvgMeter(), new MemoryMeter());
	}

	/**
	 * Sample the memory usage and record it to the active contexts.
	 */
	private void sampleMemory() {
		if (activeContexts.isEmpty()) {
			return;
		}

		long currentMemory = getUsedMemory();
		activeContexts.values().forEach(ctx -> ctx.record(currentMemory));
	}

	/**
	 * Submit a new {@link TrackingContext} to the {@link MemoryHandler}.
	 * 
	 * @param meter   The {@link DistributionSummary} to register.
	 * @param context The {@link Context} to register.
	 * @return The id of the scheduled {@link TrackingContext}.
	 */
	protected String submit(DistributionSummary meter, Context context) {
		String id = UUID.randomUUID().toString();
		activeContexts.put(id, new TrackingContext(context, meter));

		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart(Context context) {
		final String suffix = getSuffix(context);
		DistributionSummary summary = DistributionSummary.builder("memory_" + suffix + ".sampled")
				.description("Memory used during observation").baseUnit("kilobytes").tags(createTags(context))
				.register(meterRegistry);

		String id = submit(summary, context);
		context.put("memory-tracking-id", id);
		context.put("memory", getUsedMemory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStop(Context context) {
		final String suffix = getSuffix(context);
		String id = context.getOrDefault("memory-tracking-id", () -> null);
		if (id != null) {
			activeContexts.remove(id);
		}

		long memBefore = context.getOrDefault("memory", 0L);
		long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		meterRegistry.summary("memory_" + suffix + ".begin", createTags(context)).record(memBefore / 1024.0);
		meterRegistry.summary("memory_" + suffix + ".end", createTags(context)).record(memAfter / 1024.0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof Context.PhaseContext || context instanceof Context.VnetEmbeddingContext;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Ends the scheduled sampling of the memory usage.
	 */
	@Override
	public void close() {
		executor.shutdownNow();
	}

	/**
	 * Returns the used memory in bytes.
	 * 
	 * @return The used memory in bytes.
	 */
	private long getUsedMemory() {
		Runtime rt = Runtime.getRuntime();
		return rt.totalMemory() - rt.freeMemory();
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

	/**
	 * The {@link TrackingContext} to track the meters for memory usage.
	 */
	private static class TrackingContext {
		private final DistributionSummary summary;

		public TrackingContext(Observation.Context context, DistributionSummary summary) {
			this.summary = summary;
		}

		public void record(long memory) {
			this.summary.record(memory / 1024.0);
		}
	}
}
