package algorithms.simple;

import java.util.List;
import facade.ModelFacade;
import model.Link;
import model.Node;
import model.SubstrateServer;
import model.VirtualServer;

/**
 * Super simple Virtual Network Embedding algorithm. It searches for the substrate server with
 * largest residual amount of resources and checks if the whole virtual network could fit onto it.
 * If it does not, the algorithm is unable to embed the request. The resources are added all
 * together.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class SimpleVne {

  /**
   * ModelFacade instance.
   */
  static private ModelFacade facade = ModelFacade.getInstance();

  public static boolean execute(final String substrateId, final String virtualId) {
    final List<Node> subServers = facade.getAllServersOfNetwork(substrateId);
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

    for (Node actNode : facade.getAllServersOfNetwork(virtualId)) {
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
    success &= facade.embedNetworkToNetwork(substrateId, virtualId);

    // Servers
    for (Node act : facade.getAllServersOfNetwork(virtualId)) {
      success &= facade.embedServerToServer(largestServerId, act.getName());
    }

    // Switches
    for (Node act : facade.getAllSwitchesOfNetwork(virtualId)) {
      success &= facade.embedSwitchToNode(largestServerId, act.getName());
    }

    // Links
    for (Link act : facade.getAllLinksOfNetwork(virtualId)) {
      success &= facade.embedLinkToServer(largestServerId, act.getName());
    }

    return success;
  }

}
