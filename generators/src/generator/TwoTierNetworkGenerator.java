package generator;

import java.util.HashSet;
import java.util.Iterator;

import config.TwoTierConfig;
import facade.ModelFacade;
import utils.GenUtils;

/**
 * Basic implementation of a two tier network topology generator.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class TwoTierNetworkGenerator {

	/**
	 * Private constructor to avoid direct instantiation of this class.
	 */
	private TwoTierNetworkGenerator () {}
	
	/**
	 * ModelFacade instance.
	 */
	static private ModelFacade facade = ModelFacade.getInstance();

	/**
	 * Method to create the network.
	 * 
	 * @param networkId ID of the network to create.
	 * @param config TwoTierConfig with all settings.
	 * @param isVirtual True if network should be virtual.
	 */
	public static void createTwoTierNetwork (final String networkId, final TwoTierConfig config,
			boolean isVirtual) {
		final HashSet<String> serverIds = new HashSet<String>();
		final HashSet<String> coreSwitchIds = new HashSet<String>();
		final HashSet<String> rackSwitchIds = new HashSet<String>();
		
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
			throw new UnsupportedOperationException("Rack config has not exactly one rack switch "
					+ "per rack!");
		}
		
		for (int i = 0; i < config.getNumberOfRacks(); i++) {
			final String currentId = GenUtils.getSwitchId();
			rackSwitchIds.add(currentId);
			facade.addSwitchToNetwork(currentId, networkId, 1);
		}
		
		// Servers
		final int totalNumberOfServers = config.getNumberOfRacks() * config.getRack().getNumberOfServers();
		for (int i = 0; i < totalNumberOfServers; i++) {
			final String currentId = GenUtils.getServerId();
			serverIds.add(currentId);
			facade.addServerToNetwork(currentId, networkId, 
					config.getRack().getCpuPerServer(),
					config.getRack().getMemoryPerServer(),
					config.getRack().getStoragePerServer(),
					2);
		}
		
		// Links
		// Connect all core switches to rack switches
		for (String actCoreSwitch : coreSwitchIds) {
			for (String actRackSwitch : rackSwitchIds) {
				// Direction 1
				facade.addLinkToNetwork(GenUtils.getLinkdId(), networkId,
						config.getCoreBandwidth(), actCoreSwitch, actRackSwitch);
				// Direction 2
				facade.addLinkToNetwork(GenUtils.getLinkdId(), networkId,
						config.getCoreBandwidth(), actRackSwitch, actCoreSwitch);
			}
		}
		
		// Connect all rack switches to servers
		final Iterator<String> it = serverIds.iterator();
		// Iterate over all rack switches
		for (String actRackSwitch : rackSwitchIds) {
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
	}
	
}
