package test.algorithms.pm;

import java.util.Set;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmUpdate;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Test class for the VNE PM MdVNE algorithm implementation for minimizing the total communication
 * cost metric C including the update functionality.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmUpdateTotalCommunicationCostCTest
    extends VnePmMdvneAlgorithmTotalCommunicationCostCTest {

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_C;
    algo = VnePmMdvneAlgorithmUpdate.prepare(sNet, vNets);
  }

}
