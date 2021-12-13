package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metrics.embedding.TotalPathCostMetric;
import model.SubstrateNetwork;

/**
 * Test class for the metric of total path cost.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
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
	public void testEmbeddingSameHost() {
		setupEmbeddingSameHost();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final TotalPathCostMetric metric = new TotalPathCostMetric(sNet);

		// cost = 2 * SrvToSrv + 1 * SwToSrv + 4 * LnToSrv
		// cost = 2 * 1 + 1 * 2 + 4 * 1
		assertEquals(8, metric.getValue());
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

	@Test
	public void testEmbeddingTwoHops() {
		setupEmbeddingTwoHops();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final TotalPathCostMetric metric = new TotalPathCostMetric(sNet);

		// cost = 2 * SrvToSrv + 1 * SwToSrv + 4 * LnToPath(2hop)
		// cost = 2 * 1 + 1 * 2 + 4 * (4^2)
		assertEquals((2 + 2 + 4 * Math.pow(4, 2)), metric.getValue());
	}

}
