package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import metrics.embedding.AveragePathLengthMetric;
import model.SubstrateNetwork;

/**
 * Test class for the metric of the average path length.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
@Disabled
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
	public void testEmbeddingTwoHosts() {
		setupEmbeddingTwoHosts();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final AveragePathLengthMetric metric = new AveragePathLengthMetric(sNet);
		assertEquals(1, metric.getValue());
	}

}
