package test.algorithms.generic;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import model.Link;
import model.Node;
import model.SubstrateNetwork;
import model.SubstrateServer;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Abstract test class with one and two tier based networks for the algorithm
 * implementations.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public abstract class AAlgorithmTwoTierTest extends AAlgorithmTest {

	/*
	 * Positive tests.
	 */

	@Disabled
	@Test
	public void testAllOnOneServer() {
		oneTierSetupTwoServers("virt", 1);
		oneTierSetupTwoServers("sub", 2);

		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		SubstrateServer host = null;

		// Test all vServer hosts
		for (final Node n : facade.getAllServersOfNetwork("virt")) {
			// Initialize the host of the first virtual element to check
			if (host == null) {
				host = ((VirtualServer) n).getHost();
			}

			assertEquals(host, ((VirtualServer) n).getHost());
		}

		// Test all vSwitch hosts
		for (final Node n : facade.getAllSwitchesOfNetwork("virt")) {
			assertEquals(host, ((VirtualSwitch) n).getHost());
		}

		// Test all vLink hosts
		for (final Link l : facade.getAllLinksOfNetwork("virt")) {
			final VirtualLink vl = (VirtualLink) l;
			// This one host must be substrate server 1
			assertEquals(host, vl.getHost());
		}
	}

	@Test
	public void testAllOnOneRack() {
		oneTierSetupTwoServers("virt", 2);
		oneTierSetupTwoServers("sub", 2);

		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		// Test switch placement
		// Switch placement is possible on all substrate nodes
		final VirtualSwitch vSw = (VirtualSwitch) facade.getSwitchById("virt_sw");
		assertNotNull(vSw.getHost());
		final String refHostSw = vSw.getHost().getName();

		// Test server placements
		final VirtualServer vSrv1 = (VirtualServer) facade.getServerById("virt_srv1");
		final VirtualServer vSrv2 = (VirtualServer) facade.getServerById("virt_srv2");

		// Both virtual servers have to be embedded on other substrate servers
		if (vSrv1.getHost().equals(vSrv2.getHost())) {
			fail();
		}

		// Get reference hosts for later checks of links
		final String refHost1 = vSrv1.getHost().getName();
		final String refHost2 = vSrv2.getHost().getName();

		// Test link placements
		final VirtualLink vLn1 = (VirtualLink) facade.getLinkById("virt_ln1");
		final VirtualLink vLn2 = (VirtualLink) facade.getLinkById("virt_ln2");
		final VirtualLink vLn3 = (VirtualLink) facade.getLinkById("virt_ln3");
		final VirtualLink vLn4 = (VirtualLink) facade.getLinkById("virt_ln4");

		String sourceName = "";
		String targetName = "";

		// Link 1
		sourceName = vLn1.getHost().getSource().getName();
		targetName = vLn1.getHost().getTarget().getName();

		assertEquals(refHost1, sourceName);
		assertEquals(refHostSw, targetName);

		// Link 2
		sourceName = vLn2.getHost().getSource().getName();
		targetName = vLn2.getHost().getTarget().getName();

		assertEquals(refHost2, sourceName);
		assertEquals(refHostSw, targetName);

		// Link 3
		sourceName = vLn3.getHost().getSource().getName();
		targetName = vLn3.getHost().getTarget().getName();

		assertEquals(refHostSw, sourceName);
		assertEquals(refHost1, targetName);

		// Link 4
		sourceName = vLn4.getHost().getSource().getName();
		targetName = vLn4.getHost().getTarget().getName();

		assertEquals(refHostSw, sourceName);
		assertEquals(refHost2, targetName);
	}

	/*
	 * Negative tests.
	 */

	@Test
	public void testNoEmbeddingWithSplittedVm() {
		oneTierSetupTwoServers("virt", 2);
		twoTierSetupFourServers("sub", 1);

		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		initAlgo(sNet, Set.of(vNet));

		// Embedding should not be possible, because a split of one VM to embed it on
		// two substrate
		// servers is not possible although the total amount of resources could handle
		// the virtual
		// network.
		assertFalse(algo.execute());
		assertNull(vNet.getHost());
	}

	/*
	 * Utility methods.
	 */

	/**
	 * Creates a one tier network with two servers and one switch.
	 *
	 * @param networkId      Network id.
	 * @param slotsPerServer Number of CPU, memory and storage resources.
	 */
	protected void oneTierSetupTwoServers(final String networkId, final int slotsPerServer) {
		facade.addSwitchToNetwork(networkId + "_sw", networkId, 0);
		facade.addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer, slotsPerServer, slotsPerServer, 1);
		facade.addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer, slotsPerServer, slotsPerServer, 1);
		facade.addLinkToNetwork(networkId + "_ln1", networkId, 1, networkId + "_srv1", networkId + "_sw");
		facade.addLinkToNetwork(networkId + "_ln2", networkId, 1, networkId + "_srv2", networkId + "_sw");
		facade.addLinkToNetwork(networkId + "_ln3", networkId, 1, networkId + "_sw", networkId + "_srv1");
		facade.addLinkToNetwork(networkId + "_ln4", networkId, 1, networkId + "_sw", networkId + "_srv2");
	}

	/**
	 * Creates a one tier network with three servers and one switch.
	 *
	 * @param networkId      Network id.
	 * @param slotsPerServer Number of CPU, memory and storage resources.
	 */
	protected void oneTierSetupThreeServers(final String networkId, final int slotsPerServer) {
		facade.addSwitchToNetwork(networkId + "_sw", networkId, 0);
		facade.addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer, slotsPerServer, slotsPerServer, 1);
		facade.addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer, slotsPerServer, slotsPerServer, 1);
		facade.addServerToNetwork(networkId + "_srv3", networkId, slotsPerServer, slotsPerServer, slotsPerServer, 1);
		facade.addLinkToNetwork(networkId + "_ln1", networkId, 1, networkId + "_srv1", networkId + "_sw");
		facade.addLinkToNetwork(networkId + "_ln2", networkId, 1, networkId + "_srv2", networkId + "_sw");
		facade.addLinkToNetwork(networkId + "_ln3", networkId, 1, networkId + "_srv3", networkId + "_sw");
		facade.addLinkToNetwork(networkId + "_ln4", networkId, 1, networkId + "_sw", networkId + "_srv1");
		facade.addLinkToNetwork(networkId + "_ln5", networkId, 1, networkId + "_sw", networkId + "_srv2");
		facade.addLinkToNetwork(networkId + "_ln6", networkId, 1, networkId + "_sw", networkId + "_srv3");
	}

	/**
	 * Creates a two tier network with four servers total, two rack switches, and
	 * one core switch.
	 *
	 * @param networkId      Network id.
	 * @param slotsPerServer Number of CPU, memory and storage resources.
	 */
	protected void twoTierSetupFourServers(final String networkId, final int slotsPerServer) {
		facade.addSwitchToNetwork(networkId + "_csw1", networkId, 0);
		facade.addSwitchToNetwork(networkId + "_rsw1", networkId, 1);
		facade.addSwitchToNetwork(networkId + "_rsw2", networkId, 1);

		facade.addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer, slotsPerServer, slotsPerServer, 2);
		facade.addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer, slotsPerServer, slotsPerServer, 2);
		facade.addServerToNetwork(networkId + "_srv3", networkId, slotsPerServer, slotsPerServer, slotsPerServer, 2);
		facade.addServerToNetwork(networkId + "_srv4", networkId, slotsPerServer, slotsPerServer, slotsPerServer, 2);

		facade.addLinkToNetwork(networkId + "_ln1", networkId, 1, networkId + "_srv1", networkId + "_rsw1");
		facade.addLinkToNetwork(networkId + "_ln2", networkId, 1, networkId + "_srv2", networkId + "_rsw1");
		facade.addLinkToNetwork(networkId + "_ln3", networkId, 1, networkId + "_rsw1", networkId + "_srv1");
		facade.addLinkToNetwork(networkId + "_ln4", networkId, 1, networkId + "_rsw1", networkId + "_srv2");
		facade.addLinkToNetwork(networkId + "_ln5", networkId, 1, networkId + "_srv3", networkId + "_rsw2");
		facade.addLinkToNetwork(networkId + "_ln6", networkId, 1, networkId + "_srv4", networkId + "_rsw2");
		facade.addLinkToNetwork(networkId + "_ln7", networkId, 1, networkId + "_rsw2", networkId + "_srv3");
		facade.addLinkToNetwork(networkId + "_ln8", networkId, 1, networkId + "_rsw2", networkId + "_srv4");

		facade.addLinkToNetwork(networkId + "_ln9", networkId, 10, networkId + "_rsw1", networkId + "_csw1");
		facade.addLinkToNetwork(networkId + "_ln10", networkId, 10, networkId + "_rsw2", networkId + "_csw1");
		facade.addLinkToNetwork(networkId + "_ln11", networkId, 10, networkId + "_csw1", networkId + "_rsw1");
		facade.addLinkToNetwork(networkId + "_ln12", networkId, 10, networkId + "_csw1", networkId + "_rsw2");
	}

}
