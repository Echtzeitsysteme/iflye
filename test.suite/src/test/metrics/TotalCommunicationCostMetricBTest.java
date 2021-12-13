package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import metrics.embedding.TotalCommunicationCostMetricB;
import model.SubstrateNetwork;

/**
 * Test class for the metric of total communication cost as implemented in
 * {@link TotalCommunicationCostMetricB}.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TotalCommunicationCostMetricBTest extends ATotalCommunicationCostMetricTest {

	@Override
	protected void setMetric(final SubstrateNetwork sNet) {
		metric = new TotalCommunicationCostMetricB(sNet);
	}

	/*
	 * Positive tests
	 */

	@Test
	public void testEmbeddingTwoHops() {
		createTwoTierSubstrateNetwork();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final TotalCommunicationCostMetricB metric = new TotalCommunicationCostMetricB(sNet);

		assertEquals(2 * 2 * 2 * 3, metric.getValue());
	}

}
