package examples.model;

import examples.AbstractIflyeExample;
import facade.ModelFacade;

/**
 * Runnable example for simple interaction with the model.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ModelExampleSimple extends AbstractIflyeExample {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		//
		// Substrate network
		//

		// Create a network
		final String networkIdSn = "sn";
		ModelFacade.getInstance().addNetworkToRoot(networkIdSn, false);

		// Create nodes
		ModelFacade.getInstance().addServerToNetwork("sr_1", networkIdSn, 10, 10, 10, 1);
		ModelFacade.getInstance().addServerToNetwork("sr_2", networkIdSn, 10, 10, 10, 1);

		// Create links
		ModelFacade.getInstance().addLinkToNetwork("ln_1", networkIdSn, 10, "sr_1", "sr_2");
		ModelFacade.getInstance().addLinkToNetwork("ln_2", networkIdSn, 10, "sr_2", "sr_1");

		//
		// Virtual network
		//

		// Create a network
		final String networkIdVn = "vn";
		ModelFacade.getInstance().addNetworkToRoot(networkIdVn, true);

		// Create nodes
		ModelFacade.getInstance().addServerToNetwork("sr_1", networkIdVn, 10, 10, 10, 1);
		ModelFacade.getInstance().addServerToNetwork("sr_2", networkIdVn, 10, 10, 10, 1);

		// Create links
		ModelFacade.getInstance().addLinkToNetwork("ln_1", networkIdVn, 10, "sr_1", "sr_2");
		ModelFacade.getInstance().addLinkToNetwork("ln_2", networkIdVn, 10, "sr_2", "sr_1");

		// Save model to file
		ModelFacade.getInstance().persistModel();
		logger.info("=> Execution finished.");

		System.exit(0);
	}

}
