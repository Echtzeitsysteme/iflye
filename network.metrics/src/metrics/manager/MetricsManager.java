package metrics.manager;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

import io.micrometer.common.KeyValues;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.Observations;
import metrics.HasMetric;
import metrics.Reporter;
import metrics.handler.CounterHandler;
import metrics.handler.EmbeddedNetworkHandler;
import metrics.handler.MemoryHandler;
import metrics.handler.ThreadHandler;
import metrics.handler.TimingHandler;
import metrics.reporter.TextSummaryReporter;

/**
 * A manager for all metrics and observations.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class MetricsManager implements AutoCloseable {

	/**
	 * The default context to be used for all observations.
	 */
	public static final Supplier<? extends Observation.Context> DEFAULT_CONTEXT = Observation.Context::new;

	/**
	 * The current instance of the {@link MetricsManager} for each thread.
	 * 
	 * @see #getInstance()
	 * @see #wrap(Function, Tag...)
	 * @see #withTags(Function, Tag...)
	 * @see #withTags(Function, String...)
	 * @see #withTags(Function, Iterable)
	 */
	protected static final ThreadLocal<Deque<MetricsManager>> instance = ThreadLocal
			.withInitial(() -> new ArrayDeque<>());

	/**
	 * The background {@link MeterRegistry} to be used for all metrics.
	 */
	protected final CompositeMeterRegistry meterRegistry;

	/**
	 * The background {@link ObservationRegistry} to be used for all observations.
	 */
	protected final ObservationRegistry observationRegistry;

	/**
	 * The collection of all registered {@link Reporter}s.
	 */
	protected final Set<Reporter> reporters = new HashSet<>();

	/**
	 * The collection of all registered {@link HasMetric}s.
	 */
	protected final Set<HasMetric<? extends Observation.Context>> meterProviders = new HashSet<>();

	/**
	 * The tags to be used for all observations that are started by this
	 * MetricsManager.
	 */
	protected Tags tags;

	/**
	 * A Default MetricsManager configuration with a {@link TextSummaryReporter} and
	 * the {@link TimingHandler} and {@link EmbeddedNetworkHandler}.
	 */
	public static class Default extends MetricsManager {
		public Default() {
			super();

			this.addMeter(new TimingHandler());
			this.addMeter(new EmbeddedNetworkHandler());
			this.addMeter(new CounterHandler());
			this.addMeter(new MemoryHandler());
			this.addMeter(new ThreadHandler());

			this.addReporter(new TextSummaryReporter());
		}
	}

	/**
	 * Creates a new {@link MetricsManager} with the default {@link MeterRegistry}
	 * and {@link ObservationRegistry}.
	 */
	public MetricsManager() {
		this(new CompositeMeterRegistry(), Observations.getGlobalRegistry());
	}

	/**
	 * Creates a new {@link MetricsManager} with the given {@link MeterRegistry} and
	 * {@link ObservationRegistry}. Used for propagating the registries in the
	 * stack.
	 * 
	 * @param meterRegistry       The {@link MeterRegistry} to be used for all
	 *                            metrics.
	 * @param observationRegistry The {@link ObservationRegistry} to be used for all
	 *                            observations.
	 */
	protected MetricsManager(CompositeMeterRegistry meterRegistry, ObservationRegistry observationRegistry) {
		this.meterRegistry = meterRegistry;
		this.observationRegistry = observationRegistry;
		instance.get().addFirst(this);
	}

	/**
	 * @return The most recent instance of the {@link MetricsManager} for the
	 *         current thread.
	 */
	public static MetricsManager getInstance() {
		return instance.get().size() > 0 ? instance.get().getFirst() : null;
	}

	/**
	 * @return The {@link MeterRegistry} to be used for all metrics.
	 */
	public MeterRegistry getMeterRegistry() {
		return this.meterRegistry;
	}

	/**
	 * @return The {@link ObservationRegistry} to be used for all observations.
	 */
	public ObservationRegistry getObservationRegistry() {
		return this.observationRegistry;
	}

	/**
	 * @return The collection of all registered {@link Reporter}s.
	 */
	public Set<Reporter> getReporters() {
		return this.reporters;
	}

	/**
	 * @return The collection of all registered {@link HasMetric}s.
	 */
	public Set<HasMetric<? extends Observation.Context>> getMeterProviders() {
		return this.meterProviders;
	}

	/**
	 * @return The tags to be used for all observations that are started by this
	 *         {@link MetricsManager}.
	 */
	public Tags getTags() {
		return this.tags;
	}

	/**
	 * Adds a new {@link Reporter} to the {@link MetricsManager}.
	 */
	public void addReporter(final Reporter reporter) {
		this.meterProviders.forEach(reporter::registerMeterProvider);
		this.reporters.add(reporter);
		this.meterRegistry.add(reporter.getMeterRegistry());
	}

	/**
	 * Adds a new {@link HasMetric} to the {@link MetricsManager}.
	 */
	public void addMeter(final HasMetric<? extends Observation.Context> meterProvider) {
		meterProvider.setMeterRegistry(this.meterRegistry);
		this.reporters.forEach((reporter) -> reporter.registerMeterProvider(meterProvider));
		this.meterProviders.add(meterProvider);
		this.observationRegistry.observationConfig().observationHandler(meterProvider);
	}

	/**
	 * Notify reporters about finished initialization.
	 */
	public void initialized() {
		this.reporters.forEach((reporter) -> reporter.initialized());
	}

	/**
	 * Notify reporters that it is safe to flush the metrics.
	 */
	public void flush() {
		this.reporters.forEach((reporter) -> reporter.flush());
	}

	/**
	 * Notify reporters that the metrics are no longer needed and can be concluded.
	 * This can trigger summarizing or finilizing actions.
	 */
	public void conclude() {
		this.reporters.forEach((reporter) -> reporter.conclude());
	}

	/**
	 * Creates a new {@link MetricsManager} with the given tags. The new
	 * {@link MetricsManager} will have the same {@link MeterRegistry} and
	 * {@link ObservationRegistry} as this one, but will have the given tags.
	 * 
	 * @param tags The tags to be used for all observations that are started by the
	 *             new {@link MetricsManager}.
	 * @return A new {@link MetricsManager} with the given tags.
	 */
	public MetricsManager withTags(Tag... tags) {
		return withTags(Tags.of(tags));
	}

	/**
	 * Creates a new {@link MetricsManager} with the given tags. The new
	 * {@link MetricsManager} will have the same {@link MeterRegistry} and
	 * {@link ObservationRegistry} as this one, but will have the given tags.
	 * 
	 * @param tags The tags to be used for all observations that are started by the
	 *             new {@link MetricsManager}.
	 * @return A new {@link MetricsManager} with the given tags.
	 */
	public MetricsManager withTags(String... tags) {
		return withTags(Tags.of(tags));
	}

	/**
	 * Creates a new {@link MetricsManager} with the given tags. The new
	 * {@link MetricsManager} will have the same {@link MeterRegistry} and
	 * {@link ObservationRegistry} as this one, but will have the given tags.
	 * 
	 * @param tags The tags to be used for all observations that are started by the
	 *             new {@link MetricsManager}.
	 * @return A new {@link MetricsManager} with the given tags.
	 */
	public MetricsManager withTags(Iterable<? extends Tag> tags) {
		final MetricsManager metricsManager = this.clone();
		if (tags != null) {
			metricsManager.addTags(Tags.of(tags));
		}

		return metricsManager;
	}

	/**
	 * Creates a new {@link MetricsManager} based on this one. The new
	 * {@link MetricsManager} will have the same {@link MeterRegistry} and
	 * {@link ObservationRegistry} as this one.
	 * 
	 * @return A new {@link MetricsManager} with the given tags.
	 */
	public MetricsManager clone() {
		final MetricsManager metricsManager = new MetricsManager(meterRegistry, observationRegistry);
		metricsManager.addTags(this.tags);
		metricsManager.reporters.addAll(this.reporters);

		return metricsManager;
	}

	/**
	 * Runs the callable in the context of a new {@link MetricsManager} instance.
	 * The new {@link MetricsManager} will have the same {@link MeterRegistry} and
	 * {@link ObservationRegistry} as this one, but will have the given tags. The
	 * return value of the callable will be returned.
	 * 
	 * @param callable The callable to be executed with the new
	 *                 {@link MetricsManager}.
	 * @param tags     The tags to be used for all observations that are started by
	 *                 the new {@link MetricsManager}.
	 * @return The return value of the callable.
	 * @throws E Any exception that might occur.
	 */
	public <T, E extends Throwable> T withTags(Callable<T> callable, Tag... tags) throws E {
		return this.withTags(callable, Tags.of(tags));
	}

	/**
	 * Runs the callable in the context of a new {@link MetricsManager} instance.
	 * The new {@link MetricsManager} will have the same {@link MeterRegistry} and
	 * {@link ObservationRegistry} as this one, but will have the given tags. The
	 * return value of the callable will be returned.
	 * 
	 * @param callable The callable to be executed with the new
	 *                 {@link MetricsManager}.
	 * @param tags     The tags to be used for all observations that are started by
	 *                 the new {@link MetricsManager}.
	 * @return The return value of the callable.
	 * @throws E Any exception that might occur.
	 */
	public <T, E extends Throwable> T withTags(Callable<T> callable, String... tags) throws E {
		return this.withTags(callable, Tags.of(tags));
	}

	/**
	 * Runs the callable in the context of a new {@link MetricsManager} instance.
	 * The new {@link MetricsManager} will have the same {@link MeterRegistry} and
	 * {@link ObservationRegistry} as this one, but will have the given tags. The
	 * return value of the callable will be returned.
	 * 
	 * @param callable The callable to be executed with the new
	 *                 {@link MetricsManager}.
	 * @param tags     The tags to be used for all observations that are started by
	 *                 the new {@link MetricsManager}.
	 * @return The return value of the callable.
	 * @throws E Any exception that might occur.
	 */
	public <T, E extends Throwable> T withTags(Callable<T> callable, Iterable<? extends Tag> tags) throws E {
		try (final MetricsManager metricsManager = this.withTags(tags)) {
			final T result = callable.call();
			return result;
		} catch (Throwable e) {
			throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
		}
	}

	/**
	 * Wraps the call of the callable in a new {@link MetricsManager} instance. The
	 * new {@link MetricsManager} will have the same {@link MeterRegistry} and
	 * {@link ObservationRegistry} as this one, but will have the given tags. A new
	 * callable will be returned.
	 * 
	 * @param callable The callable to be executed with the new
	 *                 {@link MetricsManager}.
	 * @param tags     The tags to be used for all observations that are started by
	 *                 the new {@link MetricsManager}.
	 * @return A new callable.
	 */
	public <T, E extends Throwable> Callable<T> wrap(Callable<T> callable, Tag... tags) {
		return this.wrap(callable, Tags.of(tags));
	}

	/**
	 * Wraps the call of the callable in a new {@link MetricsManager} instance. The
	 * new {@link MetricsManager} will have the same {@link MeterRegistry} and
	 * {@link ObservationRegistry} as this one, but will have the given tags. A new
	 * callable will be returned.
	 * 
	 * @param callable The callable to be executed with the new
	 *                 {@link MetricsManager}.
	 * @param tags     The tags to be used for all observations that are started by
	 *                 the new {@link MetricsManager}.
	 * @return A new callable.
	 */
	public <T, E extends Throwable> Callable<T> wrap(Callable<T> callable, String... tags) {
		return this.wrap(callable, Tags.of(tags));
	}

	/**
	 * Wraps the call of the callable in a new {@link MetricsManager} instance. The
	 * new {@link MetricsManager} will have the same {@link MeterRegistry} and
	 * {@link ObservationRegistry} as this one, but will have the given tags. A new
	 * callable will be returned.
	 * 
	 * @param callable The callable to be executed with the new
	 *                 {@link MetricsManager}.
	 * @param tags     The tags to be used for all observations that are started by
	 *                 the new {@link MetricsManager}.
	 * @return A new callable.
	 */
	public <T, E extends Throwable> Callable<T> wrap(Callable<T> callable, Iterable<? extends Tag> tags) {
		return () -> this.withTags(callable, tags);
	}

	/**
	 * Wraps the call of the callable in a new {@link MetricsManager} instance. The
	 * new {@link MetricsManager} will have the same {@link MeterRegistry},
	 * {@link ObservationRegistry} and tags as this one. A new callable will be
	 * returned.
	 * 
	 * @param callable The callable to be executed with the new
	 *                 {@link MetricsManager}.
	 * @return A new callable.
	 */
	public <T> Callable<T> wrap(Callable<T> callable) {
		return () -> {
			try (final MetricsManager metricsManager = this.clone()) {
				return callable.call();
			}
		};
	}

	/**
	 * Adds the given tags to the {@link MetricsManager}. The tags will be merged
	 * with the existing tags.
	 * 
	 * @param tags The tags to be added to the {@link MetricsManager}.
	 * @return The {@link MetricsManager} with the added tags.
	 * @see #mergeTags(Tags)
	 */
	public MetricsManager addTags(Tag... tags) {
		return this.addTags(Tags.of(tags));
	}

	/**
	 * Adds the given tags to the {@link MetricsManager}. The tags will be merged
	 * with the existing tags.
	 * 
	 * @param tags The tags to be added to the {@link MetricsManager}.
	 * @return The {@link MetricsManager} with the added tags.
	 * @see #mergeTags(Tags)
	 */
	public MetricsManager addTags(String... tags) {
		return this.addTags(Tags.of(tags));
	}

	/**
	 * Adds the given tags to the {@link MetricsManager}. The tags will be merged
	 * with the existing tags.
	 * 
	 * @param tags The tags to be added to the {@link MetricsManager}.
	 * @return The {@link MetricsManager} with the added tags.
	 * @see #mergeTags(Tags)
	 */
	public MetricsManager addTags(Tags tags) {
		this.tags = this.mergeTags(tags);

		return this;
	}

	/**
	 * Merges the given tags with the existing tags. If the existing tags are null,
	 * the given tags will be returned. If the given tags are null, the existing
	 * tags will be returned.
	 * 
	 * @param tags The tags to be merged with the existing tags.
	 * @return The merged tags.
	 * @see #addTags(Tags)
	 */
	public Tags mergeTags(Tags tags) {
		if (this.tags == null) {
			return tags;
		}
		if (tags == null) {
			return this.tags;
		}

		return this.tags.and(tags);
	}

	/**
	 * Creates a new {@link Observation} with the given name and the tags of this
	 * {@link MetricsManager}. The new {@link Observation} will be added to the
	 * current observation stack.
	 * 
	 * @param name The name of the {@link Observation}.
	 * @return A new {@link Observation} with the given name and the tags of this
	 *         {@link MetricsManager}.
	 * @see #start(String)
	 * @see #stop()
	 * @see #observe(String, Observation.CheckedCallable)
	 */
	protected Observation createObservation(String name) {
		return createObservation(name, DEFAULT_CONTEXT);
	}

	/**
	 * Creates a new {@link Observation} with the given name and the tags of this
	 * {@link MetricsManager}. The new {@link Observation} will be added to the
	 * current observation stack.
	 * 
	 * @param name    The name of the {@link Observation}.
	 * @param context The context of the {@link Observation}.
	 * @return A new {@link Observation} with the given name and the tags of this
	 *         {@link MetricsManager}.
	 * @see #start(String, Observation.Context)
	 * @see #stop()
	 * @see #observe(String, Observation.CheckedCallable)
	 */
	protected Observation createObservation(String name, Supplier<? extends Observation.Context> context) {
		Observation observation = Observation.createNotStarted(name, context, observationRegistry);
		observation.lowCardinalityKeyValues(KeyValues.of(this.tags, Tag::getKey, Tag::getValue));
		observation.getContext().put("manager", this); // Put to make instance available even if not a VNE metrics
														// Context

		return observation;
	}

	/**
	 * Starts a new {@link Observation} with the given name and the tags of this
	 * {@link MetricsManager}. The new {@link Observation} will be added to the
	 * current observation stack.
	 * 
	 * @param name The name of the {@link Observation}.
	 * @return A new {@link Observation} with the given name and the tags of this
	 *         {@link MetricsManager}.
	 * @see #stop()
	 */
	public Observation.Context start(String name) {
		return this.start(name, DEFAULT_CONTEXT);
	}

	/**
	 * Starts a new {@link Observation} with the given name and the tags of this
	 * {@link MetricsManager}. The new {@link Observation} will be added to the
	 * current observation stack.
	 * 
	 * @param name    The name of the {@link Observation}.
	 * @param context The context of the {@link Observation}.
	 * @return A new {@link Observation} with the given name and the tags of this
	 *         {@link MetricsManager}.
	 * @see #stop()
	 */
	public Observation.Context start(String name, Supplier<? extends Observation.Context> context) {
		Observation observation = createObservation(name, context);
		observation.start();
		observation.openScope();

		return observation.getContext();
	}

	/**
	 * Will allow to retrieve the {@link Observation.Scope} at any point in time.
	 *
	 * Example: if an {@link Observation} was started with {@link #start(String)} or
	 * {@link #observe(String, io.micrometer.observation.Observation.CheckedCallable)}
	 * then this method will return the current present {@link Observation.Scope}.
	 * 
	 * @return current observation scope or {@code null} if it's not present
	 */
	public Observation.Scope getCurrentObservationScope() {
		return this.observationRegistry.getCurrentObservationScope();
	}

	/**
	 * Will allow to retrieve the {@link Observation} at any point in time.
	 * 
	 * Example: if an {@link Observation} was started with {@link #start(String)} or
	 * {@link #observe(String, io.micrometer.observation.Observation.CheckedCallable)}
	 * then this method will return the current present {@link Observation}.
	 * 
	 * @return current observation or {@code null} if it's not present
	 */
	public Observation getCurrentObservation() {
		return this.observationRegistry.getCurrentObservation();
	}

	/**
	 * Emits a new {@link Observation.Event} with the given name to the current
	 * {@link Observation}.
	 * 
	 * @param name The name of the {@link Event}.
	 * @see #start(String)
	 * @see #stop()
	 */
	public void event(String name) {
		event(Observation.Event.of(name));
	}

	/**
	 * Emits a new {@link Observation.Event} with the given name and contextualName
	 * to the current {@link Observation}.
	 * 
	 * @param name           The name of the {@link Event}.
	 * @param contextualName The contextual name of the {@link Observation.Event}.
	 * @see #start(String)
	 * @see #stop()
	 */
	public void event(String name, String contextualName) {
		event(Observation.Event.of(name, contextualName));
	}

	/**
	 * Emits the given {@link Observation.Event} to the current {@link Observation}.
	 * 
	 * @param Event The {@link Observation.Event}.
	 * @see #start(String)
	 * @see #stop()
	 */
	public void event(Observation.Event event) {
		Observation observation = this.getCurrentObservation();
		if (observation != null) {
			observation.event(event);
		}
	}

	/**
	 * Stops the current {@link Observation} and removes it from the current
	 * observation stack.
	 * 
	 * @see #start(String)
	 * @see #stopAll()
	 */
	public void stop() {
		Observation.Scope scope = getCurrentObservationScope();

		if (scope == null) {
			return;
		}

		scope.close();
		Observation observation = scope.getCurrentObservation();
		if (observation != null && observation.getEnclosingScope() == null) {
			observation.stop();
		}
	}

	/**
	 * Stops all current {@link Observation}s and removes them from the current
	 * observation stack.
	 */
	public void stopAll() {
		while (this.getCurrentObservationScope() != null) {
			this.stop();
		}
	}

	/**
	 * Observes the given callable and returns the result.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @return The result of the callable.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> T observe(String name, Callable<T> callable) throws E {
		return observe(name, DEFAULT_CONTEXT, callable);
	}

	/**
	 * Observes the given callable and returns the result.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @return The result of the callable.
	 * @throws E If the callable throws an exception.
	 */
	public <T> T observe(String name, Supplier<? extends Observation.Context> context, Callable<T> callable) {
		try {
			final Observation observation = createObservation(name, context);
			return observation.observeChecked(() -> {
				final T result = callable.call();
				if (result != null) {
					// Put to make result available even if not a VNE metrics Context
					observation.getContext().put("result", result);
				}
				return result;
			});
		} catch (Exception e) {
			throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
		}
	}

	/**
	 * Observes the given callable and returns the result.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return The result of the callable.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> T observe(String name, Callable<T> callable, Tag... tags) throws E {
		return this.observe(name, callable, Tags.of(tags));
	}

	/**
	 * Observes the given callable and returns the result.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return The result of the callable.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> T observe(String name, Supplier<? extends Observation.Context> context,
			Callable<T> callable, Tag... tags) throws E {
		return this.observe(name, context, callable, Tags.of(tags));
	}

	/**
	 * Observes the given callable and returns the result.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return The result of the callable.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> T observe(String name, Callable<T> callable, String... tags) throws E {
		return this.observe(name, callable, Tags.of(tags));
	}

	/**
	 * Observes the given callable and returns the result.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return The result of the callable.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> T observe(String name, Supplier<? extends Observation.Context> context,
			Callable<T> callable, String... tags) throws E {
		return this.observe(name, context, callable, Tags.of(tags));
	}

	/**
	 * Observes the given callable and returns the result.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return The result of the callable.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> T observe(String name, Callable<T> callable, Iterable<? extends Tag> tags)
			throws E {
		return this.observe(name, DEFAULT_CONTEXT, callable, tags);
	}

	/**
	 * Observes the given callable and returns the result.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return The result of the callable.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> T observe(String name, Supplier<? extends Observation.Context> context,
			Callable<T> callable, Iterable<? extends Tag> tags) throws E {
		try (final MetricsManager metricsManager = this.withTags(tags)) {
			return metricsManager.observe(name, context, callable);
		}
	}

	/**
	 * Observes the given runnable.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param runnable The runnable to be observed.
	 * @throws E If the runnable throws an exception.
	 */
	public <T, E extends Throwable> void observe(String name, Runnable runnable) throws E {
		this.observe(name, DEFAULT_CONTEXT, runnable);
	}

	/**
	 * Observes the given runnable.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param runnable The runnable to be observed.
	 * @throws E If the runnable throws an exception.
	 */
	public <T, E extends Throwable> void observe(String name, Supplier<? extends Observation.Context> context,
			Runnable runnable) throws E {
		this.observe(name, context, () -> {
			runnable.run();

			// Return for typed callable, ignore value
			return null;
		});
	}

	/**
	 * Observes the given runnable.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param runnable The runnable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @throws E If the runnable throws an exception.
	 */
	public <T, E extends Throwable> void observe(String name, Runnable runnable, Tag... tags) throws E {
		this.observe(name, runnable, Tags.of(tags));
	}

	/**
	 * Observes the given runnable.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param runnable The runnable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @throws E If the runnable throws an exception.
	 */
	public <T, E extends Throwable> void observe(String name, Supplier<? extends Observation.Context> context,
			Runnable runnable, Tag... tags) throws E {
		this.observe(name, context, runnable, Tags.of(tags));
	}

	/**
	 * Observes the given runnable.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param runnable The runnable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @throws E If the runnable throws an exception.
	 */
	public <T, E extends Throwable> void observe(String name, Runnable runnable, String... tags) throws E {
		this.observe(name, runnable, Tags.of(tags));
	}

	/**
	 * Observes the given runnable.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param runnable The runnable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @throws E If the runnable throws an exception.
	 */
	public <T, E extends Throwable> void observe(String name, Supplier<? extends Observation.Context> context,
			Runnable runnable, String... tags) throws E {
		this.observe(name, context, runnable, Tags.of(tags));
	}

	/**
	 * Observes the given runnable.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param runnable The runnable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @throws E If the runnable throws an exception.
	 */
	public <T, E extends Throwable> void observe(String name, Runnable runnable, Iterable<? extends Tag> tags)
			throws E {
		this.observe(name, DEFAULT_CONTEXT, runnable, tags);
	}

	/**
	 * Observes the given runnable.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param runnable The runnable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @throws E If the runnable throws an exception.
	 */
	public <T, E extends Throwable> void observe(String name, Supplier<? extends Observation.Context> context,
			Runnable runnable, Iterable<? extends Tag> tags) throws E {
		try (final MetricsManager metricsManager = this.withTags(tags)) {
			metricsManager.observe(name, context, runnable);
		}
	}

	/**
	 * Wraps the given callable with the given name into a new callable for
	 * observation.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @return A new callable for observation.
	 */
	public <T, E extends Throwable> Callable<T> wrapObserve(String name, Callable<T> callable) {
		return () -> this.observe(name, callable);
	}

	/**
	 * Wraps the given callable with the given name and context into a new callable
	 * for observation.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @return A new callable for observation.
	 */
	public <T, E extends Throwable> Callable<T> wrapObserve(String name,
			Supplier<? extends Observation.Context> context, Callable<T> callable) {
		return () -> this.observe(name, context, callable);
	}

	/**
	 * Wraps the given callable into a new callable with a new
	 * {@link MetricsManager} with the tags assigned. It will be observed with the
	 * given name.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return A new callable for observation.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> Callable<T> wrapObserve(String name, Callable<T> callable, Tag... tags) throws E {
		return this.wrapObserve(name, callable, Tags.of(tags));
	}

	/**
	 * Wraps the given callable into a new callable with a new
	 * {@link MetricsManager} with the tags assigned. It will be observed with the
	 * given name and context.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return A new callable for observation.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> Callable<T> wrapObserve(String name,
			Supplier<? extends Observation.Context> context, Callable<T> callable, Tag... tags) throws E {
		return this.wrapObserve(name, context, callable, Tags.of(tags));
	}

	/**
	 * Wraps the given callable into a new callable with a new
	 * {@link MetricsManager} with the tags assigned. It will be observed with the
	 * given name.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return A new callable for observation.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> Callable<T> wrapObserve(String name, Callable<T> callable, String... tags)
			throws E {
		return this.wrapObserve(name, callable, Tags.of(tags));
	}

	/**
	 * Wraps the given callable into a new callable with a new
	 * {@link MetricsManager} with the tags assigned. It will be observed with the
	 * given name and context.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return A new callable for observation.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> Callable<T> wrapObserve(String name,
			Supplier<? extends Observation.Context> context, Callable<T> callable, String... tags) throws E {
		return this.wrapObserve(name, context, callable, Tags.of(tags));
	}

	/**
	 * Wraps the given callable into a new callable with a new
	 * {@link MetricsManager} with the tags assigned. It will be observed with the
	 * given name.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return A new callable for observation.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> Callable<T> wrapObserve(String name, Callable<T> callable,
			Iterable<? extends Tag> tags) throws E {
		return this.wrapObserve(name, DEFAULT_CONTEXT, callable, tags);
	}

	/**
	 * Wraps the given callable into a new callable with a new
	 * {@link MetricsManager} with the tags assigned. It will be observed with the
	 * given name and context.
	 * 
	 * @param name     The name of the {@link Observation}.
	 * @param context  The context of the {@link Observation}.
	 * @param callable The callable to be observed.
	 * @param tags     The tags to be used for the {@link Observation}.
	 * @return A new callable for observation.
	 * @throws E If the callable throws an exception.
	 */
	public <T, E extends Throwable> Callable<T> wrapObserve(String name,
			Supplier<? extends Observation.Context> context, Callable<T> callable, Iterable<? extends Tag> tags)
			throws E {
		return this.wrap(() -> getInstance().observe(name, context, callable), tags);
	}

	/**
	 * Closes the {@link MetricsManager} and stops all running {@link Observation}s.
	 */
	@Override
	public void close() {
		close(false);
	}

	/**
	 * Closes the {@link MetricsManager} and stops all running {@link Observation}s.
	 * 
	 * @param closeRegistries If true, the {@link MeterRegistry} will be closed as
	 *                        well.
	 */
	public void close(final boolean closeRegistries) {
		stopAll();
		if (closeRegistries) {
			this.reporters.forEach((reporter) -> reporter.getMeterRegistry().close());
			this.meterRegistry.close();
		}

		instance.get().remove(this);
	}

	/**
	 * Closes all {@link MetricsManager}s, stops all running {@link Observation}s
	 * and closes all registries.
	 */
	public static void closeAll() {
		while (!instance.get().isEmpty()) {
			instance.get().removeFirst().close(true);
		}
	}

}
