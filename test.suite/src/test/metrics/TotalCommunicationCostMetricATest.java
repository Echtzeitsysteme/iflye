package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import metrics.embedding.TotalCommunicationCostMetricA;
import model.SubstrateNetwork;

/**
 * Test class for the metric of total communication cost as implemented in
 * {@link TotalCommunicationCostMetricA}.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TotalCommunicationCostMetricATest extends ATotalCommunicationCostMetricTest {

  @Override
  protected void setMetric(final SubstrateNetwork sNet) {
    metric = new TotalCommunicationCostMetricA(sNet);
  }

  /*
   * Positive tests
   */

  @Test
  public void testEmbeddingTwoHops() {
    createTwoTierSubstrateNetwork();
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final TotalCommunicationCostMetricA metric = new TotalCommunicationCostMetricA(sNet);

    assertEquals(5 * 2 * 2 * 3, metric.getValue());
  }

}
