package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import metrics.ActiveSubstrateServerMetric;
import model.SubstrateNetwork;

/**
 * Test class for the metric of active substrate servers.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ActiveSubstrateServerMetricTest extends AMetricTest {

  @Test
  public void testNoEmbeddings() {
    facade.addNetworkToRoot("sub", false);
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final ActiveSubstrateServerMetric metric = new ActiveSubstrateServerMetric(sNet);
    assertEquals(0, metric.getValue());
  }

  @Test
  public void testOneEmbedding() {
    createAndEmbedServers(1, "virt", 0);
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final ActiveSubstrateServerMetric metric = new ActiveSubstrateServerMetric(sNet);
    assertEquals(1, metric.getValue());
  }

  @Test
  public void testMultipleEmbedding() {
    createAndEmbedServers(42, "virt", 0);
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final ActiveSubstrateServerMetric metric = new ActiveSubstrateServerMetric(sNet);
    assertEquals(42, metric.getValue());
  }

  @Test
  public void testMultipleVirtualNetworks() {
    for (int i = 1; i <= 4; i++) {
      createAndEmbedServers(1, "virt_" + i, i - 1);
      final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

      final ActiveSubstrateServerMetric metric = new ActiveSubstrateServerMetric(sNet);
      assertEquals(i * 1, metric.getValue());
    }
  }

  /*
   * Utility methods
   */

  /**
   * Creates and embeds a given number of servers to the substrate network.
   * 
   * @param numberOfServers Number of servers to create and embed.
   * @param virtNetId String ID for the virtual network.
   * @param substrateServerOffset Offset to start creating virtual servers with.
   */
  private void createAndEmbedServers(final int numberOfServers, final String virtNetId,
      final int substrateServerOffset) {
    if (!facade.networkExists("sub")) {
      facade.addNetworkToRoot("sub", false);
    }

    facade.addNetworkToRoot(virtNetId, true);
    facade.embedNetworkToNetwork("sub", virtNetId);

    for (int i = 0; i < numberOfServers; i++) {
      facade.addServerToNetwork("ssrv_" + (i + substrateServerOffset), "sub", 0, 0, 0, 0);
      facade.addServerToNetwork("vsrv_" + virtNetId + i, virtNetId, 0, 0, 0, 0);
      facade.embedServerToServer("ssrv_" + (i + substrateServerOffset), "vsrv_" + virtNetId + i);
    }
  }

}
