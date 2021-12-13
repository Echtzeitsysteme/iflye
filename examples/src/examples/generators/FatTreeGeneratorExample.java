package examples.generators;

import facade.ModelFacade;
import generators.FatTreeNetworkGenerator;
import generators.config.FatTreeConfig;

/**
 * Runnable example for the fat tree network generator. Creates one substrate
 * network within the model.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class FatTreeGeneratorExample {

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
		System.out.println("=> Execution finished.");

		System.exit(0);
	}

}
