package test.algorithms.ilp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.Set;
import org.junit.jupiter.api.Test;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.ilp.VneIlpPathAlgorithm;
import model.Path;
import model.Server;
import model.SubstrateNetwork;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;
import test.algorithms.generic.AAlgorithmMultipleVnsTest;

/**
 * Test class for the VNE ILP algorithm (incremental version) implementation for minimizing the
 * total communication cost metric B.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VneIlpPathAlgorithmTotalCommunicationCostBTest extends AAlgorithmMultipleVnsTest {

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_B;
    algo = new VneIlpPathAlgorithm(sNet, vNets);
  }

  /**
   * This test has to be overwritten, because of the fact that this metric drives the algorithm to
   * place the switch on a virtual server, to.
   */
  @Override
  @Test
  public void testAllOnOneRack() {
    oneTierSetupTwoServers("virt", 2);
    oneTierSetupTwoServers("sub", 2);
    facade.createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

    initAlgo(sNet, Set.of(vNet));
    assertTrue(algo.execute());

    // Test switch placement
    final VirtualSwitch virtSw = (VirtualSwitch) facade.getSwitchById("virt_sw");
    assertEquals("sub_srv2", virtSw.getHost().getName());

    // Test server placements
    final VirtualServer vSrv1 = (VirtualServer) facade.getServerById("virt_srv1");
    final VirtualServer vSrv2 = (VirtualServer) facade.getServerById("virt_srv2");

    // Both virtual servers have to be embedded on other substrate servers
    if (vSrv1.getHost().equals(vSrv2.getHost())) {
      fail();
    }

    // Get reference host for later checks of links
    final String refHost1 = vSrv1.getHost().getName();

    // Test link placements
    final VirtualLink vLn1 = (VirtualLink) facade.getLinkById("virt_ln1");
    final VirtualLink vLn2 = (VirtualLink) facade.getLinkById("virt_ln2");
    final VirtualLink vLn3 = (VirtualLink) facade.getLinkById("virt_ln3");
    final VirtualLink vLn4 = (VirtualLink) facade.getLinkById("virt_ln4");

    String sourceName = "";
    String targetName = "";

    // Link 1
    if (vLn1.getHost() instanceof Path) {
      final Path pLn1 = (Path) vLn1.getHost();
      sourceName = pLn1.getSource().getName();
      targetName = pLn1.getTarget().getName();
    } else {
      fail();
    }

    assertEquals(refHost1, sourceName);
    assertEquals("sub_srv2", targetName);

    // Link 2
    if (!(vLn2.getHost() instanceof Server)) {
      fail();
    }

    assertEquals(refHost1, sourceName);
    assertEquals("sub_srv2", targetName);

    // Link 3
    if (vLn3.getHost() instanceof Path) {
      final Path pLn3 = (Path) vLn3.getHost();
      sourceName = pLn3.getSource().getName();
      targetName = pLn3.getTarget().getName();
    } else {
      fail();
    }

    assertEquals("sub_srv2", sourceName);
    assertEquals(refHost1, targetName);

    // Link 4
    if (!(vLn4.getHost() instanceof Server)) {
      fail();
    }

    assertEquals("sub_srv2", sourceName);
    assertEquals(refHost1, targetName);
  }

  /**
   * This test has to be overwritten, because of the fact that this metric drives the algorithm to
   * place the switch not necessarily on the core switch.
   */
  @Override
  @Test
  public void testAllOnMultipleRacks() {
    oneTierSetupThreeServers("virt", 1);
    twoTierSetupFourServers("sub", 1);

    facade.createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

    initAlgo(sNet, Set.of(vNet));
    assertTrue(algo.execute());

    // Test switch placement
    final VirtualSwitch virtSw = (VirtualSwitch) facade.getSwitchById("virt_sw");
    final String refSwHostName = virtSw.getHost().getName();
    assertEquals(1, virtSw.getHost().getDepth());

    // Test server placements
    final VirtualServer vSrv1 = (VirtualServer) facade.getServerById("virt_srv1");
    final VirtualServer vSrv2 = (VirtualServer) facade.getServerById("virt_srv2");
    final VirtualServer vSrv3 = (VirtualServer) facade.getServerById("virt_srv3");
    final String serverHost1 = vSrv1.getHost().getName();
    final String serverHost2 = vSrv2.getHost().getName();
    final String serverHost3 = vSrv3.getHost().getName();

    assertNotEquals(serverHost1, serverHost2);
    assertNotEquals(serverHost1, serverHost3);
    assertNotEquals(serverHost2, serverHost3);

    // Test link placements
    final VirtualLink vLn1 = (VirtualLink) facade.getLinkById("virt_ln1");
    final VirtualLink vLn2 = (VirtualLink) facade.getLinkById("virt_ln2");
    final VirtualLink vLn3 = (VirtualLink) facade.getLinkById("virt_ln3");
    final VirtualLink vLn4 = (VirtualLink) facade.getLinkById("virt_ln4");
    final VirtualLink vLn5 = (VirtualLink) facade.getLinkById("virt_ln5");
    final VirtualLink vLn6 = (VirtualLink) facade.getLinkById("virt_ln6");

    // Link 1
    final Path pLn1 = (Path) vLn1.getHost();
    assertEquals(serverHost1, pLn1.getSource().getName());
    assertEquals(refSwHostName, pLn1.getTarget().getName());

    // Link 2
    final Path pLn2 = (Path) vLn2.getHost();
    assertEquals(serverHost2, pLn2.getSource().getName());
    assertEquals(refSwHostName, pLn2.getTarget().getName());

    // Link 3
    final Path pLn3 = (Path) vLn3.getHost();
    assertEquals(serverHost3, pLn3.getSource().getName());
    assertEquals(refSwHostName, pLn3.getTarget().getName());

    // Link 4
    final Path pLn4 = (Path) vLn4.getHost();
    assertEquals(refSwHostName, pLn4.getSource().getName());
    assertEquals(serverHost1, pLn4.getTarget().getName());

    // Link 5
    final Path pLn5 = (Path) vLn5.getHost();
    assertEquals(refSwHostName, pLn5.getSource().getName());
    assertEquals(serverHost2, pLn5.getTarget().getName());

    // Link 6
    final Path pLn6 = (Path) vLn6.getHost();
    assertEquals(refSwHostName, pLn6.getSource().getName());
    assertEquals(serverHost3, pLn6.getTarget().getName());
  }

}
