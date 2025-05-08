package test.algorithms.gips.bwignore;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsBwIgnoreAlgorithm;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.gips.VneGipsAlgorithmSimpleTest;

/**
 * Test class for the VNE GIPS algorithm implementation for simple checks and
 * debugging.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsBwIgnoreAlgorithmSimpleTest extends VneGipsAlgorithmSimpleTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		// The algorithm is only able to use the total communication objective C because
		// it is hard-coded in GIPSL
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		ModelFacadeConfig.IGNORE_BW = true;
		algo = new VneGipsBwIgnoreAlgorithm();
		algo.prepare(sNet, vNets);
	}

	@Override
	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		((VneGipsBwIgnoreAlgorithm) algo).dispose();
	}

}
