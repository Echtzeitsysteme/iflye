package examples.algorithms;

import java.util.Set;

import algorithms.heuristics.TafAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the TAF algorithm implementation which implements the
 * virtual networks wihtin a rack.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TafAlgorithmIntraRackOneTierLargerExample {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Setup
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.IGNORE_BW = true;

		// Substrate network = one tier network
		final OneTierConfig substrateConfig = new OneTierConfig(4, 1, false, 1, 1, 1, 20);
		final OneTierNetworkGenerator subGen = new OneTierNetworkGenerator(substrateConfig);
		subGen.createNetwork("sub", false);

		// Virtual network = one tier network
		OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 5);
		OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
		virtGen.createNetwork("virt_1", true);

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt_1");

		// Create and execute algorithm
		TafAlgorithm taf = new TafAlgorithm(sNet, Set.of(vNet));
		taf.execute();

		virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 5);
		virtGen = new OneTierNetworkGenerator(virtualConfig);
		virtGen.createNetwork("virt_2", true);

		final VirtualNetwork vNet2 = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt_2");

		// Create and execute algorithm
		taf = new TafAlgorithm(sNet, Set.of(vNet2));
		taf.execute();

		// Save model to file
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");

		System.exit(0);
	}

}
