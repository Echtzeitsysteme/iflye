package test.algorithms.fakeilp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Embedding;
import algorithms.AlgorithmConfig.Objective;
import algorithms.ilp.VneFakeIlpAlgorithm;
import model.SubstrateElement;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.VirtualServer;
import test.algorithms.generic.AAlgorithmMultipleVnsTest;

/**
 * Test class for the VNE fake ILP algorithm (incremental version)
 * implementation for minimizing the total communication cost objective C.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneFakeIlpAlgorithmTotalCommunicationObjectiveCTest extends AAlgorithmMultipleVnsTest {

	@AfterEach
	public void resetAlgo() {
		if (algo != null) {
			((VneFakeIlpAlgorithm) algo).dispose();
		}
	}

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		AlgorithmConfig.emb = Embedding.MANUAL;
		algo = VneFakeIlpAlgorithm.prepare(sNet, vNets);
	}

	/**
	 * Tests if the algorithm prefers using already filled up substrate servers.
	 */
	@Test
	public void testPreferenceOfFilledServers() {
		// Setup
		oneTierSetupThreeServers("sub", 4);
		oneTierSetupTwoServers("virt", 1);

		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		// Actual test starts here
		facade.addNetworkToRoot("virt2", true);
		oneTierSetupTwoServers("virt2", 1);
		final VirtualNetwork vNet2 = (VirtualNetwork) facade.getNetworkById("virt2");

		initAlgo(sNet, Set.of(vNet2));
		assertTrue(algo.execute());

		// Test expects that all virtual networks are placed on the same substrate
		// server
		final SubstrateElement ref = ((VirtualServer) vNet.getNodes().get(1)).getHost();

		vNet.getNodes().forEach(n -> {
			if (n instanceof VirtualServer) {
				final VirtualServer vsrv = (VirtualServer) n;
				assertEquals(ref, vsrv.getHost());
			}
		});

		vNet2.getNodes().forEach(n -> {
			if (n instanceof VirtualServer) {
				final VirtualServer vsrv = (VirtualServer) n;
				assertEquals(ref, vsrv.getHost());
			}
		});
	}

}
