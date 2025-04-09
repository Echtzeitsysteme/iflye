package metrics.reporter;

import io.micrometer.core.instrument.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import metrics.MetricTransformer;

/**
 * A reporter that prints a summary of the metrics to a consumer. It is used to
 * print the metrics in a human-readable format.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class TextSummaryReporter extends GroupedReporter<String> {

	/**
	 * The aggregated metrics.
	 */
	protected final Map<String, Object> aggregatedMetrics = new HashMap<>();

	/**
	 * The consumer that will receive the formatted metrics.
	 */
	protected Consumer<String> consumer;

	/**
	 * The format function that will be used to format the metrics.
	 */
	protected BiFunction<String, String, String> format;

	/**
	 * Defaults how to aggregate certain metrics, overwrites those provided by the
	 * metric transformer.
	 */
	protected final Map<String, Aggregation> aggregations = new HashMap<>();

	/**
	 * The metric formats if provided by the metric transformer instead of the
	 * default (key as label, value as String).
	 */
	protected final Map<String, MetricFormat> metricFormats = new HashMap<>();

	/**
	 * Metric format overwrites for certain metrics. This is used to overwrite both
	 * the default and the metric transformer format.
	 */
	protected final Map<String, MetricFormat> metricFormatOverwrites = new HashMap<>();

	/**
	 * The order in which the metrics will be printed. If a metric is not in this
	 * list, it will be printed alphabetically at the end.
	 */
	protected final List<String> metricOrder = new LinkedList<>(List.of("time_total", "time_prepare", "time_execute",
			"time_pm", "time_ilp", "time_deploy", "time_rest", "accepted_vnrs", "total_path_cost",
			"average_path_length", "total_communication_cost_a", "total_communication_cost_b",
			"total_communication_cost_c", "total_communication_cost_d", "total_communication_objective_c",
			"total_communication_objective_d", "total_taf_communication_cost", "operation_cost", "memory_total",
			"memory_prepare", "memory_execute", "memory_start", "memory_ilp", "memory_end", "memory_pid_max"));

	/**
	 * Creates a new TextSummaryReporter with the default consumer and format.
	 */
	public TextSummaryReporter() {
		this(null, null);
	}

	/**
	 * Creates a new TextSummaryReporter with the given format and default consumer.
	 * 
	 * @param format the format function that will be used to format the metrics.
	 *               For format details, see {@link #format(String, Object)}
	 */
	public TextSummaryReporter(final BiFunction<String, String, String> format) {
		this(null, format);
	}

	/**
	 * Creates a new TextSummaryReporter with the given consumer and default format.
	 * 
	 * @param consumer the consumer that will receive the formatted metrics
	 */
	public TextSummaryReporter(final Consumer<String> consumer) {
		this(consumer, null);
	}

	/**
	 * Creates a new TextSummaryReporter with the given consumer and format.
	 * 
	 * @param consumer the consumer that will receive the formatted metrics
	 * @param format   the format function that will be used to format the metrics.
	 *                 For format details, see {@link #format(String, Object)}
	 */
	public TextSummaryReporter(final Consumer<String> consumer, final BiFunction<String, String, String> format) {
		super();

		this.consumer = consumer == null ? System.out::println : consumer;
		this.format = format == null ? (label, value) -> "=> " + label + ": " + value : format;
	}

	/**
	 * Interface for a MetricTransformer to provide aggregation types and formats.
	 * 
	 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
	 */
	public static interface AggregatingMeter extends MetricTransformer {

		/**
		 * Returns the aggregation type for the given key and value.
		 * 
		 * @param meter the meter
		 * @param key   the key of the metric
		 * @param value the value of the metric
		 * @return the aggregation type
		 */
		public Aggregation getAggregationType(Meter meter, String key, Object value);

		/**
		 * Returns instructions on how to format the aggregation for the given key and
		 * value.
		 * 
		 * @param meter the meter
		 * @param key   the key of the metric
		 * @param value the value of the metric
		 * @return the format
		 */
		default public MetricFormat getAggregationFormat(Meter meter, String key, Object value) {
			return null;
		}

	}

	/**
	 * An Aggregation specifies how multiple values should be aggregated to one.
	 * 
	 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
	 */
	@FunctionalInterface
	public static interface Aggregation {

		/**
		 * Aggregates the given value with the given aggregated value.
		 * 
		 * @param key             the key of the metric
		 * @param aggregatedValue the aggregated value
		 * @param value           the value to aggregate
		 * @return the aggregated value
		 */
		public Object aggregateValues(final String key, final Object aggregatedValue, final Object value);

		/**
		 * Aggregates the given value if there is no aggregated value (yet).
		 * 
		 * @param key   the key of the metric
		 * @param value the value to aggregate
		 * @return the aggregated value
		 */
		default Object aggregate(final String key, final Object value) {
			return aggregate(key, null, value);
		}

		/**
		 * Aggregates the given value with the given aggregated value.
		 * 
		 * @param key             the key of the metric
		 * @param aggregatedValue the aggregated value
		 * @param value           the value to aggregate
		 * @return the aggregated value
		 */
		default Object aggregate(final String key, final Object aggregatedValue, final Object value) {
			if (aggregatedValue == null) {
				return initialValue(key, value);
			} else {
				return aggregateValues(key, aggregatedValue, value);
			}
		}

		/**
		 * Returns the initial value for the given key and value if there is no
		 * aggregated value (yet).
		 * 
		 * @param key   the key of the metric
		 * @param value the value to aggregate
		 * @return the initial value
		 */
		default Object initialValue(final String key, final Object value) {
			return value;
		}

	}

	/**
	 * An enumeration of the common aggregation types.
	 * 
	 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
	 */
	public static enum AGGREGATION_TYPE implements Aggregation {
		SUM {
			@Override
			public Object aggregateValues(final String key, final Object aggregatedValue, final Object value) {
				return ((double) aggregatedValue) + ((double) value);
			}
		},
		MIN {
			@Override
			public Object aggregateValues(final String key, final Object aggregatedValue, final Object value) {
				return Math.min(((double) aggregatedValue), ((double) value));
			}
		},
		MAX {
			@Override
			public Object aggregateValues(final String key, final Object aggregatedValue, final Object value) {
				return Math.max(((double) aggregatedValue), ((double) value));
			}
		}
	}

	/**
	 * A MetricFormat specifies how to format the components of a metric.
	 * 
	 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
	 */
	@FunctionalInterface
	public static interface MetricFormat {

		/**
		 * Returns the human-readable label of the metric.
		 * 
		 * @param key   the key of the metric
		 * @param value the value of the metric
		 * @return the label of the metric
		 */
		public String label(final String key, final Object value);

		/**
		 * Returns the formatted value of the metric as a String.
		 * 
		 * @param key   the key of the metric
		 * @param value the value of the metric
		 * @return the stringified value of the metric
		 */
		default public String value(final String key, final Object value) {
			return String.valueOf(value);
		}

	}

	/**
	 * Add or overwrite the aggregation type for a metric.
	 * 
	 * @param key         the key of the metric
	 * @param aggregation the aggregation type
	 * @return this
	 */
	public TextSummaryReporter addAggregationType(final String key, final Aggregation aggregation) {
		this.aggregations.put(key, aggregation);
		return this;
	}

	/**
	 * Add or overwrite the format for a metric.
	 * 
	 * @param key          the key of the metric
	 * @param metricFormat the format
	 * @return this
	 */
	public TextSummaryReporter addMetricFormat(final String key, final MetricFormat metricFormat) {
		this.metricFormatOverwrites.put(key, metricFormat);
		return this;
	}

	/**
	 * Set the consumer to use for publishing the formatted metrics.
	 * 
	 * @param consumer the consumer
	 * @return this
	 */
	public TextSummaryReporter setConsumer(final Consumer<String> consumer) {
		this.consumer = consumer;
		return this;
	}

	/**
	 * Set the format function to use for formatting the metrics. For format
	 * details, see {@link #format(String, Object)}.
	 * 
	 * @param format the format function
	 * @return this
	 */
	public TextSummaryReporter setFormat(final BiFunction<String, String, String> format) {
		this.format = format;
		return this;
	}

	/**
	 * Collect and aggregate the metrics from the given entry. Also, extract and
	 * buffer the MetricFormat for each metric.
	 * 
	 * @param metric           the metric to collect
	 * @param meterTransformer the metric transformer
	 * @param entry            the entry to collect
	 * @param meter            the meter
	 * @return true if the entry was collected, false otherwise
	 */
	@Override
	protected boolean collectEntry(final Map<String, Object> metric, final MetricTransformer meterTransformer,
			final Map<String, Object> entry, final Meter meter) {
		boolean found = false;
		for (Map.Entry<String, Object> value : metric.entrySet()) {
			AggregatingMeter aggregatingMeter = (meterTransformer instanceof AggregatingMeter)
					? (AggregatingMeter) meterTransformer
					: null;

			// If the MetricTransformer provides a MetricFormat, buffer it for later use
			MetricFormat format = aggregatingMeter != null
					? aggregatingMeter.getAggregationFormat(meter, value.getKey(), value.getValue())
					: null;
			if (format != null) {
				this.metricFormats.put(value.getKey(), format);
			}

			// Use the registered aggregation if any, or use the aggregation provided by the
			// MetricTransformer.
			Aggregation aggregation = this.aggregations.getOrDefault(value.getKey(),
					aggregatingMeter != null
							? aggregatingMeter.getAggregationType(meter, value.getKey(), value.getValue())
							: null);
			if (aggregation == null) {
				continue;
			}

			this.aggregatedMetrics.compute(value.getKey(),
					(_ignored, v) -> aggregation.aggregate(value.getKey(), v, value.getValue()));
			found = true;
		}

		return found;
	}

	/**
	 * On conclusion, print the aggregated metrics.
	 */
	@Override
	public void conclude() {
		print();
	}

	/**
	 * Print the aggregated metrics in the given order. Metrics will be formatted
	 * according to {@link #format(String, Object)}.
	 */
	public void print() {
		final List<Map.Entry<String, Object>> metrics = new LinkedList<>(this.aggregatedMetrics.entrySet());
		Collections.sort(metrics, (a, b) -> {
			// If not in given order, sort alphabetically
			if (!this.metricOrder.contains(a.getKey()) && !this.metricOrder.contains(b.getKey())) {
				return a.getKey().compareTo(b.getKey());
			}

			// If only one in given order, put that first
			if (!this.metricOrder.contains(a.getKey()) || !this.metricOrder.contains(b.getKey())) {
				return this.metricOrder.contains(a.getKey()) ? -1 : 1;
			}

			// Else sort them in the given order
			return this.metricOrder.indexOf(a.getKey()) - this.metricOrder.indexOf(b.getKey());
		});

		for (Entry<String, Object> metric : metrics) {
			this.publish(metric.getKey(), metric.getValue());
		}
	}

	/**
	 * Formats a metric with the given key and value. First, the
	 * {@link MetricFormat} formats the key to a human-readable label (default: the
	 * key) and stringifies the value. The used {@link MetricFormat} is determined
	 * in the following order: Overwrite > as provided by {@link AggregatingMeter} >
	 * Default. The formatted label and value are then passed to the format function
	 * for conjunction.
	 * 
	 * @param metric the key of the metric
	 * @param value  the value of the metric
	 * @return the formatted metric
	 * @see MetricFormat
	 */
	protected String format(String metric, Object value) {
		MetricFormat metricFormat = this.metricFormats.getOrDefault(metric, (key, _value) -> key.replace("_", " "));
		metricFormat = this.metricFormatOverwrites.getOrDefault(metric, metricFormat);

		return this.format.apply(metricFormat.label(metric, value), metricFormat.value(metric, value));
	}

	/**
	 * Publish the given text to the consumer.
	 * 
	 * @param text the text to publish
	 */
	protected void publish(String text) {
		this.consumer.accept(text);
	}

	/**
	 * Formats the given key and value and publishes it to the consumer.
	 * 
	 * @param key   the key of the metric
	 * @param value the value of the metric
	 */
	protected void publish(String key, Object value) {
		this.publish(this.format(key, value));
	}

	/**
	 * Determines the group key for the given meter. For aggregation, the group key
	 * is the name of the meter.
	 * 
	 * @param meter the meter
	 * @return the group key
	 */
	@Override
	protected String getGroupKey(Meter meter) {
		return meter.getId().getName();
	}

	/**
	 * There is no flushing, because this reporter only reports on
	 * {@link #conclude()}.
	 */
	@Override
	protected void flushEntry(Map<String, Object> entry, String groupKey) {
		// noop
	}

}
