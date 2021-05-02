package generators;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import facade.ModelFacade;
import generators.config.IGeneratorConfig;
import generators.config.TwoTierConfig;
import generators.utils.GenUtils;

/**
 * Basic implementation of a two tier network topology generator.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class TwoTierNetworkGenerator implements INetworkGenerator {

  /**
   * Configuration of this network generator instance.
   */
  private final TwoTierConfig config;

  /**
   * Private constructor to avoid direct instantiation of this class.
   * 
   * @param config Configuration for this generator.
   */
  public TwoTierNetworkGenerator(final IGeneratorConfig config) {
    if (!(config instanceof TwoTierConfig)) {
      throw new IllegalArgumentException("Configuration instance is not an TwoTierConfig.");
    }

    this.config = (TwoTierConfig) config;
  }

  /**
   * Method to create the network.
   * 
   * @param networkId ID of the network to create.
   * @param isVirtual True if network should be virtual.
   */
  @Override
  public void createNetwork(final String networkId, boolean isVirtual) {
    final List<String> serverIds = new LinkedList<String>();
    final HashSet<String> coreSwitchIds = new HashSet<String>();
    final List<String> rackSwitchIds = new LinkedList<String>();

    // Network
    if (!facade.networkExists(networkId)) {
      facade.addNetworkToRoot(networkId, isVirtual);
    }

    // Core switches
    for (int i = 0; i < config.getNumberOfCoreSwitches(); i++) {
      final String currentId = GenUtils.getSwitchId();
      coreSwitchIds.add(currentId);
      facade.addSwitchToNetwork(currentId, networkId, 0);
    }

    // Rack switches
    // Check that rack configuration only has one rack switch per rack
    if (config.getRack().getNumberOfSwitches() != 1) {
      throw new UnsupportedOperationException(
          "Rack config has not exactly one rack switch " + "per rack!");
    }

    for (int i = 0; i < config.getNumberOfRacks(); i++) {
      final String currentId = GenUtils.getSwitchId();
      rackSwitchIds.add(currentId);
      facade.addSwitchToNetwork(currentId, networkId, 1);
    }

    // Servers
    final int totalNumberOfServers =
        config.getNumberOfRacks() * config.getRack().getNumberOfServers();
    for (int i = 0; i < totalNumberOfServers; i++) {
      final String currentId = GenUtils.getServerId();
      serverIds.add(currentId);
      facade.addServerToNetwork(currentId, networkId, config.getRack().getCpuPerServer(),
          config.getRack().getMemoryPerServer(), config.getRack().getStoragePerServer(), 2);
    }

    // Links
    // Connect all core switches to rack switches
    for (final String actCoreSwitch : coreSwitchIds) {
      for (final String actRackSwitch : rackSwitchIds) {
        // Direction 1
        facade.addLinkToNetwork(GenUtils.getLinkdId(), networkId, config.getCoreBandwidth(),
            actCoreSwitch, actRackSwitch);
        // Direction 2
        facade.addLinkToNetwork(GenUtils.getLinkdId(), networkId, config.getCoreBandwidth(),
            actRackSwitch, actCoreSwitch);
      }
    }

    // Connect all rack switches to servers
    final Iterator<String> it = serverIds.iterator();
    // Iterate over all rack switches
    for (final String actRackSwitch : rackSwitchIds) {
      // Iterate over the next n servers for this particular rack switch
      for (int i = 0; i < config.getRack().getNumberOfServers(); i++) {
        final String actLinkId = it.next();
        // Direction 1
        facade.addLinkToNetwork(GenUtils.getLinkdId(), networkId,
            config.getRack().getBandwidthPerLink(), actRackSwitch, actLinkId);
        // Direction 2
        facade.addLinkToNetwork(GenUtils.getLinkdId(), networkId,
            config.getRack().getBandwidthPerLink(), actLinkId, actRackSwitch);
      }
    }

    // Connect core switches together if option is enabled
    if (config.isCoreSwitchesConnected()) {
      throw new UnsupportedOperationException("Not implemented, yet!");
    }

    // Generate paths
    if (!isVirtual) {
      ModelFacade.getInstance().createAllPathsForNetwork(networkId);
    }
  }

}
