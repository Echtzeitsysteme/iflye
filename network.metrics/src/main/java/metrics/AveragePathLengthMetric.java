package metrics;

import model.Element;
import model.Link;
import model.SubstrateNetwork;
import model.SubstratePath;
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
    int allEmbeddedVirtualLinks = 0;

    // Collect all virtual links of all virtual networks that are embedded to sNet
    for (final VirtualNetwork actVNet : sNet.getGuests()) {
      for (final Link l : actVNet.getLinks()) {
        final VirtualLink vl = (VirtualLink) l;

        if (vl.getHosts().size() > 1) {
          throw new UnsupportedOperationException(
              "Embedding of virtual links to more than one substrate element "
                  + "is currently not supported by this metric.");
        } else if (vl.getHosts().size() == 0) {
          continue;
        }

        allEmbeddedVirtualLinks++;
        final Element e = vl.getHosts().get(0);

        if (e instanceof Link) {
          allSubstrateLinks += 1;
        } else if (e instanceof SubstratePath) {
          allSubstrateLinks += ((SubstratePath) e).getHops();
        }

      }
    }

    this.value =
        (allEmbeddedVirtualLinks == 0) ? 0 : allSubstrateLinks * 1.0 / allEmbeddedVirtualLinks;
  }

  @Override
  public double getValue() {
    return this.value;
  }

}
