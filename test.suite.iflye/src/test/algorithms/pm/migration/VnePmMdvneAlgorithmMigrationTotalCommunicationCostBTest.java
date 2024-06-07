package test.algorithms.pm.migration;

import java.util.Set;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmMigration;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmTotalCommunicationCostBTest;

/**
 * Test class for the VNE PM MdVNE algorithm implementation for minimizing the
 * total communication cost metric B including the migration functionality.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmMigrationTotalCommunicationCostBTest
		extends VnePmMdvneAlgorithmTotalCommunicationCostBTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_B;
		algo = VnePmMdvneAlgorithmMigration.prepare(sNet, vNets);
	}

}
