package generator;

import java.util.ArrayList;

import config.OneTierConfig;
import facade.ModelFacade;

public class OneTierNetworkGenerator {
	
	private OneTierNetworkGenerator () {}
	
	static private ModelFacade facade = ModelFacade.getInstance();

	public static void createOneTierNetwork (final String networkId, final OneTierConfig config, 
			boolean isVirtual) {
		final ArrayList<Integer> serverIds = new ArrayList<Integer>();
		final ArrayList<Integer> switchIds = new ArrayList<Integer>();
		
		// Network
		facade.addNetworkToRoot(networkId, isVirtual);
		
		// Servers
		for (int i = 0; i < config.getNumberOfServers(); i++) {
			final int currentId = facade.getNextId();
			serverIds.add(currentId);
			facade.addServerToNetwork(String.valueOf(currentId), networkId, config.getCpuPerServer(), 
					config.getMemoryPerServer(), config.getStoragePerServer(), 1);
		}
		
		// Switches
		for (int i = 0; i < config.getNumberOfSwitches(); i++) {
			final int currentId = facade.getNextId();
			switchIds.add(currentId);
			facade.addSwitchToNetwork(String.valueOf(currentId), networkId, 0);
		}
		
		// Links
		for (Integer actServerId : serverIds) {
			for (Integer actSwitchId : switchIds) {
				// Direction 1
				facade.addLinkToNetwork(String.valueOf(facade.getNextId()), networkId, 
						config.getBandwidthPerLink(), actServerId.toString(), 
						actSwitchId.toString());
				// Direction 2
				facade.addLinkToNetwork(String.valueOf(facade.getNextId()), networkId, 
						config.getBandwidthPerLink(), actSwitchId.toString(), 
						actServerId.toString());
			}
		}
		
		// Connect switches together
		if (config.isSwitchesConnected()) {
			throw new UnsupportedOperationException("Not implemented, yet!");
		}
	}
	
}
