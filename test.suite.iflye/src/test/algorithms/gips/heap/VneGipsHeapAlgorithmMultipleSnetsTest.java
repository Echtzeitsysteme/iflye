package test.algorithms.gips.heap;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsHeapAlgorithm;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.gips.VneGipsAlgorithmMultipleSnetsTest;

/**
 * Test class for the VNE GIPS heap algorithm implementation for multiple
 * substrate networks.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsHeapAlgorithmMultipleSnetsTest extends VneGipsAlgorithmMultipleSnetsTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		// The algorithm is only able to use the total communication objective C because
		// it is hard-coded in GIPSL
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		ModelFacadeConfig.IGNORE_BW = true;
		algo = VneGipsHeapAlgorithm.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		((VneGipsHeapAlgorithm) algo).dispose();
	}

}
