package examples.model;

import facade.ModelFacade;

/**
 * Runnable example for simple interaction with the model.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ModelExample {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Create a network
		final String networkId = "test";
		ModelFacade.getInstance().addNetworkToRoot(networkId, false);

		// Create nodes
		ModelFacade.getInstance().addSwitchToNetwork("sw_1", networkId, 0);
		ModelFacade.getInstance().addServerToNetwork("sr_1", networkId, 10, 10, 10, 1);
		ModelFacade.getInstance().addServerToNetwork("sr_2", networkId, 10, 10, 10, 1);

		// Create links
		ModelFacade.getInstance().addLinkToNetwork("ln_1", networkId, 20, "sw_1", "sr_1");
		ModelFacade.getInstance().addLinkToNetwork("ln_2", networkId, 20, "sr_1", "sw_1");
		ModelFacade.getInstance().addLinkToNetwork("ln_3", networkId, 40, "sw_1", "sr_2");
		ModelFacade.getInstance().addLinkToNetwork("ln_4", networkId, 40, "sr_2", "sw_1");

		// Try to add an invalid link to trigger an exception
		// ModelFacade.getInstance().addLinkToNetwork("ln_5", networkId, 40, "sr_2",
		// "sw_4");

		// Save model to file
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");

		System.exit(0);
	}

}
