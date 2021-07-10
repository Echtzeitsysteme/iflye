package algorithms.pm;

import java.util.HashSet;
import java.util.Set;
import algorithms.AbstractAlgorithm;
import algorithms.pm.stages.VnePmMdvneAlgorithmPipelineStageRack;
import algorithms.pm.stages.VnePmMdvneAlgorithmPipelineStageVnet;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Implementation of the model-driven virtual network algorithm that uses pattern matching as a way
 * to reduce the search space of the ILP solver. This implementation uses a three-stage pipeline
 * approach.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineThreeStages extends VnePmMdvneAlgorithm {

  /**
   * Algorithm instance (singleton).
   */
  protected static VnePmMdvneAlgorithmPipelineThreeStages instance;

  /**
   * Constructor that gets the substrate as well as the virtual network.
   * 
   * @param sNet Substrate network to work with.
   * @param vNets Set of virtual networks to work with.
   */
  protected VnePmMdvneAlgorithmPipelineThreeStages(final SubstrateNetwork sNet,
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
  public static VnePmMdvneAlgorithmPipelineThreeStages prepare(final SubstrateNetwork sNet,
      final Set<VirtualNetwork> vNets) {
    if (sNet == null || vNets == null) {
      throw new IllegalArgumentException("One of the provided network objects was null.");
    }

    if (vNets.size() == 0) {
      throw new IllegalArgumentException("Provided set of virtual networks was empty.");
    }

    if (instance == null) {
      instance = new VnePmMdvneAlgorithmPipelineThreeStages(sNet, vNets);
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
    super.dispose();
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
    // if (!repairedVnets.isEmpty()) {
    // this.patternMatcher = new EmoflonGtFactory().create();
    // this.patternMatcherVnet = new EmoflonGtVnetFactory().create();
    // }
    vNets.addAll(repairedVnets);

    //
    // Stage 1: Virtual network -> Substrate server
    //

    System.out.println("=> Starting pipeline stage #1");
    AbstractAlgorithm algo = VnePmMdvneAlgorithmPipelineStageVnet.prepare(sNet, vNets);
    if (algo.execute()) {
      return true;
    }

    //
    // Stage 2: Virtual network -> Substrate Rack
    //

    // Remove embedding of all already embedded networks
    PmAlgorithmUtils.unembedAll(sNet, vNets);

    System.out.println("=> Starting pipeline stage #2");
    dispose();
    algo = VnePmMdvneAlgorithmPipelineStageRack.prepare(sNet, vNets);
    if (algo.execute()) {
      return true;
    }

    //
    // Stage 3: Normal PM-based embedding
    //

    // Remove embedding of all already embedded networks
    PmAlgorithmUtils.unembedAll(sNet, vNets);

    System.out.println("=> Starting pipeline stage #3");
    dispose();
    algo = VnePmMdvneAlgorithm.prepare(sNet, vNets);
    return algo.execute();
  }

}
