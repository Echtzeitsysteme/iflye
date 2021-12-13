package test.model;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import generators.FatTreeNetworkGenerator;
import generators.OneTierNetworkGenerator;
import generators.config.FatTreeConfig;
import generators.config.OneTierConfig;
import model.Link;
import model.Server;
import model.SubstratePath;
import model.SubstrateServer;
import model.VirtualLink;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Test class for the ModelFacade that tests the removal of substrate servers.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadeServerRemovalTest {

	/**
	 * ModelFacade instance.
	 */
	protected ModelFacade facade = ModelFacade.getInstance();

	/**
	 * Network ID to use in all tests.
	 */
	private static final String netId = "net";

	@BeforeEach
	public void resetModel() {
		facade.resetAll();
	}

	/*
	 * Positive tests.
	 */

	@Test
	public void testRemovalOneTierServerOnlySmall() {
		setUpOneTier(2, netId, false);
		assertEquals(2, facade.getAllServersOfNetwork(netId).size());
		facade.removeSubstrateServerFromNetwork(netId + "_srv_0");
		assertEquals(1, facade.getAllServersOfNetwork(netId).size());

		// Check left over server
		assertEquals(netId + "_srv_1", facade.getServerById(netId + "_srv_1").getName());
	}

	@Test
	public void testRemovalOneTierLinksOnlySmall() {
		setUpOneTier(2, netId, false);
		assertEquals(2, facade.getAllServersOfNetwork(netId).size());
		assertEquals(4, facade.getAllLinksOfNetwork(netId).size());
		final String removeId = netId + "_srv_0";
		facade.removeSubstrateServerFromNetwork(removeId);
		assertEquals(2, facade.getAllLinksOfNetwork(netId).size());

		// Check left over links
		for (final Link l : facade.getAllLinksOfNetwork(netId)) {
			assertFalse(l.getSource().getName().equals(removeId));
			assertFalse(l.getTarget().getName().equals(removeId));
		}
	}

	@Test
	public void testRemovalOneTierPathsOnlySmall() {
		setUpOneTier(2, netId, false);
		assertEquals(2, facade.getAllServersOfNetwork(netId).size());
		assertEquals(6, facade.getAllPathsOfNetwork(netId).size());
		final String removeId = netId + "_srv_0";
		final SubstrateServer removeServer = (SubstrateServer) facade.getServerById(removeId);
		facade.removeSubstrateServerFromNetwork(removeId);
		assertEquals(2, facade.getAllPathsOfNetwork(netId).size());

		// Check left over paths
		for (final SubstratePath p : facade.getAllPathsOfNetwork(netId)) {
			assertFalse(p.getSource().getName().equals(removeId));
			assertFalse(p.getTarget().getName().equals(removeId));
			assertFalse(p.getNodes().contains(removeServer));
		}
	}

	@Test
	public void testRemovalOneTierServersOnlyLarge() {
		setUpOneTier(20, netId, false);
		assertEquals(20, facade.getAllServersOfNetwork(netId).size());

		final Set<Server> removedServers = new HashSet<>();

		for (int i = 0; i < 19; i++) {
			final String id = netId + "_srv_" + i;
			removedServers.add(facade.getServerById(id));
			facade.removeSubstrateServerFromNetwork(id);
			assertEquals(20 - i - 1, facade.getAllServersOfNetwork(netId).size());

			// Check left over servers
			removedServers.forEach(s -> {
				assertFalse(facade.getAllServersOfNetwork(netId).contains(s));
			});
		}
	}

	@Test
	public void testRemovalOneTierLinksOnlyLarge() {
		setUpOneTier(20, netId, false);
		assertEquals(20, facade.getAllServersOfNetwork(netId).size());
		assertEquals(40, facade.getAllLinksOfNetwork(netId).size());

		final Set<Server> removedServers = new HashSet<>();

		for (int i = 0; i < 19; i++) {
			final String id = netId + "_srv_" + i;
			removedServers.add(facade.getServerById(id));
			facade.removeSubstrateServerFromNetwork(id);

			assertEquals(40 - (i + 1) * 2, facade.getAllLinksOfNetwork(netId).size());

			// Check left over links
			for (final Link l : facade.getAllLinksOfNetwork(netId)) {
				removedServers.forEach(s -> {
					assertFalse(l.getSource().equals(s));
					assertFalse(l.getTarget().equals(s));
				});
			}
		}
	}

	@Test
	public void testRemovalOneTierPathsOnlyLarge() {
		setUpOneTier(20, netId, false);
		assertEquals(20, facade.getAllServersOfNetwork(netId).size());
		final int totalNumberOfPaths = 20 * (20 - 1 + 2);
		assertEquals(totalNumberOfPaths, facade.getAllPathsOfNetwork(netId).size());

		final Set<SubstrateServer> removedServers = new HashSet<>();

		for (int i = 0; i < 19; i++) {
			final String id = netId + "_srv_" + i;
			removedServers.add((SubstrateServer) facade.getServerById(id));
			facade.removeSubstrateServerFromNetwork(id);

			// Check left over paths
			for (final SubstratePath p : facade.getAllPathsOfNetwork(netId)) {
				removedServers.forEach(s -> {
					assertFalse(p.getSource().equals(s));
					assertFalse(p.getTarget().equals(s));
					assertFalse(p.getNodes().contains(s));
				});
			}
		}
	}

	@Test
	public void testRemovalFatTreeServerOnlySmall() {
		setUpFatTree(4);
		assertEquals(16, facade.getAllServersOfNetwork(netId).size());
		final String deletedId = netId + "_srv_0";
		final Server deleted = facade.getServerById(deletedId);
		facade.removeSubstrateServerFromNetwork(deletedId);
		assertEquals(15, facade.getAllServersOfNetwork(netId).size());

		// Check left over servers
		facade.getAllServersOfNetwork(netId).forEach(s -> {
			assertFalse(s.equals(deleted));
		});
	}

	@Test
	public void testRemovalFatTreeLinksOnlySmall() {
		setUpFatTree(4);
		assertEquals(16, facade.getAllServersOfNetwork(netId).size());
		assertEquals(96, facade.getAllLinksOfNetwork(netId).size());
		final String removeId = netId + "_srv_0";
		facade.removeSubstrateServerFromNetwork(removeId);
		assertEquals(94, facade.getAllLinksOfNetwork(netId).size());

		// Check left over links
		for (final Link l : facade.getAllLinksOfNetwork(netId)) {
			assertFalse(l.getSource().getName().equals(removeId));
			assertFalse(l.getTarget().getName().equals(removeId));
		}
	}

	@Test
	public void testRemovalFatTreePathsOnlySmall() {
		setUpFatTree(4);
		assertEquals(16, facade.getAllServersOfNetwork(netId).size());
		assertEquals(496, facade.getAllPathsOfNetwork(netId).size());
		final String removeId = netId + "_srv_0";
		final SubstrateServer removeServer = (SubstrateServer) facade.getServerById(removeId);
		facade.removeSubstrateServerFromNetwork(removeId);
		assertEquals(462, facade.getAllPathsOfNetwork(netId).size());

		// Check left over paths
		for (final SubstratePath p : facade.getAllPathsOfNetwork(netId)) {
			assertFalse(p.getSource().getName().equals(removeId));
			assertFalse(p.getTarget().getName().equals(removeId));
			assertFalse(p.getNodes().contains(removeServer));
		}
	}

	@Test
	public void testRemovalOneTierEmbeddingServer() {
		setUpOneTier(2, netId, false);
		assertEquals(2, facade.getAllServersOfNetwork(netId).size());
		final String removeId = netId + "_srv_0";

		facade.addNetworkToRoot("vnet", true);
		facade.addServerToNetwork("vnet_srv_0", "vnet", 1, 1, 1, 1);
		facade.embedNetworkToNetwork(netId, "vnet");
		facade.embedServerToServer(removeId, "vnet_srv_0");

		final SubstrateServer deleted = ((SubstrateServer) facade.getServerById(removeId));
		assertEquals(1, deleted.getGuestServers().size());
		final VirtualServer guest = (VirtualServer) facade.getServerById("vnet_srv_0");
		assertNotNull(guest.getHost());

		facade.removeSubstrateServerFromNetwork(removeId);
		assertEquals(1, facade.getAllServersOfNetwork(netId).size());
		assertNull(guest.getHost());
	}

	@Test
	public void testRemovalOneTierEmbeddingSwitch() {
		setUpOneTier(2, netId, false);
		assertEquals(2, facade.getAllServersOfNetwork(netId).size());
		final String removeId = netId + "_srv_0";

		facade.addNetworkToRoot("vnet", true);
		facade.addSwitchToNetwork("vnet_sw_0", "vnet", 0);
		facade.embedNetworkToNetwork(netId, "vnet");
		facade.embedSwitchToNode(removeId, "vnet_sw_0");

		final SubstrateServer deleted = ((SubstrateServer) facade.getServerById(removeId));
		assertEquals(1, deleted.getGuestSwitches().size());
		final VirtualSwitch guest = (VirtualSwitch) facade.getSwitchById("vnet_sw_0");
		assertNotNull(guest.getHost());

		facade.removeSubstrateServerFromNetwork(removeId);
		assertEquals(1, facade.getAllServersOfNetwork(netId).size());
		assertNull(guest.getHost());
	}

	@Test
	public void testRemovalOneTierEmbeddingLink() {
		setUpOneTier(2, netId, false);
		assertEquals(2, facade.getAllServersOfNetwork(netId).size());
		final String removeId = netId + "_srv_0";

		facade.addNetworkToRoot("vnet", true);
		facade.addSwitchToNetwork("vnet_sw_0", "vnet", 0);
		facade.addServerToNetwork("vnet_srv_0", "vnet", 1, 1, 1, 1);
		facade.addLinkToNetwork("vnet_l_0", "vnet", 1, "vnet_sw_0", "vnet_srv_0");
		facade.embedNetworkToNetwork(netId, "vnet");
		facade.embedSwitchToNode(removeId, "vnet_sw_0");
		facade.embedServerToServer(removeId, "vnet_srv_0");
		facade.embedLinkToServer(removeId, "vnet_l_0");

		final SubstrateServer deleted = ((SubstrateServer) facade.getServerById(removeId));
		assertEquals(1, deleted.getGuestLinks().size());
		final VirtualLink guest = (VirtualLink) facade.getLinkById("vnet_l_0");
		assertNotNull(guest.getHost());

		facade.removeSubstrateServerFromNetwork(removeId);
		assertEquals(1, facade.getAllServersOfNetwork(netId).size());
		assertNull(guest.getHost());
	}

	/*
	 * Negative tests.
	 */

	@Test
	public void testRejectVirtualServer() {
		setUpOneTier(2, netId, false);
		facade.addNetworkToRoot("vnet", true);
		facade.addServerToNetwork("vnet_srv_0", "vnet", 1, 1, 1, 1);

		assertThrows(IllegalArgumentException.class, () -> {
			facade.removeSubstrateServerFromNetwork("vnet_srv_0");
		});
	}

	/*
	 * Utility methods.
	 */

	/**
	 * Sets an one tier based network with n servers up.
	 *
	 * @param servers   Number of servers to create.
	 * @param id        Network id.
	 * @param isVirtual True if new network must be virtual.
	 */
	static void setUpOneTier(final int servers, final String id, final boolean isVirtual) {
		final OneTierConfig subConfig = new OneTierConfig(servers, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(subConfig);
		gen.createNetwork(id, isVirtual);
	}

	/**
	 * Sets a fat tree based network with parameter k up.
	 *
	 * @param k Fat tree parameter.
	 */
	private void setUpFatTree(final int k) {
		final FatTreeConfig subConfig = new FatTreeConfig(k);
		final FatTreeNetworkGenerator gen = new FatTreeNetworkGenerator(subConfig);
		gen.createNetwork(netId, false);
	}

}
