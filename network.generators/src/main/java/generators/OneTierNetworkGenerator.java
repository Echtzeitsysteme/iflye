package generators;

import java.util.HashSet;
import facade.ModelFacade;
import generators.config.GlobalGeneratorConfig;
import generators.config.IGeneratorConfig;
import generators.config.OneTierConfig;

/**
 * Basic implementation of a one tier network topology generator.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class OneTierNetworkGenerator implements INetworkGenerator {

  /**
   * Configuration of this network generator instance.
   */
  private final OneTierConfig config;

  /**
   * Set for the switch ID generation.
   */
  final HashSet<String> switchIds = new HashSet<String>();

  /**
   * Set for the server ID generation.
   */
  final HashSet<String> serverIds = new HashSet<String>();

  /**
   * Counter for the link ID generation.
   */
  private int linkCounter = 0;

  /**
   * Private constructor to avoid direct instantiation of this class.
   * 
   * @param config Configuration for this generator.
   */
  public OneTierNetworkGenerator(final IGeneratorConfig config) {
    if (!(config instanceof OneTierConfig)) {
      throw new IllegalArgumentException("Configuration instance is not an OneTierConfig.");
    }

    this.config = (OneTierConfig) config;
  }

  /**
   * Method to create the network.
   * 
   * @param networkId ID of the network to create.
   * @param isVirtual True if network should be virtual.
   */
  @Override
  public void createNetwork(final String networkId, boolean isVirtual) {
    // Reset sets and counter
    switchIds.clear();
    serverIds.clear();
    linkCounter = 0;

    // Network
    if (!facade.networkExists(networkId)) {
      facade.addNetworkToRoot(networkId, isVirtual);
    }

    // Servers
    for (int i = 0; i < config.getNumberOfServers(); i++) {
      final String currentId = getNextServerId(networkId);
      serverIds.add(currentId);
      facade.addServerToNetwork(currentId, networkId, config.getCpuPerServer(),
          config.getMemoryPerServer(), config.getStoragePerServer(), 1);
    }

    // Switches
    for (int i = 0; i < config.getNumberOfSwitches(); i++) {
      final String currentId = getNextSwitchId(networkId);
      switchIds.add(currentId);
      facade.addSwitchToNetwork(currentId, networkId, 0);
    }

    // Links
    for (final String actServerId : serverIds) {
      for (final String actSwitchId : switchIds) {
        // Direction 1
        facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getBandwidthPerLink(),
            actServerId, actSwitchId);
        // Direction 2
        facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getBandwidthPerLink(),
            actSwitchId, actServerId);
      }
    }

    // Connect switches together if option is enabled
    if (config.isSwitchesConnected()) {
      throw new UnsupportedOperationException("Not implemented, yet!");
    }

    // Generate paths
    if (!isVirtual) {
      ModelFacade.getInstance().createAllPathsForNetwork(networkId);
    }
  }

  /**
   * Generated the next free switch ID for this particular network.
   * 
   * @param networkId Network ID.
   * @return Next free switch ID for this particular network.
   */
  private String getNextSwitchId(final String networkId) {
    return networkId + GlobalGeneratorConfig.SEPARATOR + GlobalGeneratorConfig.SWITCH
        + GlobalGeneratorConfig.SEPARATOR + switchIds.size();
  }

  /**
   * Generated the next free server ID for this particular network.
   * 
   * @param networkId Network ID.
   * @return Next free server ID for this particular network.
   */
  private String getNextServerId(final String networkId) {
    return networkId + GlobalGeneratorConfig.SEPARATOR + GlobalGeneratorConfig.SERVER
        + GlobalGeneratorConfig.SEPARATOR + serverIds.size();
  }

  /**
   * Generated the next free link ID for this particular network.
   * 
   * @param networkId Network ID.
   * @return Next free link ID for this particular network.
   */
  private String getNextLinkId(final String networkId) {
    return networkId + GlobalGeneratorConfig.SEPARATOR + GlobalGeneratorConfig.LINK
        + GlobalGeneratorConfig.SEPARATOR + linkCounter++;
  }

}
