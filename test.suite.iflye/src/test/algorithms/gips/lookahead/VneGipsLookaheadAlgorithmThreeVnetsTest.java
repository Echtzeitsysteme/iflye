package test.algorithms.gips.lookahead;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

import model.Network;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Test class for the VNE GIPS lookahead algorithm implementation for tests with
 * two virtual network.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsLookaheadAlgorithmThreeVnetsTest extends AVneGipsLookaheadAlgorithmTest {

	//
	// Tests
	//

	@Test
	public void testThreeSmallVnetsOnlySwitches() {
		facade.addSwitchToNetwork("ssw1", "sub", 0);
		facade.addServerToNetwork("ssrv1", "sub", 10, 10, 10, 1);
		facade.addLinkToNetwork("ssl1", "sub", 10, "ssw1", "ssrv1");
		facade.addLinkToNetwork("ssl2", "sub", 10, "ssrv1", "ssw1");

		facade.addSwitchToNetwork("vsw1", "virt", 0);

		facade.addNetworkToRoot("virt2", true);
		facade.addSwitchToNetwork("vsw2", "virt2", 0);

		facade.addNetworkToRoot("virtv", true);
		facade.addSwitchToNetwork("vswv", "virtv", 0);

		// Testing
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		addVirtualNetworkToAlgo("virt");
		checkAndValidate("virt");
	}

	@Test
	public void testThreeSmallVnetsOnlyServers() {
		facade.addSwitchToNetwork("ssw1", "sub", 0);
		facade.addServerToNetwork("ssrv1", "sub", 10, 10, 10, 1);
		facade.addLinkToNetwork("ssl1", "sub", 10, "ssw1", "ssrv1");
		facade.addLinkToNetwork("ssl2", "sub", 10, "ssrv1", "ssw1");

		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);

		facade.addNetworkToRoot("virt2", true);
		facade.addServerToNetwork("vsrv2", "virt2", 1, 1, 1, 1);

		facade.addNetworkToRoot("virt3", true);
		facade.addServerToNetwork("vsrv3", "virt3", 1, 1, 1, 1);

		// Testing
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		addVirtualNetworkToAlgo("virt");
		checkAndValidate("virt");
	}

	@Test
	public void testThreeCompleteVnets() {
		facade.addSwitchToNetwork("ssw1", "sub", 0);
		facade.addServerToNetwork("ssrv1", "sub", 10, 10, 10, 1);
		facade.addLinkToNetwork("ssl1", "sub", 10, "ssw1", "ssrv1");
		facade.addLinkToNetwork("ssl2", "sub", 10, "ssrv1", "ssw1");

		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);
		facade.addSwitchToNetwork("vsw1", "virt", 0);
		facade.addLinkToNetwork("vl1", "virt", 1, "vsrv1", "vsw1");
		facade.addLinkToNetwork("vl2", "virt", 1, "vsw1", "vsrv1");

		facade.addNetworkToRoot("virt2", true);
		facade.addServerToNetwork("vsrv2", "virt2", 1, 1, 1, 1);
		facade.addSwitchToNetwork("vsw2", "virt2", 0);
		facade.addLinkToNetwork("vl3", "virt2", 1, "vsrv2", "vsw2");
		facade.addLinkToNetwork("vl4", "virt2", 1, "vsw2", "vsrv2");

		facade.addNetworkToRoot("virt3", true);
		facade.addServerToNetwork("vsrv3", "virt3", 1, 1, 1, 1);
		facade.addSwitchToNetwork("vsw3", "virt3", 0);
		facade.addLinkToNetwork("vl5", "virt3", 1, "vsrv3", "vsw3");
		facade.addLinkToNetwork("vl6", "virt3", 1, "vsw3", "vsrv3");

		// Testing
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		addVirtualNetworkToAlgo("virt");
		checkAndValidate("virt");
	}

	//
	// Utilities
	//

	private void addVirtualNetworkToAlgo(final String vNetId) {
		if (this.vNets == null) {
			this.vNets = new HashSet<>();
		}
		for (final Network net : facade.getAllNetworks()) {
			if (net instanceof VirtualNetwork vNetToAdd) {
				if (vNetToAdd.getName().equals(vNetId)) {
					vNets.add(vNetToAdd);
				}
			}
		}
	}

}
