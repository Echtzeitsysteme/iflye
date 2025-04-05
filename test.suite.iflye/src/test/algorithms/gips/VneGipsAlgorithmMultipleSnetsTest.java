package test.algorithms.gips;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the VNE GIPS algorithm implementation for multiple substrate
 * networks.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsAlgorithmMultipleSnetsTest extends AAlgorithmTest {

	/**
	 * Substrate network A.
	 */
	protected SubstrateNetwork sNetA;

	/**
	 * Substrate network B.
	 */
	protected SubstrateNetwork sNetB;

	/**
	 * Virtual network.
	 */
	protected VirtualNetwork vNet;

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
	public void testNopEmbedding() {
		facade.addNetworkToRoot("sub2", false);
		checkAndValidate();
	}

	@Test
	public void testInitWithSnetNull() {
		facade.addNetworkToRoot("sub2", false);
		this.sNetA = null;
		this.sNetB = null;
		checkAndValidate();
	}

	@Test
	public void testOneSrv2SrvOnlyOneNetworkPossible() {
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		facade.addNetworkToRoot("sub2", false);
		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);
		sNetA = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		checkAndValidate();
	}

	@Test
	public void testOneSrv2SrvBothNetworksPossible() {
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		facade.addNetworkToRoot("sub2", false);
		facade.addServerToNetwork("ssrv2", "sub2", 1, 1, 1, 1);
		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);
		sNetA = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		checkAndValidate();
	}

	//
	// Utility methods
	//

	private void checkAndValidate() {
		sNetA = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");

		initAlgo(sNetA, Set.of(vNet));
		assertTrue(algo.execute());

		// Get new network objects because the model was reloaded from file
		sNetA = (SubstrateNetwork) facade.getNetworkById("sub");
		sNetB = (SubstrateNetwork) facade.getNetworkById("sub2");
		vNet = (VirtualNetwork) facade.getNetworkById(vNet.getName());

		// The virtual network must be embedded into one of both substrate networks
		assertNotNull(vNet.getHost());
		final boolean vNetOnSubA = vNet.getHost().equals(sNetA);
		final boolean vNetOnSubB = vNet.getHost().equals(sNetB);
		assertTrue(vNetOnSubA || vNetOnSubB);

		final boolean subAChosen = sNetA.getGuests().contains(vNet);
		final boolean subBChosen = sNetB.getGuests().contains(vNet);

		// Both networks must not be chosen
		assertFalse(subAChosen && subBChosen);

		// One of the networks must be chosen
		assertTrue(subAChosen || subBChosen);

		// Validation must also not fail
		facade.validateModel();
	}

}
