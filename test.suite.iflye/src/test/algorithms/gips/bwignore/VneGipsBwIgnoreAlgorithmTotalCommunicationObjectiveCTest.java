package test.algorithms.gips.bwignore;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsBwIgnoreAlgorithm;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.gips.VneGipsAlgorithmTotalCommunicationObjectiveCTest;

/**
 * Test class for the VNE GIPS sequence algorithm implementation for minimizing
 * the total communication cost objective C.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsBwIgnoreAlgorithmTotalCommunicationObjectiveCTest
		extends VneGipsAlgorithmTotalCommunicationObjectiveCTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		ModelFacadeConfig.IGNORE_BW = true;
		algo = new VneGipsBwIgnoreAlgorithm();
		algo.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		if (algo != null) {
			((VneGipsBwIgnoreAlgorithm) algo).dispose();
		}
	}

	//
	// Tests
	//

	@Disabled
	public void testAllOnOneServer() {
	}

	@Disabled
	public void testPreferenceOfFilledServers() {
	}

	@Disabled
	public void testAllOnMultipleRacks() {
	}

}
