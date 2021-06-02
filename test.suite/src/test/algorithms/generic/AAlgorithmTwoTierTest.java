package test.algorithms.generic;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.Set;
import org.junit.jupiter.api.Test;
import model.Link;
import model.Node;
import model.Path;
import model.SubstrateNetwork;
import model.SubstrateServer;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Abstract test class with one and two tier based networks for the algorithm implementations.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public abstract class AAlgorithmTwoTierTest extends AAlgorithmTest {

  /*
   * Positive tests.
   */

  @Test
  public void testAllOnOneServer() {
    AlgorithmTestHelper.oneTierSetupTwoServers("virt", 1);
    AlgorithmTestHelper.oneTierSetupTwoServers("sub", 2);
    facade.createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

    initAlgo(sNet, Set.of(vNet));
    assertTrue(algo.execute());

    SubstrateServer host = null;

    // Test all vServer hosts
    for (final Node n : facade.getAllServersOfNetwork("virt")) {
      // Initialize the host of the first virtual element to check
      if (host == null) {
        host = ((VirtualServer) n).getHost();
      }

      assertEquals(host, ((VirtualServer) n).getHost());
    }

    // Test all vSwitch hosts
    for (final Node n : facade.getAllSwitchesOfNetwork("virt")) {
      assertEquals(host, ((VirtualSwitch) n).getHost());
    }

    // Test all vLink hosts
    for (final Link l : facade.getAllLinksOfNetwork("virt")) {
      final VirtualLink vl = (VirtualLink) l;
      // This one host must be substrate server 1
      assertEquals(host, vl.getHost());
    }
  }

  @Test
  public void testAllOnOneRack() {
    AlgorithmTestHelper.oneTierSetupTwoServers("virt", 2);
    AlgorithmTestHelper.oneTierSetupTwoServers("sub", 2);
    facade.createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

    initAlgo(sNet, Set.of(vNet));
    assertTrue(algo.execute());

    // Test switch placement
    final VirtualSwitch virtSw = (VirtualSwitch) facade.getSwitchById("virt_sw");
    assertEquals("sub_sw", virtSw.getHost().getName());

    // Test server placements
    final VirtualServer vSrv1 = (VirtualServer) facade.getServerById("virt_srv1");
    final VirtualServer vSrv2 = (VirtualServer) facade.getServerById("virt_srv2");

    // Both virtual servers have to be embedded on other substrate servers
    if (vSrv1.getHost().equals(vSrv2.getHost())) {
      fail();
    }

    // Get reference hosts for later checks of links
    final String refHost1 = vSrv1.getHost().getName();
    final String refHost2 = vSrv2.getHost().getName();

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
    assertEquals("sub_sw", targetName);

    // Link 2
    if (vLn2.getHost() instanceof Path) {
      final Path pLn2 = (Path) vLn2.getHost();
      sourceName = pLn2.getSource().getName();
      targetName = pLn2.getTarget().getName();
    } else {
      fail();
    }

    assertEquals(refHost2, sourceName);
    assertEquals("sub_sw", targetName);

    // Link 3
    if (vLn3.getHost() instanceof Path) {
      final Path pLn3 = (Path) vLn3.getHost();
      sourceName = pLn3.getSource().getName();
      targetName = pLn3.getTarget().getName();
    } else {
      fail();
    }

    assertEquals("sub_sw", sourceName);
    assertEquals(refHost1, targetName);

    // Link 4
    if (vLn4.getHost() instanceof Path) {
      final Path pLn4 = (Path) vLn4.getHost();
      sourceName = pLn4.getSource().getName();
      targetName = pLn4.getTarget().getName();
    } else {
      fail();
    }

    assertEquals("sub_sw", sourceName);
    assertEquals(refHost2, targetName);
  }

  @Test
  public void testAllOnMultipleRacks() {
    AlgorithmTestHelper.oneTierSetupThreeServers("virt", 1);
    AlgorithmTestHelper.twoTierSetupFourServers("sub", 1);

    facade.createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

    initAlgo(sNet, Set.of(vNet));
    assertTrue(algo.execute());

    // Test switch placement
    final VirtualSwitch virtSw = (VirtualSwitch) facade.getSwitchById("virt_sw");
    assertEquals("sub_csw1", virtSw.getHost().getName());

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
    assertEquals("sub_csw1", pLn1.getTarget().getName());

    // Link 2
    final Path pLn2 = (Path) vLn2.getHost();
    assertEquals(serverHost2, pLn2.getSource().getName());
    assertEquals("sub_csw1", pLn2.getTarget().getName());

    // Link 3
    final Path pLn3 = (Path) vLn3.getHost();
    assertEquals(serverHost3, pLn3.getSource().getName());
    assertEquals("sub_csw1", pLn3.getTarget().getName());

    // Link 4
    final Path pLn4 = (Path) vLn4.getHost();
    assertEquals("sub_csw1", pLn4.getSource().getName());
    assertEquals(serverHost1, pLn4.getTarget().getName());

    // Link 5
    final Path pLn5 = (Path) vLn5.getHost();
    assertEquals("sub_csw1", pLn5.getSource().getName());
    assertEquals(serverHost2, pLn5.getTarget().getName());

    // Link 6
    final Path pLn6 = (Path) vLn6.getHost();
    assertEquals("sub_csw1", pLn6.getSource().getName());
    assertEquals(serverHost3, pLn6.getTarget().getName());
  }

  /*
   * Negative tests.
   */

  @Test
  public void testNoEmbeddingWithSplittedVm() {
    AlgorithmTestHelper.oneTierSetupTwoServers("virt", 2);
    AlgorithmTestHelper.twoTierSetupFourServers("sub", 1);

    facade.createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

    initAlgo(sNet, Set.of(vNet));

    // Embedding should not be possible, because a split of one VM to embed it on two substrate
    // servers is not possible although the total amount of resources could handle the virtual
    // network.
    assertFalse(algo.execute());
    assertNull(vNet.getHost());
  }

  @Test
  public void testNoEmbeddingIfVnetTooLargeSingle() {
    AlgorithmTestHelper.oneTierSetupTwoServers("virt", 3);
    AlgorithmTestHelper.twoTierSetupFourServers("sub", 1);

    facade.createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

    initAlgo(sNet, Set.of(vNet));

    // Embedding should not be possible, because the total size of the virtual network can not fit
    // on the substrate one.
    assertFalse(algo.execute());
  }

  @Test
  public void testNoEmbeddingIfVnetTooLargeMulti() {
    AlgorithmTestHelper.oneTierSetupTwoServers("virt", 2);
    AlgorithmTestHelper.twoTierSetupFourServers("sub", 2);

    facade.createAllPathsForNetwork("sub");

    SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

    initAlgo(sNet, Set.of(vNet));
    assertTrue(algo.execute());

    facade.addNetworkToRoot("virt2", true);
    AlgorithmTestHelper.oneTierSetupTwoServers("virt2", 3);

    sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final VirtualNetwork vNet2 = (VirtualNetwork) facade.getNetworkById("virt2");
    initAlgo(sNet, Set.of(vNet2));

    // Embedding should not be possible, because the total size of the virtual network can not fit
    // on the substrate one.
    assertFalse(algo.execute());
  }

}
