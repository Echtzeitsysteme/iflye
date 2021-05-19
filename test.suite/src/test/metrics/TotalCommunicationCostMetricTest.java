package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.Before;
import org.junit.Test;
import facade.config.ModelFacadeConfig;
import metrics.TotalCommunicationCostMetric;
import model.Path;
import model.SubstrateNetwork;

/**
 * Test class for the metric of total communication cost.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TotalCommunicationCostMetricTest extends AMetricTest {

  @Before
  public void setup() {
    createVirtualNetwork();
  }

  /*
   * Positive tests
   */

  @Test
  public void testNoEmbeddings() {
    createSubstrateNetwork();
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final TotalCommunicationCostMetric metric = new TotalCommunicationCostMetric(sNet);
    assertEquals(0, metric.getValue());
  }

  @Test
  public void testEmbeddingSameHost() {
    createSubstrateNetwork();
    setupEmbeddingSameHost();
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final TotalCommunicationCostMetric metric = new TotalCommunicationCostMetric(sNet);

    assertEquals(0, metric.getValue());
  }

  @Test
  public void testEmbeddingTwoHosts() {
    createSubstrateNetwork();
    setupEmbeddingTwoHosts();
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final TotalCommunicationCostMetric metric = new TotalCommunicationCostMetric(sNet);

    assertEquals(2 * 3, metric.getValue());
  }

  @Test
  public void testEmbeddingTwoHops() {
    createTwoTierSubstrateNetwork();
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final TotalCommunicationCostMetric metric = new TotalCommunicationCostMetric(sNet);

    assertEquals((4 - 1) * 2 * 3, metric.getValue());
  }

  /*
   * Negative tests
   */

  @Test
  public void testPathConfigIsWrong() {
    int oldMaxPathLength = ModelFacadeConfig.MAX_PATH_LENGTH;
    ModelFacadeConfig.MAX_PATH_LENGTH = 1;
    createTwoTierSubstrateNetwork();
    ModelFacadeConfig.MAX_PATH_LENGTH = oldMaxPathLength;
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    assertThrows(UnsupportedOperationException.class, () -> {
      new TotalCommunicationCostMetric(sNet);
    });
  }

  /*
   * Utility methods
   */

  /**
   * Create a basic two tier substrate network for testing purposes.
   */
  private void createTwoTierSubstrateNetwork() {
    facade.addNetworkToRoot("sub", false);
    facade.addServerToNetwork("ssrv1", "sub", 0, 0, 0, 0);
    facade.addServerToNetwork("ssrv2", "sub", 0, 0, 0, 0);
    facade.addSwitchToNetwork("cssw", "sub", 0);
    facade.addSwitchToNetwork("rssw1", "sub", 0);
    facade.addSwitchToNetwork("rssw2", "sub", 0);
    facade.addLinkToNetwork("sln1", "sub", 100, "rssw1", "ssrv1");
    facade.addLinkToNetwork("sln2", "sub", 100, "rssw2", "ssrv2");
    facade.addLinkToNetwork("sln3", "sub", 100, "ssrv1", "rssw1");
    facade.addLinkToNetwork("sln4", "sub", 100, "ssrv2", "rssw2");
    facade.addLinkToNetwork("sln5", "sub", 100, "cssw", "rssw1");
    facade.addLinkToNetwork("sln6", "sub", 100, "cssw", "rssw2");
    facade.addLinkToNetwork("sln7", "sub", 100, "rssw1", "cssw");
    facade.addLinkToNetwork("sln8", "sub", 100, "rssw2", "cssw");
    facade.createAllPathsForNetwork("sub");

    facade.embedNetworkToNetwork("sub", "virt");
    facade.embedSwitchToNode("cssw", "vsw");
    facade.embedServerToServer("ssrv1", "vsrv1");
    facade.embedServerToServer("ssrv2", "vsrv2");

    final Path pa = facade.getPathFromSourceToTarget(facade.getServerById("ssrv1"),
        facade.getSwitchById("cssw"));
    final Path pb = facade.getPathFromSourceToTarget(facade.getServerById("ssrv2"),
        facade.getSwitchById("cssw"));
    final Path pc = facade.getPathFromSourceToTarget(facade.getSwitchById("cssw"),
        facade.getServerById("ssrv1"));
    final Path pd = facade.getPathFromSourceToTarget(facade.getSwitchById("cssw"),
        facade.getServerById("ssrv2"));

    if (pa != null && pb != null && pc != null && pd != null) {
      facade.embedLinkToPath(pa.getName(), "vln1");
      facade.embedLinkToPath(pc.getName(), "vln2");
      facade.embedLinkToPath(pb.getName(), "vln3");
      facade.embedLinkToPath(pd.getName(), "vln4");
    }
  }

}
