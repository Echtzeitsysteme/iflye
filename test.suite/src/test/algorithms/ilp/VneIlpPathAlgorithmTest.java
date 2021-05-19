package test.algorithms.ilp;

import java.util.Set;
import algorithms.ilp.VneIlpPathAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmTwoTierTest;

/**
 * Test class for the VNE ILP algorithm implementation.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VneIlpPathAlgorithmTest extends AAlgorithmTwoTierTest {

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final VirtualNetwork vNet) {
    algo = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
  }

}
