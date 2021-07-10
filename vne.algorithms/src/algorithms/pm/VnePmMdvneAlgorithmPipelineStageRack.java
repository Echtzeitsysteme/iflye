package algorithms.pm;

import java.util.HashSet;
import java.util.Set;
import gt.IncrementalPatternMatcher;
import gt.PatternMatchingDelta;
import gt.emoflon.EmoflonGtFactory;
import gt.emoflon.EmoflonGtRackFactory;
import ilp.wrapper.config.IlpSolverConfig;
import ilp.wrapper.impl.IncrementalGurobiSolver;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Implementation of the model-driven virtual network algorithm that uses pattern matching as a way
 * to reduce the search space of the ILP solver. This implementation embeds virtual networks onto
 * racks.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineStageRack extends VnePmMdvneAlgorithm {

  /**
   * Algorithm instance (singleton).
   */
  protected static VnePmMdvneAlgorithmPipelineStageRack instance;

  /**
   * Incremental pattern matcher to use for the second pipeline stage.
   */
  protected IncrementalPatternMatcher patternMatcherRack;

  /**
   * Constructor that gets the substrate as well as the virtual network.
   * 
   * @param sNet Substrate network to work with.
   * @param vNets Set of virtual networks to work with.
   */
  protected VnePmMdvneAlgorithmPipelineStageRack(final SubstrateNetwork sNet,
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
  public static VnePmMdvneAlgorithmPipelineStageRack prepare(final SubstrateNetwork sNet,
      final Set<VirtualNetwork> vNets) {
    if (sNet == null || vNets == null) {
      throw new IllegalArgumentException("One of the provided network objects was null.");
    }

    if (vNets.size() == 0) {
      throw new IllegalArgumentException("Provided set of virtual networks was empty.");
    }

    if (instance == null) {
      instance = new VnePmMdvneAlgorithmPipelineStageRack(sNet, vNets);
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
    if (this.patternMatcherRack != null) {
      this.patternMatcherRack.dispose();
    }
    super.dispose();
    instance = null;
  }

  @Override
  public boolean execute() {
    GlobalMetricsManager.measureMemory();
    init();

    // // Check overall embedding possibility
    // checkOverallResources();
    //
    // // Repair model consistency: Substrate network
    // repairSubstrateNetwork();
    //
    // // Repair model consistency: Virtual network(s)
    // final Set<VirtualNetwork> repairedVnets = repairVirtualNetworks();
    // if (!repairedVnets.isEmpty()) {
    // this.patternMatcher = new EmoflonGtFactory().create();
    // this.patternMatcherRack = new EmoflonGtRackFactory().create();
    // }
    // vNets.addAll(repairedVnets);

    //
    // Stage 2: Virtual network -> Rack
    //

    // // Remove embedding of all already embedded networks
    // PmAlgorithmUtils.unembedAll(sNet, vNets);
    // System.out.println("=> Starting pipeline stage #2");

    GlobalMetricsManager.startPmTime();
    final PatternMatchingDelta deltaTwo = patternMatcherRack.run();
    GlobalMetricsManager.endPmTime();

    // Uses the "normal" delta to ILP translator of the super class
    delta2Ilp(deltaTwo);
    GlobalMetricsManager.measureMemory();
    final Set<VirtualNetwork> rejectedNetworksTwo = solveIlp();

    rejectedNetworksTwo.addAll(ignoredVnets);
    embedNetworks(rejectedNetworksTwo);
    GlobalMetricsManager.endDeployTime();
    GlobalMetricsManager.measureMemory();
    return rejectedNetworksTwo.isEmpty();
  }

  /*
   * Helper methods.
   */

  /**
   * Initializes the algorithm by creating a new incremental solver object and a new pattern matcher
   * object.
   */
  @Override
  public void init() {
    // Create new ILP solver object on every method call.
    ilpSolver = new IncrementalGurobiSolver(IlpSolverConfig.TIME_OUT, IlpSolverConfig.RANDOM_SEED);

    if (patternMatcher == null) {
      patternMatcher = new EmoflonGtFactory().create();
    }

    if (patternMatcherRack == null) {
      patternMatcherRack = new EmoflonGtRackFactory().create();
    }
  }

}
