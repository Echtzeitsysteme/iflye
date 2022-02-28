package metrics.embedding;

import metrics.IMetric;
import model.SubstrateNetwork;

/**
 * Average path length metric. This one equals the sum of all substrate links
 * from substrate paths with embedded virtual links on them divided by the
 * number of virtual links.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class AveragePathLengthMetric implements IMetric {

	/**
	 * Calculated value of this metric.
	 */
	private final double value;

	/**
	 * Creates a new instance of this metric for the provided substrate network.
	 *
	 * @param sNet Substrate network to calculate the metric for.
	 */
	public AveragePathLengthMetric(final SubstrateNetwork sNet) {
		value = -1;
	}

	@Override
	public double getValue() {
		return this.value;
	}

}
