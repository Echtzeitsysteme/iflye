package test.algorithms.random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import algorithms.random.RandomVneAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.Link;
import model.Node;
import model.SubstrateNetwork;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the random algorithm implementation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class RandomAlgorithmTest extends AAlgorithmTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		algo = new RandomVneAlgorithm(sNet, vNets);
	}

	@BeforeEach
	public void setUp() {
		facade.resetAll();

		// Network setup
		ModelFacade.getInstance().addNetworkToRoot("sub", false);
		ModelFacade.getInstance().addNetworkToRoot("virt", true);

		// Normal model setup
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.IGNORE_BW = true;
	}

	@AfterEach
	public void validate() {
		ModelFacade.getInstance().validateModel();
	}

	/*
	 * Positive tests.
	 */

	@Test
	public void testAllOnOneServer() {
		oneTierSetupTwoServers("virt", 1);
		oneTierSetupTwoServers("sub", 2);
		ModelFacade.getInstance().createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));
		assertTrue(randomVne.execute());

		// Test all vServer hosts
		for (final Node n : ModelFacade.getInstance().getAllServersOfNetwork("virt")) {
			assertNotNull(((VirtualServer) n).getHost());
		}

		// Test all vSwitch hosts
		for (final Node n : ModelFacade.getInstance().getAllSwitchesOfNetwork("virt")) {
			assertNotNull(((VirtualSwitch) n).getHost());
		}

		// Test all vLink hosts
		for (final Link l : ModelFacade.getInstance().getAllLinksOfNetwork("virt")) {
			final VirtualLink vl = (VirtualLink) l;
			// This one host must be substrate server 1
			assertNotNull(vl.getHost());
		}
	}

	@Test
	public void testAllOnOneRack() {
		oneTierSetupTwoServers("virt", 2);
		oneTierSetupTwoServers("sub", 2);
		ModelFacade.getInstance().createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));
		assertTrue(randomVne.execute());

		// Test switch placement
		final VirtualSwitch virtSw = (VirtualSwitch) ModelFacade.getInstance().getSwitchById("virt_sw");
		assertNotNull(virtSw.getHost());

		// Test server placements
		final VirtualServer vSrv1 = (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv1");
		final VirtualServer vSrv2 = (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv2");
		assertNotNull(vSrv1.getHost());
		assertNotNull(vSrv2.getHost());

		// Test link placements
		final VirtualLink vLn1 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln1");
		final VirtualLink vLn2 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln2");
		final VirtualLink vLn3 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln3");
		final VirtualLink vLn4 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln4");

		// Link 1
		assertNotNull(vLn1.getHost());

		// Link 2
		assertNotNull(vLn2.getHost());

		// Link 3
		assertNotNull(vLn3.getHost());

		// Link 4
		assertNotNull(vLn4.getHost());
	}

	@Test
	public void testAllOnOneRackTwoTier() {
		oneTierSetupTwoServers("virt", 1);
		twoTierSetupFourServers("sub", 1);

		ModelFacade.getInstance().createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));
		assertTrue(randomVne.execute());

		// Test switch placement
		final VirtualSwitch virtSw = (VirtualSwitch) ModelFacade.getInstance().getSwitchById("virt_sw");
		assertNotNull(virtSw.getHost());

		// Test server placements
		final VirtualServer vSrv1 = (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv1");
		final VirtualServer vSrv2 = (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv2");
		assertNotNull(vSrv1.getHost());
		assertNotNull(vSrv2.getHost());

		// Test link placements
		final VirtualLink vLn1 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln1");
		final VirtualLink vLn2 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln2");
		final VirtualLink vLn3 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln3");
		final VirtualLink vLn4 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln4");

		// Link 1
		assertNotNull(vLn1.getHost());

		// Link 2
		assertNotNull(vLn2.getHost());

		// Link 3
		assertNotNull(vLn3.getHost());

		// Link 4
		assertNotNull(vLn4.getHost());
	}

	@Test
	public void testAllOnMultipleRacks() {
		oneTierSetupThreeServers("virt", 1);
		twoTierSetupFourServers("sub", 1);

		ModelFacade.getInstance().createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));
		assertTrue(randomVne.execute());

		// Test switch placement
		final VirtualSwitch virtSw = (VirtualSwitch) ModelFacade.getInstance().getSwitchById("virt_sw");
		assertNotNull(virtSw.getHost());

		// Test server placements
		final VirtualServer vSrv1 = (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv1");
		final VirtualServer vSrv2 = (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv2");
		final VirtualServer vSrv3 = (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv3");
		assertNotNull(vSrv1.getHost());
		assertNotNull(vSrv2.getHost());
		assertNotNull(vSrv3.getHost());

		// Test link placements
		final VirtualLink vLn1 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln1");
		final VirtualLink vLn2 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln2");
		final VirtualLink vLn3 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln3");
		final VirtualLink vLn4 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln4");
		final VirtualLink vLn5 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln5");
		final VirtualLink vLn6 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln6");

		// Link 1
		assertNotNull(vLn1.getHost());

		// Link 2
		assertNotNull(vLn2.getHost());

		// Link 3
		assertNotNull(vLn3.getHost());

		// Link 4
		assertNotNull(vLn4.getHost());

		// Link 5
		assertNotNull(vLn5.getHost());

		// Link 6
		assertNotNull(vLn6.getHost());
	}

	/*
	 * Negative tests.
	 */

	@Test
	public void testRejectIgnoreBandwidth() {
		ModelFacadeConfig.IGNORE_BW = false;

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		assertThrows(UnsupportedOperationException.class, () -> {
			new RandomVneAlgorithm(sNet, Set.of(vNet));
		});
	}

	@Test
	public void testRejectMinPathLength() {
		ModelFacadeConfig.MIN_PATH_LENGTH = 3;

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		assertThrows(UnsupportedOperationException.class, () -> {
			new RandomVneAlgorithm(sNet, Set.of(vNet));
		});
	}

	@Test
	public void testRejectNoSubPaths() {
		oneTierSetupTwoServers("virt", 2);
		oneTierSetupTwoServers("sub", 2);
		// Path generation removed intentionally

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		assertThrows(UnsupportedOperationException.class, () -> {
			new RandomVneAlgorithm(sNet, Set.of(vNet));
		});
	}

	@Test
	public void testNoEmbeddingWithSplittedVm() {
		oneTierSetupTwoServers("virt", 2);
		twoTierSetupFourServers("sub", 1);

		ModelFacade.getInstance().createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));

		// Embedding should not be possible, because a split of one VM to embed it on
		// two substrate servers is not possible although the total amount of resources
		// could handle the virtual network.
		assertFalse(randomVne.execute());
	}

	@Test
	public void testMultipleVnsAtOnce() {
		oneTierSetupTwoServers("virt", 2);
		twoTierSetupFourServers("sub", 1);

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		assertThrows(IllegalArgumentException.class, () -> {
			new RandomVneAlgorithm(sNet, Set.of(vNet, vNet));
		});
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
	private static void oneTierSetupTwoServers(final String networkId, final int slotsPerServer) {
		ModelFacade.getInstance().addSwitchToNetwork(networkId + "_sw", networkId, 0);
		ModelFacade.getInstance().addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer, slotsPerServer,
				slotsPerServer, 1);
		ModelFacade.getInstance().addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer, slotsPerServer,
				slotsPerServer, 1);
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln1", networkId, 1, networkId + "_srv1",
				networkId + "_sw");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln2", networkId, 1, networkId + "_srv2",
				networkId + "_sw");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln3", networkId, 1, networkId + "_sw",
				networkId + "_srv1");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln4", networkId, 1, networkId + "_sw",
				networkId + "_srv2");
	}

	/**
	 * Creates a one tier network with three servers and one switch.
	 *
	 * @param networkId      Network id.
	 * @param slotsPerServer Number of CPU, memory and storage resources.
	 */
	private static void oneTierSetupThreeServers(final String networkId, final int slotsPerServer) {
		ModelFacade.getInstance().addSwitchToNetwork(networkId + "_sw", networkId, 0);
		ModelFacade.getInstance().addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer, slotsPerServer,
				slotsPerServer, 1);
		ModelFacade.getInstance().addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer, slotsPerServer,
				slotsPerServer, 1);
		ModelFacade.getInstance().addServerToNetwork(networkId + "_srv3", networkId, slotsPerServer, slotsPerServer,
				slotsPerServer, 1);
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln1", networkId, 1, networkId + "_srv1",
				networkId + "_sw");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln2", networkId, 1, networkId + "_srv2",
				networkId + "_sw");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln3", networkId, 1, networkId + "_srv3",
				networkId + "_sw");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln4", networkId, 1, networkId + "_sw",
				networkId + "_srv1");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln5", networkId, 1, networkId + "_sw",
				networkId + "_srv2");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln6", networkId, 1, networkId + "_sw",
				networkId + "_srv3");
	}

	/**
	 * Creates a two tier network with four servers total, two rack switches, and
	 * one core switch.
	 *
	 * @param networkId      Network id.
	 * @param slotsPerServer Number of CPU, memory and storage resources.
	 */
	private static void twoTierSetupFourServers(final String networkId, final int slotsPerServer) {
		ModelFacade.getInstance().addSwitchToNetwork(networkId + "_csw1", networkId, 0);
		ModelFacade.getInstance().addSwitchToNetwork(networkId + "_rsw1", networkId, 1);
		ModelFacade.getInstance().addSwitchToNetwork(networkId + "_rsw2", networkId, 1);

		ModelFacade.getInstance().addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer, slotsPerServer,
				slotsPerServer, 2);
		ModelFacade.getInstance().addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer, slotsPerServer,
				slotsPerServer, 2);
		ModelFacade.getInstance().addServerToNetwork(networkId + "_srv3", networkId, slotsPerServer, slotsPerServer,
				slotsPerServer, 2);
		ModelFacade.getInstance().addServerToNetwork(networkId + "_srv4", networkId, slotsPerServer, slotsPerServer,
				slotsPerServer, 2);

		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln1", networkId, 0, networkId + "_srv1",
				networkId + "_rsw1");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln2", networkId, 0, networkId + "_srv2",
				networkId + "_rsw1");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln3", networkId, 0, networkId + "_rsw1",
				networkId + "_srv1");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln4", networkId, 0, networkId + "_rsw1",
				networkId + "_srv2");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln5", networkId, 0, networkId + "_srv3",
				networkId + "_rsw2");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln6", networkId, 0, networkId + "_srv4",
				networkId + "_rsw2");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln7", networkId, 0, networkId + "_rsw2",
				networkId + "_srv3");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln8", networkId, 0, networkId + "_rsw2",
				networkId + "_srv4");

		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln9", networkId, 0, networkId + "_rsw1",
				networkId + "_csw1");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln10", networkId, 0, networkId + "_rsw2",
				networkId + "_csw1");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln11", networkId, 0, networkId + "_csw1",
				networkId + "_rsw1");
		ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln12", networkId, 0, networkId + "_csw1",
				networkId + "_rsw2");
	}

}
