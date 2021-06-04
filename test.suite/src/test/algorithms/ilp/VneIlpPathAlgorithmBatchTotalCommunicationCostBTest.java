package test.algorithms.ilp;

import java.util.Set;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.ilp.VneIlpPathAlgorithmBatch;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Test class for the VNE ILP algorithm (batch version) implementation for minimizing the total
 * communication cost metric B.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VneIlpPathAlgorithmBatchTotalCommunicationCostBTest
    extends VneIlpPathAlgorithmTotalCommunicationCostBTest {

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_B;
    algo = new VneIlpPathAlgorithmBatch(sNet, vNets);
  }

}
