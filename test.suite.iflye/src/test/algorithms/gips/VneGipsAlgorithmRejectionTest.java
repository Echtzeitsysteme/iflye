package test.algorithms.gips;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsAlgorithm;
import generators.OneTierNetworkGenerator;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import model.SubstrateNetwork;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the VNE GIPS algorithm implementation for rejecting VNs that
 * can not be embedded properly.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsAlgorithmRejectionTest extends AAlgorithmTest {

	/**
	 * Substrate network.
	 */
	SubstrateNetwork sNet;

	/**
	 * Virtual network.
	 */
	VirtualNetwork vNet;

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		// The algorithm is only able to use the total communication objective C because
		// it is hard-coded in GIPSL
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		algo = new VneGipsAlgorithm();
		algo.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		((VneGipsAlgorithm) algo).dispose();
	}

	//
	// Tests
	//

	@Test
	public void testOverallVnetTooLarge() {
		sNet = setUpSubNet(2, 2);
		vNet = setUpVirtNet(9);
		embedAndCheckReject();
	}

	@Test
	public void testOneServerInVnetTooLarge() {
		sNet = setUpSubNet(2, 2);
		vNet = setUpVirtNet(2);
		facade.addServerToNetwork("virt" + "_srv-x", "virt", 5, 5, 5, 1);
		facade.addLinkToNetwork("virt" + "_link-x1", "virt", 1, "virt_sw_0", "virt_srv-x");
		facade.addLinkToNetwork("virt" + "_link-x2", "virt", 1, "virt_srv-x", "virt_sw_0");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		embedAndCheckReject();
	}

	@Test
	public void testOneServerTooLarge() {
		sNet = setUpSubNet(2, 2);
		facade.addServerToNetwork("virt" + "_srv-x", "virt", 100, 100, 100, 0);
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		embedAndCheckReject();
	}

	@Test
	public void testAllLinksInVnetTooLarge() {
		sNet = setUpSubNet(2, 2);
		vNet = setUpVirtNet(4);
		for (int i = 0; i < 8; i++) {
			((VirtualLink) facade.getLinkById("virt_ln_" + i)).setBandwidth(100);
		}
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		embedAndCheckReject();
	}

	@Test
	public void testEmptySnet() {
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = setUpVirtNet(2);

		// Check test preconditions
		assertTrue(sNet.getNodess().isEmpty());
		assertTrue(sNet.getPaths().isEmpty());
		assertTrue(sNet.getLinks().isEmpty());

		embedAndCheckReject();
	}

	//
	// Utility methods
	//

	public void embedAndCheckReject() {
		initAlgo(sNet, Set.of(vNet));
		assertFalse(algo.execute());

		// Get new network objects because the model was reloaded from file
		sNet = (SubstrateNetwork) facade.getNetworkById(sNet.getName());
		vNet = (VirtualNetwork) facade.getNetworkById(vNet.getName());
		checkAllElementsNotEmbedded(sNet, Set.of(vNet));

		facade.validateModel();
	}

	private void checkAllElementsNotEmbedded(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		assertTrue(sNet.getGuests().isEmpty());
		sNet.getPaths().forEach(p -> {
			assertTrue(p.getGuestLinks().isEmpty());
		});
		sNet.getNodess().forEach(n -> {
			if (n instanceof SubstrateSwitch) {
				final SubstrateSwitch sw = (SubstrateSwitch) n;
				assertTrue(sw.getGuestSwitches().isEmpty());
			} else if (n instanceof SubstrateServer) {
				final SubstrateServer srv = (SubstrateServer) n;
				assertTrue(srv.getGuestLinks().isEmpty());
				assertTrue(srv.getGuestNetworks().isEmpty());
				assertTrue(srv.getGuestServers().isEmpty());
				assertTrue(srv.getGuestSwitches().isEmpty());
			}
		});

		for (final VirtualNetwork vNet : vNets) {
			assertNull(vNet.getHost());
			assertNull(vNet.getHostServer());
			vNet.getLinks().forEach(l -> {
				final VirtualLink vl = (VirtualLink) l;
				assertNull(vl.getHost());
			});
			vNet.getNodess().forEach(n -> {
				if (n instanceof VirtualSwitch) {
					final VirtualSwitch sw = (VirtualSwitch) n;
					assertNull(sw.getHost());
				} else if (n instanceof VirtualServer) {
					final VirtualServer srv = (VirtualServer) n;
					assertNull(srv.getHost());
				}
			});
		}
	}

	public SubstrateNetwork setUpSubNet(final int numberOfServersPerRack, final int numberOfRacks) {
		final TwoTierConfig config = new TwoTierConfig();
		config.setCoreBandwidth(10);
		config.setCoreSwitchesConnected(false);
		config.setNumberOfCoreSwitches(1);
		config.setNumberOfRacks(numberOfRacks);
		final OneTierConfig rack = new OneTierConfig(numberOfServersPerRack, 1, false, 2, 2, 2, 10);
		config.setRack(rack);
		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(config);
		gen.createNetwork("sub", false);
		facade.createAllPathsForNetwork("sub");
		return (SubstrateNetwork) facade.getNetworkById("sub");
	}

	public VirtualNetwork setUpVirtNet(final int numberOfServers) {
		final OneTierConfig config = new OneTierConfig(numberOfServers, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
		gen.createNetwork("virt", true);
		return (VirtualNetwork) facade.getNetworkById("virt");
	}

}
