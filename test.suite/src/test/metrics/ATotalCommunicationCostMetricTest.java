package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metrics.IMetric;
import model.SubstrateNetwork;

/**
 * Abstract test class for the metric(s) of total communication cost.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public abstract class ATotalCommunicationCostMetricTest extends AMetricTest {

	/**
	 * Metric object to test.
	 */
	protected IMetric metric;

	/**
	 * Method that sets up the metric object to test.
	 *
	 * @param sNet Substrate network to init metric with.
	 */
	protected abstract void setMetric(final SubstrateNetwork sNet);

	@BeforeEach
	public void setup() {
		createVirtualNetwork();
	}

	/*
	 * Positive tests
	 */

	@Test
	public void testNoEmbeddings() {
		createSubstrateNetwork();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		setMetric(sNet);

		assertEquals(0, metric.getValue());
	}

	@Test
	public void testEmbeddingTwoHosts() {
		createSubstrateNetwork();
		setupEmbeddingTwoHosts();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		setMetric(sNet);

		assertEquals(2 * 2 * 3, metric.getValue());
	}

}
