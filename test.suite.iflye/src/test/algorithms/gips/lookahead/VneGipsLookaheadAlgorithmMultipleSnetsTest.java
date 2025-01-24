package test.algorithms.gips.lookahead;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsLookaheadAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.gips.VneGipsAlgorithmMultipleSnetsTest;

/**
 * Test class for the VNE GIPS look-ahead algorithm implementation for multiple
 * substrate networks.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsLookaheadAlgorithmMultipleSnetsTest extends VneGipsAlgorithmMultipleSnetsTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		// The algorithm is only able to use the total communication objective C because
		// it is hard-coded in GIPSL
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		algo = VneGipsLookaheadAlgorithm.prepare(sNet, vNets);
		((VneGipsLookaheadAlgorithm) algo).setVNetId("virt");
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		((VneGipsLookaheadAlgorithm) algo).dispose();
	}

}
