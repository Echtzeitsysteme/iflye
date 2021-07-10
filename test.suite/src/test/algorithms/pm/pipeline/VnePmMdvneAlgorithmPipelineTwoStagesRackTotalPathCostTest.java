package test.algorithms.pm.pipeline;

import java.util.Set;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRack;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmTotalPathCostTest;

/**
 * Test class for the VNE pattern matching algorithm implementation for minimizing the total path
 * cost metric including the pipeline functionality.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesRackTotalPathCostTest
    extends VnePmMdvneAlgorithmTotalPathCostTest {

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
    algo = VnePmMdvneAlgorithmPipelineTwoStagesRack.prepare(sNet, vNets);
  }

}
