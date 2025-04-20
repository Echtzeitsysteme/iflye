package metrics.handler;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
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
 * Handles all metrics related to the thread usage.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class ThreadHandler implements HasMetric<Context>, AutoCloseable {

	/**
	 * The {@link MeterRegistry} to register the metrics to.
	 */
	private MeterRegistry meterRegistry;

	/**
	 * The {@link ScheduledExecutorService} to sample the thread usage.
	 */
	private final ScheduledExecutorService executor;

	/**
	 * The {@link ThreadMXBean} to sample the thread usage.
	 */
	private final ThreadMXBean threadBean;

	/**
	 * The {@link Map} of active contexts.
	 */
	private final Map<String, TrackingContext> activeContexts = new ConcurrentHashMap<>();

	/**
	 * Start a new {@link ThreadHandler} and schedule the thread sampling.
	 */
	public ThreadHandler() {
		this.threadBean = ManagementFactory.getThreadMXBean();
		this.executor = Executors
				.newSingleThreadScheduledExecutor(new SampleThreadFactory("observer-thread-sampler-%d-%d"));
		executor.scheduleAtFixedRate(this::sampleThreads, 0, 200, TimeUnit.MILLISECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<MetricTransformer> getProvidedMeters() {
		class ThreadMeter implements TextSummaryReporter.AggregatingMeter, NotionReporter.NotionMeter {
			@Override
			public Map<String, Object> toEntry(Meter meter, Map<String, Object> unmodifiableEntry) {
				DistributionSummary memory = (DistributionSummary) meter;
				String name = meter.getId().getName();

				Map<String, Object> entry = new HashMap<>();
				entry.put(name + ".avg", memory.mean());
				entry.put(name + ".max", memory.max());
				entry.put(name + ".count", (double) memory.count());
				return entry;
			}

			@Override
			public boolean supportsMeter(Meter meter) {
				String name = meter.getId().getName();
				return meter instanceof DistributionSummary && name.startsWith("threads_");
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
		return List.of(new ThreadMeter());
	}

	/**
	 * Sample the thread usage and record it to the active contexts.
	 */
	private void sampleThreads() {
		if (activeContexts.isEmpty()) {
			return;
		}

		long threadCount = this.threadBean.getThreadCount();
		long totalStartedThreadCount = this.threadBean.getTotalStartedThreadCount();
		long daemonThreadCount = this.threadBean.getDaemonThreadCount();

		activeContexts.values().forEach(ctx -> ctx.record(threadCount, totalStartedThreadCount, daemonThreadCount));
	}

	/**
	 * Submit a new {@link TrackingContext} to the active contexts.
	 * 
	 * @param context The {@link TrackingContext} to submit.
	 * @return The id of the submitted {@link TrackingContext}.
	 */
	protected String submit(TrackingContext context) {
		String id = UUID.randomUUID().toString();
		activeContexts.put(id, context);

		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart(Context context) {
		final String suffix = getSuffix(context);
		DistributionSummary totalThreadSummary = DistributionSummary.builder("threads_" + suffix + ".total")
				.description("Total threads used during processing").tags(createTags(context)).register(meterRegistry);
		DistributionSummary startedThreadSummary = DistributionSummary.builder("threads_" + suffix + ".started")
				.description("Started threads used").tags(createTags(context)).register(meterRegistry);
		DistributionSummary daemonThreadSummary = DistributionSummary.builder("threads_" + suffix + ".daemon")
				.description("Daemon threads used").tags(createTags(context)).register(meterRegistry);

		String id = submit(new TrackingContext(context, totalThreadSummary, startedThreadSummary, daemonThreadSummary));
		context.put("thread-tracking-id", id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStop(Context context) {
		String id = context.getOrDefault("thread-tracking-id", () -> null);
		if (id != null) {
			activeContexts.remove(id);
		}
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
	 * Ends the scheduled thread sampling.
	 */
	@Override
	public void close() {
		executor.shutdownNow();
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
	 * The {@link TrackingContext} to track the thread usage.
	 */
	private static class TrackingContext {
		private final DistributionSummary totalThreadSummary;
		private final DistributionSummary startedThreadSummary;
		private final DistributionSummary daemonThreadSummary;

		public TrackingContext(Observation.Context context, DistributionSummary totalThreadSummary,
				DistributionSummary startedThreadSummary, DistributionSummary daemonThreadSummary) {
			this.totalThreadSummary = totalThreadSummary;
			this.startedThreadSummary = startedThreadSummary;
			this.daemonThreadSummary = daemonThreadSummary;
		}

		public void record(long threadCount, long startedThreadCount, long daemonThreadCount) {
			this.totalThreadSummary.record(threadCount);
			this.startedThreadSummary.record(startedThreadCount);
			this.daemonThreadSummary.record(daemonThreadCount);
		}
	}
}
