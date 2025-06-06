package test.algorithms.pm.migration;

import java.util.Set;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmMigration;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmTotalPathCostTest;

/**
 * Test class for the VNE pattern matching algorithm implementation for
 * minimizing the total path cost metric including the migration functionality.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmMigrationTotalPathCostTest extends VnePmMdvneAlgorithmTotalPathCostTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
		algo = new VnePmMdvneAlgorithmMigration();
		algo.prepare(sNet, vNets);
	}

}
