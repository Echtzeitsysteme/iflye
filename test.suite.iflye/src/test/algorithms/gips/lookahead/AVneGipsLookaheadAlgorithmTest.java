package test.algorithms.gips.lookahead;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsLookaheadAlgorithm;
import facade.config.ModelFacadeConfig;
import model.Network;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the VNE GIPS lookahead algorithm implementation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class AVneGipsLookaheadAlgorithmTest extends AAlgorithmTest {

	/**
	 * Substrate network.
	 */
	protected SubstrateNetwork sNet;

	/**
	 * Virtual network.
	 */
	protected Set<VirtualNetwork> vNets;

	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets, final String vNetId) {
		// The algorithm is only able to use the total communication objective C because
		// it is hard-coded in GIPSL
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		ModelFacadeConfig.IGNORE_BW = true;
		algo = new VneGipsLookaheadAlgorithm();
		algo.prepare(sNet, vNets);
		((VneGipsLookaheadAlgorithm) algo).setVNetId(vNetId);
	}

	@Override
	public void initAlgo(SubstrateNetwork sNet, Set<VirtualNetwork> vNets) {
		initAlgo(sNet, vNets, null);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		if (algo != null) {
			((VneGipsLookaheadAlgorithm) algo).dispose();
		}
	}

	//
	// Utility methods
	//

	protected void checkAndValidate(final String vNetId) {
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNets = new HashSet<>();
		facade.getAllNetworks().forEach(n -> {
			if (n instanceof VirtualNetwork vn) {
				vNets.add(vn);
			}
		});

		initAlgo(sNet, vNets, vNetId);
		assertTrue(algo.execute());

		// Get new network objects because the model was reloaded from file
		sNet = (SubstrateNetwork) facade.getNetworkById(sNet.getName());
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById(vNetId);
		assertEquals(sNet, vNet.getHost());
		assertTrue(sNet.getGuests().contains(vNet));

		// Check that all virtual networks with other IDs than `vNetId` are not embedded
		for (final Network net : facade.getAllNetworks()) {
			if (net instanceof VirtualNetwork notVNet) {
				if (!vNet.getName().equals(vNetId)) {
					assertNull(notVNet.getHost());
					assertFalse(sNet.getGuests().contains(notVNet));
				}
			}
		}

		// Validate the overall model
		facade.validateModel();
	}

}
