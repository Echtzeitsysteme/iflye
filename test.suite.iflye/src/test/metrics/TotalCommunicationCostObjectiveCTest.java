package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import metrics.embedding.TotalCommunicationCostObjectiveC;
import model.SubstrateNetwork;

/**
 * Test class for the metric of total communication cost as implemented in
 * {@link TotalCommunicationCostObjectiveC}.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TotalCommunicationCostObjectiveCTest extends ATotalCommunicationCostMetricTest {

	@Override
	protected void setMetric(final SubstrateNetwork sNet) {
		metric = new TotalCommunicationCostObjectiveC(sNet);
	}

	/*
	 * Positive tests
	 */

	@Override
	@Test
	public void testEmbeddingTwoHosts() {
		createSubstrateNetwork();
		setupEmbeddingTwoHosts();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		setMetric(sNet);

		assertEquals(2 * 2 * 3 + 3, metric.getValue());
	}

}
