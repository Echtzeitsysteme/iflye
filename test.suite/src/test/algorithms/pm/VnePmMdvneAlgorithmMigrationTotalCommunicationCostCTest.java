package test.algorithms.pm;

import java.util.Set;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmMigration;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Test class for the VNE PM MdVNE algorithm implementation for minimizing the total communication
 * cost metric C including the migration functionality.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmMigrationTotalCommunicationCostCTest
    extends VnePmMdvneAlgorithmTotalCommunicationCostCTest {

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_C;
    algo = VnePmMdvneAlgorithmMigration.prepare(sNet, vNets);
  }

}
