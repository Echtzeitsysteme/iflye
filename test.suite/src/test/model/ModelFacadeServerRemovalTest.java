package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.Link;
import model.Path;
import model.Server;

/**
 * Test class for the ModelFacade that tests the removal of substrate servers.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadeServerRemovalTest {

  /**
   * ModelFacade instance.
   */
  protected ModelFacade facade = ModelFacade.getInstance();

  private static final String netId = "net";

  @BeforeEach
  public void resetModel() {
    facade.resetAll();
  }

  @Test
  public void testRemovalOneTierServerOnlySmall() {
    setUpOneTier(2);
    assertEquals(2, facade.getAllServersOfNetwork(netId).size());
    facade.removeSubstrateServerFromNetwork(netId + "_srv_0");
    assertEquals(1, facade.getAllServersOfNetwork(netId).size());

    // Check left over server
    assertEquals(netId + "_srv_1", facade.getServerById(netId + "_srv_1").getName());
  }

  @Test
  public void testRemovalOneTierLinksOnlySmall() {
    setUpOneTier(2);
    assertEquals(2, facade.getAllServersOfNetwork(netId).size());
    assertEquals(4, facade.getAllLinksOfNetwork(netId).size());
    final String removeId = netId + "_srv_0";
    facade.removeSubstrateServerFromNetwork(removeId);
    assertEquals(2, facade.getAllLinksOfNetwork(netId).size());

    // Check left over links
    for (final Link l : facade.getAllLinksOfNetwork(netId)) {
      assertFalse(l.getSource().getName().equals(removeId));
      assertFalse(l.getTarget().getName().equals(removeId));
    }
  }

  @Test
  public void testRemovalOneTierPathsOnlySmall() {
    setUpOneTier(2);
    assertEquals(2, facade.getAllServersOfNetwork(netId).size());
    assertEquals(6, facade.getAllPathsOfNetwork(netId).size());
    final String removeId = netId + "_srv_0";
    final Server removeServer = facade.getServerById(removeId);
    facade.removeSubstrateServerFromNetwork(removeId);
    assertEquals(2, facade.getAllPathsOfNetwork(netId).size());

    // Check left over paths
    for (final Path p : facade.getAllPathsOfNetwork(netId)) {
      assertFalse(p.getSource().getName().equals(removeId));
      assertFalse(p.getTarget().getName().equals(removeId));
      assertFalse(p.getNodes().contains(removeServer));
    }
  }

  @Test
  public void testRemovalOneTierServersOnlyLarge() {
    setUpOneTier(20);
    assertEquals(20, facade.getAllServersOfNetwork(netId).size());

    final Set<Server> removedServers = new HashSet<Server>();

    for (int i = 0; i < 19; i++) {
      final String id = netId + "_srv_" + i;
      removedServers.add(facade.getServerById(id));
      facade.removeSubstrateServerFromNetwork(id);
      assertEquals(20 - i - 1, facade.getAllServersOfNetwork(netId).size());

      // Check left over servers
      removedServers.forEach(s -> {
        assertFalse(facade.getAllServersOfNetwork(netId).contains(s));
      });
    }
  }

  @Test
  public void testRemovalOneTierLinksOnlyLarge() {
    setUpOneTier(20);
    assertEquals(20, facade.getAllServersOfNetwork(netId).size());
    assertEquals(40, facade.getAllLinksOfNetwork(netId).size());

    final Set<Server> removedServers = new HashSet<Server>();

    for (int i = 0; i < 19; i++) {
      final String id = netId + "_srv_" + i;
      removedServers.add(facade.getServerById(id));
      facade.removeSubstrateServerFromNetwork(id);

      assertEquals(40 - (i + 1) * 2, facade.getAllLinksOfNetwork(netId).size());

      // Check left over links
      for (final Link l : facade.getAllLinksOfNetwork(netId)) {
        removedServers.forEach(s -> {
          assertFalse(l.getSource().equals(s));
          assertFalse(l.getTarget().equals(s));
        });
      }
    }
  }

  @Test
  public void testRemovalOneTierPathsOnlyLarge() {
    setUpOneTier(20);
    assertEquals(20, facade.getAllServersOfNetwork(netId).size());
    final int totalNumberOfPaths = 20 * (20 - 1 + 2);
    assertEquals(totalNumberOfPaths, facade.getAllPathsOfNetwork(netId).size());

    final Set<Server> removedServers = new HashSet<Server>();

    for (int i = 0; i < 19; i++) {
      final String id = netId + "_srv_" + i;
      removedServers.add(facade.getServerById(id));
      facade.removeSubstrateServerFromNetwork(id);

      // Check left over paths
      for (final Path p : facade.getAllPathsOfNetwork(netId)) {
        removedServers.forEach(s -> {
          assertFalse(p.getSource().equals(s));
          assertFalse(p.getTarget().equals(s));
          assertFalse(p.getNodes().contains(s));
        });
      }
    }
  }

  /*
   * Utility methods.
   */

  /**
   * Sets an one tier based network with two servers up.
   * 
   * @param servers Number of servers to create.
   */
  private void setUpOneTier(final int servers) {
    final OneTierConfig subConfig = new OneTierConfig(servers, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(subConfig);
    gen.createNetwork(netId, false);
  }

}
