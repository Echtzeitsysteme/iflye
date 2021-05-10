package metrics;

import java.util.List;
import model.Node;
import model.Path;
import model.SubstrateNetwork;
import model.SubstrateServer;
import model.VirtualNetwork;
import model.VirtualServer;

/**
 * Implementation of the cost function of paper [1]. [1] Meng, Xiaoqiao, Vasileios Pappas, and Li
 * Zhang. "Improving the scalability of data center networks with traffic-aware virtual machine
 * placement." 2010 Proceedings IEEE INFOCOM. IEEE, 2010.
 * 
 * "For the sake of illustration, we define C_ij as the number of switches on the routing path from
 * VM i to j. With such a definition, the objective function is the sum of the traffic rate
 * perceived by every switch."
 * 
 * "D_ij denotes traffic rate from VM i to j."
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TotalCommunicationCostMetric implements IMetric {

  /**
   * Calculated cost.
   */
  private double cost;

  /**
   * Creates a new instance of this metric for the provided substrate network.
   * 
   * @param sNet Substrate network to calculate the metric for.
   */
  public TotalCommunicationCostMetric(final SubstrateNetwork sNet) {
    double cost = 0;

    // Iterate over all virtual networks that are embedded on the substrate network
    for (final VirtualNetwork vNet : sNet.getGuests()) {
      final List<Node> guestServers = facade.getAllServersOfNetwork(vNet.getName());

      // Check every connection from one virtual server to every other virtual server
      for (final Node source : guestServers) {
        for (final Node target : guestServers) {
          if (!source.equals(target)) {
            if (source.getOutgoingLinks().size() != 1) {
              throw new UnsupportedOperationException(
                  "Source server has more than one outgoing link.");
            }

            // The virtual bandwidth of the source is the bandwidth of its outgoing link
            final int sourceBw = source.getOutgoingLinks().get(0).getBandwidth();

            // Get host servers for both the source virtual server and the target virtual server
            final SubstrateServer sourceHost = ((VirtualServer) source).getHost();
            final SubstrateServer targetHost = ((VirtualServer) target).getHost();

            // Get path from source substrate to source target virtual server
            final Path p = facade.getPathFromSourceToTarget(sourceHost, targetHost);

            // If a path exists
            if (p != null) {
              // Incremental cost is the number of switches = number of hops - 1 times the source
              // bandwidth
              cost += ((p.getHops() - 1) * sourceBw);
            } else if (sourceHost.equals(targetHost)) {
              // If source and target host are the same, the cost must not be incremented.
              // (Embedding on the same host has cost = 0.)
            } else {
              throw new UnsupportedOperationException(
                  "There is no path between source and target host. "
                      + "Maybe, the configuration of the path generation is wrong.");
            }
          }
        }
      }
    }

    this.cost = cost;
  }

  @Override
  public double getValue() {
    return cost;
  }

}
