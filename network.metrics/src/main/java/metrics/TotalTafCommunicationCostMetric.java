package metrics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import algorithms.heuristics.TafAlgorithm;
import model.Network;
import model.Node;
import model.Server;
import model.SubstrateNetwork;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.Switch;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Implementation of the cost function of paper [1]. [1] Zeng, D., Guo, S., Huang, H., Yu, S., and
 * Leung, V. C.M., “Optimal VM Placement in Data Centers with Architectural and Resource
 * Constraints,” International Journal of Autonomous and Adaptive Communications Systems, vol. 8,
 * no. 4, pp. 392–406, 2015.
 *
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class TotalTafCommunicationCostMetric implements IMetric {

  /**
   * Calculated cost.
   */
  double cost;

  /**
   * Creates a new instance of this metric for the provided substrate network.
   * 
   * @param sNet Substrate network to calculate the metric for.
   */
  public TotalTafCommunicationCostMetric(final SubstrateNetwork sNet) {
    checkPreConditions(sNet);
    double cost = 0;

    // For all embedded virtual networks
    for (final VirtualNetwork guest : sNet.getGuests()) {
      final List<Node> guestServers = facade.getAllServersOfNetwork(guest.getName());

      // This metric needs a pair of all virtual servers to all other virtual servers. Therefore,
      // the implementation has to iterate over all guest servers twice.
      for (final Node source : guestServers) {
        for (final Node target : guestServers) {
          // Exclude source = target
          if (!source.equals(target)) {
            final int minBw = getMinBw((Server) source, (Server) target);
            final VirtualServer vSource = (VirtualServer) source;
            final VirtualServer vTarget = (VirtualServer) target;

            if (vSource.getHost().equals(vTarget.getHost())) {
              // source and target are embedded on the same substrate server
              // C_ALPHA = same substrate server
              cost += minBw * TafAlgorithm.C_ALPHA;
            } else if (isEmbeddedOnOneRack(guest)) {
              // C_BETA = same substrate rack
              cost += minBw * TafAlgorithm.C_BETA;
            } else {
              // C_GAMMA = else
              cost += minBw * TafAlgorithm.C_GAMMA;
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

  /**
   * Returns the minimum of the outgoing link bandwidth of two given servers.
   * 
   * @param a Server 1.
   * @param b Server 2.
   * @return Minimum of the outgoing link bandwidth of both servers.
   */
  private int getMinBw(final Server a, final Server b) {
    if (a.getOutgoingLinks().size() != 1 || b.getOutgoingLinks().size() != 1) {
      throw new UnsupportedOperationException("Both servers must have exactly one outgoing link!");
    }

    final int bwA = a.getOutgoingLinks().get(0).getBandwidth();
    final int bwB = b.getOutgoingLinks().get(0).getBandwidth();

    return Math.min(bwA, bwB);
  }

  /**
   * Returns true if the virtual network is embedded on one rack. In advanced: It returns true if
   * all virtual switches are embedded on substrate switches that have direct connections to the
   * substrate servers used by the embedding.
   * 
   * @param vNet
   * @return True if given virtual network is embedded on one rack only.
   */
  private boolean isEmbeddedOnOneRack(final VirtualNetwork vNet) {
    // Get all substrate servers hosting the virtual ones
    final Set<SubstrateServer> sServers = new HashSet<SubstrateServer>();
    for (final Node n : facade.getAllServersOfNetwork(vNet.getName())) {
      sServers.add(((VirtualServer) n).getHost());
    }

    // Iterate through all virtual switches
    for (final Node n : facade.getAllSwitchesOfNetwork(vNet.getName())) {
      final VirtualSwitch vSwitch = (VirtualSwitch) n;
      final SubstrateSwitch sSwitch = (SubstrateSwitch) vSwitch.getHost();

      // Check that the substrate switch has a direct connection to all substrate servers that host
      // the virtual ones
      if (!isSwitchConnectedToAllServers(sSwitch, sServers)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks all conditions for this metric. These are: (1) All servers must only have one outgoing
   * and one incoming link; (2) All servers must be connected to one switch only; (3) All virtual
   * networks must only have one layer of switches.
   * 
   * @param sNet Substrate network to check conditions for.
   */
  private void checkPreConditions(final SubstrateNetwork sNet) {
    /*
     * Substrate network
     */

    // (1) All servers must only have one outgoing and one incoming link
    // (2) All servers must be connected to one switch only
    checkAllServerConnections(sNet);


    /*
     * Virtual network
     */
    for (final VirtualNetwork vNet : sNet.getGuests()) {
      // (1) and (2)
      checkAllServerConnections(vNet);

      // (3) All virtual networks must only have one layer of switches
      checkOnlyOneLayerOfSwitches(vNet);
    }

  }

  /**
   * This method iterates over all servers of the given network and ensures that (1) all servers
   * have exactly one outgoing and one incoming link; (2) the server is connected to a switch.
   * 
   * @param net Network to check server conditions.
   */
  private void checkAllServerConnections(final Network net) {
    for (final Node n : facade.getAllServersOfNetwork(net.getName())) {
      // Only one outgoing link
      if (n.getOutgoingLinks().size() != 1) {
        throw new UnsupportedOperationException("There is more than one outgoing link.");
      }

      // Only one incoming link
      if (n.getIncomingLinks().size() != 1) {
        throw new UnsupportedOperationException("There is more than one incoming link.");
      }

      // Server must be connected to a switch
      if (!(n.getOutgoingLinks().get(0).getTarget() instanceof Switch)) {
        throw new UnsupportedOperationException(
            "There is a server that is not connected to a switch.");
      }
    }
  }

  /**
   * This method iterates over all switches of the given network and ensures that (3) all switches
   * are on the same layer.
   * 
   * @param net Network to check switch conditions.
   */
  private void checkOnlyOneLayerOfSwitches(final Network net) {
    for (final Node n : facade.getAllSwitchesOfNetwork(net.getName())) {
      if (n.getDepth() != 0) {
        throw new UnsupportedOperationException(
            "There is a switch thats depth is not equal to zero.");
      }
    }
  }

  /**
   * Returns true if a given substrate switch is connected to every substrate server of a given set.
   * 
   * @param sw Substrate switch to test.
   * @param srvs Set of substrate servers to test.
   * @return True if substrate switch is connected to every given server.
   */
  private boolean isSwitchConnectedToAllServers(final SubstrateSwitch sw,
      final Set<SubstrateServer> srvs) {
    for (final SubstrateServer srv : srvs) {
      if (!srv.getOutgoingLinks().get(0).getTarget().equals(sw)
          || !srv.getIncomingLinks().get(0).getSource().equals(sw)) {
        return false;
      }
    }

    return true;
  }

}
