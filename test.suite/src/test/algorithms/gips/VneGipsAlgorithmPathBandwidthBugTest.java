package test.algorithms.gips;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsAlgorithm;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the VNE GIPS algorithm implementation to trigger the minimum
 * path/link bandwidth bug. This test is based on a scenario created by Marco
 * Volle in his master's thesis.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
//@Disabled
public class VneGipsAlgorithmPathBandwidthBugTest extends AAlgorithmTest {

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
		algo = VneGipsAlgorithm.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		if (algo != null) {
			((VneGipsAlgorithm) algo).dispose();
		}
	}

	@BeforeEach
	public void resetModel() {
		facade.resetAll();
	}

	//
	// Tests
	//

	@Test
	public void testScenario34() {
		// Load the model file
		facade.loadModel("resources/triggerPathResidualBwBug.xmi");
		assertNotNull(facade.getNetworkById("sub"));
		assertFalse(facade.getAllPathsOfNetwork("sub").isEmpty());

		assertThrows(InternalError.class, () -> {
			facade.validateModel();
		});

		facade.updateAllPathsResidualBandwidth("sub");

		// validation must not fail before the embedding
		facade.validateModel();

		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("v4");

		// Sanity check
		assertNotNull(sNet);
		assertNotNull(vNet);
		assertNull(vNet.getHost());

		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		// validation must not fail
		facade.validateModel();
	}

}
