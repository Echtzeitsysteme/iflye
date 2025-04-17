package scenarios.load;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoflon.gips.core.milp.SolverOutput;
import org.emoflon.gips.core.milp.SolverStatus;

import algorithms.gips.GipsAlgorithm;
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

/**
 * Collect statistics about the ILP solver if run for a Gips Algorithm.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class GipsIlpHandler implements HasMetric<Context.VnetEmbeddingContext> {

	/**
	 * The {@link MeterRegistry} to register the metrics to.
	 */
	private MeterRegistry meterRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<MetricTransformer> getProvidedMeters() {
		class IlpMeter implements MetricTransformer, NotionReporter.NotionMeter {
			@Override
			public Map<String, Object> toEntry(Meter meter, Map<String, Object> unmodifiableEntry) {
				DistributionSummary distributionMeter = (DistributionSummary) meter;

				Map<String, Object> entry = new HashMap<>();
				if (meter.getId().getName().equals("ilp.status")) {
					entry.put(meter.getId().getName(), SolverStatus.values()[(int) distributionMeter.max()].name());
				} else {
					entry.put(meter.getId().getName(), distributionMeter.max());
				}
				return entry;
			}

			@Override
			public boolean supportsMeter(Meter meter) {
				return meter instanceof DistributionSummary && meter.getId().getName().startsWith("ilp.");
			}

			@Override
			public boolean shouldResetMeter(Meter meter) {
				return true;
			}

			@Override
			public PropertyFormat getNotionPropertyFormat(Meter meter, String key, Object value) {
				return key.equals("ilp.status") ? NotionReporter.PROPERTY_TYPE.SELECT
						: NotionReporter.PROPERTY_TYPE.NUMBER;
			}
		}
		return List.of(new IlpMeter());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStop(Context.VnetEmbeddingContext context) {

		if (!(context.getAlgorithm() instanceof GipsAlgorithm)) {
			return;
		}

		GipsAlgorithm gipsAlgorithm = (GipsAlgorithm) context.getAlgorithm();
		SolverOutput solverOutput = gipsAlgorithm.getSolverOutput();

		meterRegistry.summary("ilp.objective_value", createTags(context)).record(solverOutput.objectiveValue());
		meterRegistry.summary("ilp.solution_count", createTags(context)).record(solverOutput.solutionCount());
		meterRegistry.summary("ilp.constraints", createTags(context)).record(solverOutput.stats().constraints());
		meterRegistry.summary("ilp.mappings", createTags(context)).record(solverOutput.stats().mappings());
		meterRegistry.summary("ilp.vars", createTags(context)).record(solverOutput.stats().vars());
		meterRegistry.summary("ilp.status", createTags(context)).record(solverOutput.status().ordinal());

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
