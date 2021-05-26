package algorithms;

import java.util.List;
import algorithms.ilp.VneIlpPathAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import model.Link;
import model.Path;
import model.Server;
import model.SubstrateElement;
import model.Switch;
import model.VirtualElement;

/**
 * Cost utility helper for {@link VneIlpPathAlgorithm} and {@link VnePmMdvneAlgorithm}.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class CostUtility {

  /**
   * Returns the cost for a link to path/server embedding.
   *
   * @param hosts List of substrate elements.
   * @return Cost for this particular mapping.
   */
  public static double getTotalPathCostLink(final List<SubstrateElement> hosts) {
    if (hosts.size() == 1) {
      if (hosts.get(0) instanceof Server) {
        return 1;
      } else if (hosts.get(0) instanceof Link) {
        return 2;
      }
      return 1;
    } else if (hosts.size() > 1) {
      return Math.pow(4, hosts.size());
    }

    throw new IllegalArgumentException("Element(s) not matched.");
  }

  /**
   * Returns the cost for a link to path/server embedding.
   *
   * @param host Substrate element hosting the virtual link.
   * @return Cost for this particular mapping.
   */
  public static double getTotalPathCostLink(final SubstrateElement host) {
    if (host instanceof Server) {
      return 1;
    } else if (host instanceof Path) {
      final Path p = (Path) host;
      if (p.getHops() == 1) {
        return 2;
      } else if (p.getHops() > 1) {
        return Math.pow(4, p.getHops());
      }
    }

    throw new IllegalArgumentException("Element not matched.");
  }

  /**
   * Returns the cost for a node to node embedding.
   * 
   * @param virtualElement Virtual node to embed.
   * @param substrateElement Substrate node to embed.
   * @return Cost for this particular mapping.
   */
  public static double getTotalPathCostNode(final VirtualElement virtualElement,
      final SubstrateElement substrateElement) {
    if (virtualElement instanceof Server) {
      if (substrateElement instanceof Server) {
        return 1;
      }
    } else if (virtualElement instanceof Switch) {
      if (substrateElement instanceof Switch) {
        return 1;
      } else if (substrateElement instanceof Server) {
        return 2;
      }
    }

    // return Integer.MAX_VALUE;
    return 0;
  }

  public static double getTotalCommunicationCostNode() {
    // TODO
    return -1;
  }

  public static double getTotalCommunicationCostLink() {
    // TODO
    return -1;
  }

  /**
   * Returns the costs to reject a virtual network.
   * 
   * @return Cost to reject a virtual network.
   */
  public static double getNetworkRejectionCost() {
    return 1_000_000;
  }

}
