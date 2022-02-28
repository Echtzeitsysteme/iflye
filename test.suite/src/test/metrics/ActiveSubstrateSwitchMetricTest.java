package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import metrics.embedding.ActiveSubstrateSwitchMetric;
import model.SubstrateNetwork;

/**
 * Test class for the metric of active substrate switches.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ActiveSubstrateSwitchMetricTest extends AMetricTest {

	@Test
	public void testNoEmbeddings() {
		facade.addNetworkToRoot("sub", false);
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

		final ActiveSubstrateSwitchMetric metric = new ActiveSubstrateSwitchMetric(sNet);
		assertEquals(0, metric.getValue());
	}

	@Test
	public void testOneEmbedding() {
		createAndEmbedSwitches(1, "virt", 0);
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

		final ActiveSubstrateSwitchMetric metric = new ActiveSubstrateSwitchMetric(sNet);
		assertEquals(1, metric.getValue());
	}

	@Test
	public void testMultipleEmbedding() {
		createAndEmbedSwitches(42, "virt", 0);
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

		final ActiveSubstrateSwitchMetric metric = new ActiveSubstrateSwitchMetric(sNet);
		assertEquals(42, metric.getValue());
	}

	@Test
	public void testMultipleVirtualNetworks() {
		for (int i = 1; i <= 4; i++) {
			createAndEmbedSwitches(1, "virt_" + i, i - 1);
			final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

			final ActiveSubstrateSwitchMetric metric = new ActiveSubstrateSwitchMetric(sNet);
			assertEquals(i * 1, metric.getValue());
		}
	}

	/*
	 * Utility methods
	 */

	/**
	 * Creates and embeds a given number of switches to the substrate network.
	 *
	 * @param numberOfSwitches      Number of switches to create and embed.
	 * @param virtNetId             String ID for the virtual network.
	 * @param substrateSwitchOffset Offset to start creating virtual switches with.
	 */
	private void createAndEmbedSwitches(final int numberOfSwitches, final String virtNetId,
			final int substrateSwitchOffset) {
		if (!facade.networkExists("sub")) {
			facade.addNetworkToRoot("sub", false);
		}

		facade.addNetworkToRoot(virtNetId, true);
		facade.embedNetworkToNetwork("sub", virtNetId);

		for (int i = 0; i < numberOfSwitches; i++) {
			facade.addSwitchToNetwork("ssw_" + (i + substrateSwitchOffset), "sub", 0);
			facade.addSwitchToNetwork("vsw_" + virtNetId + i, virtNetId, 0);
			facade.embedSwitchToSwitch("ssw_" + (i + substrateSwitchOffset), "vsw_" + virtNetId + i);
		}
	}

}
