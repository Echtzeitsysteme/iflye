package test.metrics;

import metrics.embedding.TotalCommunicationCostMetricA;
import model.SubstrateNetwork;

/**
 * Test class for the metric of total communication cost as implemented in
 * {@link TotalCommunicationCostMetricA}.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TotalCommunicationCostMetricATest extends ATotalCommunicationCostMetricTest {

	@Override
	protected void setMetric(final SubstrateNetwork sNet) {
		metric = new TotalCommunicationCostMetricA(sNet);
	}

}
