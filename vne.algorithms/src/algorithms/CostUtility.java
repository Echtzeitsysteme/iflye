package algorithms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import algorithms.ilp.VneIlpPathAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import model.Link;
import model.Node;
import model.Path;
import model.Server;
import model.SubstrateElement;
import model.SubstrateLink;
import model.SubstratePath;
import model.SubstrateSwitch;
import model.Switch;
import model.VirtualElement;
import model.VirtualLink;

/**
 * Cost utility helper for {@link VneIlpPathAlgorithm} and {@link VnePmMdvneAlgorithm}.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class CostUtility {

  /**
   * Returns the total path cost for a link to path/server embedding.
   * 
   * Implementation of the cost function of paper [1]. [1] Tomaszek S., Leblebici E., Wang L.,
   * Schürr A. (2018) Virtual Network Embedding: Reducing the Search Space by Model Transformation
   * Techniques. In: Rensink A., Sánchez Cuadrado J. (eds) Theory and Practice of Model
   * Transformation. ICMT 2018. Lecture Notes in Computer Science, vol 10888. Springer, Cham
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
   * Returns the total path cost for a link to path/server embedding.
   * 
   * Implementation of the cost function of paper [1]. [1] Tomaszek S., Leblebici E., Wang L.,
   * Schürr A. (2018) Virtual Network Embedding: Reducing the Search Space by Model Transformation
   * Techniques. In: Rensink A., Sánchez Cuadrado J. (eds) Theory and Practice of Model
   * Transformation. ICMT 2018. Lecture Notes in Computer Science, vol 10888. Springer, Cham
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
   * Returns the total path cost for a node to node embedding.
   * 
   * Implementation of the cost function of paper [1]. [1] Tomaszek S., Leblebici E., Wang L.,
   * Schürr A. (2018) Virtual Network Embedding: Reducing the Search Space by Model Transformation
   * Techniques. In: Rensink A., Sánchez Cuadrado J. (eds) Theory and Practice of Model
   * Transformation. ICMT 2018. Lecture Notes in Computer Science, vol 10888. Springer, Cham
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

  /**
   * Returns the total communication cost for a node to node embedding (cost = 0).
   * 
   * Implementation of the cost function of paper [1]. [1] Meng, Xiaoqiao, Vasileios Pappas, and Li
   * Zhang. "Improving the scalability of data center networks with traffic-aware virtual machine
   * placement." 2010 Proceedings IEEE INFOCOM. IEEE, 2010.
   * 
   * @return Total communication cost for a node to node embedding.
   */
  public static double getTotalCommunicationCostNode() {
    // Node to node placement does not increment the total communication cost metric
    return 0;
  }

  /**
   * Returns the total communication cost for a link to element embedding.
   * 
   * Implementation of the cost function of paper [1]. [1] Meng, Xiaoqiao, Vasileios Pappas, and Li
   * Zhang. "Improving the scalability of data center networks with traffic-aware virtual machine
   * placement." 2010 Proceedings IEEE INFOCOM. IEEE, 2010.
   * 
   * From the paper: "For the sake of illustration, we define C_ij as the number of switches on the
   * routing path from VM i to j.
   * 
   * @param virt Virtual link to embed.
   * @param host Substrate element hosting the virtual link.
   * @return Total communication cost for a link to element embedding.
   */
  public static double getTotalCommunicationCostLink(final VirtualLink virt,
      final SubstrateElement host) {
    if (host instanceof Server) {
      return 0;
    } else if (host instanceof SubstratePath) {
      final SubstratePath subPath = (SubstratePath) host;

      int switches = 0;

      // Get number of switches in the path
      for (final Node n : subPath.getNodes()) {
        if (n instanceof Switch) {
          switches++;
        }
      }

      return virt.getBandwidth() * switches;
    }

    throw new IllegalArgumentException("Element not matched.");
  }

  /**
   * Returns the total communication cost for a link to list of elements embedding.
   * 
   * Implementation of the cost function of paper [1]. [1] Meng, Xiaoqiao, Vasileios Pappas, and Li
   * Zhang. "Improving the scalability of data center networks with traffic-aware virtual machine
   * placement." 2010 Proceedings IEEE INFOCOM. IEEE, 2010.
   * 
   * From the paper: "For the sake of illustration, we define C_ij as the number of switches on the
   * routing path from VM i to j.
   * 
   * @param virt Virtual link to embed.
   * @param hosts List of substrate elements hosting the virtual link.
   * @return Total communication cost for a link to element embedding.
   */
  public static double getTotalCommunicationCostLink(final VirtualLink virt,
      final List<SubstrateElement> hosts) {
    if (hosts.size() == 1) {
      return getTotalCommunicationCostLink(virt, hosts.get(0));
    } else if (hosts.size() > 1) {
      // Using a set to filter out duplicate switches
      final Set<SubstrateSwitch> switches = new HashSet<SubstrateSwitch>();
      hosts.stream().forEach(h -> {
        final SubstrateLink l = (SubstrateLink) h;
        if (l.getSource() instanceof SubstrateSwitch) {
          switches.add((SubstrateSwitch) l.getSource());
        }

        if (l.getTarget() instanceof SubstrateSwitch) {
          switches.add((SubstrateSwitch) l.getTarget());
        }
      });

      return virt.getBandwidth() * switches.size();
    }

    throw new IllegalArgumentException("Element not matched.");
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
