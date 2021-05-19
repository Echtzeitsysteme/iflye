package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.SubstratePath;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualLink;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Test class for the ModelFacade that tests some embedding tasks.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadeEmbeddingTest {

  @BeforeEach
  public void resetModel() {
    ModelFacade.getInstance().resetAll();

    // Network setup
    ModelFacade.getInstance().addNetworkToRoot("sub", false);
    ModelFacade.getInstance().addNetworkToRoot("virt", true);
  }

  @Test
  public void testEmbedNetworkToNetwork() {
    // No guests before embedding anything
    assertTrue(
        ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().isEmpty());

    ModelFacade.getInstance().embedNetworkToNetwork("sub", "virt");

    assertFalse(
        ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().isEmpty());
    assertEquals("virt", ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub"))
        .getGuests().get(0).getName());
  }

  @Test
  public void testEmbedServerToServer() {
    ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
    ModelFacade.getInstance().addServerToNetwork("2", "virt", 1, 1, 1, 0);

    ModelFacade.getInstance().embedServerToServer("1", "2");
    assertEquals(1,
        ((SubstrateServer) ModelFacade.getInstance().getServerById("1")).getGuestServers().size());
    assertEquals("1",
        ((VirtualServer) ModelFacade.getInstance().getServerById("2")).getHost().getName());
  }

  @Test
  public void testEmbedServerToServerRejectCpu() {
    ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
    ModelFacade.getInstance().addServerToNetwork("2", "virt", 2, 1, 1, 0);

    assertThrows(UnsupportedOperationException.class, () -> {
      ModelFacade.getInstance().embedServerToServer("1", "2");
    });
  }

  @Test
  public void testEmbedServerToServerRejectMemory() {
    ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
    ModelFacade.getInstance().addServerToNetwork("2", "virt", 1, 2, 1, 0);

    assertThrows(UnsupportedOperationException.class, () -> {
      ModelFacade.getInstance().embedServerToServer("1", "2");
    });
  }

  @Test
  public void testEmbedServerToServerRejectStorage() {
    ModelFacade.getInstance().addServerToNetwork("1", "sub", 1, 1, 1, 0);
    ModelFacade.getInstance().addServerToNetwork("2", "virt", 1, 1, 2, 0);

    assertThrows(UnsupportedOperationException.class, () -> {
      ModelFacade.getInstance().embedServerToServer("1", "2");
    });
  }

  @Test
  public void testEmbedSwitchToServer() {
    ModelFacade.getInstance().addServerToNetwork("1", "sub", 0, 0, 0, 0);
    ModelFacade.getInstance().addSwitchToNetwork("2", "virt", 0);

    ModelFacade.getInstance().embedSwitchToNode("1", "2");
    assertEquals(1,
        ((SubstrateServer) ModelFacade.getInstance().getServerById("1")).getGuestSwitches().size());
    assertEquals("1",
        ((VirtualSwitch) ModelFacade.getInstance().getSwitchById("2")).getHost().getName());
  }

  @Test
  public void testEmbedSwitchtoSwitch() {
    ModelFacade.getInstance().addSwitchToNetwork("1", "sub", 0);
    ModelFacade.getInstance().addSwitchToNetwork("2", "virt", 0);

    ModelFacade.getInstance().embedSwitchToNode("1", "2");
    assertEquals(1,
        ((SubstrateSwitch) ModelFacade.getInstance().getSwitchById("1")).getGuestSwitches().size());
    assertEquals("1",
        ((VirtualSwitch) ModelFacade.getInstance().getSwitchById("2")).getHost().getName());
  }

  @Test
  public void testEmbedLinkToServer() {
    ModelFacade.getInstance().addServerToNetwork("1", "sub", 0, 0, 0, 0);

    ModelFacade.getInstance().addServerToNetwork("2", "virt", 0, 0, 0, 0);
    ModelFacade.getInstance().addServerToNetwork("3", "virt", 0, 0, 0, 0);
    ModelFacade.getInstance().addLinkToNetwork("4", "virt", 0, "2", "3");

    ModelFacade.getInstance().embedLinkToServer("1", "4");
    assertEquals(1,
        ((SubstrateServer) ModelFacade.getInstance().getServerById("1")).getGuestLinks().size());
    assertEquals("1",
        ((VirtualLink) ModelFacade.getInstance().getLinkById("4")).getHost().getName());
  }

  @Test
  public void testEmbedLinkBwIgnore() {
    // Set ignore bandwidth to true in ModelFacadeConfig.
    ModelFacadeConfig.IGNORE_BW = true;

    ModelFacade.getInstance().addServerToNetwork("srv1", "sub", 0, 0, 0, 0);
    ModelFacade.getInstance().addServerToNetwork("srv2", "sub", 0, 0, 0, 0);
    ModelFacade.getInstance().addLinkToNetwork("l3", "sub", 10, "srv1", "srv2");
    ModelFacade.getInstance().addLinkToNetwork("l4", "sub", 10, "srv2", "srv1");

    ModelFacade.getInstance().addServerToNetwork("srv5", "virt", 0, 0, 0, 0);
    ModelFacade.getInstance().addServerToNetwork("srv6", "virt", 0, 0, 0, 0);
    ModelFacade.getInstance().addLinkToNetwork("l7", "virt", 12, "srv5", "srv6");
    ModelFacade.getInstance().addLinkToNetwork("l8", "virt", 12, "srv6", "srv5");
    ModelFacade.getInstance().createAllPathsForNetwork("sub");

    ModelFacade.getInstance().embedLinkToPath("0", "l7");

    final SubstratePath subPath = (SubstratePath) ModelFacade.getInstance().getPathById("0");
    assertEquals(1, subPath.getGuestLinks().size());
    assertEquals("l7", subPath.getGuestLinks().get(0).getName());

    // Reset configuration afterwards
    ModelFacadeConfig.IGNORE_BW = false;
  }

  @Test
  public void testEmbedMultipleNetworksToNetwork() {
    ModelFacade.getInstance().embedNetworkToNetwork("sub", "virt");

    ModelFacade.getInstance().addNetworkToRoot("virt_2", true);
    ModelFacade.getInstance().embedNetworkToNetwork("sub", "virt_2");

    assertFalse(
        ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().isEmpty());
    assertEquals(2,
        ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().size());
    assertEquals("virt", ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub"))
        .getGuests().get(0).getName());
    assertEquals("virt_2", ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub"))
        .getGuests().get(1).getName());
  }

  @Ignore
  @Test
  public void testEmbedLinkToPath() {
    // TODO: Implement after creation of all paths is implemented.
  }

}
