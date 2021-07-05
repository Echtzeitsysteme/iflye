package metrics;

import java.util.List;
import model.Link;
import model.Node;
import model.Server;
import model.SubstrateElement;
import model.SubstrateLink;
import model.SubstratePath;
import model.SubstrateServer;
import model.Switch;
import model.VirtualElement;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;

/**
 * Cost utility helper for the VneIlpPathAlgorithm and the VnePmMdvneAlgorithm.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class CostUtility {

  /**
   * Returns the total path cost for a link to path/server embedding.
   * 
   * Implementation of the cost function of paper [1].
   * 
   * [1] Tomaszek S., Leblebici E., Wang L., Schürr A. (2018) Virtual Network Embedding: Reducing
   * the Search Space by Model Transformation Techniques. In: Rensink A., Sánchez Cuadrado J. (eds)
   * Theory and Practice of Model Transformation. ICMT 2018. Lecture Notes in Computer Science, vol
   * 10888. Springer, Cham
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
   * Implementation of the cost function of paper [1].
   * 
   * [1] Tomaszek S., Leblebici E., Wang L., Schürr A. (2018) Virtual Network Embedding: Reducing
   * the Search Space by Model Transformation Techniques. In: Rensink A., Sánchez Cuadrado J. (eds)
   * Theory and Practice of Model Transformation. ICMT 2018. Lecture Notes in Computer Science, vol
   * 10888. Springer, Cham
   *
   * @param host Substrate element hosting the virtual link.
   * @return Cost for this particular mapping.
   */
  public static double getTotalPathCostLink(final SubstrateElement host) {
    if (host instanceof Server) {
      return 1;
    } else if (host instanceof SubstratePath) {
      final SubstratePath p = (SubstratePath) host;
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
   * Implementation of the cost function of paper [1].
   * 
   * [1] Tomaszek S., Leblebici E., Wang L., Schürr A. (2018) Virtual Network Embedding: Reducing
   * the Search Space by Model Transformation Techniques. In: Rensink A., Sánchez Cuadrado J. (eds)
   * Theory and Practice of Model Transformation. ICMT 2018. Lecture Notes in Computer Science, vol
   * 10888. Springer, Cham
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
   * Implementation of the cost function of paper [1].
   * 
   * [1] Meng, Xiaoqiao, Vasileios Pappas, and Li Zhang. "Improving the scalability of data center
   * networks with traffic-aware virtual machine placement." 2010 Proceedings IEEE INFOCOM. IEEE,
   * 2010.
   * 
   * @return Total communication cost for a node to node embedding.
   */
  public static double getTotalCommunicationCostNodeAB() {
    // Node to node placement does not increment the total communication cost metric
    return 0;
  }

  /**
   * Returns the adapted total communication cost for a node to node embedding. This one prefers
   * already filled up substrate servers over empty ones.
   * 
   * @param virtualElement Virtual node to embed.
   * @param substrateElement Substrate node to embed.
   * @return Cost for this particular mapping.
   */
  public static double getTotalCommunicationCostNodeC(final VirtualElement virtualElement,
      final SubstrateElement substrateElement) {
    if (virtualElement instanceof VirtualServer && substrateElement instanceof SubstrateServer) {
      // final VirtualServer vsrv = (VirtualServer) virtualElement;
      final SubstrateServer ssrv = (SubstrateServer) substrateElement;
      return 1.0 * ssrv.getResidualCpu() / ssrv.getCpu()
          + 1.0 * ssrv.getResidualMemory() / ssrv.getMemory()
          + 1.0 * ssrv.getResidualStorage() / ssrv.getStorage();
    }

    return 0;
  }

  /**
   * Returns the adapted total communication cost for a node to node embedding. This one prefers
   * empty substrate servers over non-empty ones.
   * 
   * @param virtualElement Virtual node to embed.
   * @param substrateElement Substrate node to embed.
   * @return Cost for this particular mapping.
   */
  public static double getTotalCommunicationCostNodeD(final VirtualElement virtualElement,
      final SubstrateElement substrateElement) {
    if (virtualElement instanceof VirtualServer && substrateElement instanceof SubstrateServer) {
      return 1.0 / getTotalCommunicationCostNodeC(virtualElement, substrateElement);
    }

    return 0;
  }

  /**
   * Returns the total communication cost for a link to element embedding as defined in [1].
   * 
   * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
   * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
   * 
   * @param virt Virtual link to embed.
   * @param host Substrate element hosting the virtual link.
   * @return Total communication cost for a link to element embedding.
   */
  public static double getTotalCommunicationCostLinkA(final VirtualLink virt,
      final SubstrateElement host) {
    if (host instanceof Server) {
      // Server -> 0 hops
      return 0;
    } else if (host instanceof SubstratePath) {
      final SubstratePath subPath = (SubstratePath) host;
      if (subPath.getHops() == 1) {
        // Path, 1 hop -> 1 hop * virtual bandwidth
        return virt.getBandwidth();
      } else if (subPath.getHops() > 1) {
        // Path, >1 hop -> 5 * virtual bandwidth
        return 5 * virt.getBandwidth();
      }
    } else if (host instanceof SubstrateLink) {
      // Link -> 1 hop * virtual bandwidth
      return virt.getBandwidth();
    }

    throw new IllegalArgumentException("Element not matched.");
  }

  /**
   * Returns the total communication cost for a link to list of elements embedding as defined in
   * [1].
   * 
   * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
   * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
   * 
   * @param virt Virtual link to embed.
   * @param hosts List of substrate elements hosting the virtual link.
   * @return Total communication cost for a link to element embedding.
   */
  public static double getTotalCommunicationCostLinkA(final VirtualLink virt,
      final List<SubstrateElement> hosts) {
    if (hosts.size() == 1) {
      // One host object -> call other cost method for calculation
      return getTotalCommunicationCostLinkA(virt, hosts.get(0));
    } else if (hosts.size() > 1) {
      // More than one host object -> 5 * virtual bandwidth
      return 5 * virt.getBandwidth();
    }

    throw new IllegalArgumentException("Element not matched.");
  }

  /**
   * Returns the total communication cost for a link to element embedding. In comparison to the
   * paper [1], we define the cost of one hop as 1 as stated in [2].
   * 
   * Implementation of the cost function of paper [1] and [2].
   * 
   * [1] Meng, Xiaoqiao, Vasileios Pappas, and Li Zhang. "Improving the scalability of data center
   * networks with traffic-aware virtual machine placement." 2010 Proceedings IEEE INFOCOM. IEEE,
   * 2010.
   * 
   * [2] M. G. Rabbani, R. P. Esteves, M. Podlesny, G. Simon, L. Z. Granville and R. Boutaba, "On
   * tackling virtual data center embedding problem," 2013 IFIP/IEEE International Symposium on
   * Integrated Network Management (IM 2013), 2013, pp. 177-184.
   * 
   * From the paper [1]: "For the sake of illustration, we define C_ij as the number of switches on
   * the routing path from VM i to j."
   * 
   * From the paper [2]: "We consider the hop-count between the virtual nodes (i.e., VM or virtual
   * switch) multiplied by the corresponding requested bandwidth of the virtual link connecting the
   * two virtual nodes"
   * 
   * @param virt Virtual link to embed.
   * @param host Substrate element hosting the virtual link.
   * @return Total communication cost for a link to element embedding.
   */
  public static double getTotalCommunicationCostLinkBCD(final VirtualLink virt,
      final SubstrateElement host) {
    if (host instanceof Server) {
      // Server -> 0 hops
      return 0;
    } else if (host instanceof SubstratePath) {
      // Path -> n hops * virtual bandwidth
      final SubstratePath subPath = (SubstratePath) host;
      return virt.getBandwidth() * subPath.getHops();
    } else if (host instanceof SubstrateLink) {
      // Link -> 1 hop * virtual bandwidth
      return virt.getBandwidth();
    }

    throw new IllegalArgumentException("Element not matched.");
  }

  /**
   * Returns the total communication cost for a link to list of elements embedding. In comparison to
   * the paper [1], we define the cost of one hop as 1 as stated in [2].
   * 
   * Implementation of the cost function of paper [1] and [2].
   * 
   * [1] Meng, Xiaoqiao, Vasileios Pappas, and Li Zhang. "Improving the scalability of data center
   * networks with traffic-aware virtual machine placement." 2010 Proceedings IEEE INFOCOM. IEEE,
   * 2010.
   * 
   * [2] M. G. Rabbani, R. P. Esteves, M. Podlesny, G. Simon, L. Z. Granville and R. Boutaba, "On
   * tackling virtual data center embedding problem," 2013 IFIP/IEEE International Symposium on
   * Integrated Network Management (IM 2013), 2013, pp. 177-184.
   * 
   * From the paper [1]: "For the sake of illustration, we define C_ij as the number of switches on
   * the routing path from VM i to j."
   * 
   * From the paper [2]: "We consider the hop-count between the virtual nodes (i.e., VM or virtual
   * switch) multiplied by the corresponding requested bandwidth of the virtual link connecting the
   * two virtual nodes"
   * 
   * @param virt Virtual link to embed.
   * @param hosts List of substrate elements hosting the virtual link.
   * @return Total communication cost for a link to element embedding.
   */
  public static double getTotalCommunicationCostLinkBCD(final VirtualLink virt,
      final List<SubstrateElement> hosts) {
    if (hosts.size() == 1) {
      // One host object -> call other cost method for calculation
      return getTotalCommunicationCostLinkBCD(virt, hosts.get(0));
    } else if (hosts.size() > 1) {
      // More than one host object -> n hops * virtual bandwidth
      return virt.getBandwidth() * hosts.size();
    }

    throw new IllegalArgumentException("Element not matched.");
  }

  /**
   * Returns the static costs to reject a virtual network.
   * 
   * @return Cost to reject a virtual network.
   */
  public static double getNetworkRejectionCost() {
    return 1_000_000;
  }

  /**
   * Returns the dynamic costs to reject a virtual network.
   * 
   * @param vNet Virtual network to calculate rejection cost for.
   * @return Cost to reject a virtual network.
   */
  public static double getNetworkRejectionCost(final VirtualNetwork vNet) {
    double cost = 0;
    final double bwFactor = 10;
    final double srvFactor = 10;

    for (final Node n : vNet.getNodes()) {
      if (n instanceof VirtualServer) {
        final VirtualServer vsrv = (VirtualServer) n;
        cost += srvFactor * (vsrv.getCpu() + vsrv.getMemory() + vsrv.getStorage());
      }
    }

    for (final Link l : vNet.getLinks()) {
      final VirtualLink vl = (VirtualLink) l;
      cost += bwFactor * vl.getBandwidth();
    }

    return cost;
  }

  /*
   * Traffic-amount first algorithm specific constants.
   */

  /**
   * Cost for a VNE inside one substrate server.
   */
  public static final int TAF_C_ALPHA = 1;

  /**
   * Cost for a VNE inside one substrate rack.
   */
  public static final int TAF_C_BETA = 3;

  /**
   * Cost for a VNE on multiple substrate racks.
   */
  public static final int TAF_C_GAMMA = 5;

}
