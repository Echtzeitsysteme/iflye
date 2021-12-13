package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metrics.IMetric;
import model.SubstrateNetwork;
import model.SubstrateNode;
import model.SubstratePath;

/**
 * Abstract test class for the metric(s) of total communication cost.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
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
	public void testEmbeddingSameHost() {
		createSubstrateNetwork();
		setupEmbeddingSameHost();
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

	/*
	 * Utility methods
	 */

	/**
	 * Create a basic two tier substrate network for testing purposes.
	 */
	protected void createTwoTierSubstrateNetwork() {
		facade.addNetworkToRoot("sub", false);
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		facade.addServerToNetwork("ssrv2", "sub", 1, 1, 1, 1);
		facade.addSwitchToNetwork("cssw", "sub", 0);
		facade.addSwitchToNetwork("rssw1", "sub", 0);
		facade.addSwitchToNetwork("rssw2", "sub", 0);
		facade.addLinkToNetwork("sln1", "sub", 100, "rssw1", "ssrv1");
		facade.addLinkToNetwork("sln2", "sub", 100, "rssw2", "ssrv2");
		facade.addLinkToNetwork("sln3", "sub", 100, "ssrv1", "rssw1");
		facade.addLinkToNetwork("sln4", "sub", 100, "ssrv2", "rssw2");
		facade.addLinkToNetwork("sln5", "sub", 100, "cssw", "rssw1");
		facade.addLinkToNetwork("sln6", "sub", 100, "cssw", "rssw2");
		facade.addLinkToNetwork("sln7", "sub", 100, "rssw1", "cssw");
		facade.addLinkToNetwork("sln8", "sub", 100, "rssw2", "cssw");
		facade.createAllPathsForNetwork("sub");

		facade.embedNetworkToNetwork("sub", "virt");
		facade.embedSwitchToNode("cssw", "vsw");
		facade.embedServerToServer("ssrv1", "vsrv1");
		facade.embedServerToServer("ssrv2", "vsrv2");

		final SubstratePath pa = facade.getPathFromSourceToTarget((SubstrateNode) facade.getServerById("ssrv1"),
				(SubstrateNode) facade.getSwitchById("cssw"));
		final SubstratePath pb = facade.getPathFromSourceToTarget((SubstrateNode) facade.getServerById("ssrv2"),
				(SubstrateNode) facade.getSwitchById("cssw"));
		final SubstratePath pc = facade.getPathFromSourceToTarget((SubstrateNode) facade.getSwitchById("cssw"),
				(SubstrateNode) facade.getServerById("ssrv1"));
		final SubstratePath pd = facade.getPathFromSourceToTarget((SubstrateNode) facade.getSwitchById("cssw"),
				(SubstrateNode) facade.getServerById("ssrv2"));

		if (pa != null && pb != null && pc != null && pd != null) {
			facade.embedLinkToPath(pa.getName(), "vln1");
			facade.embedLinkToPath(pc.getName(), "vln2");
			facade.embedLinkToPath(pb.getName(), "vln3");
			facade.embedLinkToPath(pd.getName(), "vln4");
		}
	}

}
