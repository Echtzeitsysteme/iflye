package test.algorithms.gips.lookahead;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

import model.Network;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.VirtualServer;

/**
 * Test class for the VNE GIPS lookahead algorithm implementation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsLookaheadAlgorithmActualLookaheadTest extends AVneGipsLookaheadAlgorithmTest {

	//
	// Tests
	//

//	@Test
//	public void testTwoSmallVnetsOnlySwitches() {
//		facade.addSwitchToNetwork("ssw1", "sub", 0);
//		facade.addServerToNetwork("ssrv1", "sub", 10, 10, 10, 1);
//		facade.addLinkToNetwork("ssl1", "sub", 10, "ssw1", "ssrv1");
//		facade.addLinkToNetwork("ssl2", "sub", 10, "ssrv1", "ssw1");
//
//		facade.addSwitchToNetwork("vsw1", "virt", 0);
//
//		facade.addNetworkToRoot("virt2", true);
//		facade.addSwitchToNetwork("vsw2", "virt2", 0);
//
//		// Testing
//		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
//		addVirtualNetworkToAlgo("virt");
//		checkAndValidate("virt");
//	}

	/**
	 * The second virtual server (of the second virtual network) completely fills up
	 * the second substrate server. Hence, the lookahead algorithm must place the
	 * first virtual server onto the first substrate server. Otherwise, the complete
	 * problem would be non-feasible.
	 */
	@Test
	public void testTwoSmallVnetsOnlyServers() {
		facade.addSwitchToNetwork("ssw1", "sub", 0);
		facade.addServerToNetwork("ssrv1", "sub", 10, 10, 10, 1);
		facade.addServerToNetwork("ssrv2", "sub", 20, 20, 20, 1);
		facade.addLinkToNetwork("ssl1", "sub", 10, "ssw1", "ssrv1");
		facade.addLinkToNetwork("ssl2", "sub", 10, "ssrv1", "ssw1");
		facade.addLinkToNetwork("ssl3", "sub", 10, "ssw1", "ssrv2");
		facade.addLinkToNetwork("ssl4", "sub", 10, "ssrv2", "ssw1");

		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);

		facade.addNetworkToRoot("virt2", true);
		facade.addServerToNetwork("vsrv2", "virt2", 20, 20, 20, 1);

		// Testing
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		addVirtualNetworkToAlgo("virt");
		checkAndValidate("virt");

		final VirtualServer vsrv = (VirtualServer) facade.getServerById("vsrv1");
		assertNotNull(vsrv);
		assertEquals("ssrv1", vsrv.getHost().getName());
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
