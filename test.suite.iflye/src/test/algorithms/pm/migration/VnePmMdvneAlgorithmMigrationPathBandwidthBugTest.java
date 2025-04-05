package test.algorithms.pm.migration;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmMigration;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AVneAlgorithmPathBandwidthBugTest;

/**
 * Test class to trigger the minimum path/link bandwidth bug.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmMigrationPathBandwidthBugTest extends AVneAlgorithmPathBandwidthBugTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		// The algorithm is only able to use the total communication objective C because
		// it is hard-coded in GIPSL
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		algo = new VnePmMdvneAlgorithmMigration();
		algo.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		if (algo != null) {
			((VnePmMdvneAlgorithmMigration) algo).dispose();
		}
	}

}
