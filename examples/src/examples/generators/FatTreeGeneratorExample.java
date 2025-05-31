package examples.generators;

import examples.AbstractIflyeExample;
import facade.ModelFacade;
import generators.FatTreeNetworkGenerator;
import generators.config.FatTreeConfig;

/**
 * Runnable example for the fat tree network generator. Creates one substrate
 * network within the model.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class FatTreeGeneratorExample extends AbstractIflyeExample {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Fat tree network generation
		final FatTreeConfig config = new FatTreeConfig(4);
		config.setBwCoreToAggr(100);
		config.setBwAggrToEdge(40);
		final FatTreeNetworkGenerator gen = new FatTreeNetworkGenerator(config);
		gen.createNetwork("sub", false);

		// Save model to file
		ModelFacade.getInstance().persistModel();
		logger.info("=> Execution finished.");

		System.exit(0);
	}

}
