package scenarios;

import java.util.Random;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;

/**
 * Abstract class to generate scenarios based on the evaluation chapter of the dissertation [1].
 * There are some changes and assumptions: (1) This implementation does not use the mentioned
 * Bitbrains distribution for generating random values of the virtual networks. (2) This
 * implementation does not feature any special properties like master-failover-servers, low latency
 * links, or high performance computing nodes. (3) Every generated workload (virtual network) is a
 * one tier network with (star topology), but every server of a virtual network is configured
 * exactly the same way.
 * 
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
 * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public abstract class AMdvneAdaptedScenario implements IScenario {

  /**
   * Pseudo random number generator with a seed.
   */
  private final Random randGen = new Random(0);

  /*
   * Substrate parameters
   */

  /**
   * Amount of CPU per substrate server.
   */
  public final int substrateCpu = 32;

  /**
   * Amount of memory per substrate server.
   */
  public final int substrateMem = 512;

  /**
   * Amount of storage per substrate server.
   */
  public final int substrateSto = 1000;

  /**
   * Amount of bandwidth per link that connects a substrate server.
   */
  public final int substrateBwSrv = 1000;

  /**
   * Amount of bandwidth for all links between rack and core switches.
   */
  public final int substrateBwCore = 10_000;

  /**
   * Number of substrate servers per rack.
   */
  public final int serversPerRack = 10;

  /*
   * Virtual parameters
   */

  public final int virtualCpuMin = 1;
  public final int virtualCpuMax = 32;
  public final int virtualMemMin = 1;
  public final int virtualMemMax = 511;
  public final int virtualStoMin = 50;
  public final int virtualStoMax = 300;

  public final int virtualTrafficMin = 100;
  public final int virtualTrafficMax = 1000;

  public final int virtualServersMin = 2;
  public final int virtualServersMax = 10;

  /*
   * Utility methods
   */

  /**
   * Returns the next random integer of the pseudo random number generator in (min, max).
   * 
   * @param min Minimum (exclusive).
   * @param max Maximum (exclusive).
   * @return Random integer in (min, max).
   */
  protected int getNextRandIntInterval(final int min, final int max) {
    return 1 + min + randGen.nextInt(max - min - 1);
  }

  /**
   * Sets one instance of a virtual network up (according to the configuration and to pseudo random
   * values from all attribute ranges above).
   * 
   * @param virtualNetworkId The ID for the virtual network to build.
   */
  protected void virtualSetup(final String virtualNetworkId) {
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
