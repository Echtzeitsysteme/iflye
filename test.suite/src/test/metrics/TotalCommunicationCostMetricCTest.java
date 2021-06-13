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

  @Override
  @Test
  public void testEmbeddingTwoHosts() {
    createSubstrateNetwork();
    setupEmbeddingTwoHosts();
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    setMetric(sNet);

    assertEquals(2 * 2 * 3 + 3, metric.getValue());
  }

}
