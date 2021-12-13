package test.algorithms.pm.migration;

import java.util.Set;

import algorithms.pm.VnePmMdvneAlgorithmMigration;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmRepairModelNetworkTest;

/**
 * Test class for the VNE pattern matching algorithm migration implementation
 * for repairing a removed virtual network in the model. This test should
 * trigger the algorithm to repair the substrate network information after a
 * ungraceful removal of a previously embedded virtual network.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmMigrationRepairModelNetworkTest extends VnePmMdvneAlgorithmRepairModelNetworkTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		algo = VnePmMdvneAlgorithmMigration.prepare(sNet, vNets);
	}

}
