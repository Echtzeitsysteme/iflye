package metrics.embedding;

import metrics.IMetric;
import model.SubstrateNetwork;

/**
 * Implementation of the cost function of paper [1]. [1] Tomaszek S., Leblebici
 * E., Wang L., Schürr A. (2018) Virtual Network Embedding: Reducing the Search
 * Space by Model Transformation Techniques. In: Rensink A., Sánchez Cuadrado J.
 * (eds) Theory and Practice of Model Transformation. ICMT 2018. Lecture Notes
 * in Computer Science, vol 10888. Springer, Cham
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TotalPathCostMetric implements IMetric {

	/**
	 * Calculated cost.
	 */
	private double cost;

	/**
	 * Creates a new instance of this metric for the provided substrate network.
	 *
	 * @param sNet Substrate network to calculate the metric for.
	 */
	public TotalPathCostMetric(final SubstrateNetwork sNet) {
		cost = -1;
	}

	@Override
	public double getValue() {
		return cost;
	}

}
