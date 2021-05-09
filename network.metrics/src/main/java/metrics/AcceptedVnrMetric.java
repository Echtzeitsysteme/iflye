package metrics;

import model.SubstrateNetwork;

/**
 * Accepted virtual network request metrics. This one equals the number of virtual networks embedded
 * in a given substrate network.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class AcceptedVnrMetric implements IMetric {

  /**
   * Calculated value of this metric.
   */
  final int value;

  /**
   * Creates a new instance of this metric for the provided substrate network.
   * 
   * @param sNet Substrate network to calculate the metric for.
   */
  public AcceptedVnrMetric(final SubstrateNetwork sNet) {
    // value = number of guest networks in substrate network
    this.value = sNet.getGuests().size();
  }

  @Override
  public double getValue() {
    return value;
  }

}
