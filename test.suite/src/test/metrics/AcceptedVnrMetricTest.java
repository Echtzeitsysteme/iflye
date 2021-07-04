package test.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import metrics.embedding.AcceptedVnrMetric;
import model.SubstrateNetwork;

/**
 * Test class for the metric of accepted virtual network requests.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class AcceptedVnrMetricTest extends AMetricTest {

  @Test
  public void testNoEmbeddings() {
    facade.addNetworkToRoot("sub", false);
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final AcceptedVnrMetric metric = new AcceptedVnrMetric(sNet);
    assertEquals(0, metric.getValue());
  }

  @Test
  public void testWithOneEmbedding() {
    facade.addNetworkToRoot("sub", false);
    facade.addNetworkToRoot("virt", true);
    facade.embedNetworkToNetwork("sub", "virt");
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final AcceptedVnrMetric metric = new AcceptedVnrMetric(sNet);
    assertEquals(1, metric.getValue());
  }

  @Test
  public void testWithMoreEmbeddings() {
    facade.addNetworkToRoot("sub", false);

    for (int i = 1; i < 11; i++) {
      facade.addNetworkToRoot("virt_" + i, true);
      facade.embedNetworkToNetwork("sub", "virt_" + i);
    }

    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

    final AcceptedVnrMetric metric = new AcceptedVnrMetric(sNet);
    assertEquals(10, metric.getValue());
  }

}
