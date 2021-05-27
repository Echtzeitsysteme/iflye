package test.algorithms.pm;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import algorithms.pm.VnePmMdvneAlgorithm;
import facade.config.ModelFacadeConfig;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstratePath;
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
    facade.createAllPathsForNetwork("sub");
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
    oneTierSetupTwoServers("sub", 100);
    facade.createAllPathsForNetwork("sub");
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    oneTierSetupTwoServers("virt", 100);
    final VirtualNetwork currVnet = (VirtualNetwork) facade.getNetworkById("virt");

    initAlgo(sNet, Set.of(currVnet));
    assertTrue(algo.execute());

    checkAllElementsEmbeddedOnSubstrateNetwork(sNet, Set.of(currVnet));
  }

  @Test
  public void testPathLinkBandwidthDecrement() {
    oneTierSetupTwoServers("sub", 100);
    facade.createAllPathsForNetwork("sub");
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    oneTierSetupTwoServers("virt", 100);
    final VirtualNetwork currVnet = (VirtualNetwork) facade.getNetworkById("virt");

    initAlgo(sNet, Set.of(currVnet));
    assertTrue(algo.execute());

    sNet.getPaths().forEach(p -> {
      if (p.getHops() == 1) {
        // Path itself
        final SubstratePath sp = (SubstratePath) p;
        assertTrue(sp.getBandwidth() != sp.getResidualBandwidth());

        // Links
        sp.getLinks().forEach(l -> {
          final SubstrateLink sl = (SubstrateLink) l;
          assertTrue(sl.getBandwidth() != sl.getResidualBandwidth());
        });
      }
    });
  }

  /*
   * Negative tests
   */

  @Test
  public void testNoPathsInNetwork() {
    oneTierSetupTwoServers("sub", 1);
    // Removed path generation on purpose
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    oneTierSetupTwoServers("virt", 1);
    final VirtualNetwork currVnet = (VirtualNetwork) facade.getNetworkById("virt");

    assertThrows(UnsupportedOperationException.class, () -> {
      initAlgo(sNet, Set.of(currVnet));
    });
  }

  @Test
  public void testMinimumPathLength() {
    oneTierSetupTwoServers("sub", 1);
    ModelFacadeConfig.MIN_PATH_LENGTH = 2;
    facade.createAllPathsForNetwork("sub");
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    oneTierSetupTwoServers("virt", 1);
    final VirtualNetwork currVnet = (VirtualNetwork) facade.getNetworkById("virt");

    assertThrows(UnsupportedOperationException.class, () -> {
      initAlgo(sNet, Set.of(currVnet));
    });
  }

}
