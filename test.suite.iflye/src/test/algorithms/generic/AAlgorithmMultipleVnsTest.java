package test.algorithms.generic;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Abstract test class with one and two tier based networks for the algorithm
 * implementations which also uses more than one virtual network at once.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public abstract class AAlgorithmMultipleVnsTest extends AAlgorithmTwoTierTest {

	@Test
	public void testTwoVnsAtOnceOneTier() {
		facade.addNetworkToRoot("virt2", true);
		oneTierSetupTwoServers("virt", 1);
		oneTierSetupTwoServers("virt2", 1);
		oneTierSetupTwoServers("sub", 2);
		facade.createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

		final Set<VirtualNetwork> vNets = new HashSet<>();

		vNets.add((VirtualNetwork) facade.getNetworkById("virt"));
		vNets.add((VirtualNetwork) facade.getNetworkById("virt2"));

		initAlgo(sNet, vNets);
		assertTrue(algo.execute());

		checkAllElementsEmbeddedOnSubstrateNetwork(sNet, vNets);
	}

	@Test
	public void testFourVnsAtOnceOneTier() {
		facade.addNetworkToRoot("virt2", true);
		facade.addNetworkToRoot("virt3", true);
		facade.addNetworkToRoot("virt4", true);
		oneTierSetupTwoServers("virt", 1);
		oneTierSetupTwoServers("virt2", 1);
		oneTierSetupTwoServers("virt3", 1);
		oneTierSetupTwoServers("virt4", 1);
		oneTierSetupTwoServers("sub", 4);
		facade.createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

		final Set<VirtualNetwork> vNets = new HashSet<>();

		vNets.add((VirtualNetwork) facade.getNetworkById("virt"));
		vNets.add((VirtualNetwork) facade.getNetworkById("virt2"));
		vNets.add((VirtualNetwork) facade.getNetworkById("virt3"));
		vNets.add((VirtualNetwork) facade.getNetworkById("virt4"));

		initAlgo(sNet, vNets);
		assertTrue(algo.execute());

		checkAllElementsEmbeddedOnSubstrateNetwork(sNet, vNets);
	}

	@Test
	public void testFourVnsAtOnceTwoTier() {
		facade.addNetworkToRoot("virt2", true);
		facade.addNetworkToRoot("virt3", true);
		facade.addNetworkToRoot("virt4", true);
		oneTierSetupTwoServers("virt", 2);
		oneTierSetupTwoServers("virt2", 2);
		oneTierSetupTwoServers("virt3", 2);
		oneTierSetupTwoServers("virt4", 2);
		twoTierSetupFourServers("sub", 8);
		facade.createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

		final Set<VirtualNetwork> vNets = new HashSet<>();

		vNets.add((VirtualNetwork) facade.getNetworkById("virt"));
		vNets.add((VirtualNetwork) facade.getNetworkById("virt2"));
		vNets.add((VirtualNetwork) facade.getNetworkById("virt3"));
		vNets.add((VirtualNetwork) facade.getNetworkById("virt4"));

		initAlgo(sNet, vNets);
		assertTrue(algo.execute());

		checkAllElementsEmbeddedOnSubstrateNetwork(sNet, vNets);
	}

	@Test
	public void testMultipleVnsExistButEmbedOnlyOne() {
		facade.addNetworkToRoot("virt2", true);
		oneTierSetupTwoServers("virt", 2);
		oneTierSetupTwoServers("virt2", 2);
		twoTierSetupFourServers("sub", 8);
		facade.createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		checkAllElementsEmbeddedOnSubstrateNetwork(sNet, Set.of(vNet));

		final VirtualNetwork vNet2 = (VirtualNetwork) facade.getNetworkById("virt2");
		assertNull(vNet2.getHost());
	}

}
