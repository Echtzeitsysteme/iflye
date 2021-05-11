package scenarios;

import java.util.Random;
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
 * Evaluation scenario of the dissertation [1]. There are some changes and assumptions: (1)
 * 
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
 * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class MdvneAdaptedScenario implements IScenario {

  /**
   * Amount of CPU per substrate server.
   */
  public static final int substrateCpu = 32;

  /**
   * Amount of memory per substrate server.
   */
  public static final int substrateMem = 512;

  /**
   * Amount of storage per substrate server.
   */
  public static final int substrateSto = 1000;

  /**
   * Amount of bandwidth per link that connects a substrate server.
   */
  public static final int substrateBwSrv = 1000;

  /**
   * Amount of bandwidth for all links between rack and core switches.
   */
  public static final int substrateBwCore = 10_000;

  public static final int virtualCpuMin = 1;
  public static final int virtualCpuMax = 32;
  public static final int virtualMemMin = 1;
  public static final int virtualMemMax = 511;
  public static final int virtualStoMin = 50;
  public static final int virtualStoMax = 300;

  public static final int virtualTrafficMin = 100;
  public static final int virtualTrafficMax = 1000;

  public static final int virtualServersMin = 2;
  public static final int virtualServersMax = 10;

  /**
   * Number of substrate racks.
   */
  private static int racks = 8;

  /**
   * Number of substrate servers per rack.
   */
  public static final int serversPerRack = 10;

  /**
   * Number of virtual network requests (VNRs).
   */
  private static int numberOfVnrs = 40;

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
      final AbstractAlgorithm algo = new TafAlgorithm(sub, virt);
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
    final OneTierConfig substrateRackConfig = new OneTierConfig(serversPerRack, 1, false,
        substrateCpu, substrateMem, substrateSto, substrateBwSrv);
    final TwoTierConfig substrateConfig = new TwoTierConfig();
    substrateConfig.setRack(substrateRackConfig);
    substrateConfig.setNumberOfCoreSwitches(2);
    substrateConfig.setNumberOfRacks(racks);
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
    final int numberOfServers = getNextRandIntInterval(virtualServersMin, virtualServersMax);

    final OneTierConfig virtualConfig =
        new OneTierConfig(numberOfServers, 1, false, cpu, memory, storage, bandwidth);
    final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
    virtGen.createNetwork(virtualNetworkId, true);
  }

}
