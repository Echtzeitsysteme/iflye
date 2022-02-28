package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metrics.CostUtility;
import metrics.embedding.TotalTafCommunicationCostMetric;
import model.SubstrateNetwork;

/**
 * Test class for the metric of total TAF (traffic amount first) communication
 * cost.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TotalTafCommunicationCostMetricTest extends AMetricTest {

	@BeforeEach
	public void setup() {
		createVirtualNetwork();
	}

	@Test
	public void testNoEmbeddings() {
		createSubstrateNetwork();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final TotalTafCommunicationCostMetric metric = new TotalTafCommunicationCostMetric(sNet);
		assertEquals(0, metric.getValue());
	}

	@Test
	public void testEmbeddingTwoHosts() {
		createSubstrateNetwork();
		setupEmbeddingTwoHosts();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final TotalTafCommunicationCostMetric metric = new TotalTafCommunicationCostMetric(sNet);

		// cost = 2 * C_BETA * vLink.bandwidth
		// cost = 2 * C_BETA * 3
		assertEquals(2 * CostUtility.TAF_C_BETA * 3, metric.getValue());
	}

}
