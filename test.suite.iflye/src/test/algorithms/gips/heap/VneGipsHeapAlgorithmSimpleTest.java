package test.algorithms.gips.heap;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsHeapAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.gips.VneGipsAlgorithmSimpleTest;

/**
 * Test class for the VNE GIPS heap algorithm implementation for simple checks
 * and debugging.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsHeapAlgorithmSimpleTest extends VneGipsAlgorithmSimpleTest {

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
		algo = VneGipsHeapAlgorithm.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		((VneGipsHeapAlgorithm) algo).dispose();
	}

}
