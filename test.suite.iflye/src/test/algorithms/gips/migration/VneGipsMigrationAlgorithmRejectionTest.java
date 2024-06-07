package test.algorithms.gips.migration;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsMigrationAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.gips.VneGipsAlgorithmRejectionTest;

/**
 * Test class for the VNE GIPS algorithm with enabled migration implementation
 * for rejecting VNs that can not be embedded properly.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsMigrationAlgorithmRejectionTest extends VneGipsAlgorithmRejectionTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		// The algorithm is only able to use the total communication objective C because
		// it is hard-coded in GIPSL
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		algo = VneGipsMigrationAlgorithm.prepare(sNet, vNets);
	}

	@Override
	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		((VneGipsMigrationAlgorithm) algo).dispose();
	}

}
