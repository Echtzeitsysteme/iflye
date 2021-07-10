package algorithms.heuristics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import facade.config.ModelFacadeConfig;
import metrics.CostUtility;
import metrics.manager.GlobalMetricsManager;
import model.Link;
import model.Node;
import model.Server;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;
import model.SubstratePath;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.Switch;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;

/**
 * Implementation of the TAF algorithm of the paper [1]. Please note:
 * <ul>
 * <li>The link bandwidths are not taken into account.</li>
 * <li>The mapping of a virtual link to a substrate link or path is not specified by the TAF
 * algorithm.</li>
 * </ul>
 * 
 * Parts of this implementation are heavily inspired, taken or adapted from the idyve project [2].
 * 
 * [1] Zeng, D., Guo, S., Huang, H., Yu, S., and Leung, V. C.M., “Optimal VM Placement in Data
 * Centers with Architectural and Resource Constraints,” International Journal of Autonomous and
 * Adaptive Communications Systems, vol. 8, no. 4, pp. 392–406, 2015.
 * 
 * [2] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
 * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
 *
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TafAlgorithm extends AbstractAlgorithm {

  /*
   * Data from model (will be imported in initialization method).
   */

  /**
   * All virtual links of the virtual network.
   */
  private final List<VirtualLink> virtualLinks = new LinkedList<VirtualLink>();

  /**
   * All virtual servers of the virtual network.
   */
  private final List<VirtualServer> virtualServers = new LinkedList<VirtualServer>();

  /**
   * All substrate servers of the substrate network.
   */
  private final List<SubstrateServer> substrateServers = new LinkedList<SubstrateServer>();

  /**
   * Map of virtual -> substrate server.
   */
  private final Map<VirtualServer, SubstrateServer> placedVms =
      new HashMap<VirtualServer, SubstrateServer>();

  /**
   * Model of the TAF communication cost. This is only a data type without logic. It is needed for
   * the ordering of substrate servers depending on their communication cost.
   */
  private class TafCommunicationCost implements Comparable<TafCommunicationCost> {

    /**
     * Communication cost.
     */
    private final double communicationCost;

    /**
     * Substrate server.
     */
    private final SubstrateServer substrateServer;

    /**
     * Constructor.
     *
     * @param communicationCost Communication cost.
     * @param substrateServer Substrate server.
     */
    public TafCommunicationCost(final double communicationCost,
        final SubstrateServer substrateServer) {
      super();
      this.communicationCost = communicationCost;
      this.substrateServer = substrateServer;
    }

    @Override
    public int compareTo(final TafCommunicationCost obj) {
      return (int) (communicationCost - obj.getCommunicationCost());
    }

    public double getCommunicationCost() {
      return communicationCost;
    }

    public SubstrateServer getSubstrateServer() {
      return substrateServer;
    }

  }

  /**
   * Model of the TAF T vector. This is only a data type without logic.
   */
  private class TafTVectorData implements Comparable<TafTVectorData> {
    /**
     * Source server of the data set.
     */
    private final Server sourceServer;

    /**
     * Target server of the data set.
     */
    private final Server targetServer;

    /**
     * Bandwidth between the two servers.
     */
    private final int bandwidth;

    /**
     * Constructor.
     *
     * @param sourceServer Source server.
     * @param targetServer Target server.
     * @param bandwidth Bandwidth between source and target.
     */
    public TafTVectorData(final Server sourceServer, final Server targetServer,
        final int bandwidth) {
      super();
      this.sourceServer = sourceServer;
      this.targetServer = targetServer;
      this.bandwidth = bandwidth;
    }

    @Override
    public int compareTo(final TafTVectorData obj) {
      return bandwidth - obj.getBandwidth();
    }

    public int getBandwidth() {
      return bandwidth;
    }

    public Server getSourceServer() {
      return sourceServer;
    }

    public Server getTargetServer() {
      return targetServer;
    }

  }

  /**
   * Public constructor that initializes the instance of this algorithm.
   * 
   * @param sNet Substrate network to embed virtual network in.
   * @param vNets Virtual network to generate embedding for.
   */
  public TafAlgorithm(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    super(sNet, vNets);

    if (vNets.size() != 1) {
      throw new IllegalArgumentException(
          "The TAF algorithm is only suited for one virtual network at a time.");
    }

    // Add virtual links from model
    final List<Link> vLinks = facade.getAllLinksOfNetwork(getFirstVnet().getName());
    for (final Link l : vLinks) {
      virtualLinks.add((VirtualLink) l);
    }

    // Add virtual servers from model
    final List<Node> vServers = facade.getAllServersOfNetwork(getFirstVnet().getName());
    for (final Node n : vServers) {
      virtualServers.add((VirtualServer) n);
    }

    // Add substrate servers from model
    final List<Node> sServers = facade.getAllServersOfNetwork(sNet.getName());
    for (final Node n : sServers) {
      substrateServers.add((SubstrateServer) n);
    }

    // Check pre-conditions
    checkPreConditions();
  }

  /**
   * Checks every condition necessary to run this algorithm. If a condition is not met, it throws an
   * UnsupportedOperationException.
   */
  private void checkPreConditions() {
    // Every substrate server must be connected to exactly one switch
    for (final SubstrateServer s : substrateServers) {
      if (s.getOutgoingLinks().size() != 1) {
        throw new UnsupportedOperationException(
            "Substrate server connected to more than one other node.");
      }
    }

    // There has to be more than one virtual server
    if (virtualServers.size() <= 1) {
      throw new UnsupportedOperationException("There are not enough virtual servers available.");
    }

    // Path creation has to be enabled for paths with length = 1
    if (ModelFacadeConfig.MIN_PATH_LENGTH != 1) {
      throw new UnsupportedOperationException("Minimum path length must be 1.");
    }

    // Bandwidth ignore must be true
    if (!ModelFacadeConfig.IGNORE_BW) {
      throw new UnsupportedOperationException("Bandwidth ignore flag must be set.");
    }

    // Objective
    if (AlgorithmConfig.obj != Objective.TOTAL_TAF_COMMUNICATION_COST) {
      throw new IllegalArgumentException("The TAF algorithm can only optimize its own metric.");
    }

    // There must be generated substrate paths
    if (sNet.getPaths().isEmpty()) {
      throw new UnsupportedOperationException("Generated paths are missing in substrate network.");
    }

    // TODO: Maybe check for total amount of paths here!
  }

  /**
   * Starts the algorithm and embeds the generated mapping in the model.
   * 
   * @return True if execution was successful and a valid embedding was found.
   */
  @Override
  public boolean execute() {
    GlobalMetricsManager.measureMemory();

    final boolean success = algorithm1();
    GlobalMetricsManager.dummyMemory();
    if (success) {
      GlobalMetricsManager.startDeployTime();
      embed();
      GlobalMetricsManager.endDeployTime();
    }

    GlobalMetricsManager.measureMemory();
    return success;
  }

  /**
   * Embeds the calculated mappings in the model.
   */
  private void embed() {
    // Network
    facade.embedNetworkToNetwork(sNet.getName(), getFirstVnet().getName());

    // Embed all servers
    for (final Entry<VirtualServer, SubstrateServer> m : placedVms.entrySet()) {
      facade.embedServerToServer(m.getValue().getName(), m.getKey().getName());
    }

    // Embed all links and the switch
    final String vSwitchId =
        facade.getAllSwitchesOfNetwork(getFirstVnet().getName()).get(0).getName();

    if (allVirtualServersToOneSubstrateServer()) {
      // If the virtual network can be placed onto one substrate server
      // Switch
      final Iterator<SubstrateServer> sServerIt = placedVms.values().iterator();
      final String sServerId = sServerIt.next().getName();
      facade.embedSwitchToNode(sServerId, vSwitchId);

      // Links
      for (final VirtualLink l : virtualLinks) {
        facade.embedLinkToServer(sServerId, l.getName());
      }
    } else {
      // If the virtual network can *not* be placed onto one substrate server
      SubstrateSwitch commonSwitch = null;

      if (allVirtualServersToOneRack()) {
        // Case embedding on one rack
        // Get lowest common substrate switch -> Place virtual switch there
        commonSwitch = getLowestCommonSwitch(placedVms.values());
      } else {
        // Case embedding on multiple racks
        commonSwitch = getHighestCommonSwitch(placedVms.values());
      }

      facade.embedSwitchToNode(commonSwitch.getName(), vSwitchId);

      // Get links from servers to that switch -> Embed virtual links onto them
      for (final VirtualLink l : virtualLinks) {
        Node source = null;
        Node target = null;

        // links source = server -> links target = switch
        if (l.getSource() instanceof Server) {
          source = placedVms.get(l.getSource());
          target = commonSwitch;
        } else {
          // links source = switch -> links target = server
          source = commonSwitch;
          target = placedVms.get(l.getTarget());

        }

        // Forward only, because all backward links are part of the collection virtualLinks
        final SubstratePath sPath =
            facade.getPathFromSourceToTarget((SubstrateNode) source, (SubstrateNode) target);
        // final Set<Link> sLinks = facade.getAllLinksFromPath(sPath);
        facade.embedLinkToPath(sPath.getName(), l.getName());
      }
    }
  }

  /**
   * Returns the lowest common switch for a given collection of substrate servers.
   * 
   * @param serverCol Collection of substrate servers.
   * @return Lowest common switch for a given collection of substrate servers.
   */
  private SubstrateSwitch getLowestCommonSwitch(final Collection<SubstrateServer> serverCol) {
    final List<SubstrateServer> servers = new LinkedList<SubstrateServer>(serverCol);
    final Set<Switch> switches = new HashSet<Switch>();

    // Search for all switches that are part of the new embedding
    for (final SubstrateServer s : servers) {
      for (final Link l : s.getOutgoingLinks()) {
        if (l.getTarget() instanceof Switch) {
          switches.add((Switch) l.getTarget());
        }
      }
    }

    // Find lowest placed switch (highest depth value)
    int lowestDepth = Integer.MIN_VALUE;
    Switch lowestSwitch = null;
    for (final Switch s : switches) {
      if (s.getDepth() > lowestDepth) {
        lowestDepth = s.getDepth();
        lowestSwitch = s;
      }
    }

    return (SubstrateSwitch) lowestSwitch;
  }

  /**
   * Returns the highest common switch for a given collection of substrate servers.
   * 
   * @param serverCol Collection of substrate servers.
   * @return Highest common switch for a given collection of substrate servers.
   */
  private SubstrateSwitch getHighestCommonSwitch(final Collection<SubstrateServer> serverCol) {
    final List<SubstrateServer> servers = new LinkedList<SubstrateServer>(serverCol);
    final Set<Switch> switches = new HashSet<Switch>();

    // Search for all switches that are part of the new embedding
    for (final SubstrateServer s : servers) {
      for (final SubstratePath p : s.getOutgoingPaths()) {
        switches.addAll(p.getNodes().stream().filter(Switch.class::isInstance)
            .map(Switch.class::cast).collect(Collectors.toSet()));
      }
    }

    // Find highest placed switch (lowest depth value)
    int lowestDepth = Integer.MAX_VALUE;
    Switch lowestSwitch = null;
    for (final Switch s : switches) {
      if (s.getDepth() < lowestDepth) {
        lowestDepth = s.getDepth();
        lowestSwitch = s;
      }
    }

    return (SubstrateSwitch) lowestSwitch;
  }

  /**
   * Algorithm 1 of paper [1]. The TAF Algorithm.
   */
  private boolean algorithm1() {
    // Require: pairwise traffic rate: T
    // 1: Sort all elements in matrix T to a vector T in a decreasing order
    final List<TafTVectorData> tvector = createTvector();

    // 2: While at least one VM has not been placed do
    while (!placedVms.keySet().containsAll(virtualServers)) {

      // 3: Let Vi and Vj be the PM pair with maximum rate Tij in the head of T
      final TafTVectorData nextPair = tvector.get(0);
      final VirtualServer serverVi = (VirtualServer) nextPair.getSourceServer();
      final VirtualServer serverVj = (VirtualServer) nextPair.getTargetServer();

      // 4: If both Vi and Vj have not been placed then
      if (!placedVms.containsKey(serverVi) && !placedVms.containsKey(serverVj)) {

        // 5: Place Vi and Vj to the target server Sd found by algorithm 2(Vi; Vj)
        final SubstrateServer serverSd = algorithm2(Arrays.asList(serverVi, serverVj));

        // 6: If Sd == null then
        if (serverSd == null) {

          // 7: Place Vi to Sd found by algorithm 2(Vi)
          final SubstrateServer substrateServerForVi = algorithm2(Arrays.asList(serverVi));
          if (substrateServerForVi == null) {
            return false;
          }
          placedVms.put(serverVi, substrateServerForVi);

          // 8: Place Vj to Sd found by algorithm 2(Vj)
          final SubstrateServer substrateServerForVj = algorithm2(Arrays.asList(serverVj));
          if (substrateServerForVj == null) {
            return false;
          }
          placedVms.put(serverVj, substrateServerForVj);

          // 9: End if
        } else {
          placedVms.put(serverVi, serverSd);
          placedVms.put(serverVj, serverSd);
        }

        // 10: Else if only Vi has already been placed then
      } else if (placedVms.containsKey(serverVi) && !placedVms.containsKey(serverVj)) {

        // 11: Place Vj to Sd found by algorithm 2(Vj)
        final SubstrateServer substrateServerForVj = algorithm2(Arrays.asList(serverVj));
        if (substrateServerForVj == null) {
          return false;
        }
        placedVms.put(serverVj, substrateServerForVj);

        // 12: Else if only Vj has already been placed then
      } else if (!placedVms.containsKey(serverVi) && placedVms.containsKey(serverVj)) {

        // 13: Place Vi to Sd found by algorithm 2(Vi)
        final SubstrateServer substrateServerForVi = algorithm2(Arrays.asList(serverVi));
        if (substrateServerForVi == null) {
          return false;
        }
        placedVms.put(serverVi, substrateServerForVi);

        // 14: End if
      }

      // 15: Remove Tij from T
      tvector.remove(nextPair);

      // 16: End while
    }

    return true;
  }

  /**
   * Algorithm 2 of paper [1]. Find the server with minimal incremental communication cost.
   *
   * @param listServerV Virtual servers
   * @return The server to embed the virtual server
   */
  private SubstrateServer algorithm2(final List<VirtualServer> listServerV) {
    SubstrateServer serverSd = null;

    // Require: A set of VMs V to be placed
    // Ensure: Target server Sd

    // 1: Sc <- empty list
    final TreeSet<TafCommunicationCost> serverSc = new TreeSet<>();

    // 2: For all server S with enough residual resources satisfying all VMs in V do
    final List<SubstrateServer> serverSEnoughResources = getSubstrateServerCandidates(listServerV);
    sortServerCollection(serverSEnoughResources);

    for (final SubstrateServer s : serverSEnoughResources) {

      // 3: Calculate the incremental communication cost U after placing them on S
      final double incCommCost = calcIncrComCost(listServerV, s);

      // 4: Add S into the candidate server set Sc
      serverSc.add(new TafCommunicationCost(incCommCost, s));

      // 5: End for
    }

    // 6: If Sc is not empty; the
    if (!serverSc.isEmpty()) {

      // 7: Sd = arg minS2Sc U
      serverSd = serverSc.first().getSubstrateServer();
    } else {
      // 8: Else
      // 9: Sd null
      serverSd = null;

      // 10: End if
    }

    return serverSd;
  }

  /*
   * Utility methods.
   */

  /**
   * This method sorts the provided collection of servers in reverse order. It is necessary, because
   * the model classes Node and Server do not provide a custom implementation of compareTo(obj o).
   * 
   * @param servers Collection of servers to sort.
   * @return Sorted list of provided substrate servers.
   */
  private List<SubstrateServer> sortServerCollection(final Collection<SubstrateServer> servers) {
    final List<SubstrateServer> sorted = new LinkedList<SubstrateServer>();
    final List<String> names = new LinkedList<String>();

    for (final SubstrateServer s : servers) {
      names.add(s.getName());
    }

    Collections.sort(names, Collections.reverseOrder());

    for (final String n : names) {
      for (final SubstrateServer s : servers) {
        if (s.getName().equals(n)) {
          sorted.add(s);
        }
      }
    }

    return sorted;
  }

  /**
   * Creates the initial T vector.
   * 
   * @return T vector in reverse order.
   */
  private List<TafTVectorData> createTvector() {
    final List<TafTVectorData> tVector = new LinkedList<>();

    // Extra collection for all virtual links, because the algorithm removes some of them.
    final List<VirtualLink> processVirtualLinks = new LinkedList<>();

    // Add all virtual links of the virtual network to the collection.
    processVirtualLinks.addAll(virtualLinks);

    // Do while there are virtual links left
    while (!processVirtualLinks.isEmpty()) {
      final VirtualLink sourceLink = processVirtualLinks.remove(0);
      if (sourceLink.getSource() instanceof Server) {
        for (final VirtualLink targetLink : virtualLinks) {
          if (targetLink.getTarget() instanceof Server
              && !targetLink.getTarget().equals(sourceLink.getSource())) {
            final int interVmTraffic =
                Math.max(sourceLink.getBandwidth(), targetLink.getBandwidth());
            tVector.add(new TafTVectorData((VirtualServer) sourceLink.getSource(),
                (VirtualServer) targetLink.getTarget(), interVmTraffic));
          }
        }
      }
    }
    Collections.sort(tVector, Collections.reverseOrder());
    return tVector;
  }

  /**
   * Returns a list of substrate server candidates that are able to embed all virtual servers given.
   * 
   * @param listServerV List of virtual servers to handle.
   * @return List of substrate servers that are able to handle the embedding of all given virtual
   *         servers.
   */
  private List<SubstrateServer> getSubstrateServerCandidates(
      final List<VirtualServer> listServerV) {
    final List<SubstrateServer> candidates = new LinkedList<>();

    // Sum required resources
    final long sumCpu = listServerV.stream().mapToLong(e -> e.getCpu()).sum();
    final long sumMem = listServerV.stream().mapToLong(e -> e.getMemory()).sum();
    final long sumSto = listServerV.stream().mapToLong(e -> e.getStorage()).sum();

    for (final SubstrateServer s : substrateServers) {
      // Get residual resources of substrate server from model
      long resCpu = s.getResidualCpu();
      long resMem = s.getResidualMemory();
      long resSto = s.getResidualStorage();

      // Update residual resources of substrate server according to already created mappings
      // (The mappings are not made in the model itself, yet!)
      for (final VirtualServer vPlaced : placedVms.keySet()) {
        if (placedVms.get(vPlaced).equals(s)) {
          resCpu -= vPlaced.getCpu();
          resMem -= vPlaced.getMemory();
          resSto -= vPlaced.getStorage();
        }
      }

      // If requirements are fulfilled, add substrate server s to candidates
      if (sumCpu <= resCpu && sumMem <= resMem && sumSto <= resSto) {
        candidates.add(s);
      }
    }

    return candidates;
  }

  /**
   * Calculates the incremental communication cost for the placement of a list of virtual servers.
   * 
   * @param listServerV List of virtual servers.
   * @param serverS Substrate server.
   * @return Incremental communication cost.
   */
  private int calcIncrComCost(final List<VirtualServer> listServerV,
      final SubstrateServer serverS) {
    int cost;
    final Set<Switch> rackSwitches = getRackSwitches();
    final SubstrateLink link = (SubstrateLink) serverS.getOutgoingLinks().get(0);
    final SubstrateNode node = (SubstrateNode) link.getTarget();
    if (node instanceof Switch) {
      rackSwitches.add((Switch) node);
    }

    if (allVirtualServersToOneSubstrateServer()) {
      // All servers are placed to one rack (intra-server)
      cost = CostUtility.TAF_C_ALPHA;
    } else if (rackSwitches.size() == 1) {
      // All virtual servers are placed in one rack (inter-server and intra-rack)
      cost = CostUtility.TAF_C_BETA;
    } else {
      // All virtual servers are placed in multiple racks (intra-rack)
      cost = CostUtility.TAF_C_GAMMA;
    }

    return cost;
  }

  /**
   * Returns true if all virtual servers are placed on one single substrate server.
   * 
   * @return True if all virtual servers are placed on one single substrate server.
   */
  private boolean allVirtualServersToOneSubstrateServer() {
    return Collections.frequency(placedVms.values(),
        placedVms.get(virtualServers.get(0))) == placedVms.size();
  }

  /**
   * Returns true if all virtual servers are placed on one single substrate rack.
   * 
   * @return true if all virtual servers are placed on one single substrate rack.
   */
  private boolean allVirtualServersToOneRack() {
    Node tmpSw = null;

    for (final SubstrateServer ssrv : placedVms.values()) {
      if (!ssrv.getOutgoingLinks().get(0).getTarget().equals(tmpSw) && tmpSw != null) {
        return false;
      } else {
        tmpSw = ssrv.getOutgoingLinks().get(0).getTarget();
      }
    }
    return true;
  }

  /**
   * Returns a set of all rack switches for all substrate servers with a mapped virtual switch.
   * 
   * @return Set of all rack switches for all substrate servers with a mapped virtual switch.
   */
  private Set<Switch> getRackSwitches() {
    final Set<Switch> rackSwitches = new HashSet<Switch>();

    // Iterate over all substrate servers with planned mappings
    for (final SubstrateServer s : placedVms.values()) {
      // Iterate over all outgoing links of this particular server
      for (final Link l : s.getOutgoingLinks()) {
        // Check if link target is a switch
        final Node n = l.getTarget();
        if (n instanceof Switch) {
          rackSwitches.add((Switch) n);
        }
      }
    }

    return rackSwitches;
  }

}
