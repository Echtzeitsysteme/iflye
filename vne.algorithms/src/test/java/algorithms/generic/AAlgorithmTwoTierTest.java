package algorithms.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.Test;
import facade.ModelFacade;
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
    oneTierSetupTwoServers("virt", 1);
    oneTierSetupTwoServers("sub", 2);
    ModelFacade.getInstance().createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    initAlgo(sNet, vNet);
    assertTrue(algo.execute());

    SubstrateServer host = null;

    // Test all vServer hosts
    for (final Node n : ModelFacade.getInstance().getAllServersOfNetwork("virt")) {
      // Initialize the host of the first virtual element to check
      if (host == null) {
        host = ((VirtualServer) n).getHost();
      }

      assertEquals(host, ((VirtualServer) n).getHost());
    }

    // Test all vSwitch hosts
    for (final Node n : ModelFacade.getInstance().getAllSwitchesOfNetwork("virt")) {
      assertEquals(host, ((VirtualSwitch) n).getHost());
    }

    // Test all vLink hosts
    for (final Link l : ModelFacade.getInstance().getAllLinksOfNetwork("virt")) {
      final VirtualLink vl = (VirtualLink) l;
      // This one host must be substrate server 1
      assertEquals(host, vl.getHost());
    }
  }

  @Test
  public void testAllOnOneRack() {
    oneTierSetupTwoServers("virt", 2);
    oneTierSetupTwoServers("sub", 2);
    ModelFacade.getInstance().createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    initAlgo(sNet, vNet);
    assertTrue(algo.execute());

    // Test switch placement
    final VirtualSwitch virtSw = (VirtualSwitch) ModelFacade.getInstance().getSwitchById("virt_sw");
    assertEquals("sub_sw", virtSw.getHost().getName());

    // Test server placements
    final VirtualServer vSrv1 =
        (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv1");
    final VirtualServer vSrv2 =
        (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv2");
    assertEquals("sub_srv1", vSrv1.getHost().getName());
    assertEquals("sub_srv2", vSrv2.getHost().getName());

    // Test link placements
    final VirtualLink vLn1 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln1");
    final VirtualLink vLn2 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln2");
    final VirtualLink vLn3 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln3");
    final VirtualLink vLn4 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln4");

    // Link 1
    final Path pLn1 = (Path) vLn1.getHost();
    assertEquals("sub_srv1", pLn1.getSource().getName());
    assertEquals("sub_sw", pLn1.getTarget().getName());

    // Link 2
    final Path pLn2 = (Path) vLn2.getHost();
    assertEquals("sub_srv2", pLn2.getSource().getName());
    assertEquals("sub_sw", pLn2.getTarget().getName());

    // Link 3
    final Path pLn3 = (Path) vLn3.getHost();
    assertEquals("sub_sw", pLn3.getSource().getName());
    assertEquals("sub_srv1", pLn3.getTarget().getName());

    // Link 4
    final Path pLn4 = (Path) vLn4.getHost();
    assertEquals("sub_sw", pLn4.getSource().getName());
    assertEquals("sub_srv2", pLn4.getTarget().getName());
  }

  @Test
  public void testAllOnMultipleRacks() {
    oneTierSetupThreeServers("virt", 1);
    twoTierSetupFourServers("sub", 1);

    ModelFacade.getInstance().createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    initAlgo(sNet, vNet);
    assertTrue(algo.execute());

    // TODO:

    // Test switch placement
    final VirtualSwitch virtSw = (VirtualSwitch) ModelFacade.getInstance().getSwitchById("virt_sw");
    assertEquals("sub_csw1", virtSw.getHost().getName());

    // Test server placements
    final VirtualServer vSrv1 =
        (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv1");
    final VirtualServer vSrv2 =
        (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv2");
    final VirtualServer vSrv3 =
        (VirtualServer) ModelFacade.getInstance().getServerById("virt_srv3");
    assertEquals("sub_srv1", vSrv1.getHost().getName());
    assertEquals("sub_srv2", vSrv2.getHost().getName());
    assertEquals("sub_srv3", vSrv3.getHost().getName());

    // Test link placements
    final VirtualLink vLn1 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln1");
    final VirtualLink vLn2 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln2");
    final VirtualLink vLn3 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln3");
    final VirtualLink vLn4 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln4");
    final VirtualLink vLn5 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln5");
    final VirtualLink vLn6 = (VirtualLink) ModelFacade.getInstance().getLinkById("virt_ln6");

    // Link 1
    final Path pLn1 = (Path) vLn1.getHost();
    assertEquals("sub_srv1", pLn1.getSource().getName());
    assertEquals("sub_csw1", pLn1.getTarget().getName());

    // Link 2
    final Path pLn2 = (Path) vLn2.getHost();
    assertEquals("sub_srv2", pLn2.getSource().getName());
    assertEquals("sub_csw1", pLn2.getTarget().getName());

    // Link 3
    final Path pLn3 = (Path) vLn3.getHost();
    assertEquals("sub_srv3", pLn3.getSource().getName());
    assertEquals("sub_csw1", pLn3.getTarget().getName());

    // Link 4
    final Path pLn4 = (Path) vLn4.getHost();
    assertEquals("sub_csw1", pLn4.getSource().getName());
    assertEquals("sub_srv1", pLn4.getTarget().getName());

    // Link 5
    final Path pLn5 = (Path) vLn5.getHost();
    assertEquals("sub_csw1", pLn5.getSource().getName());
    assertEquals("sub_srv2", pLn5.getTarget().getName());

    // Link 6
    final Path pLn6 = (Path) vLn6.getHost();
    assertEquals("sub_csw1", pLn6.getSource().getName());
    assertEquals("sub_srv3", pLn6.getTarget().getName());
  }

  /*
   * Negative tests.
   */

  @Test
  public void testNoEmbeddingWithSplittedVm() {
    oneTierSetupTwoServers("virt", 2);
    twoTierSetupFourServers("sub", 1);

    ModelFacade.getInstance().createAllPathsForNetwork("sub");

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    initAlgo(sNet, vNet);

    // Embedding should not be possible, because a split of one VM to embed it on two substrate
    // servers is not possible although the total amount of resources could handle the virtual
    // network.
    assertFalse(algo.execute());
  }

  /*
   * Utility methods.
   */

  /**
   * Creates a one tier network with two servers and one switch.
   * 
   * @param networkId Network id.
   * @param slotsPerServer Number of CPU, memory and storage resources.
   */
  protected static void oneTierSetupTwoServers(final String networkId, final int slotsPerServer) {
    ModelFacade.getInstance().addSwitchToNetwork(networkId + "_sw", networkId, 0);
    ModelFacade.getInstance().addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer,
        slotsPerServer, slotsPerServer, 1);
    ModelFacade.getInstance().addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer,
        slotsPerServer, slotsPerServer, 1);
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln1", networkId, 1,
        networkId + "_srv1", networkId + "_sw");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln2", networkId, 1,
        networkId + "_srv2", networkId + "_sw");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln3", networkId, 1, networkId + "_sw",
        networkId + "_srv1");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln4", networkId, 1, networkId + "_sw",
        networkId + "_srv2");
  }

  /**
   * Creates a one tier network with three servers and one switch.
   * 
   * @param networkId Network id.
   * @param slotsPerServer Number of CPU, memory and storage resources.
   */
  protected static void oneTierSetupThreeServers(final String networkId, final int slotsPerServer) {
    ModelFacade.getInstance().addSwitchToNetwork(networkId + "_sw", networkId, 0);
    ModelFacade.getInstance().addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer,
        slotsPerServer, slotsPerServer, 1);
    ModelFacade.getInstance().addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer,
        slotsPerServer, slotsPerServer, 1);
    ModelFacade.getInstance().addServerToNetwork(networkId + "_srv3", networkId, slotsPerServer,
        slotsPerServer, slotsPerServer, 1);
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln1", networkId, 1,
        networkId + "_srv1", networkId + "_sw");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln2", networkId, 1,
        networkId + "_srv2", networkId + "_sw");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln3", networkId, 1,
        networkId + "_srv3", networkId + "_sw");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln4", networkId, 1, networkId + "_sw",
        networkId + "_srv1");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln5", networkId, 1, networkId + "_sw",
        networkId + "_srv2");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln6", networkId, 1, networkId + "_sw",
        networkId + "_srv3");
  }

  /**
   * Creates a two tier network with four servers total, two rack switches, and one core switch.
   * 
   * @param networkId Network id.
   * @param slotsPerServer Number of CPU, memory and storage resources.
   */
  protected static void twoTierSetupFourServers(final String networkId, final int slotsPerServer) {
    ModelFacade.getInstance().addSwitchToNetwork(networkId + "_csw1", networkId, 0);
    ModelFacade.getInstance().addSwitchToNetwork(networkId + "_rsw1", networkId, 1);
    ModelFacade.getInstance().addSwitchToNetwork(networkId + "_rsw2", networkId, 1);

    ModelFacade.getInstance().addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer,
        slotsPerServer, slotsPerServer, 2);
    ModelFacade.getInstance().addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer,
        slotsPerServer, slotsPerServer, 2);
    ModelFacade.getInstance().addServerToNetwork(networkId + "_srv3", networkId, slotsPerServer,
        slotsPerServer, slotsPerServer, 2);
    ModelFacade.getInstance().addServerToNetwork(networkId + "_srv4", networkId, slotsPerServer,
        slotsPerServer, slotsPerServer, 2);

    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln1", networkId, 0,
        networkId + "_srv1", networkId + "_rsw1");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln2", networkId, 0,
        networkId + "_srv2", networkId + "_rsw1");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln3", networkId, 0,
        networkId + "_rsw1", networkId + "_srv1");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln4", networkId, 0,
        networkId + "_rsw1", networkId + "_srv2");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln5", networkId, 0,
        networkId + "_srv3", networkId + "_rsw2");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln6", networkId, 0,
        networkId + "_srv4", networkId + "_rsw2");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln7", networkId, 0,
        networkId + "_rsw2", networkId + "_srv3");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln8", networkId, 0,
        networkId + "_rsw2", networkId + "_srv4");

    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln9", networkId, 0,
        networkId + "_rsw1", networkId + "_csw1");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln10", networkId, 0,
        networkId + "_rsw2", networkId + "_csw1");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln11", networkId, 0,
        networkId + "_csw1", networkId + "_rsw1");
    ModelFacade.getInstance().addLinkToNetwork(networkId + "_ln12", networkId, 0,
        networkId + "_csw1", networkId + "_rsw2");
  }


}
