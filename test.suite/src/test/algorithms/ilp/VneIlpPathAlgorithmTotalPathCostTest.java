package test.algorithms.ilp;

import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.ilp.VneIlpPathAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmMultipleVnsTest;

/**
 * Test class for the VNE ILP algorithm implementation for minimizing the total path cost metric.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VneIlpPathAlgorithmTotalPathCostTest extends AAlgorithmMultipleVnsTest {

  @AfterEach
  public void validateModel() {
    facade.validateModel();
  }

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
    algo = new VneIlpPathAlgorithm(sNet, vNets);
  }

}
