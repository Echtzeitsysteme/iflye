package test.algorithms.pm.pipeline;

import java.util.Set;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackA;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmRepairModelServerTest;

/**
 * Test class for the VNE pattern matching algorithm pipeline implementation for repairing a removed
 * substrate server in the model. This test should trigger the algorithm to re-embed all virtual
 * networks that had elements placed on the substrate server removed.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesRackARepairModelServerTest
    extends VnePmMdvneAlgorithmRepairModelServerTest {

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    algo = VnePmMdvneAlgorithmPipelineTwoStagesRackA.prepare(sNet, vNets);
  }

}

