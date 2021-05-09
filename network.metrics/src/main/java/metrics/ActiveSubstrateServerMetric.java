package metrics;

import model.Node;
import model.SubstrateNetwork;
import model.SubstrateServer;

/**
 * Active substrate servers metric. This one equals the number of substrate servers that have one or
 * more embedded virtual networks on them.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ActiveSubstrateServerMetric implements IMetric {

  /**
   * Calculated value of this metric.
   */
  final int value;

  /**
   * Creates a new instance of this metric for the provided substrate network.
   * 
   * @param sNet Substrate network to calculate the metric for.
   */
  public ActiveSubstrateServerMetric(final SubstrateNetwork sNet) {
    int value = 0;

    for (final Node n : facade.getAllServersOfNetwork(sNet.getName())) {
      final SubstrateServer srv = (SubstrateServer) n;
      if (!srv.getGuestServers().isEmpty() || !srv.getGuestSwitches().isEmpty()
          || !srv.getGuestLinks().isEmpty()) {
        value++;
      }
    }

    this.value = value;
  }

  @Override
  public double getValue() {
    return this.value;
  }

}
