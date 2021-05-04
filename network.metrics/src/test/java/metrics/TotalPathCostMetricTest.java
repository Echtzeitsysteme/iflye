package metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.Before;
import org.junit.Test;
import model.Path;
import model.SubstrateNetwork;

/**
 * Test class for the metric of total path cost.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class TotalPathCostMetricTest extends AMetricTest {

  @Before
  public void setup() {
    createSubstrateNetwork();
    createVirtualNetwork();
  }

  @Test
  public void testNoEmbeddings() {
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final TotalPathCostMetric metric = new TotalPathCostMetric(sNet);
    assertEquals(0, metric.getValue());
  }

  @Test
  public void testEmbeddingSameHost() {
    facade.embedNetworkToNetwork("sub", "virt");
    facade.embedSwitchToNode("ssrv1", "vsw");
    facade.embedServerToServer("ssrv1", "vsrv1");
    facade.embedServerToServer("ssrv1", "vsrv2");
    facade.embedLinkToServer("ssrv1", "vln1");
    facade.embedLinkToServer("ssrv1", "vln2");
    facade.embedLinkToServer("ssrv1", "vln3");
    facade.embedLinkToServer("ssrv1", "vln4");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final TotalPathCostMetric metric = new TotalPathCostMetric(sNet);

    // cost = 2 * SrvToSrv + 1 * SwToSrv + 4 * LnToSrv
    // cost = 2 * 1 + 1 * 2 + 4 * 1
    assertEquals(8, metric.getValue());
  }

  @Test
  public void testEmbeddingTwoHosts() {
    facade.embedNetworkToNetwork("sub", "virt");
    facade.embedSwitchToNode("ssw", "vsw");
    facade.embedServerToServer("ssrv1", "vsrv1");
    facade.embedServerToServer("ssrv2", "vsrv2");
    facade.embedLinkToLink("sln1", "vln1");
    facade.embedLinkToLink("sln2", "vln2");
    facade.embedLinkToLink("sln3", "vln3");
    facade.embedLinkToLink("sln4", "vln4");

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final TotalPathCostMetric metric = new TotalPathCostMetric(sNet);

    // cost = 2 * SrvToSrv + 1 * SwToSw + 4 * LnToLn(1hop)
    // cost = 2 * 1 + 1 * 1 + 4 * 2
    assertEquals(11, metric.getValue());
  }

  @Test
  public void testEmbeddingTwoHops() {
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

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final TotalPathCostMetric metric = new TotalPathCostMetric(sNet);

    // cost = 2 * SrvToSrv + 1 * SwToSrv + 4 * LnToPath(2hop)
    // cost = 2 * 1 + 1 * 2 + 4 * (4^2)
    assertEquals((2 + 2 + 4 * Math.pow(4, 2)), metric.getValue());
  }

}
