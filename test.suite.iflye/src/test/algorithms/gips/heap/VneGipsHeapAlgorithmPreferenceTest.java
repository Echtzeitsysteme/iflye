package test.algorithms.gips.heap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsHeapAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.VirtualServer;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the VNE GIPS heap algorithm implementation for the correct
 * server preference.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsHeapAlgorithmPreferenceTest extends AAlgorithmTest {

	/**
	 * Substrate network.
	 */
	protected SubstrateNetwork sNet;

	/**
	 * Virtual network.
	 */
	protected VirtualNetwork vNet;

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		// The algorithm is only able to use the total communication objective C because
		// it is hard-coded in GIPSL
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		algo = new VneGipsHeapAlgorithm();
		algo.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		((VneGipsHeapAlgorithm) algo).dispose();
	}

	//
	// Tests
	//

	// Multiple substrate elements

	@Test
	public void testTwoSrvs2MultipleSrvsPreference() {
		facade.addServerToNetwork("ssrv1", "sub", 2, 2, 2, 2);
		facade.addServerToNetwork("ssrv2", "sub", 2, 2, 2, 2);

		// first virtual network
		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		checkAndValidate();

		// second virtual network
		facade.addNetworkToRoot("virt2", true);
		facade.addServerToNetwork("vsrv2", "virt2", 1, 1, 1, 1);
		vNet = (VirtualNetwork) facade.getNetworkById("virt2");
		checkAndValidate();

		// both virtual servers must not be embedded onto the same substrate host
		final VirtualServer vsrv1 = (VirtualServer) facade.getServerById("vsrv1");
		final VirtualServer vsrv2 = (VirtualServer) facade.getServerById("vsrv2");
		assertNotNull(vsrv1);
		assertNotNull(vsrv2);
		assertNotNull(vsrv1.getHost());
		assertNotNull(vsrv2.getHost());
		assertNotEquals(vsrv1.getHost(), vsrv2.getHost());
	}

	//
	// Utility methods
	//

	private void checkAndValidate() {
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");

		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		// Get new network objects because the model was reloaded from file
		sNet = (SubstrateNetwork) facade.getNetworkById(sNet.getName());
		assertEquals(sNet, vNet.getHost());
		assertTrue(sNet.getGuests().contains(vNet));
		facade.validateModel();
	}

}
