package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metrics.CostUtility;
import metrics.embedding.TotalTafCommunicationCostMetric;
import model.SubstrateNetwork;
import model.SubstrateNode;
import model.SubstratePath;

/**
 * Test class for the metric of total TAF (traffic amount first) communication
 * cost.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
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
	public void testEmbeddingSameHost() {
		createSubstrateNetwork();
		setupEmbeddingSameHost();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final TotalTafCommunicationCostMetric metric = new TotalTafCommunicationCostMetric(sNet);

		// cost = 2 * C_ALPHA * vLink.bandwidth
		// cost = 2 * 1 * 3
		assertEquals(2 * CostUtility.TAF_C_ALPHA * 3, metric.getValue());
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

	@Test
	public void testEmbeddingTwoTierSubstrate() {
		setupTwoTierSubstrateNetwork();
		setupEmbeddingTwoTier();
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final TotalTafCommunicationCostMetric metric = new TotalTafCommunicationCostMetric(sNet);

		// cost = 2 * C_GAMMA * vLink.bandwidth
		// cost = 2 * C_GAMMA * 3
		assertEquals(2 * CostUtility.TAF_C_GAMMA * 3, metric.getValue());
	}

	/*
	 * Utility methods
	 */

	/**
	 * Sets a two tier substrate network with two servers, two rack switches and one
	 * core switch up. Also includes all links and paths.
	 */
	private void setupTwoTierSubstrateNetwork() {
		facade.addNetworkToRoot("sub", false);
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 2);
		facade.addServerToNetwork("ssrv2", "sub", 1, 1, 1, 2);
		facade.addSwitchToNetwork("scsw", "sub", 0);
		facade.addSwitchToNetwork("srsw1", "sub", 1);
		facade.addSwitchToNetwork("srsw2", "sub", 1);

		facade.addLinkToNetwork("sln1", "sub", 100, "srsw1", "ssrv1");
		facade.addLinkToNetwork("sln2", "sub", 100, "srsw2", "ssrv2");
		facade.addLinkToNetwork("sln3", "sub", 100, "ssrv1", "srsw1");
		facade.addLinkToNetwork("sln4", "sub", 100, "ssrv2", "srsw2");

		facade.addLinkToNetwork("sln5", "sub", 100, "srsw1", "scsw");
		facade.addLinkToNetwork("sln6", "sub", 100, "srsw2", "scsw");
		facade.addLinkToNetwork("sln7", "sub", 100, "scsw", "srsw1");
		facade.addLinkToNetwork("sln8", "sub", 100, "scsw", "srsw2");

		facade.createAllPathsForNetwork("sub");
	}

	/**
	 * Embeds the virtual network with two virtual servers and one virtual switch
	 * onto the two tier substrate network. The virtual switch gets placed onto the
	 * substrate core switch and both virtual servers are embedded to one substrate
	 * server each.
	 */
	private void setupEmbeddingTwoTier() {
		facade.embedNetworkToNetwork("sub", "virt");
		facade.embedSwitchToNode("scsw", "vsw");
		facade.embedServerToServer("ssrv1", "vsrv1");
		facade.embedServerToServer("ssrv2", "vsrv2");

		final SubstratePath pSrv1ToCsw = facade.getPathFromSourceToTarget((SubstrateNode) facade.getServerById("ssrv1"),
				(SubstrateNode) facade.getSwitchById("scsw"));
		final SubstratePath pCswToSrv1 = facade.getPathFromSourceToTarget((SubstrateNode) facade.getSwitchById("scsw"),
				(SubstrateNode) facade.getServerById("ssrv1"));
		final SubstratePath pSrv2ToCsw = facade.getPathFromSourceToTarget((SubstrateNode) facade.getServerById("ssrv2"),
				(SubstrateNode) facade.getSwitchById("scsw"));
		final SubstratePath pCswToSrv2 = facade.getPathFromSourceToTarget((SubstrateNode) facade.getSwitchById("scsw"),
				(SubstrateNode) facade.getServerById("ssrv2"));

		facade.embedLinkToPath(pSrv1ToCsw.getName(), "vln1");
		facade.embedLinkToPath(pCswToSrv1.getName(), "vln2");
		facade.embedLinkToPath(pCswToSrv2.getName(), "vln3");
		facade.embedLinkToPath(pSrv2ToCsw.getName(), "vln4");
	}

}
