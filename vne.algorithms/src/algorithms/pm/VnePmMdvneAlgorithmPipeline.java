package algorithms.pm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import gt.emoflon.EmoflonPatternMatcherVnet;
import gt.emoflon.EmoflonPatternMatcherVnetFactory;
import ilp.wrapper.config.IlpSolverConfig;
import ilp.wrapper.impl.IncrementalGurobiSolver;
import metrics.manager.GlobalMetricsManager;
import model.Link;
import model.Node;
import model.SubstrateElement;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualElement;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualNode;
import patternmatching.IncrementalPatternMatcher;
import patternmatching.PatternMatchingDelta;
import patternmatching.PatternMatchingDelta.Match;
import patternmatching.emoflon.EmoflonPatternMatcherFactory;

/**
 * Implementation of the model-driven virtual network algorithm that uses pattern matching as a way
 * to reduce the search space of the ILP solver. This implementation uses a two-stage pipeline
 * approach.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipeline extends VnePmMdvneAlgorithm {

  /**
   * ILP delta generator that converts matches and given model objects into ILP constraints for the
   * solver.
   * 
   * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
   */
  class IlpDeltaGeneratorVnet extends IlpDeltaGenerator {

    /**
     * Translates and adds a match from a virtual network to a substrate server.
     * 
     * @param match Match to get information from.
     */
    public void addNetworkToServerMatch(final Match match) {
      final VirtualNetwork vnet = (VirtualNetwork) match.getVirtual();
      final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();
      delta.addVariable(varName, getCost(vnet, (SubstrateServer) match.getSubstrate()));
      delta.setVariableWeightForConstraint("vsnet" + match.getVirtual().getName(), 1, varName);

      delta.setVariableWeightForConstraint("cpu" + match.getSubstrate().getName(), vnet.getCpu(),
          varName);
      delta.setVariableWeightForConstraint("mem" + match.getSubstrate().getName(), vnet.getMemory(),
          varName);
      delta.setVariableWeightForConstraint("sto" + match.getSubstrate().getName(),
          vnet.getStorage(), varName);
      variablesToMatch.put(varName, match);

      // SOS match
      addSosMappings(match.getVirtual().getName(), varName);
    }

    /**
     * Adds a new virtual network.
     * 
     * @param vnet VirtualNetwork to get information from.
     */
    public void addNewVirtualNetwork(final VirtualNetwork vnet) {
      delta.addEqualsConstraint("vsnet" + vnet.getName(), 1);
      delta.setVariableWeightForConstraint("vsnet" + vnet.getName(), 1, "rej" + vnet.getName());
    }

  }

  /**
   * Algorithm instance (singleton).
   */
  protected static VnePmMdvneAlgorithmPipeline instance;

  /**
   * Incremental pattern matcher to use for the first pipeline stage.
   */
  protected IncrementalPatternMatcher patternMatcherVnet;

  /**
   * Constructor that gets the substrate as well as the virtual network.
   * 
   * @param sNet Substrate network to work with.
   * @param vNets Set of virtual networks to work with.
   */
  protected VnePmMdvneAlgorithmPipeline(final SubstrateNetwork sNet,
      final Set<VirtualNetwork> vNets) {
    super(sNet, vNets);
  }

  /**
   * Initializes a new instance of the VNE pattern matching algorithm.
   * 
   * @param sNet Substrate network to work with.
   * @param vNets Set of virtual networks to work with.
   * @return Instance of this algorithm implementation.
   */
  public static VnePmMdvneAlgorithmPipeline prepare(final SubstrateNetwork sNet,
      final Set<VirtualNetwork> vNets) {
    if (sNet == null || vNets == null) {
      throw new IllegalArgumentException("One of the provided network objects was null.");
    }

    if (vNets.size() == 0) {
      throw new IllegalArgumentException("Provided set of virtual networks was empty.");
    }

    if (instance == null) {
      instance = new VnePmMdvneAlgorithmPipeline(sNet, vNets);
    }
    instance.sNet = sNet;
    instance.vNets = new HashSet<VirtualNetwork>();
    instance.vNets.addAll(vNets);

    instance.checkPreConditions();
    return instance;
  }

  /**
   * Resets the ILP solver and the pattern matcher.
   */
  @Override
  public void dispose() {
    if (instance == null) {
      return;
    }
    if (this.ilpSolver != null) {
      this.ilpSolver.dispose();
    }
    if (this.patternMatcher != null) {
      this.patternMatcher.dispose();
    }
    if (this.patternMatcherVnet != null) {
      this.patternMatcherVnet.dispose();
    }
    instance = null;
  }

  @Override
  public boolean execute() {
    GlobalMetricsManager.measureMemory();
    init();

    // Check overall embedding possibility
    checkOverallResources();

    // Repair model consistency: Substrate network
    repairSubstrateNetwork();

    // Repair model consistency: Virtual network(s)
    final Set<VirtualNetwork> repairedVnets = repairVirtualNetworks();
    if (!repairedVnets.isEmpty()) {
      this.patternMatcher = new EmoflonPatternMatcherFactory().create();
      this.patternMatcherVnet = new EmoflonPatternMatcherVnetFactory().create();
    }
    vNets.addAll(repairedVnets);

    //
    // Stage 1: Virtual network -> Substrate server
    //

    GlobalMetricsManager.startPmTime();
    final PatternMatchingDelta delta = patternMatcherVnet.run();
    GlobalMetricsManager.endPmTime();

    delta2Ilp(delta);
    GlobalMetricsManager.measureMemory();
    final Set<VirtualNetwork> rejectedNetworks = solveIlp();

    if (rejectedNetworks.isEmpty()) {
      rejectedNetworks.addAll(ignoredVnets);
      embedNetworks(rejectedNetworks);
      GlobalMetricsManager.endDeployTime();
      GlobalMetricsManager.measureMemory();
      return rejectedNetworks.isEmpty();
    }

    //
    // Stage 2: Normal PM-based embedding
    //

    System.out.println("=> Starting pipeline stage #2");
    dispose();
    final AbstractAlgorithm algo = VnePmMdvneAlgorithm.prepare(sNet, vNets);
    return algo.execute();
  }

  /**
   * Translates the given pattern matching delta to an ILP formulation.
   * 
   * @param delta Pattern matching delta to translate into an ILP formulation.
   */
  @Override
  protected void delta2Ilp(final PatternMatchingDelta delta) {
    final IlpDeltaGeneratorVnet gen = new IlpDeltaGeneratorVnet();

    // add new elements
    addElementsToSolver(gen);

    // Virtual network -> substrate server matches
    delta.getNewNetworkServerMatchPositives().stream() //
        .filter(m -> !ignoredVnets.contains(m.getVirtual())) //
        .filter(m -> vNets.contains(m.getVirtual())) //
        .forEach(gen::addNetworkToServerMatch);

    // apply delta in ILP generator
    gen.apply();
  }

  /*
   * Helper methods.
   */

  /**
   * Adds the elements of the substrate and the virtual network to the given delta generator
   * (solver).
   * 
   * @param gen ILP delta generator VNet to add elements to.
   */
  protected void addElementsToSolver(final IlpDeltaGeneratorVnet gen) {
    // Substrate network
    for (final Node n : sNet.getNodes()) {
      if (n instanceof SubstrateServer) {
        gen.addNewSubstrateServer((SubstrateServer) n);
      } else if (n instanceof SubstrateSwitch) {
        // Nothing to do here
      }
    }

    for (final Link l : sNet.getLinks()) {
      if (l instanceof SubstrateLink) {
        gen.addNewSubstrateLink((SubstrateLink) l);
      }
    }

    // Virtual networks
    final Iterator<VirtualNetwork> it = vNets.iterator();
    while (it.hasNext()) {
      final VirtualNetwork vNet = it.next();
      if (ignoredVnets.contains(vNet)) {
        continue;
      }

      // Network match
      gen.addNewNetworkMatch(new Match(vNet, sNet));

      // Network itself for hyper edge
      gen.addNewVirtualNetwork(vNet);
    }
  }

  /**
   * Updates and embeds the actual mappings for a given map of names (strings) and booleans.
   * 
   * @param mappings Map of strings and booleans. The keys are mapping names and the values define
   *        if the mapping was chosen.
   * @return Returns a set of all virtual networks that could not be embedded.
   */
  @Override
  protected Set<VirtualNetwork> updateMappingsAndEmbed(final Map<String, Boolean> mappings) {
    // Embed elements
    final Set<VirtualNetwork> rejectedNetworks = new HashSet<VirtualNetwork>();
    final EmoflonPatternMatcherVnet engine = (EmoflonPatternMatcherVnet) patternMatcherVnet;

    for (final String s : mappings.keySet()) {
      if (!mappings.get(s)) {
        continue;
      }

      final Match m = variablesToMatch.get(s);

      // Network -> Network (rejected)
      if (m.getVirtual() instanceof VirtualNetwork
          && m.getSubstrate() instanceof SubstrateNetwork) {
        rejectedNetworks.add((VirtualNetwork) m.getVirtual());
        continue;
      }

      // Embed element: Either use emoflon/GT or use manual mode.
      switch (AlgorithmConfig.emb) {
        case EMOFLON:
          // Create embedding via matches and graph transformation
          engine.apply((VirtualElement) m.getVirtual(), (SubstrateElement) m.getSubstrate(), true);
          break;
        case EMOFLON_WO_UPDATE:
          // Create embedding via matches and graph transformation
          engine.apply((VirtualElement) m.getVirtual(), (SubstrateElement) m.getSubstrate(), false);
          break;
        default:
          throw new UnsupportedOperationException();
      }
    }

    return rejectedNetworks;
  }

  /**
   * Initializes the algorithm by creating a new incremental solver object and a new pattern matcher
   * object.
   */
  @Override
  public void init() {
    // Create new ILP solver object on every method call.
    ilpSolver = new IncrementalGurobiSolver(IlpSolverConfig.TIME_OUT, IlpSolverConfig.RANDOM_SEED);

    if (patternMatcher == null) {
      patternMatcher = new EmoflonPatternMatcherFactory().create();
    }

    if (patternMatcherVnet == null) {
      patternMatcherVnet = new EmoflonPatternMatcherVnetFactory().create();
    }
  }

  /*
   * Cost functions.
   */

  @Override
  public double getCost(final VirtualElement virt, final SubstrateElement host) {
    if (virt instanceof Link) {
      return IlpSolverConfig.transformObj(getLinkCost((VirtualLink) virt, host));
    } else if (virt instanceof Node && host instanceof Node) {
      return IlpSolverConfig.transformObj(getNodeCost((VirtualNode) virt, (SubstrateNode) host));
    } else if (virt instanceof VirtualNetwork) {
      return IlpSolverConfig.transformObj(getNetCost((VirtualNetwork) virt, (SubstrateNode) host));
    }

    throw new IllegalArgumentException();
  }

  public double getNetCost(final VirtualNetwork net, final SubstrateNode sub) {
    double cost = 0;

    for (final Node n : net.getNodes()) {
      cost += getNodeCost((VirtualNode) n, sub);
    }

    for (final Link l : net.getLinks()) {
      cost += getLinkCost((VirtualLink) l, sub);
    }

    return cost;
  }

}
