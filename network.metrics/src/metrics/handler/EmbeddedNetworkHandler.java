package metrics.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.observation.Observation;
import metrics.HasMetric;
import metrics.IMetric;
import metrics.MetricTransformer;
import metrics.embedding.AcceptedVnrMetric;
import metrics.embedding.AveragePathLengthMetric;
import metrics.embedding.OperatingCostMetric;
import metrics.embedding.TotalCommunicationCostMetricA;
import metrics.embedding.TotalCommunicationCostMetricB;
import metrics.embedding.TotalCommunicationCostMetricC;
import metrics.embedding.TotalCommunicationCostMetricD;
import metrics.embedding.TotalCommunicationCostObjectiveC;
import metrics.embedding.TotalCommunicationCostObjectiveD;
import metrics.embedding.TotalPathCostMetric;
import metrics.embedding.TotalTafCommunicationCostMetric;
import metrics.manager.Context;
import metrics.reporter.NotionReporter;
import metrics.reporter.NotionReporter.PropertyFormat;
import metrics.reporter.TextSummaryReporter;
import metrics.reporter.TextSummaryReporter.Aggregation;
import model.SubstrateNetwork;

/**
 * Handles all metrics related to the embedded network.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class EmbeddedNetworkHandler implements HasMetric<Context.VnetEmbeddingContext> {

	/**
	 * The {@link MeterRegistry} to register the metrics to.
	 */
	private MeterRegistry meterRegistry;

	/**
	 * The map of metrics supported by this handler.
	 */
	private final Map<String, Function<SubstrateNetwork, IMetric>> metrics = new HashMap<>();

	public EmbeddedNetworkHandler() {
		this.metrics.put("accepted_vnrs", (sNet) -> new AcceptedVnrMetric(sNet));
		this.metrics.put("total_path_cost", (sNet) -> new TotalPathCostMetric(sNet));
		this.metrics.put("average_path_length", (sNet) -> new AveragePathLengthMetric(sNet));
		this.metrics.put("total_communication_cost_a", (sNet) -> new TotalCommunicationCostMetricA(sNet));
		this.metrics.put("total_communication_cost_b", (sNet) -> new TotalCommunicationCostMetricB(sNet));
		this.metrics.put("total_communication_cost_c", (sNet) -> new TotalCommunicationCostMetricC(sNet));
		this.metrics.put("total_communication_cost_d", (sNet) -> new TotalCommunicationCostMetricD(sNet));
		this.metrics.put("total_communication_objective_c", (sNet) -> new TotalCommunicationCostObjectiveC(sNet));
		this.metrics.put("total_communication_objective_d", (sNet) -> new TotalCommunicationCostObjectiveD(sNet));
		this.metrics.put("total_taf_communication_cost", (sNet) -> new TotalTafCommunicationCostMetric(sNet));
		this.metrics.put("operation_cost", (sNet) -> new OperatingCostMetric(sNet));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<MetricTransformer> getProvidedMeters() {
		class NetworkMeter implements TextSummaryReporter.AggregatingMeter, NotionReporter.NotionMeter {
			@Override
			public Map<String, Object> toEntry(Meter meter, Map<String, Object> unmodifiableEntry) {
				DistributionSummary summary = (DistributionSummary) meter;
				Map<String, Object> entry = new HashMap<>();
				entry.put(meter.getId().getName(), toIntegerIfAcceptedVNRs(meter, summary.max()));
				return entry;
			}

			@Override
			public boolean supportsMeter(Meter meter) {
				return meter instanceof DistributionSummary
						&& EmbeddedNetworkHandler.this.metrics.containsKey(meter.getId().getName());
			}

			@Override
			public boolean shouldResetMeter(Meter meter) {
				return true;
			}

			@Override
			public Aggregation getAggregationType(Meter meter, String key, Object value) {
				return TextSummaryReporter.AGGREGATION_TYPE.MAX;
			}

			@Override
			public PropertyFormat getNotionPropertyFormat(Meter meter, String key, Object value) {
				return NotionReporter.PROPERTY_TYPE.NUMBER;
			}

			protected Number toIntegerIfAcceptedVNRs(Meter meter, double value) {
				return meter.getId().getName().equals("accepted_vnrs") ? Double.valueOf(value).intValue()
						: Double.valueOf(value).doubleValue();
			}
		}
		return List.of(new NetworkMeter());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof Context.VnetEmbeddingContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStop(Context.VnetEmbeddingContext context) {
		SubstrateNetwork sNet = context.getSubstrateNetwork();
		List<Tag> tags = createTags(context);

		this.metrics
				.forEach((key, metric) -> this.meterRegistry.summary(key, tags).record(metric.apply(sNet).getValue()));
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
