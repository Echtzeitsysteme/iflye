package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metrics.embedding.AveragePathLengthMetric;
import model.SubstrateNetwork;

/**
 * Test class for the metric of the average path length.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class AveragePathLengthMetricTest extends AMetricTest {

	@BeforeEach
	public void setup() {
		createSubstrateNetwork();
		createVirtualNetwork();
	}

	@Test
	public void testNoEmbeddings() {
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

		final AveragePathLengthMetric metric = new AveragePathLengthMetric(sNet);
		assertEquals(0, metric.getValue());
	}

	@Test
	public void testEmbeddingSameHost() {
		setupEmbeddingSameHost();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final AveragePathLengthMetric metric = new AveragePathLengthMetric(sNet);
		assertEquals(0, metric.getValue());
	}

	@Test
	public void testEmbeddingTwoHosts() {
		setupEmbeddingTwoHosts();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final AveragePathLengthMetric metric = new AveragePathLengthMetric(sNet);
		assertEquals(1, metric.getValue());
	}

	@Test
	public void testEmbeddingTwoHops() {
		setupEmbeddingTwoHops();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final AveragePathLengthMetric metric = new AveragePathLengthMetric(sNet);
		assertEquals(2, metric.getValue());
	}

}
