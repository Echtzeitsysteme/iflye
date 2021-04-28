package examples.model;

import facade.ModelFacade;

public class TempModelRunner {

	public static void main(final String[] args) {
		final String networkId = "test";
		ModelFacade.getInstance().addNetworkToRoot(networkId, false);
		ModelFacade.getInstance().addSwitchToNetwork("sw_1", networkId, 0);
		ModelFacade.getInstance().addServerToNetwork("sr_1", networkId, 10, 10, 10, 1);
		ModelFacade.getInstance().addServerToNetwork("sr_2", networkId, 10, 10, 10, 1);
		ModelFacade.getInstance().addLinkToNetwork("ln_1", networkId, 20, "sw_1", "sr_1");
		ModelFacade.getInstance().addLinkToNetwork("ln_2", networkId, 20, "sr_1", "sw_1");
		ModelFacade.getInstance().addLinkToNetwork("ln_3", networkId, 40, "sw_1", "sr_2");
		ModelFacade.getInstance().addLinkToNetwork("ln_4", networkId, 40, "sr_2", "sw_1");
		
		ModelFacade.getInstance().addLinkToNetwork("ln_5", networkId, 40, "sr_2", "sw_4");

		ModelFacade.getInstance().dummy();

		System.out.println("=> Execution finished.");
	}

}
