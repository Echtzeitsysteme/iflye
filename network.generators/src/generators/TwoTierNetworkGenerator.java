package generators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import generators.config.GlobalGeneratorConfig;
import generators.config.IGeneratorConfig;
import generators.config.TwoTierConfig;

/**
 * Basic implementation of a two tier network topology generator.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TwoTierNetworkGenerator implements INetworkGenerator {

	/**
	 * Core switch ID prefix.
	 */
	public static String CORE_SWITCH_PREFIX = "c" + GlobalGeneratorConfig.SWITCH;

	/**
	 * Rack switch ID prefix.
	 */
	public static String RACK_SWITCH_PREFIX = "r" + GlobalGeneratorConfig.SWITCH;

	/**
	 * Configuration of this network generator instance.
	 */
	private final TwoTierConfig config;

	/**
	 * List for the core switch ID generation.
	 */
	private final List<String> coreSwitchIds = new LinkedList<>();

	/**
	 * List for the rack switch ID generation.
	 */
	private final List<String> rackSwitchIds = new LinkedList<>();

	/**
	 * List for the server ID generation.
	 */
	private final List<String> serverIds = new LinkedList<>();

	/**
	 * Counter for the link ID generation.
	 */
	private int linkCounter = 0;

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
		// Reset sets and counter
		coreSwitchIds.clear();
		rackSwitchIds.clear();
		serverIds.clear();
		linkCounter = 0;

		// Network
		if (!facade.networkExists(networkId)) {
			facade.addNetworkToRoot(networkId, isVirtual);
		}

		// Core switches
		for (int i = 0; i < config.getNumberOfCoreSwitches(); i++) {
			final String currentId = getNextCoreSwitchId(networkId);
			coreSwitchIds.add(currentId);
			facade.addSwitchToNetwork(currentId, networkId, 0);
		}

		// Rack switches
		// Check that rack configuration only has one rack switch per rack
		if (config.getRack().getNumberOfSwitches() != 1) {
			throw new UnsupportedOperationException("Rack config has not exactly one rack switch " + "per rack!");
		}

		for (int i = 0; i < config.getNumberOfRacks(); i++) {
			final String currentId = getNextRackSwitchId(networkId);
			rackSwitchIds.add(currentId);
			facade.addSwitchToNetwork(currentId, networkId, 1);
		}

		// Servers
		final int totalNumberOfServers = config.getNumberOfRacks() * config.getRack().getNumberOfServers();
		for (int i = 0; i < totalNumberOfServers; i++) {
			final String currentId = getNextServerId(networkId);
			serverIds.add(currentId);
			facade.addServerToNetwork(currentId, networkId, config.getRack().getCpuPerServer(),
					config.getRack().getMemoryPerServer(), config.getRack().getStoragePerServer(), 2);
		}

		// Links
		// Connect all core switches to rack switches
		for (final String actCoreSwitch : coreSwitchIds) {
			for (final String actRackSwitch : rackSwitchIds) {
				// Direction 1
				facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getCoreBandwidth(), actCoreSwitch,
						actRackSwitch);
				// Direction 2
				facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getCoreBandwidth(), actRackSwitch,
						actCoreSwitch);
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
				facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getRack().getBandwidthPerLink(),
						actRackSwitch, actLinkId);
				// Direction 2
				facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getRack().getBandwidthPerLink(),
						actLinkId, actRackSwitch);
			}
		}

		// Connect core switches together if option is enabled
		if (config.isCoreSwitchesConnected()) {
			throw new UnsupportedOperationException("Not implemented, yet!");
		}
	}

	/**
	 * Generated the next free core switch ID for this particular network.
	 *
	 * @param networkId Network ID.
	 * @return Next free core switch ID for this particular network.
	 */
	private String getNextCoreSwitchId(final String networkId) {
		return networkId + GlobalGeneratorConfig.SEPARATOR + CORE_SWITCH_PREFIX + GlobalGeneratorConfig.SEPARATOR
				+ coreSwitchIds.size();
	}

	/**
	 * Generated the next free rack switch ID for this particular network.
	 *
	 * @param networkId Network ID.
	 * @return Next free rack switch ID for this particular network.
	 */
	private String getNextRackSwitchId(final String networkId) {
		return networkId + GlobalGeneratorConfig.SEPARATOR + RACK_SWITCH_PREFIX + GlobalGeneratorConfig.SEPARATOR
				+ rackSwitchIds.size();
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
