package metrics;

import model.Link;
import model.SubstrateNetwork;
import model.VirtualLink;
import model.VirtualNetwork;

/**
 * Average path length metric. This one equals the sum of all substrate links from substrate paths
 * with embedded virtual links on them divided by the number of virtual links.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class AveragePathLengthMetric implements IMetric {

  /**
   * Calculated value of this metric.
   */
  final double value;

  /**
   * Creates a new instance of this metric for the provided substrate network.
   * 
   * @param sNet Substrate network to calculate the metric for.
   */
  public AveragePathLengthMetric(final SubstrateNetwork sNet) {
    int allSubstrateLinks = 0;
    int allVirtualLinks = 0;

    // Collect all virtual links of all virtual networks that are embedded to sNet
    for (final VirtualNetwork actVNet : sNet.getGuests()) {
      for (final Link l : actVNet.getLinks()) {
        final VirtualLink vl = (VirtualLink) l;
        allVirtualLinks++;
        allSubstrateLinks += vl.getHosts().size();
      }
    }

    this.value = allVirtualLinks == 0 ? 0 : allSubstrateLinks * 1.0 / allVirtualLinks;
  }

  @Override
  public double getValue() {
    return this.value;
  }

}
