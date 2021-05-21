package test.algorithms.pm;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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

  @AfterEach
  public void resetAlgo() {
    if (algo != null) {
      ((VnePmMdvneAlgorithm) algo).dispose();
    }
  }

  @Test
  public void testMultipleNetworksAfterEachOther() {
    twoTierSetupFourServers("sub", 3);
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    for (int i = 0; i < 2; i++) {
      final String currVnetId = "virt" + i;
      facade.addNetworkToRoot(currVnetId, true);
      oneTierSetupTwoServers(currVnetId, 1);

      final VirtualNetwork currVnet = (VirtualNetwork) facade.getNetworkById(currVnetId);
      initAlgo(sNet, Set.of(currVnet));
      assertTrue(algo.execute());

      checkAllElementsEmbeddedOnSubstrateNetwork(sNet, Set.of(currVnet));
    }
  }

  @Test
  public void testMaxSizeVnet() {
    oneTierSetupTwoServers("sub", 2);
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    oneTierSetupTwoServers("virt", 2);
    final VirtualNetwork currVnet = (VirtualNetwork) facade.getNetworkById("virt");

    initAlgo(sNet, Set.of(currVnet));
    assertTrue(algo.execute());

    checkAllElementsEmbeddedOnSubstrateNetwork(sNet, Set.of(currVnet));
  }

}
