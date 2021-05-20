package test.algorithms.pm;

import java.util.Set;
import algorithms.pm.VnePmMdvneAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmMultipleVnsTest;

/**
 * Test class for the VNE pattern matching algorithm implementation.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmTest extends AAlgorithmMultipleVnsTest {

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    algo = VnePmMdvneAlgorithm.prepare(sNet, vNets);
  }

}
