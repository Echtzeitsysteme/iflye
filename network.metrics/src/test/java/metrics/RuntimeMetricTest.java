package metrics;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.Test;

/**
 * Test class for the metric of runtime.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class RuntimeMetricTest extends AMetricTest {

  /*
   * Positive tests
   */

  @Test
  public void testTimeGreaterZero() {
    final RuntimeMetric metric = new RuntimeMetric();
    metric.stop();
    assertTrue(metric.getValue() > 0);
  }

  @Test
  public void testTimeFrame() {
    final RuntimeMetric metric = new RuntimeMetric();
    try {
      Thread.sleep(10);
    } catch (final InterruptedException e) {
      fail();
    }
    metric.stop();
    assertTrue(metric.getValue() > 10 * 1_000_000);
  }

  @Test
  public void testGetMilliSeconds() {
    final RuntimeMetric metric = new RuntimeMetric();
    try {
      Thread.sleep(1);
    } catch (final InterruptedException e) {
      fail();
    }
    metric.stop();
    assertTrue(metric.getMilliSeconds() > 1);
    assertTrue(metric.getMilliSeconds() < 2);
  }


  /*
   * Negative tests
   */

  @Test
  public void testNotSopped() {
    final RuntimeMetric metric = new RuntimeMetric();

    assertThrows(UnsupportedOperationException.class, () -> {
      metric.getValue();
    });
  }

}
