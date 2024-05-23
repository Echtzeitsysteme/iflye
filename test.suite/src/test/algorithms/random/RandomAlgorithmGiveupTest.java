package test.algorithms.random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

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
 * Test class for the random algorithm implementation. It should give up if
 * there is no embedding available.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class RandomAlgorithmGiveupTest extends AAlgorithmTest {

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
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	public void testServerImpossible() {
		oneTierSetupTwoServers("virt", 3);
		oneTierSetupTwoServers("sub", 2);
		ModelFacade.getInstance().createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));
		assertFalse(randomVne.execute());

		// Test all vServer hosts
		for (final Node n : ModelFacade.getInstance().getAllServersOfNetwork("virt")) {
			assertNull(((VirtualServer) n).getHost());
		}

		// Test all vSwitch hosts
		for (final Node n : ModelFacade.getInstance().getAllSwitchesOfNetwork("virt")) {
			assertNull(((VirtualSwitch) n).getHost());
		}

		// Test all vLink hosts
		for (final Link l : ModelFacade.getInstance().getAllLinksOfNetwork("virt")) {
			final VirtualLink vl = (VirtualLink) l;
			// This one host must be substrate server 1
			assertNull(vl.getHost());
		}
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

}
