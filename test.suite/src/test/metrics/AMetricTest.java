package test.metrics;

import org.junit.jupiter.api.BeforeEach;

import facade.ModelFacade;
import model.SubstrateLink;

/**
 * Abstract metric test class.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public abstract class AMetricTest {

	/**
	 * ModelFacade object to work with.
	 */
	protected final ModelFacade facade = ModelFacade.getInstance();

	@BeforeEach
	public void resetModel() {
		facade.resetAll();
	}

	/*
	 * Utility methods
	 */

	/**
	 * Creates the substrate network to test on.
	 */
	protected void createSubstrateNetwork() {
		facade.addNetworkToRoot("sub", false);
		facade.addServerToNetwork("ssrv1", "sub", 2, 2, 2, 1);
		facade.addServerToNetwork("ssrv2", "sub", 2, 2, 2, 1);
		facade.addSwitchToNetwork("ssw", "sub", 0);
		facade.addLinkToNetwork("sln1", "sub", 100, "ssw", "ssrv1");
		facade.addLinkToNetwork("sln2", "sub", 100, "ssw", "ssrv2");
		facade.addLinkToNetwork("sln3", "sub", 100, "ssrv1", "ssw");
		facade.addLinkToNetwork("sln4", "sub", 100, "ssrv2", "ssw");
	}

	/**
	 * Creates the virtual network to test on.
	 */
	protected void createVirtualNetwork() {
		facade.addNetworkToRoot("virt", true);
		facade.addSwitchToNetwork("vsw", "virt", 0);
		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 0);
		facade.addServerToNetwork("vsrv2", "virt", 1, 1, 1, 0);
		facade.addLinkToNetwork("vln1", "virt", 3, "vsw", "vsrv1");
		facade.addLinkToNetwork("vln2", "virt", 3, "vsw", "vsrv2");
		facade.addLinkToNetwork("vln3", "virt", 3, "vsrv1", "vsw");
		facade.addLinkToNetwork("vln4", "virt", 3, "vsrv2", "vsw");
	}

	/**
	 * Sets an embedding with two substrate servers up. The switch will be embedded
	 * on the substrate switch.
	 */
	protected void setupEmbeddingTwoHosts() {
		facade.embedNetworkToNetwork("sub", "virt");
		facade.embedSwitchToNode("ssw", "vsw");
		facade.embedServerToServer("ssrv1", "vsrv1");
		facade.embedServerToServer("ssrv2", "vsrv2");
		final SubstrateLink l1 = (SubstrateLink) facade.getLinkFromSourceToTarget("ssw", "ssrv1");
		final SubstrateLink l2 = (SubstrateLink) facade.getLinkFromSourceToTarget("ssw", "ssrv2");
		final SubstrateLink l3 = (SubstrateLink) facade.getLinkFromSourceToTarget("ssrv1", "ssw");
		final SubstrateLink l4 = (SubstrateLink) facade.getLinkFromSourceToTarget("ssrv2", "ssw");
		facade.embedLinkToLink(l1.getName(), "vln1");
		facade.embedLinkToLink(l2.getName(), "vln2");
		facade.embedLinkToLink(l3.getName(), "vln3");
		facade.embedLinkToLink(l4.getName(), "vln4");
	}

}
