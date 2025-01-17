package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;

/**
 * Test class for the ModelFacade that tests some `getById` methods for invalid
 * values.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ModelFacadeGetByIdInvalidTest {

	/**
	 * ModelFacade instance to use.
	 */
	private ModelFacade mf = null;

	/**
	 * ID that does not exist within the model.
	 */
	final String id = "testThisDoesNotExist";

	@BeforeEach
	public void resetModel() {
		this.mf = ModelFacade.getInstance();
		mf.resetAll();
		mf.addNetworkToRoot("sub", false);
	}

	@Test
	public void testGetAllServersOfNetwork() {
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getAllServersOfNetwork(id));
	}

	@Test
	public void testGetAllSwitchesOfNetwork() {
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getAllSwitchesOfNetwork(id));
	}

	@Test
	public void testGetAllLinksOfNetwork() {
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getAllLinksOfNetwork(id));
	}

	@Test
	public void testGetAllPathsOfNetwork() {
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getAllPathsOfNetwork(id));
	}

	@Test
	public void testGetServerById() {
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getServerById(id));
	}

	@Test
	public void testGetSwitchById() {
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getSwitchById(id));
	}

	@Test
	public void testGetNodeById() {
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getNodeById(id));
	}

	@Test
	public void testGetLinkById() {
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getLinkById(id));
	}

	@Test
	public void testGetPathById() {
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getPathById(id));
	}

	@Test
	public void testGetNetworkId() {
		assertEquals(1, mf.getAllNetworks().size());
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getNetworkById(id));
	}

	@Test
	public void testAddServerToNetworkId() {
		assertEquals(1, mf.getAllNetworks().size());
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.addServerToNetwork("testServer", id, 1, 1, 1, 1));
	}

	@Test
	public void testAddSwitchToNetworkId() {
		assertEquals(1, mf.getAllNetworks().size());
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.addSwitchToNetwork("testSwitch", id, 1));
	}

	@Test
	public void testAddLinkToNetworkIdNetworkNotExistent() {
		assertEquals(1, mf.getAllNetworks().size());
		assertThrowsExactly(IllegalArgumentException.class,
				() -> mf.addLinkToNetwork("testLink", id, 1, "source", "target"));
	}

	@Test
	public void testAddLinkToNetworkIdSourceNotExistent() {
		mf.addServerToNetwork("target", "sub", 1, 1, 1, 1);
		assertEquals(1, mf.getAllNetworks().size());
		assertThrowsExactly(IllegalArgumentException.class,
				() -> mf.addLinkToNetwork("testLink", id, 1, "source", "target"));
	}

	@Test
	public void testAddLinkToNetworkIdTargetNotExistent() {
		mf.addServerToNetwork("source", "sub", 1, 1, 1, 1);
		assertEquals(1, mf.getAllNetworks().size());
		assertThrowsExactly(IllegalArgumentException.class,
				() -> mf.addLinkToNetwork("testLink", id, 1, "source", "target"));
	}

	@Test
	public void testCreateAllPathsForNetwork() {
		assertEquals(1, mf.getAllNetworks().size());
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.createAllPathsForNetwork(id));
	}

	@Test
	public void testDoesNodeIdExist() {
		assertEquals(1, mf.getAllNetworks().size());
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.doesNodeIdExist("testNode", id));
	}

	@Test
	public void testDoesLinkIdExist() {
		assertEquals(1, mf.getAllNetworks().size());
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.doesLinkIdExist("testNode", id));
	}

	@Test
	public void testGetPathFromSourceToTargetIdSourceNotExistent() {
		assertEquals(1, mf.getAllNetworks().size());

		// Extra setup
		mf.addServerToNetwork("target", "sub", 1, 1, 1, 1);
		mf.addServerToNetwork("notSource", "sub", 1, 1, 1, 1);
		mf.addLinkToNetwork("l1", "sub", 1, "notSource", "target");
		mf.addLinkToNetwork("l2", "sub", 1, "target", "notSource");
		mf.createAllPathsForNetwork("sub");
		assertEquals(2, mf.getAllPathsOfNetwork("sub").size());

		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getPathFromSourceToTarget("source", "target"));
	}

	@Test
	public void testGetPathFromSourceToTargetIdTargetNotExistent() {
		assertEquals(1, mf.getAllNetworks().size());

		// Extra setup
		mf.addServerToNetwork("notTarget", "sub", 1, 1, 1, 1);
		mf.addServerToNetwork("source", "sub", 1, 1, 1, 1);
		mf.addLinkToNetwork("l1", "sub", 1, "source", "notTarget");
		mf.addLinkToNetwork("l2", "sub", 1, "notTarget", "source");
		mf.createAllPathsForNetwork("sub");
		assertEquals(2, mf.getAllPathsOfNetwork("sub").size());

		assertThrowsExactly(IllegalArgumentException.class, () -> mf.getPathFromSourceToTarget("source", "target"));
	}

	@Test
	public void testEmbedServerToServerVdoesNotExist() {
		assertEquals(1, mf.getAllNetworks().size());
		mf.addServerToNetwork("ssrv", "sub", 1, 1, 1, 1);
		mf.addNetworkToRoot("virt", true);
		assertEquals(2, mf.getAllNetworks().size());
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.embedServerToServer("ssrv", "vsrv"));
	}

	@Test
	public void testEmbedServerToServerSdoesNotExist() {
		assertEquals(1, mf.getAllNetworks().size());
		mf.addNetworkToRoot("virt", true);
		mf.addServerToNetwork("vsrv", "virt", 1, 1, 1, 1);
		assertEquals(2, mf.getAllNetworks().size());

		assertThrowsExactly(IllegalArgumentException.class, () -> mf.embedServerToServer("ssrv", "vsrv"));
	}

	@Test
	public void testEmbedSwitchToNodeVdoesNotExist() {
		assertEquals(1, mf.getAllNetworks().size());
		mf.addSwitchToNetwork("ssw", "sub", 0);
		mf.addNetworkToRoot("virt", true);
		assertEquals(2, mf.getAllNetworks().size());

		assertThrowsExactly(IllegalArgumentException.class, () -> mf.embedSwitchToNode("vsw", "ssw"));
	}

	@Test
	public void testEmbedSwitchToNodeSdoesNotExist() {
		assertEquals(1, mf.getAllNetworks().size());
		mf.addNetworkToRoot("virt", true);
		mf.addSwitchToNetwork("vsw", "virt", 0);
		assertEquals(2, mf.getAllNetworks().size());

		assertThrowsExactly(IllegalArgumentException.class, () -> mf.embedSwitchToNode("ssw", "vsw"));
	}

	@Test
	public void testEmbedLinkToServerVdoesNotExist() {
		assertEquals(1, mf.getAllNetworks().size());
		mf.addServerToNetwork("ssrv", "sub", 1, 1, 1, 1);
		mf.addNetworkToRoot("virt", true);
		assertEquals(2, mf.getAllNetworks().size());

		assertThrowsExactly(IllegalArgumentException.class, () -> mf.embedLinkToServer("ssrv", "vl"));
	}

	@Test
	public void testEmbedLinkToServerSdoesNotExist() {
		assertEquals(1, mf.getAllNetworks().size());
		mf.addNetworkToRoot("virt", true);
		mf.addServerToNetwork("vsrv1", "virt", 0, 0, 0, 0);
		mf.addServerToNetwork("vsrv2", "virt", 0, 0, 0, 0);
		mf.addLinkToNetwork("vl", "virt", 0, "vsrv1", "vsrv2");
		assertEquals(2, mf.getAllNetworks().size());

		assertThrowsExactly(IllegalArgumentException.class, () -> mf.embedLinkToServer("ssrv", "vl"));
	}

	@Test
	public void testEmbedLinkToPathVdoesNotExist() {
		assertEquals(1, mf.getAllNetworks().size());
		mf.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		mf.addServerToNetwork("ssrv2", "sub", 0, 0, 0, 0);
		mf.addLinkToNetwork("sl1", "sub", 0, "ssrv1", "ssrv2");
		mf.addLinkToNetwork("sl2", "sub", 0, "ssrv2", "ssrv1");
		mf.createAllPathsForNetwork("sub");
		assertEquals(2, mf.getAllPathsOfNetwork("sub").size());
		final String pathName = mf.getAllPathsOfNetwork("sub").get(0).getName();
		mf.addNetworkToRoot("virt", true);
		assertEquals(2, mf.getAllNetworks().size());

		assertThrowsExactly(IllegalArgumentException.class, () -> mf.embedLinkToPath(pathName, "vl"));
	}

	@Test
	public void testUpdateAllPathsResidualBandwidth() {
		assertEquals(1, mf.getAllNetworks().size());
		assertThrowsExactly(IllegalArgumentException.class, () -> mf.updateAllPathsResidualBandwidth("notSub"));
	}

}
