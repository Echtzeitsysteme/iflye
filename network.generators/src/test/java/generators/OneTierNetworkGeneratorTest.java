package generators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import facade.ModelFacade;
import generators.config.OneTierConfig;
import model.Link;
import model.Network;
import model.Node;
import model.Path;
import model.Server;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateServer;
import model.Switch;
import model.VirtualNetwork;

/**
 * Test class for the OneTierNetworkGenerator.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class OneTierNetworkGeneratorTest {

  @Before
  public void resetModel() {
    ModelFacade.getInstance().resetAll();
  }

  @Test
  public void testNoNetworksAfterInit() {
    final OneTierConfig config = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    new OneTierNetworkGenerator(config);
    assertTrue(ModelFacade.getInstance().getAllNetworks().isEmpty());
  }

  @Test
  public void testNumberOfElementsSmallSubstrate() {
    final OneTierConfig config = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("test", false);

    final Network net = ModelFacade.getInstance().getNetworkById("test");

    // Number of nodes
    assertEquals(3, net.getNodes().size());

    // Servers
    assertEquals(2, ModelFacade.getInstance().getAllServersOfNetwork("test").size());

    // Switches
    assertEquals(1, ModelFacade.getInstance().getAllSwitchesOfNetwork("test").size());

    // Links
    assertEquals(4, net.getLinks().size());

    // Paths, only 2 because of the default values for path limits
    assertEquals(2, net.getPaths().size());
  }

  @Test
  public void testNumberOfElementsSmallVirtual() {
    final OneTierConfig config = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("test", true);

    final Network net = ModelFacade.getInstance().getNetworkById("test");

    // Number of nodes
    assertEquals(3, net.getNodes().size());

    // Servers
    assertEquals(2, ModelFacade.getInstance().getAllServersOfNetwork("test").size());

    // Switches
    assertEquals(1, ModelFacade.getInstance().getAllSwitchesOfNetwork("test").size());

    // Links
    assertEquals(4, net.getLinks().size());

    // Paths, no paths for virtual networks
    assertEquals(0, net.getPaths().size());
  }

  @Test
  public void testNumberOfElementsLarge() {
    final OneTierConfig config = new OneTierConfig(100, 5, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("test", false);

    final Network net = ModelFacade.getInstance().getNetworkById("test");

    // Number of nodes
    assertEquals(105, net.getNodes().size());

    // Servers
    assertEquals(100, ModelFacade.getInstance().getAllServersOfNetwork("test").size());

    // Switches
    assertEquals(5, ModelFacade.getInstance().getAllSwitchesOfNetwork("test").size());

    // Links
    assertEquals(1000, net.getLinks().size());

    // Paths, only 100 * 100 - 100 because of the default values for path limits
    assertEquals(100 * 100 - 100, net.getPaths().size());
  }

  @Test
  public void testVirtualSubstrate() {
    final OneTierConfig config = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("sub", false);
    gen.createNetwork("virt", true);

    assertEquals(2, ModelFacade.getInstance().getAllNetworks().size());
    assertTrue(ModelFacade.getInstance().getNetworkById("sub") instanceof SubstrateNetwork);
    assertTrue(ModelFacade.getInstance().getNetworkById("virt") instanceof VirtualNetwork);
  }

  @Test
  public void testServerNormalParameters() {
    final OneTierConfig config = new OneTierConfig(2, 1, false, 1, 2, 3, 0);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("sub", false);

    for (final Node n : ModelFacade.getInstance().getAllServersOfNetwork("sub")) {
      final SubstrateServer srv = (SubstrateServer) n;
      assertEquals(1, srv.getCpu());
      assertEquals(2, srv.getMemory());
      assertEquals(3, srv.getStorage());
    }
  }

  @Test
  public void testServerResidualParameters() {
    final OneTierConfig config = new OneTierConfig(2, 1, false, 1, 2, 3, 0);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("sub", false);

    for (final Node n : ModelFacade.getInstance().getAllServersOfNetwork("sub")) {
      final SubstrateServer srv = (SubstrateServer) n;
      assertEquals(srv.getCpu(), srv.getResidualCpu());
      assertEquals(srv.getMemory(), srv.getResidualMemory());
      assertEquals(srv.getStorage(), srv.getResidualStorage());
    }
  }

  @Test
  public void testServerTwoLinksEach() {
    final OneTierConfig config = new OneTierConfig(2, 1, false, 1, 2, 3, 1);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("sub", false);

    for (final Node n : ModelFacade.getInstance().getAllServersOfNetwork("sub")) {
      final SubstrateServer srv = (SubstrateServer) n;
      assertEquals(1, srv.getOutgoingLinks().size());
      assertEquals(1, srv.getIncomingLinks().size());
    }
  }

  @Test
  public void testLinkParameters() {
    final OneTierConfig config = new OneTierConfig(2, 1, false, 1, 1, 1, 42);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("sub", false);

    final List<Link> links = ModelFacade.getInstance().getAllLinksOfNetwork("sub");

    for (final Link l : links) {
      final SubstrateLink sl = (SubstrateLink) l;
      assertNotNull(l.getSource());
      assertNotNull(l.getTarget());
      assertEquals(42, l.getBandwidth());
      assertEquals(l.getBandwidth(), sl.getResidualBandwidth());
    }
  }

  @Test
  public void testDepths() {
    final OneTierConfig config = new OneTierConfig(4, 3, false, 1, 1, 1, 42);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("sub", false);

    for (final Node n : ModelFacade.getInstance().getNetworkById("sub").getNodes()) {
      if (n instanceof Server) {
        assertEquals(1, ((Server) n).getDepth());
      } else if (n instanceof Switch) {
        assertEquals(0, ((Switch) n).getDepth());
      } else {
        fail("Node type should not be part of a OneTierNetwork.");
      }
    }
  }

  @Test
  public void testLinkConnections() {
    final OneTierConfig config = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("sub", false);

    final List<Link> links = ModelFacade.getInstance().getAllLinksOfNetwork("sub");
    assertEquals("srv_0", links.get(0).getSource().getName());
    assertEquals("sw_2", links.get(0).getTarget().getName());
    assertEquals("sw_2", links.get(1).getSource().getName());
    assertEquals("srv_0", links.get(1).getTarget().getName());
    assertEquals("srv_1", links.get(2).getSource().getName());
    assertEquals("sw_2", links.get(2).getTarget().getName());
    assertEquals("sw_2", links.get(3).getSource().getName());
    assertEquals("srv_1", links.get(3).getTarget().getName());
  }

  @Test
  public void testPathsSourceAndTargetNotNull() {
    final OneTierConfig config = new OneTierConfig(4, 2, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("sub", false);

    for (final Path p : ModelFacade.getInstance().getNetworkById("sub").getPaths()) {
      assertNotNull(p.getSource());
      assertNotNull(p.getTarget());
    }
  }

  @Ignore
  @Test
  public void testSwitchesConnected() {
    // TODO: Implement after implementing the feature.
  }

}
