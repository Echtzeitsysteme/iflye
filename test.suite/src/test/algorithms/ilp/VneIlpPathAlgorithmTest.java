package test.algorithms.ilp;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
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
  public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    algo = new VneIlpPathAlgorithm(sNet, vNets);
  }

  @Test
  public void testTwoVnsAtOnceOneTier() {
    facade.addNetworkToRoot("virt2", true);
    oneTierSetupTwoServers("virt", 1);
    oneTierSetupTwoServers("virt2", 1);
    oneTierSetupTwoServers("sub", 2);
    facade.createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final Set<VirtualNetwork> vNets = new HashSet<VirtualNetwork>();

    vNets.add((VirtualNetwork) facade.getNetworkById("virt"));
    vNets.add((VirtualNetwork) facade.getNetworkById("virt2"));

    initAlgo(sNet, vNets);
    assertTrue(algo.execute());

    checkAllElementsEmbeddedOnSubstrateNetwork(sNet, vNets);
  }

  @Test
  public void testFourVnsAtOnceOneTier() {
    facade.addNetworkToRoot("virt2", true);
    facade.addNetworkToRoot("virt3", true);
    facade.addNetworkToRoot("virt4", true);
    oneTierSetupTwoServers("virt", 1);
    oneTierSetupTwoServers("virt2", 1);
    oneTierSetupTwoServers("virt3", 1);
    oneTierSetupTwoServers("virt4", 1);
    oneTierSetupTwoServers("sub", 4);
    facade.createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final Set<VirtualNetwork> vNets = new HashSet<VirtualNetwork>();

    vNets.add((VirtualNetwork) facade.getNetworkById("virt"));
    vNets.add((VirtualNetwork) facade.getNetworkById("virt2"));
    vNets.add((VirtualNetwork) facade.getNetworkById("virt3"));
    vNets.add((VirtualNetwork) facade.getNetworkById("virt4"));

    initAlgo(sNet, vNets);
    assertTrue(algo.execute());

    checkAllElementsEmbeddedOnSubstrateNetwork(sNet, vNets);
  }

  @Test
  public void testFourVnsAtOnceTwoTier() {
    facade.addNetworkToRoot("virt2", true);
    facade.addNetworkToRoot("virt3", true);
    facade.addNetworkToRoot("virt4", true);
    oneTierSetupTwoServers("virt", 2);
    oneTierSetupTwoServers("virt2", 2);
    oneTierSetupTwoServers("virt3", 2);
    oneTierSetupTwoServers("virt4", 2);
    twoTierSetupFourServers("sub", 8);
    facade.createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final Set<VirtualNetwork> vNets = new HashSet<VirtualNetwork>();

    vNets.add((VirtualNetwork) facade.getNetworkById("virt"));
    vNets.add((VirtualNetwork) facade.getNetworkById("virt2"));
    vNets.add((VirtualNetwork) facade.getNetworkById("virt3"));
    vNets.add((VirtualNetwork) facade.getNetworkById("virt4"));

    initAlgo(sNet, vNets);
    assertTrue(algo.execute());

    checkAllElementsEmbeddedOnSubstrateNetwork(sNet, vNets);
  }

}
