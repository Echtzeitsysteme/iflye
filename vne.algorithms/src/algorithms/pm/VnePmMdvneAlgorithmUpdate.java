package algorithms.pm;

import java.util.HashSet;
import java.util.Set;
import facade.ModelFacade;
import metrics.manager.GlobalMetricsManager;
import model.Node;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;
import patternmatching.PatternMatchingDelta;
import patternmatching.emoflon.EmoflonPatternMatcherFactory;

/**
 * Implementation of the model-driven virtual network algorithm that uses pattern matching as a way
 * to reduce the search space of the ILP solver. This implementation also uses update functionality
 * in case a virtual network does not fit on the current state of the substrate network.
 * 
 * Parts of this implementation are heavily inspired, taken or adapted from the idyve project [1].
 * 
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
 * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
 *
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmUpdate extends VnePmMdvneAlgorithm {

  /**
   * Set of virtual networks that are rejected despite they could not have been embedded through the
   * update mechanism.
   */
  final Set<VirtualNetwork> rejectedDespiteUpdate = new HashSet<VirtualNetwork>();

  /**
   * Constructor that gets the substrate as well as the virtual network.
   * 
   * @param sNet Substrate network to work with.
   * @param vNets Set of virtual networks to work with.
   */
  private VnePmMdvneAlgorithmUpdate(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    super(sNet, vNets);
  }

  /**
   * Initializes a new instance of the VNE pattern matching algorithm with update functionality.
   * 
   * @param sNet Substrate network to work with.
   * @param vNets Set of virtual networks to work with.
   * @return Instance of this algorithm implementation.
   */
  public static VnePmMdvneAlgorithm prepare(final SubstrateNetwork sNet,
      final Set<VirtualNetwork> vNets) {
    if (sNet == null || vNets == null) {
      throw new IllegalArgumentException("One of the provided network objects was null.");
    }

    if (vNets.size() == 0) {
      throw new IllegalArgumentException("Provided set of virtual networks was empty.");
    }

    if (instance == null) {
      instance = new VnePmMdvneAlgorithmUpdate(sNet, vNets);
    }
    setSnet(sNet);
    final Set<VirtualNetwork> vNetsInt = new HashSet<VirtualNetwork>();
    vNetsInt.addAll(vNets);
    setVnets(vNetsInt);

    instance.checkPreConditions();
    return instance;
  }

  @Override
  public boolean execute() {
    init();

    // Check overall embedding possibility
    checkOverallResources();

    // Repair model consistency: Substrate network
    repairSubstrateNetwork();

    // Repair model consistency: Virtual network(s)
    final Set<VirtualNetwork> repairedVnets = repairVirtualNetworks();
    if (!repairedVnets.isEmpty()) {
      this.patternMatcher = new EmoflonPatternMatcherFactory().create();
    }
    vNets.addAll(repairedVnets);

    GlobalMetricsManager.startPmTime();
    final PatternMatchingDelta delta = patternMatcher.run();
    GlobalMetricsManager.endPmTime();

    delta2Ilp(delta);
    Set<VirtualNetwork> rejectedNetworks = solveIlp();
    rejectedDespiteUpdate.addAll(rejectedNetworks);

    // Check if embedding update routing must be started
    if (!rejectedNetworks.isEmpty()) {
      System.out.println("=> Started embedding update.");
      embedNetworks(rejectedNetworks);
      rejectedNetworks = tryUpdateEmbedding();
    }

    rejectedDespiteUpdate.addAll(ignoredVnets);
    embedNetworks(rejectedDespiteUpdate);
    GlobalMetricsManager.endDeployTime();
    return rejectedDespiteUpdate.isEmpty();
  }

  /**
   * Removes the smallest virtual network currently embedded on the substrate one and tries the
   * embedding job again. If it fails again, the method removes the next smallest virtual network
   * and tries again. If no virtual network to remove is left, the method returns a set of rejected
   * networks.
   * 
   * @return Set of virtual networks that could not be embedded onto the substrate one.
   */
  private Set<VirtualNetwork> tryUpdateEmbedding() {
    Set<VirtualNetwork> rejectedNetworks = new HashSet<VirtualNetwork>();
    VirtualNetwork removalCandidate = findAndUnembedSmallestNetwork();

    while (removalCandidate != null) {
      unembedAll(vNets);
      vNets.add(removalCandidate);
      unembedAll(vNets);
      init();
      this.patternMatcher = new EmoflonPatternMatcherFactory().create();

      GlobalMetricsManager.startPmTime();
      final PatternMatchingDelta delta = patternMatcher.run();
      GlobalMetricsManager.endPmTime();

      delta2Ilp(delta);
      rejectedNetworks.clear();
      rejectedNetworks.addAll(solveIlp());

      rejectedDespiteUpdate.addAll(rejectedNetworks);
      rejectedDespiteUpdate.retainAll(rejectedNetworks);

      if (rejectedNetworks.isEmpty()) {
        break;
      }

      removalCandidate = findAndUnembedSmallestNetwork();
    }

    return rejectedNetworks;
  }

  /**
   * Method that removed the embedding for all given networks if there exists one. Moreover, this
   * method "repairs" the possible floating state if the virtual network itself is not embedded, but
   * its elements are.
   * 
   * @param vNets Set of virtual networks to remove embeddings for.
   */
  private void unembedAll(final Set<VirtualNetwork> vNets) {
    // Iterate over all given virtual networks
    for (final VirtualNetwork vNet : vNets) {
      // If virtual network has no host, but one of the nodes is embedded -> Embed the whole virtual
      // network object again (otherwise the removal of the virtual network fails)
      if (vNet.getHost() == null) {
        final Node n = vNet.getNodes().get(0);

        if (n instanceof VirtualSwitch) {
          if (((VirtualSwitch) n).getHost() != null) {
            ModelFacade.getInstance().embedNetworkToNetwork(sNet.getName(), vNet.getName());
          }
        } else if (n instanceof VirtualServer) {
          if (((VirtualServer) n).getHost() != null) {
            ModelFacade.getInstance().embedNetworkToNetwork(sNet.getName(), vNet.getName());
          }
        }
      }

      // Remove embedding of whole virtual network with all of its elements
      if (vNet.getHost() != null) {
        ModelFacade.getInstance().removeNetworkEmbedding(vNet.getName());
      }
    }
  }

  /**
   * Finds the smallest virtual network currently embedded on the substrate network and removes its
   * embedding. If there is no such network, the method returns null.
   * 
   * @return Smallest virtual network object currently embedded on the substrate network or null if
   */
  private VirtualNetwork findAndUnembedSmallestNetwork() {
    VirtualNetwork smallest = null;
    long res = Integer.MAX_VALUE;

    for (final VirtualNetwork vn : sNet.getGuests()) {
      long aRes = 0;
      for (final Node n : vn.getNodes()) {
        if (n instanceof VirtualServer) {
          final VirtualServer vsrv = (VirtualServer) n;
          aRes += vsrv.getCpu();
          aRes += vsrv.getMemory();
          aRes += vsrv.getStorage();
        }
      }

      if (aRes < res) {
        res = aRes;
        smallest = vn;
      }
    }

    if (smallest != null) {
      ModelFacade.getInstance().unembedVirtualNetwork(smallest);
    }

    return smallest;
  }

}
