package metrics;

import model.SubstrateNetwork;

/**
 * Accepted virtual network request metrics. This one equals the number of virtual networks embedded
 * in a given substrate network.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class AcceptedVnrMetric implements IMetric {

  /**
   * SubstrateNetwork to get metrics from.
   */
  final SubstrateNetwork sNet;

  /**
   * Calculated value of this metric.
   */
  final int value;

  public AcceptedVnrMetric(final SubstrateNetwork sNet) {
    this.sNet = sNet;

    // value = number of guest networks in substrate network
    this.value = sNet.getGuests().size();
  }

  @Override
  public double getValue() {
    return value;
  }

}
