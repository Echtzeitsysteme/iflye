package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metrics.embedding.TotalPathCostMetric;
import model.SubstrateNetwork;

/**
 * Test class for the metric of total path cost.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TotalPathCostMetricTest extends AMetricTest {

	@BeforeEach
	public void setup() {
		createSubstrateNetwork();
		createVirtualNetwork();
	}

	@Test
	public void testNoEmbeddings() {
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final TotalPathCostMetric metric = new TotalPathCostMetric(sNet);
		assertEquals(0, metric.getValue());
	}

	@Test
	public void testEmbeddingTwoHosts() {
		setupEmbeddingTwoHosts();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final TotalPathCostMetric metric = new TotalPathCostMetric(sNet);

		// cost = 2 * SrvToSrv + 1 * SwToSw + 4 * LnToLn(1hop)
		// cost = 2 * 1 + 1 * 1 + 4 * 2
		assertEquals(11, metric.getValue());
	}

}
