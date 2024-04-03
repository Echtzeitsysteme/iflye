package test.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.SubstratePath;

/**
 * Test class for the ModelFacade that tests some embedding tasks.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ModelFacadeIsEmbeddingPossibleGenericTest extends ModelFacadeEmbeddingTest {

	@Test
	public void testEmbedNetworkToNetwork() {
		// No guests before embedding anything
		assertTrue(((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().isEmpty());

		assertTrue(ModelFacade.getInstance().isEmbeddingPossibleGeneric("sub", "virt", false));
	}

	@Test
	public void testEmbedServerToServer() {
		ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "virt", 1, 1, 1, 0);

		assertTrue(ModelFacade.getInstance().isEmbeddingPossibleGeneric("1", "2", false));
	}

	@Test
	public void testEmbedServerToServerRejectCpu() {
		ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "virt", 2, 1, 1, 0);

		assertFalse(ModelFacade.getInstance().isEmbeddingPossibleGeneric("1", "2", false));
	}

	@Test
	public void testEmbedServerToServerRejectMemory() {
		ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "virt", 1, 2, 1, 0);

		assertFalse(ModelFacade.getInstance().isEmbeddingPossibleGeneric("1", "2", false));
	}

	@Test
	public void testEmbedServerToServerRejectStorage() {
		ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "virt", 1, 1, 2, 0);

		assertFalse(ModelFacade.getInstance().isEmbeddingPossibleGeneric("1", "2", false));
	}

	@Test
	public void testEmbedSwitchToServer() {
		ModelFacade.getInstance().addServerToNetwork("1", "sub", 0, 0, 0, 0);
		ModelFacade.getInstance().addSwitchToNetwork("2", "virt", 0);

		assertTrue(ModelFacade.getInstance().isEmbeddingPossibleGeneric("1", "2", false));
	}

	@Test
	public void testEmbedSwitchtoSwitch() {
		ModelFacade.getInstance().addSwitchToNetwork("1", "sub", 0);
		ModelFacade.getInstance().addSwitchToNetwork("2", "virt", 0);

		assertTrue(ModelFacade.getInstance().isEmbeddingPossibleGeneric("1", "2", false));
	}

	@Test
	public void testEmbedLinkToServer() {
		ModelFacade.getInstance().addServerToNetwork("1", "sub", 0, 0, 0, 0);

		ModelFacade.getInstance().addServerToNetwork("2", "virt", 0, 0, 0, 0);
		ModelFacade.getInstance().addServerToNetwork("3", "virt", 0, 0, 0, 0);
		ModelFacade.getInstance().addLinkToNetwork("4", "virt", 0, "2", "3");

		assertTrue(ModelFacade.getInstance().isEmbeddingPossibleGeneric("1", "2", false));
	}

	@Test
	public void testEmbedLinkBwIgnore() {
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

		assertTrue(ModelFacade.getInstance().isEmbeddingPossibleGeneric(pathName, "l7", true));
	}

	@Test
	public void testEmbedMultipleNetworksToNetwork() {
		ModelFacade.getInstance().embedNetworkToNetwork("sub", "virt");

		ModelFacade.getInstance().addNetworkToRoot("virt_2", true);
		assertTrue(ModelFacade.getInstance().isEmbeddingPossibleGeneric("sub", "virt_2", false));
	}

	@Test
	public void testEmbedLinkToPathNormal() {
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

		assertTrue(ModelFacade.getInstance().isEmbeddingPossibleGeneric(sp.getName(), "vl", false));
	}

	@Test
	public void testEmbedLinkToPathIgnoreBw() {
		// Set ignore bandwidth to true in ModelFacadeConfig.
		ModelFacadeConfig.IGNORE_BW = true;
		ModelFacadeConfig.LINK_HOST_EMBED_PATH = false;

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
		assertTrue(ModelFacade.getInstance().isEmbeddingPossibleGeneric(sp.getName(), "vl", true));
	}

	/*
	 * Negative tests.
	 */

	@Test
	public void testEmbedLinkToPathRejectBw() {

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

		assertFalse(ModelFacade.getInstance().isEmbeddingPossibleGeneric(sp.getName(), "vl", false));
	}

	@Test
	public void testEmbedNetworkToNetworkVirtNotExist() {
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().isEmbeddingPossibleGeneric("sub", "aaa", false);
		});
	}

	@Test
	public void testEmbedNetworkToNetworkSubNotExist() {
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().isEmbeddingPossibleGeneric("aaa", "virt", false);
		});
	}

	@Disabled
	@Test
	public void testEmbedNetworkToNetworkAlreadyEmbedded() {
		ModelFacade.getInstance().embedNetworkToNetwork("sub", "virt");
		assertFalse(ModelFacade.getInstance().isEmbeddingPossibleGeneric("sub", "virt", false));
	}

}
