package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import metrics.embedding.TotalCommunicationCostObjectiveD;
import model.SubstrateNetwork;

/**
 * Test class for the metric of total communication cost as implemented in
 * {@link TotalCommunicationCostObjectiveD}.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TotalCommunicationCostObjectiveDTest extends ATotalCommunicationCostMetricTest {

	@Override
	protected void setMetric(final SubstrateNetwork sNet) {
		metric = new TotalCommunicationCostObjectiveD(sNet);
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

		assertEquals(13.333, metric.getValue(), 0.01);
	}

}
