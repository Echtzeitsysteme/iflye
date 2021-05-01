package generators;

import java.util.HashSet;
import facade.ModelFacade;
import generators.config.OneTierConfig;
import generators.utils.GenUtils;

/**
 * Basic implementation of a one tier network topology generator.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class OneTierNetworkGenerator {

  /**
   * Private constructor to avoid direct instantiation of this class.
   */
  private OneTierNetworkGenerator() {}

  /**
   * ModelFacade instance.
   */
  static private ModelFacade facade = ModelFacade.getInstance();

  /**
   * Method to create the network.
   * 
   * @param networkId ID of the network to create.
   * @param config OneTierConfig with all settings.
   * @param isVirtual True if network should be virtual.
   */
  public static void createOneTierNetwork(final String networkId, final OneTierConfig config,
      boolean isVirtual) {
    final HashSet<String> serverIds = new HashSet<String>();
    final HashSet<String> switchIds = new HashSet<String>();

    // Network
    if (!facade.networkExists(networkId)) {
      facade.addNetworkToRoot(networkId, isVirtual);
    }

    // Servers
    for (int i = 0; i < config.getNumberOfServers(); i++) {
      final String currentId = GenUtils.getServerId();
      serverIds.add(currentId);
      facade.addServerToNetwork(String.valueOf(currentId), networkId, config.getCpuPerServer(),
          config.getMemoryPerServer(), config.getStoragePerServer(), 1);
    }

    // Switches
    for (int i = 0; i < config.getNumberOfSwitches(); i++) {
      final String currentId = GenUtils.getSwitchId();
      switchIds.add(currentId);
      facade.addSwitchToNetwork(String.valueOf(currentId), networkId, 0);
    }

    // Links
    for (final String actServerId : serverIds) {
      for (final String actSwitchId : switchIds) {
        // Direction 1
        facade.addLinkToNetwork(GenUtils.getLinkdId(), networkId, config.getBandwidthPerLink(),
            actServerId, actSwitchId);
        // Direction 2
        facade.addLinkToNetwork(GenUtils.getLinkdId(), networkId, config.getBandwidthPerLink(),
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

}