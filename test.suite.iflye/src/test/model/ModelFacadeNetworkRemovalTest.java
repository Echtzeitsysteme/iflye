package test.model;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstratePath;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;
import test.utils.GenericTestUtils;

/**
 * Test class for the ModelFacade that tests the removal of networks.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ModelFacadeNetworkRemovalTest {

	/**
	 * ModelFacade instance.
	 */
	protected ModelFacade facade = ModelFacade.getInstance();

	/**
	 * Old bandwidth ignore config.
	 */
	private boolean oldIgnoreBw;

	/**
	 * Network ID to use in all tests.
	 */
	private static final String netId = "net";

	@BeforeEach
	public void resetModel() {
		facade.resetAll();
		oldIgnoreBw = ModelFacadeConfig.IGNORE_BW;
	}

	@AfterEach
	public void restoreConfig() {
		ModelFacadeConfig.IGNORE_BW = oldIgnoreBw;
	}

	/*
	 * Positive tests.
	 */

	@Test
	public void testRemovalOfSubstrateNetworkBasic() {
		ModelFacadeServerRemovalTest.setUpOneTier(2, netId, false);
		assertNotNull(facade.getNetworkById(netId));
		facade.removeNetworkFromRoot(netId);
		assertFalse(facade.networkExists(netId));
	}

	@Test
	public void testRemovalOfSubstrateNetworkEmbedding() {
		ModelFacadeServerRemovalTest.setUpOneTier(4, netId, false);
		assertNotNull(facade.getNetworkById(netId));
		ModelFacadeServerRemovalTest.setUpOneTier(2, "virt", true);
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById(netId);
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		// Embed virtual onto substrate network
		GenericTestUtils.vneFakeIlpEmbedding(sNet, Set.of(vNet));
		assertFalse(sNet.getGuests().isEmpty());

		// Remove substrate network
		facade.removeNetworkFromRoot(netId);
		assertTrue(sNet.getGuests().isEmpty());

		// Check all virtual elements for host embeddings (there should be none).
		// Virtual network
		assertNull(vNet.getHost());

		// Virtual links
		vNet.getLinks().forEach(l -> {
			final VirtualLink vl = (VirtualLink) l;
			assertNull(vl.getHost());
		});

		vNet.getNodess().forEach(n -> {
			// Virtual servers
			if (n instanceof VirtualServer) {
				final VirtualServer vsrv = (VirtualServer) n;
				assertNull(vsrv.getHost());
			} else {
				// Virtual switches
				final VirtualSwitch vsw = (VirtualSwitch) n;
				assertNull(vsw.getHost());
			}
		});
	}

	@Test
	public void testRemovalOfVirtualNetworkBasic() {
		ModelFacadeServerRemovalTest.setUpOneTier(2, netId, true);
		assertNotNull(facade.getNetworkById(netId));
		facade.removeNetworkFromRoot(netId);
		assertFalse(facade.networkExists(netId));
	}

	@Test
	public void testRemovalOfVirtualNetworkEmbedding() {
		ModelFacadeServerRemovalTest.setUpOneTier(4, netId, false);
		assertNotNull(facade.getNetworkById(netId));
		ModelFacadeServerRemovalTest.setUpOneTier(2, "virt", true);
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById(netId);
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		// Embed virtual onto substrate network
		GenericTestUtils.vneFakeIlpEmbedding(sNet, Set.of(vNet));
		assertFalse(sNet.getGuests().isEmpty());

		// Remove substrate network
		facade.removeNetworkFromRoot("virt");

		// Check all substrate elements for guest embeddings (there should be none).
		// Network
		assertTrue(sNet.getGuests().isEmpty());

		// Links
		sNet.getLinks().forEach(l -> {
			final SubstrateLink sl = (SubstrateLink) l;
			assertTrue(sl.getGuestLinks().isEmpty());
			assertEquals(1, sl.getResidualBandwidth());
		});

		// Paths
		sNet.getPaths().forEach(p -> {
			final SubstratePath sp = p;
			assertTrue(sp.getGuestLinks().isEmpty());
			assertEquals(1, sp.getResidualBandwidth());
		});

		sNet.getNodess().forEach(n -> {
			// Servers
			if (n instanceof SubstrateServer) {
				final SubstrateServer ssrv = (SubstrateServer) n;
				assertTrue(ssrv.getGuestServers().isEmpty());
				assertEquals(1, ssrv.getResidualCpu());
				assertEquals(1, ssrv.getResidualMemory());
				assertEquals(1, ssrv.getResidualStorage());
			} else {
				// Switches
				final SubstrateSwitch ssw = (SubstrateSwitch) n;
				assertTrue(ssw.getGuestSwitches().isEmpty());
			}
		});
	}

	/*
	 * Negative tests.
	 */

	@Test
	public void testRejectNetworkDoesNotExist() {
		assertThrows(IllegalArgumentException.class, () -> {
			facade.removeNetworkFromRoot("aaa");
		});
	}

	@Test
	public void testNoBwIncreaseIfBwIgnoreIsTrue() {
		ModelFacadeConfig.IGNORE_BW = true;
		ModelFacadeServerRemovalTest.setUpOneTier(4, netId, false);
		assertNotNull(facade.getNetworkById(netId));
		ModelFacadeServerRemovalTest.setUpOneTier(2, "virt", true);
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById(netId);
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		// Embed virtual onto substrate network
		GenericTestUtils.vneFakeIlpEmbedding(sNet, Set.of(vNet));
		assertFalse(sNet.getGuests().isEmpty());

		// Remove substrate network
		facade.removeNetworkFromRoot("virt");

		// All substrate links and paths must have bandwidth 1. This ensures that the
		// removal did not
		// increment the substrate bandwidth, if it was not decreased before.
		// Links
		sNet.getLinks().forEach(l -> {
			final SubstrateLink sl = (SubstrateLink) l;
			assertTrue(sl.getGuestLinks().isEmpty());
			assertEquals(1, sl.getResidualBandwidth());
		});

		// Paths
		sNet.getPaths().forEach(p -> {
			final SubstratePath sp = p;
			assertTrue(sp.getGuestLinks().isEmpty());
			assertEquals(1, sp.getResidualBandwidth());
		});
	}

}
