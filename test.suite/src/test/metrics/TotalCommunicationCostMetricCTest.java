package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import metrics.TotalCommunicationCostMetricC;
import model.SubstrateNetwork;

/**
 * Test class for the metric of total communication cost as implemented in
 * {@link TotalCommunicationCostMetricC}.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TotalCommunicationCostMetricCTest extends ATotalCommunicationCostMetricTest {

  @Override
  protected void setMetric(final SubstrateNetwork sNet) {
    metric = new TotalCommunicationCostMetricC(sNet);
  }

  /*
   * Positive tests
   */

  @Test
  public void testEmbeddingTwoHops() {
    createTwoTierSubstrateNetwork();
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final TotalCommunicationCostMetricC metric = new TotalCommunicationCostMetricC(sNet);

    assertEquals(2 * 2 * 2 * 3 + 2, metric.getValue());
  }

  @Override
  @Test
  public void testEmbeddingSameHost() {
    createSubstrateNetwork();
    setupEmbeddingSameHost();
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    setMetric(sNet);

    assertEquals(0 + 2, metric.getValue());
  }

  @Override
  @Test
  public void testEmbeddingTwoHosts() {
    createSubstrateNetwork();
    setupEmbeddingTwoHosts();
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    setMetric(sNet);

    assertEquals(2 * 2 * 3 + 2.0 / (0.5 + 0.5 + 0.5 + 1), metric.getValue());
  }

}
