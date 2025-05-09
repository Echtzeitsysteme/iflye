package metrics.reporter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.Observation;
import metrics.HasMetric;
import metrics.MetricTransformer;
import metrics.Reporter;

/**
 * GroupedReporter is a base class for reporters that group metrics by a
 * specific key.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public abstract class GroupedReporter<T> extends SimpleMeterRegistry implements Reporter {

	/**
	 * The set of meter providers that are used to transform the metrics.
	 */
	protected final Set<HasMetric<? extends Observation.Context>> meterProviders = new HashSet<>();

	public GroupedReporter() {
		super();
	}

	/**
	 * The default implementation of the MetricTransformer interface. It transforms
	 * each meter into its distinct measurements by key.
	 * 
	 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
	 */
	public static class DefaultMetricTransformer implements MetricTransformer {

		/**
		 * The default supports all meters.
		 * 
		 * @param meter the meter to check
		 * @return true if the meter is supported, there is no unsupported meter.
		 */
		@Override
		public boolean supportsMeter(Meter meter) {
			return true;
		}

		/**
		 * Transforms the meter into a map of key-value pairs. The key is the
		 * measurement name and the value its value.
		 * 
		 * @param meter             the meter to transform
		 * @param unmodifiableEntry the already existing unmodifiable entry this values
		 *                          will be added to
		 * @return a map of key-value pairs
		 */
		@Override
		public Map<String, Object> toEntry(final Meter meter, final Map<String, Object> unmodifiableEntry) {
			final Map<String, Object> entry = new HashMap<>();
			Meter.Id id = meter.getId();
			String metricName = id.getName();
			for (Measurement measurement : meter.measure()) {
				String statName = measurement.getStatistic().toString().toLowerCase();
				String key = metricName;

				key += "." + statName;
				entry.put(key, measurement.getValue());
			}

			return entry;
		}

	}

	/**
	 * The entry class that holds the values and tags of grouped meters.
	 * 
	 * @param values the values of the meters
	 * @param tags   the tags of the meters
	 */
	public static record Entry(Map<String, Object> values, Map<String, String> tags) {
	}

	/**
	 * Registers a meter provider that is used to transform the metrics.
	 * 
	 * @param meterProvider the meter provider to register
	 */
	@Override
	public void registerMeterProvider(HasMetric<? extends Observation.Context> meterProvider) {
		this.meterProviders.add(meterProvider);
	}

	/**
	 * Groups the entries by the group key and flushes them. This allows the
	 * implementation to write all related metrics at once to the same entry.
	 * 
	 * @see #flushEntry(Map, Object)
	 * @see #groupEntriesBy()
	 */
	@Override
	public void flush() {
		Map<T, Entry> entries = groupEntriesBy();

		for (Map.Entry<T, Entry> entry : entries.entrySet()) {
			flushEntry(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * Groups the meters by the group key to create a map of entries. Each entry
	 * starts with an initial entry associated by its group key. Further metrics are
	 * then collected to that entry.
	 * 
	 * @return a map of entries grouped by the group key
	 * @see #getGroupKey(Meter)
	 * @see #getInitialEntry(Meter)
	 * @see #collectEntry(Map, MetricTransformer, Map, Meter)
	 */
	protected Map<T, Entry> groupEntriesBy() {
		Map<T, Entry> entries = new LinkedHashMap<>();

		for (Meter meter : getMeters()) {
			T groupKey = getGroupKey(meter);

			final Entry entry = entries.computeIfAbsent(groupKey,
					_ignored -> new Entry(new HashMap<>(), new HashMap<>()));
			entry.tags.putAll(getTags(meter));
			boolean found = false;
			boolean reset = false;
			for (HasMetric<? extends Observation.Context> meterProvider : this.meterProviders) {
				MetricTransformer meterTransformer = meterProvider.getProvidedMeter(meter);
				if (meterTransformer != null) {
					final Map<String, Object> meterValues = meterTransformer.toEntry(meter,
							Collections.unmodifiableMap(entry.values));
					found |= collectEntry(meterValues, meterTransformer, entry, meter);
					reset |= meterTransformer.shouldResetMeter(meter);
				}
			}

			if (!found) {
				reset |= applyDefaultCollector(meter, entry);
			}

			if (reset) {
				resetMeter(meter);
			}
		}

		return entries;
	}

	/**
	 * Applies the default collector for a meter if no other collector was found.
	 * 
	 * @param meter the meter to collect
	 * @param entry the entry to collect the meter to
	 * @return true if the meter should be reset, false otherwise
	 */
	protected boolean applyDefaultCollector(Meter meter, final Entry entry) {
		MetricTransformer meterTransformer = new DefaultMetricTransformer();
		final Map<String, Object> meterValues = meterTransformer.toEntry(meter,
				Collections.unmodifiableMap(entry.values));
		collectEntry(meterValues, meterTransformer, entry, meter);

		return meterTransformer.shouldResetMeter(meter);
	}

	/**
	 * Removes the meter after grouped collection from the registry and resets its
	 * state.
	 * 
	 * @param meter the meter to reset
	 * @see #groupEntriesBy()
	 */
	protected void resetMeter(Meter meter) {
		this.remove(meter);
	}

	/**
	 * Determines the group key for the given meter. This key is used to group the
	 * metrics together.
	 * 
	 * @param meter the meter to get the group key for
	 * @return the group key
	 * @see #groupEntriesBy()
	 */
	protected abstract T getGroupKey(final Meter meter);

	/**
	 * Returns the tags of the given meter as a map.
	 * 
	 * @param meter the meter to get the tags for
	 * @return a map of tags
	 */
	protected Map<String, String> getTags(final Meter meter) {
		final Meter.Id id = meter.getId();
		final Map<String, String> tags = id.getTags().stream().collect(Collectors.toMap(Tag::getKey, Tag::getValue));
		return tags;
	}

	/**
	 * Adds the metrics for the given meter to their entry. This allows the
	 * implementation to apply additional transformations to the entry.
	 * 
	 * @param metric           the metric to add
	 * @param meterTransformer the metric transformer to use
	 * @param entry            the entry to add the metric to
	 * @param meter            the meter to add the metric for
	 * @return true if the entry was modified, false otherwise
	 * @see #groupEntriesBy()
	 */
	protected boolean collectEntry(final Map<String, Object> metric, final MetricTransformer meterTransformer,
			final Entry entry, final Meter meter) {
		entry.values.putAll(metric);
		return true;
	}

	/**
	 * Flushes a single entry with the given group key. This allows the
	 * implementation to write all related metrics at once to the same entry.
	 * 
	 * @param entry    the entry to flush
	 * @param groupKey the group key of the entry
	 * @see #groupEntriesBy()
	 * @see #flush()
	 * @see #getGroupKey(Meter)
	 * @see #getInitialEntry(Meter)
	 */
	protected abstract void flushEntry(Entry entry, T groupKey);

}
