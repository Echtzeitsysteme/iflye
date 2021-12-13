package test.algorithms.pm;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import algorithms.AbstractAlgorithm;
import algorithms.ilp.VneIlpPathAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.VirtualServer;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the VNE pattern matching algorithm implementation for
 * repairing a removed substrate server in the model. This test should trigger
 * the algorithm to re-embed all virtual networks that had elements placed on
 * the substrate server removed.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmRepairModelServerTest extends AAlgorithmTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		algo = VnePmMdvneAlgorithm.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		if (algo != null) {
			((VnePmMdvneAlgorithm) algo).dispose();
		}
	}

	@Override
	@AfterEach
	public void restoreConfig() {
		// This method has to overwrite the method from super class, because we do not
		// want to validate
		// the model after each test. This is the case, because some tests intentionally
		// left the model
		// in an inconsistent state.
		// facade.validateModel();
		ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
		ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
		ModelFacadeConfig.IGNORE_BW = oldIgnoreBw;
	}

	/*
	 * Positive tests.
	 */

	@Test
	public void testConsistentModelAfterRepairOneServer() {
		// Setup
		VnePmMdvneAlgorithmRepairModelNetworkTest.setUpNetworks(5);
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		// Embed first virtual network with another algorithm
		final AbstractAlgorithm algoLoc = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
		assertTrue(algoLoc.execute());

		// Remove a used substrate server ungracefully
		final VirtualServer vsrvToRemoveHost = (VirtualServer) facade.getServerById("virt_srv_1");
		facade.removeSubstrateServerFromNetworkSimple(vsrvToRemoveHost.getHost().getName());

		// Create another virtual network to embed
		final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
		virtGen.createNetwork("virt2", true);
		final VirtualNetwork vNet2 = (VirtualNetwork) facade.getNetworkById("virt2");

		// Embed the second virtual network
		initAlgo(sNet, Set.of(vNet2));
		assertTrue(algo.execute());
		assertNotNull(((VirtualNetwork) facade.getNetworkById("virt")).getHost());
		assertNotNull(((VirtualNetwork) facade.getNetworkById("virt2")).getHost());

		// Validation must pass to show that the algorithm repaired the model of the
		// virtual network
		facade.validateModel();
	}

	@Test
	public void testConsistentModelAfterRepairTwoServers() {
		// Setup
		VnePmMdvneAlgorithmRepairModelNetworkTest.setUpNetworks(6);
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		// Embed first virtual network with another algorithm
		final AbstractAlgorithm algoLoc = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
		assertTrue(algoLoc.execute());

		// Remove a used substrate server ungracefully
		final VirtualServer vsrvToRemoveHost1 = (VirtualServer) facade.getServerById("virt_srv_0");
		facade.removeSubstrateServerFromNetworkSimple(vsrvToRemoveHost1.getHost().getName());
		final VirtualServer vsrvToRemoveHost2 = (VirtualServer) facade.getServerById("virt_srv_1");
		facade.removeSubstrateServerFromNetworkSimple(vsrvToRemoveHost2.getHost().getName());

		// Create another virtual network to embed
		final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
		virtGen.createNetwork("virt2", true);
		final VirtualNetwork vNet2 = (VirtualNetwork) facade.getNetworkById("virt2");

		// Embed the second virtual network
		initAlgo(sNet, Set.of(vNet2));
		assertTrue(algo.execute());
		assertNotNull(((VirtualNetwork) facade.getNetworkById("virt")).getHost());
		assertNotNull(((VirtualNetwork) facade.getNetworkById("virt2")).getHost());

		// Validation must pass to show that the algorithm repaired the model of the
		// virtual network
		facade.validateModel();
	}

	@Test
	public void testConsistentModelAfterRepairMultipleVns() {
		// Setup
		VnePmMdvneAlgorithmRepairModelNetworkTest.setUpNetworks(20);
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

		final Set<VirtualNetwork> vNets = new HashSet<>();
		final VirtualNetwork vNet = ((VirtualNetwork) facade.getNetworkById("virt"));
		vNets.add(vNet);
		AbstractAlgorithm algoLoc = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
		assertTrue(algoLoc.execute());

		// Create more virtual networks and embed them one by one (because it is faster)
		for (int i = 1; i <= 5; i++) {
			final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
			final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
			virtGen.createNetwork("virt" + i, true);
			final VirtualNetwork vNetI = (VirtualNetwork) facade.getNetworkById("virt" + i);
			vNets.add(vNetI);
			algoLoc = new VneIlpPathAlgorithm(sNet, Set.of(vNetI));
			assertTrue(algoLoc.execute());
		}

		// Embed first virtual network with another algorithm

		// Remove a used substrate server ungracefully for every virtual network
		for (int i = 1; i <= 5; i++) {
			final VirtualServer vsrvToRemoveHost = (VirtualServer) facade.getServerById("virt" + i + "_srv_0");
			facade.removeSubstrateServerFromNetworkSimple(vsrvToRemoveHost.getHost().getName());
		}

		// Create another virtual network to embed
		final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
		virtGen.createNetwork("virt99", true);
		final VirtualNetwork vNet99 = (VirtualNetwork) facade.getNetworkById("virt99");

		// Embed the second virtual network
		initAlgo(sNet, Set.of(vNet99));
		assertTrue(algo.execute());
		assertNotNull(((VirtualNetwork) facade.getNetworkById("virt99")).getHost());
		vNets.forEach(v -> {
			assertNotNull(v.getHost());
		});

		// Validation must pass to show that the algorithm repaired the model of the
		// virtual network
		facade.validateModel();
	}

	@Test
	public void testConsistentModelAfterRepairNotAllVnetsEmbedded() {
		// Setup
		VnePmMdvneAlgorithmRepairModelNetworkTest.setUpNetworks(20);
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		// Embed first virtual network with another algorithm
		final AbstractAlgorithm algoLoc = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
		assertTrue(algoLoc.execute());

		// Remove a used substrate server ungracefully
		final VirtualServer vsrvToRemoveHost = (VirtualServer) facade.getServerById("virt_srv_0");
		facade.removeSubstrateServerFromNetworkSimple(vsrvToRemoveHost.getHost().getName());

		// Create another virtual network to embed
		final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
		virtGen.createNetwork("virt2", true);
		virtGen.createNetwork("virt3", true);
		final VirtualNetwork vNet3 = (VirtualNetwork) facade.getNetworkById("virt3");

		// Embed the second virtual network
		initAlgo(sNet, Set.of(vNet3));
		assertTrue(algo.execute());
		assertNotNull(((VirtualNetwork) facade.getNetworkById("virt")).getHost());
		assertNull(((VirtualNetwork) facade.getNetworkById("virt2")).getHost());
		assertNotNull(((VirtualNetwork) facade.getNetworkById("virt3")).getHost());

		// Validation must pass to show that the algorithm repaired the model of the
		// virtual network
		facade.validateModel();
	}

	/*
	 * Negative tests.
	 */

	@Test
	public void testUnconsistentModelBefore() {
		VnePmMdvneAlgorithmRepairModelNetworkTest.setUpNetworks(4);
		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		final VneIlpPathAlgorithm algo = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		final VirtualServer vsrvToRemoveHost = (VirtualServer) facade.getServerById("virt_srv_1");
		facade.removeSubstrateServerFromNetworkSimple(vsrvToRemoveHost.getHost().getName());
		assertThrows(InternalError.class, () -> {
			facade.validateModel();
		});
	}

}
