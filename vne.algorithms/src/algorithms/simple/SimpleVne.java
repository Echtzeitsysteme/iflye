package algorithms.simple;

import java.util.List;
import algorithms.AbstractAlgorithm;
import model.Link;
import model.Node;
import model.SubstrateNetwork;
import model.SubstrateServer;
import model.VirtualNetwork;
import model.VirtualServer;

/**
 * Super simple Virtual Network Embedding algorithm. It searches for the substrate server with
 * largest residual amount of resources and checks if the whole virtual network could fit onto it.
 * If it does not, the algorithm is unable to embed the request. The resources are added all
 * together.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class SimpleVne extends AbstractAlgorithm {

  /**
   * Initializes a new object of this simple VNE algorithm.
   * 
   * @param sNet Substrate network to work with.
   * @param vNet Virtual network to work with.
   */
  public SimpleVne(final SubstrateNetwork sNet, final VirtualNetwork vNet) {
    super(sNet, vNet);
  }

  @Override
  public boolean execute() {
    final List<Node> subServers = facade.getAllServersOfNetwork(sNet.getName());
    String largestServerId = "";
    int largestServerRes = Integer.MIN_VALUE;

    for (Node actNode : subServers) {
      final SubstrateServer actServer = (SubstrateServer) actNode;
      final int resSum = actServer.getResidualCpu() + actServer.getResidualMemory()
          + actServer.getResidualStorage();
      if (largestServerRes < resSum) {
        largestServerRes = resSum;
        largestServerId = actServer.getName();
      }
    }

    // Check if embedding is possible
    int summedCpu = 0;
    int summedMem = 0;
    int summedStor = 0;

    for (Node actNode : facade.getAllServersOfNetwork(vNet.getName())) {
      final VirtualServer actServer = (VirtualServer) actNode;
      summedCpu += actServer.getCpu();
      summedMem += actServer.getMemory();
      summedMem += actServer.getStorage();
    }

    final SubstrateServer largestSubServer =
        (SubstrateServer) facade.getServerById(largestServerId);

    if (!(summedCpu <= largestSubServer.getResidualCpu()
        && summedMem <= largestSubServer.getResidualMemory()
        && summedStor <= largestSubServer.getResidualStorage())) {
      System.out.println("=> SimpleVne: Embedding not possible due to resource constraints.");
      return false;
    }

    /*
     * Place embedding on model
     */

    boolean success = true;

    // Network
    success &= facade.embedNetworkToNetwork(sNet.getName(), vNet.getName());

    // Servers
    for (Node act : facade.getAllServersOfNetwork(vNet.getName())) {
      success &= facade.embedServerToServer(largestServerId, act.getName());
    }

    // Switches
    for (Node act : facade.getAllSwitchesOfNetwork(vNet.getName())) {
      success &= facade.embedSwitchToNode(largestServerId, act.getName());
    }

    // Links
    for (Link act : facade.getAllLinksOfNetwork(vNet.getName())) {
      success &= facade.embedLinkToServer(largestServerId, act.getName());
    }

    return success;
  }

}
