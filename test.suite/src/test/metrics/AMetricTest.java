package test.metrics;

import org.junit.jupiter.api.BeforeEach;
import facade.ModelFacade;
import model.Path;

/**
 * Abstract metric test class.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public abstract class AMetricTest {

  /**
   * ModelFacade object to work with.
   */
  protected final ModelFacade facade = ModelFacade.getInstance();

  @BeforeEach
  public void resetModel() {
    facade.resetAll();
  }

  /*
   * Utility methods
   */

  /**
   * Creates the substrate network to test on.
   */
  protected void createSubstrateNetwork() {
    facade.addNetworkToRoot("sub", false);
    facade.addServerToNetwork("ssrv1", "sub", 2, 2, 2, 1);
    facade.addServerToNetwork("ssrv2", "sub", 2, 2, 2, 1);
    facade.addSwitchToNetwork("ssw", "sub", 0);
    facade.addLinkToNetwork("sln1", "sub", 100, "ssw", "ssrv1");
    facade.addLinkToNetwork("sln2", "sub", 100, "ssw", "ssrv2");
    facade.addLinkToNetwork("sln3", "sub", 100, "ssrv1", "ssw");
    facade.addLinkToNetwork("sln4", "sub", 100, "ssrv2", "ssw");
  }

  /**
   * Creates the virtual network to test on.
   */
  protected void createVirtualNetwork() {
    facade.addNetworkToRoot("virt", true);
    facade.addSwitchToNetwork("vsw", "virt", 0);
    facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 0);
    facade.addServerToNetwork("vsrv2", "virt", 1, 1, 1, 0);
    facade.addLinkToNetwork("vln1", "virt", 3, "vsw", "vsrv1");
    facade.addLinkToNetwork("vln2", "virt", 3, "vsw", "vsrv2");
    facade.addLinkToNetwork("vln3", "virt", 3, "vsrv1", "vsw");
    facade.addLinkToNetwork("vln4", "virt", 3, "vsrv2", "vsw");
  }

  /**
   * Sets an embedding with one host only (server) up. All elements of the virtual network are
   * embedded on the one substrate server.
   */
  protected void setupEmbeddingSameHost() {
    facade.embedNetworkToNetwork("sub", "virt");
    facade.embedSwitchToNode("ssrv1", "vsw");
    facade.embedServerToServer("ssrv1", "vsrv1");
    facade.embedServerToServer("ssrv1", "vsrv2");
    facade.embedLinkToServer("ssrv1", "vln1");
    facade.embedLinkToServer("ssrv1", "vln2");
    facade.embedLinkToServer("ssrv1", "vln3");
    facade.embedLinkToServer("ssrv1", "vln4");
  }

  /**
   * Sets an embedding with two substrate servers up. The switch will be embedded on the substrate
   * switch.
   */
  protected void setupEmbeddingTwoHosts() {
    facade.createAllPathsForNetwork("sub");
    facade.embedNetworkToNetwork("sub", "virt");
    facade.embedSwitchToNode("ssw", "vsw");
    facade.embedServerToServer("ssrv1", "vsrv1");
    facade.embedServerToServer("ssrv2", "vsrv2");
    final Path p1 = facade.getPathFromSourceToTarget("ssw", "ssrv1");
    final Path p2 = facade.getPathFromSourceToTarget("ssw", "ssrv2");
    final Path p3 = facade.getPathFromSourceToTarget("ssrv1", "ssw");
    final Path p4 = facade.getPathFromSourceToTarget("ssrv2", "ssw");
    facade.embedLinkToPath(p1.getName(), "vln1");
    facade.embedLinkToPath(p2.getName(), "vln2");
    facade.embedLinkToPath(p3.getName(), "vln3");
    facade.embedLinkToPath(p4.getName(), "vln4");
  }

  /**
   * Sets an embedding with two virtual servers on one substrate server and one virtual switch on
   * the other substrate server up.
   */
  protected void setupEmbeddingTwoHops() {
    facade.createAllPathsForNetwork("sub");
    facade.embedNetworkToNetwork("sub", "virt");
    facade.embedSwitchToNode("ssrv2", "vsw");
    facade.embedServerToServer("ssrv1", "vsrv1");
    facade.embedServerToServer("ssrv1", "vsrv2");

    final Path pa = facade.getPathFromSourceToTarget(facade.getServerById("ssrv1"),
        facade.getServerById("ssrv2"));
    final Path pb = facade.getPathFromSourceToTarget(facade.getServerById("ssrv2"),
        facade.getServerById("ssrv1"));

    facade.embedLinkToPath(pa.getName(), "vln1");
    facade.embedLinkToPath(pa.getName(), "vln2");
    facade.embedLinkToPath(pb.getName(), "vln3");
    facade.embedLinkToPath(pb.getName(), "vln4");
  }

}
