package scenarios.gen;

import java.util.Random;
import java.util.Set;
import algorithms.AbstractAlgorithm;
import algorithms.heuristics.TafAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Evaluation scenario of paper [1]. There are some changes and assumptions: (1) Every virtual
 * network is a one tier network with exactly one switch. (2) Every virtual machine of one
 * particular virtual network has the same amount of CPU, memory and bandwidth. The same applies to
 * every link of a virtual network. (3) The substrate network is a two tier network with two core
 * switches and parameters as configured below (see variables and constants).
 * 
 * [1] Zeng, D., Guo, S., Huang, H., Yu, S., and Leung, V. C.M., Optimal VM Placement in Data
 * Centers with Architectural and Resource Constraints, International Journal of Autonomous and
 * Adaptive Communications Systems, vol. 8, no. 4, pp. 392-406, 2015.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class OptimalVmScenario implements IScenario {

  /**
   * Amount of CPU per substrate server.
   */
  public static final int substrateCpu = 1500;

  /**
   * Amount of memory per substrate server.
   */
  public static final int substrateMem = 1500;

  /**
   * Amount of storage per substrate server. The paper [1] uses this value for I/O instead of
   * storage.
   */
  public static final int substrateSto = 300;

  /**
   * Amount of bandwidth per link that connects a substrate server. This value was not explicitly
   * given in the paper [1].
   */
  public static final int substrateBwSrv = 1000;

  /**
   * Amount of bandwidth for all links between rack and core switches. This values was not
   * explicitly given in the paper [1].
   */
  public static final int substrateBwCore = 10_000;

  public static final int virtualCpuMin = 0;
  public static final int virtualCpuMax = 300;
  public static final int virtualMemMin = 0;
  public static final int virtualMemMax = 500;
  public static final int virtualStoMin = 0;
  public static final int virtualStoMax = 50;

  public static final int virtualTrafficMin = 0;
  public static final int virtualTrafficMax = 10;

  /**
   * Number of virtual servers per request.
   */
  private static int L = 5;

  /**
   * Number of substrate racks.
   */
  private static int M = 2;

  /**
   * Number of substrate servers per rack.
   */
  private static int N = 2;

  /**
   * Number of virtual network requests (VNRs).
   */
  private static int numberOfVnrs = 10;

  /**
   * Pseudo random number generator with a seed.
   */
  private static final Random randGen = new Random(0);

  /**
   * Entry point method for this scenario.
   * 
   * @param args Runtime arguments that will be ignored.
   */
  public static void main(final String[] args) {
    // TODO: Remove bandwidth ignoring after changing the VNE algorithm instance below.
    ModelFacadeConfig.IGNORE_BW = true;
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;

    substrateSetup("sub");
    final SubstrateNetwork sub = (SubstrateNetwork) facade.getNetworkById("sub");

    for (int i = 0; i < numberOfVnrs; i++) {
      final String virtualNetworkId = "v" + i;
      virtualSetup(virtualNetworkId);
      final VirtualNetwork virt = (VirtualNetwork) facade.getNetworkById(virtualNetworkId);
      // TODO: Change the algorithm instance later on.
      final AbstractAlgorithm algo = new TafAlgorithm(sub, Set.of(virt));
      final boolean success = algo.execute();

      if (success) {
        System.out.println("Embedded VNR: " + virtualNetworkId);
      } else {
        System.err.println("Rejected VNR: " + virtualNetworkId);
      }
    }

    // Save model to file
    ModelFacade.getInstance().persistModel();
    System.out.println("=> Scenario finished.");

    System.exit(0);
  }

  /**
   * Returns the next random integer of the pseudo random number generator in (min, max).
   * 
   * @param min Minimum (exclusive).
   * @param max Maximum (exclusive).
   * @return Random integer in (min, max).
   */
  private static int getNextRandIntInterval(final int min, final int max) {
    return 1 + min + randGen.nextInt(max - min - 1);
  }

  /**
   * Sets the substrate network up (according to the configuration).
   * 
   * @param substrateNetworkId The ID for the substrate network to build.
   */
  private static void substrateSetup(final String substrateNetworkId) {
    final OneTierConfig substrateRackConfig =
        new OneTierConfig(N, 1, false, substrateCpu, substrateMem, substrateSto, substrateBwSrv);
    final TwoTierConfig substrateConfig = new TwoTierConfig();
    substrateConfig.setRack(substrateRackConfig);
    substrateConfig.setNumberOfCoreSwitches(2);
    substrateConfig.setNumberOfRacks(M);
    final TwoTierNetworkGenerator subGen = new TwoTierNetworkGenerator(substrateConfig);
    subGen.createNetwork(substrateNetworkId, false);
  }

  /**
   * Sets one instance of a virtual network up (according to the configuration and to pseudo random
   * values from all attribute ranges above).
   * 
   * @param virtualNetworkId The ID for the virtual network to build.
   */
  private static void virtualSetup(final String virtualNetworkId) {
    final int cpu = getNextRandIntInterval(virtualCpuMin, virtualCpuMax);
    final int memory = getNextRandIntInterval(virtualMemMin, virtualMemMax);
    final int storage = getNextRandIntInterval(virtualStoMin, virtualStoMax);
    final int bandwidth = getNextRandIntInterval(virtualTrafficMin, virtualTrafficMax);

    final OneTierConfig virtualConfig =
        new OneTierConfig(L, 1, false, cpu, memory, storage, bandwidth);
    final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
    virtGen.createNetwork(virtualNetworkId, true);
  }

}
