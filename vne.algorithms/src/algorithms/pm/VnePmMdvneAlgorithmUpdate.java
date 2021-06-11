package algorithms.pm;

import java.util.HashSet;
import java.util.Set;
import facade.ModelFacade;
import ilp.wrapper.IlpSolverException;
import ilp.wrapper.Statistics;
import metrics.manager.GlobalMetricsManager;
import model.Node;
import model.SubstrateNetwork;
import model.VirtualLink;
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

    final IlpDeltaGenerator gen = new IlpDeltaGenerator();

    // add new elements
    addElementsToSolver(gen);

    // add new matches
    delta.getNewServerMatchPositives().stream()
        .filter(m -> !ignoredVnets.contains(((VirtualServer) m.getVirtual()).getNetwork()))
        .filter(m -> vNets.contains(((VirtualServer) m.getVirtual()).getNetwork()))
        .forEach(gen::addServerMatch);
    delta.getNewSwitchMatchPositives().stream()
        .filter(m -> !ignoredVnets.contains(((VirtualSwitch) m.getVirtual()).getNetwork()))
        .filter(m -> vNets.contains(((VirtualSwitch) m.getVirtual()).getNetwork()))
        .forEach(gen::addSwitchMatch);

    // Important: Due to the fact that both link constraint generating methods check the existence
    // of the node mapping variables, the link constraints have to be added *after* all node
    // constraints.
    delta.getNewLinkPathMatchPositives().stream()
        .filter(m -> !ignoredVnets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
        .filter(m -> vNets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
        .forEach(gen::addLinkPathMatch);
    delta.getNewLinkServerMatchPositives().stream()
        .filter(m -> !ignoredVnets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
        .filter(m -> vNets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
        .forEach(gen::addLinkServerMatch);

    // apply delta in ILP generator
    gen.apply();

    GlobalMetricsManager.startIlpTime();
    final Statistics solve = ilpSolver.solve();
    GlobalMetricsManager.endIlpTime();
    Set<VirtualNetwork> rejectedNetworks = null;
    if (solve.isFeasible()) {
      GlobalMetricsManager.startDeployTime();
      rejectedNetworks = updateMappingsAndEmbed(ilpSolver.getMappings());
    } else {
      throw new IlpSolverException("Problem was infeasible.");
    }

    if (!rejectedNetworks.isEmpty()) {
      System.out.println("=> Started recursive embedding update.");
      rejectedNetworks = recursiveUpdateEmbedding();
    }

    rejectedNetworks.addAll(ignoredVnets);
    embedNetworks(rejectedNetworks);
    GlobalMetricsManager.endDeployTime();
    return rejectedNetworks.isEmpty();
  }

  /**
   * Removes the smallest virtual network currently embedded on the substrate one and tries the
   * embedding job again. If it fails again, the method calls itself again to remove the next
   * smallest virtual network currently embedded until there is no network left.
   * 
   * @return Set of virtual networks that could not be embedded onto the substrate one.
   */
  private Set<VirtualNetwork> recursiveUpdateEmbedding() {
    final VirtualNetwork removalCandidate = findAndUnembedSmallestNetwork();

    // Recursive exit condition
    if (removalCandidate == null) {
      return new HashSet<VirtualNetwork>();
    }

    vNets.add(removalCandidate);
    init();
    this.patternMatcher = new EmoflonPatternMatcherFactory().create();

    GlobalMetricsManager.startPmTime();
    final PatternMatchingDelta delta = patternMatcher.run();
    GlobalMetricsManager.endPmTime();

    final IlpDeltaGenerator gen = new IlpDeltaGenerator();

    // add new elements
    addElementsToSolver(gen);

    // add new matches
    delta.getNewServerMatchPositives().stream()
        .filter(m -> !ignoredVnets.contains(((VirtualServer) m.getVirtual()).getNetwork()))
        .filter(m -> vNets.contains(((VirtualServer) m.getVirtual()).getNetwork()))
        .forEach(gen::addServerMatch);
    delta.getNewSwitchMatchPositives().stream()
        .filter(m -> !ignoredVnets.contains(((VirtualSwitch) m.getVirtual()).getNetwork()))
        .filter(m -> vNets.contains(((VirtualSwitch) m.getVirtual()).getNetwork()))
        .forEach(gen::addSwitchMatch);

    // Important: Due to the fact that both link constraint generating methods check the existence
    // of the node mapping variables, the link constraints have to be added *after* all node
    // constraints.
    delta.getNewLinkPathMatchPositives().stream()
        .filter(m -> !ignoredVnets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
        .filter(m -> vNets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
        .forEach(gen::addLinkPathMatch);
    delta.getNewLinkServerMatchPositives().stream()
        .filter(m -> !ignoredVnets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
        .filter(m -> vNets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
        .forEach(gen::addLinkServerMatch);

    // apply delta in ILP generator
    gen.apply();

    GlobalMetricsManager.startIlpTime();
    final Statistics solve = ilpSolver.solve();
    GlobalMetricsManager.endIlpTime();
    Set<VirtualNetwork> rejectedNetworks = null;
    if (solve.isFeasible()) {
      GlobalMetricsManager.startDeployTime();
      rejectedNetworks = updateMappingsAndEmbed(ilpSolver.getMappings());
    } else {
      throw new IlpSolverException("Problem was infeasible.");
    }

    if (!rejectedNetworks.isEmpty()) {
      rejectedNetworks = recursiveUpdateEmbedding();
    }

    return rejectedNetworks;
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
