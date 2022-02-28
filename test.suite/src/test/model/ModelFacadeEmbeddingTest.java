package test.model;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.Link;
import model.Node;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstratePath;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualLink;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Test class for the ModelFacade that tests some embedding tasks.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ModelFacadeEmbeddingTest {

	boolean oldLinkHostEmbed;
	boolean oldIgnoreBw;

	@BeforeEach
	public void resetModel() {
		ModelFacade.getInstance().resetAll();

		// Network setup
		ModelFacade.getInstance().addNetworkToRoot("sub", false);
		ModelFacade.getInstance().addNetworkToRoot("virt", true);

		oldIgnoreBw = ModelFacadeConfig.IGNORE_BW;
	}

	@AfterEach
	public void restoreConfig() {
		ModelFacadeConfig.IGNORE_BW = oldIgnoreBw;
	}

	@Test
	public void testEmbedNetworkToNetwork() {
		// No guests before embedding anything
		assertTrue(((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().isEmpty());

		ModelFacade.getInstance().embedNetworkToNetwork("sub", "virt");

		assertFalse(((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().isEmpty());
		assertEquals("virt",
				((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().get(0).getName());
	}

	@Test
	public void testEmbedServerToServer() {
		ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "virt", 1, 1, 1, 0);

		ModelFacade.getInstance().embedServerToServer("1", "2");
		assertEquals(1, ((SubstrateServer) ModelFacade.getInstance().getServerById("1")).getGuestServers().size());
		assertEquals("1", ((VirtualServer) ModelFacade.getInstance().getServerById("2")).getHost().getName());
	}

	@Test
	public void testEmbedServerToServerRejectCpu() {
		ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "virt", 2, 1, 1, 0);

		assertThrows(UnsupportedOperationException.class, () -> {
			ModelFacade.getInstance().embedServerToServer("1", "2");
		});
	}

	@Test
	public void testEmbedServerToServerRejectMemory() {
		ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "virt", 1, 2, 1, 0);

		assertThrows(UnsupportedOperationException.class, () -> {
			ModelFacade.getInstance().embedServerToServer("1", "2");
		});
	}

	@Test
	public void testEmbedServerToServerRejectStorage() {
		ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "virt", 1, 1, 2, 0);

		assertThrows(UnsupportedOperationException.class, () -> {
			ModelFacade.getInstance().embedServerToServer("1", "2");
		});
	}

	@Test
	public void testEmbedSwitchtoSwitch() {
		ModelFacade.getInstance().addSwitchToNetwork("1", "sub", 0);
		ModelFacade.getInstance().addSwitchToNetwork("2", "virt", 0);

		ModelFacade.getInstance().embedSwitchToSwitch("1", "2");
		assertEquals(1, ((SubstrateSwitch) ModelFacade.getInstance().getSwitchById("1")).getGuestSwitches().size());
		assertEquals("1", ((VirtualSwitch) ModelFacade.getInstance().getSwitchById("2")).getHost().getName());
	}

	@Test
	public void testEmbedLinkBwIgnore() {
		// Set ignore bandwidth to true in ModelFacadeConfig.
		ModelFacadeConfig.IGNORE_BW = true;

		ModelFacade.getInstance().addServerToNetwork("srv1", "sub", 0, 0, 0, 0);
		ModelFacade.getInstance().addServerToNetwork("srv2", "sub", 0, 0, 0, 0);
		ModelFacade.getInstance().addLinkToNetwork("l3", "sub", 10, "srv1", "srv2");
		ModelFacade.getInstance().addLinkToNetwork("l4", "sub", 10, "srv2", "srv1");

		ModelFacade.getInstance().addServerToNetwork("srv5", "virt", 0, 0, 0, 0);
		ModelFacade.getInstance().addServerToNetwork("srv6", "virt", 0, 0, 0, 0);
		ModelFacade.getInstance().addLinkToNetwork("l7", "virt", 12, "srv5", "srv6");
		ModelFacade.getInstance().addLinkToNetwork("l8", "virt", 12, "srv6", "srv5");
		ModelFacade.getInstance().createAllPathsForNetwork("sub");

		final String pathName = "path-srv1-srv2";

		ModelFacade.getInstance().embedLinkToPath(pathName, "l7");

		final SubstratePath subPath = ModelFacade.getInstance().getPathById(pathName);
		assertEquals(1, subPath.getGuestLinks().size());
		assertEquals("l7", subPath.getGuestLinks().get(0).getName());

		// Reset configuration afterwards
		ModelFacadeConfig.IGNORE_BW = false;
	}

	@Test
	public void testEmbedMultipleNetworksToNetwork() {
		ModelFacade.getInstance().embedNetworkToNetwork("sub", "virt");

		ModelFacade.getInstance().addNetworkToRoot("virt_2", true);
		ModelFacade.getInstance().embedNetworkToNetwork("sub", "virt_2");

		assertFalse(((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().isEmpty());
		assertEquals(2, ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().size());
		assertEquals("virt",
				((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().get(0).getName());
		assertEquals("virt_2",
				((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().get(1).getName());
	}

	@Test
	public void testEmbedLinkToPathNormal() {
		ModelFacadeConfig.IGNORE_BW = false;

		// Substrate network
		final OneTierConfig subConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(subConfig);
		gen.createNetwork("net", false);
		assertFalse(ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());

		// Virtual elements
		ModelFacade.getInstance().addNetworkToRoot("v", true);
		ModelFacade.getInstance().addServerToNetwork("vsrv", "v", 1, 1, 1, 1);
		ModelFacade.getInstance().addSwitchToNetwork("vsw", "v", 0);
		ModelFacade.getInstance().addLinkToNetwork("vl", "v", 1, "vsrv", "vsw");

		final SubstratePath sp = ModelFacade.getInstance().getPathFromSourceToTarget("net_srv_0", "net_sw_0");
		assertTrue(ModelFacade.getInstance().embedLinkToPath(sp.getName(), "vl"));
		final VirtualLink guest = (VirtualLink) ModelFacade.getInstance().getLinkById("vl");
		assertEquals(sp, guest.getHost());
		assertEquals(guest, sp.getGuestLinks().get(0));
	}

	@Test
	public void testEmbedLinkToPathIgnoreBw() {
		// Set ignore bandwidth to true in ModelFacadeConfig.
		ModelFacadeConfig.IGNORE_BW = true;

		// Substrate network
		final OneTierConfig subConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(subConfig);
		gen.createNetwork("net", false);
		assertFalse(ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());

		// Virtual elements
		ModelFacade.getInstance().addNetworkToRoot("v", true);
		ModelFacade.getInstance().addServerToNetwork("vsrv", "v", 1, 1, 1, 1);
		ModelFacade.getInstance().addSwitchToNetwork("vsw", "v", 0);
		ModelFacade.getInstance().addLinkToNetwork("vl", "v", 10, "vsrv", "vsw");

		final SubstratePath sp = ModelFacade.getInstance().getPathFromSourceToTarget("net_srv_0", "net_sw_0");
		assertTrue(ModelFacade.getInstance().embedLinkToPath(sp.getName(), "vl"));
		final VirtualLink guest = (VirtualLink) ModelFacade.getInstance().getLinkById("vl");
		assertEquals(sp, guest.getHost());
		assertEquals(guest, sp.getGuestLinks().get(0));
	}

	@Test
	public void testRemoveNetworkEmbedding() {
		// Substrate network
		final OneTierConfig subConfig = new OneTierConfig(2, 1, false, 5, 5, 5, 10);
		final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(subConfig);
		gen.createNetwork("net", false);
		assertFalse(ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());

		// Virtual elements
		ModelFacade.getInstance().addNetworkToRoot("v", true);
		ModelFacade.getInstance().addServerToNetwork("vsrv1", "v", 1, 1, 1, 1);
		ModelFacade.getInstance().addServerToNetwork("vsrv2", "v", 1, 1, 1, 1);
		ModelFacade.getInstance().addSwitchToNetwork("vsw", "v", 0);
		ModelFacade.getInstance().addLinkToNetwork("vl1", "v", 1, "vsrv1", "vsw");
		ModelFacade.getInstance().addLinkToNetwork("vl2", "v", 1, "vsw", "vsrv1");
		ModelFacade.getInstance().addLinkToNetwork("vl3", "v", 1, "vsrv2", "vsw");
		ModelFacade.getInstance().addLinkToNetwork("vl4", "v", 1, "vsw", "vsrv2");

		ModelFacade.getInstance().embedNetworkToNetwork("net", "v");
		ModelFacade.getInstance().embedSwitchToSwitch("net_sw_0", "vsw");
		ModelFacade.getInstance().embedServerToServer("net_srv_0", "vsrv1");
		ModelFacade.getInstance().embedServerToServer("net_srv_1", "vsrv2");
		final SubstratePath path1 = ModelFacade.getInstance().getPathFromSourceToTarget("net_srv_0", "net_sw_0");
		final SubstratePath path2 = ModelFacade.getInstance().getPathFromSourceToTarget("net_sw_0", "net_srv_0");
		final SubstratePath path3 = ModelFacade.getInstance().getPathFromSourceToTarget("net_srv_1", "net_sw_0");
		final SubstratePath path4 = ModelFacade.getInstance().getPathFromSourceToTarget("net_sw_0", "net_srv_1");
		ModelFacade.getInstance().embedLinkToPath(path1.getName(), "vl1");
		ModelFacade.getInstance().embedLinkToPath(path2.getName(), "vl2");
		ModelFacade.getInstance().embedLinkToPath(path3.getName(), "vl3");
		ModelFacade.getInstance().embedLinkToPath(path4.getName(), "vl4");

		// Remove embedding
		ModelFacade.getInstance().removeNetworkEmbedding("v");

		// Check all virtual elements
		for (final Node n : ModelFacade.getInstance().getAllServersOfNetwork("v")) {
			final VirtualServer vsrv = (VirtualServer) n;
			assertNull(vsrv.getHost());
		}

		for (final Node n : ModelFacade.getInstance().getAllSwitchesOfNetwork("v")) {
			final VirtualSwitch vsw = (VirtualSwitch) n;
			assertNull(vsw.getHost());
		}

		for (final Link l : ModelFacade.getInstance().getAllLinksOfNetwork("v")) {
			final VirtualLink vl = (VirtualLink) l;
			assertNull(vl.getHost());
		}

		// Check all substrate elements
		for (final Node n : ModelFacade.getInstance().getAllServersOfNetwork("net")) {
			final SubstrateServer ssrv = (SubstrateServer) n;
			assertTrue(ssrv.getGuestServers().isEmpty());
			assertEquals(ssrv.getCpu(), ssrv.getResidualCpu());
			assertEquals(ssrv.getMemory(), ssrv.getResidualMemory());
			assertEquals(ssrv.getStorage(), ssrv.getResidualStorage());
		}

		for (final Node n : ModelFacade.getInstance().getAllSwitchesOfNetwork("net")) {
			final SubstrateSwitch ssw = (SubstrateSwitch) n;
			assertTrue(ssw.getGuestSwitches().isEmpty());
		}

		for (final Link l : ModelFacade.getInstance().getAllLinksOfNetwork("net")) {
			final SubstrateLink sl = (SubstrateLink) l;
			assertEquals(sl.getBandwidth(), sl.getResidualBandwidth());
		}

		for (final SubstratePath p : ModelFacade.getInstance().getAllPathsOfNetwork("net")) {
			final SubstratePath sp = p;
			assertTrue(sp.getGuestLinks().isEmpty());
			assertEquals(sp.getBandwidth(), sp.getResidualBandwidth());
		}
	}

	/*
	 * Negative tests.
	 */

	@Test
	public void testEmbedLinkToPathRejectBw() {
		ModelFacadeConfig.IGNORE_BW = false;

		// Substrate network
		final OneTierConfig subConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(subConfig);
		gen.createNetwork("net", false);
		assertFalse(ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());

		// Virtual elements
		ModelFacade.getInstance().addNetworkToRoot("v", true);
		ModelFacade.getInstance().addServerToNetwork("vsrv", "v", 1, 1, 1, 1);
		ModelFacade.getInstance().addSwitchToNetwork("vsw", "v", 0);
		ModelFacade.getInstance().addLinkToNetwork("vl", "v", 10, "vsrv", "vsw");

		final SubstratePath sp = ModelFacade.getInstance().getPathFromSourceToTarget("net_srv_0", "net_sw_0");

		assertThrows(UnsupportedOperationException.class, () -> {
			ModelFacade.getInstance().embedLinkToPath(sp.getName(), "vl");
		});
	}

	@Test
	public void testEmbedNetworkToNetworkVirtNotExist() {
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().embedNetworkToNetwork("sub", "aaa");
		});
	}

	@Test
	public void testEmbedNetworkToNetworkSubNotExist() {
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().embedNetworkToNetwork("aaa", "virt");
		});
	}

	@Test
	public void testEmbedNetworkToNetworkAlreadyEmbedded() {
		ModelFacade.getInstance().embedNetworkToNetwork("sub", "virt");
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().embedNetworkToNetwork("sub", "virt");
		});
	}

	@Test
	public void testRemoveNetworkEmbeddingRejectIdNotExists() {
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().removeNetworkEmbedding("42");
		});
	}

}
