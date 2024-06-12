package test.algorithms.gips.bwignore;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsBwIgnoreAlgorithm;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.VirtualLink;
import model.VirtualNetwork;
import test.algorithms.gips.VneGipsAlgorithmRejectionTest;

/**
 * Test class for the VNE GIPS bandwidth ignore algorithm implementation for
 * rejecting VNs that can not be embedded properly.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsBwIgnoreAlgorithmRejectionTest extends VneGipsAlgorithmRejectionTest {

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
		ModelFacadeConfig.IGNORE_BW = true;
		algo = VneGipsBwIgnoreAlgorithm.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		((VneGipsBwIgnoreAlgorithm) algo).dispose();
	}

	//
	// Tests
	//

	@Test
	@Override
	public void testAllLinksInVnetTooLarge() {
		sNet = setUpSubNet(2, 2);
		vNet = setUpVirtNet(4);
		for (int i = 0; i < 8; i++) {
			((VirtualLink) facade.getLinkById("virt_ln_" + i)).setBandwidth(100);
		}
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		embedAndCheckNoReject();
	}

	//
	// Utilities
	//

	public void embedAndCheckNoReject() {
		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		facade.validateModel();
	}

}
