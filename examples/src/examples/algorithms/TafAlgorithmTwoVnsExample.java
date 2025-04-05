package examples.algorithms;

import java.util.Set;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.heuristics.TafAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the TAF algorithm implementation which implements two
 * virtual networks.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TafAlgorithmTwoVnsExample {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Setup
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.IGNORE_BW = true;
		AlgorithmConfig.obj = Objective.TOTAL_TAF_COMMUNICATION_COST;

		// Substrate network = two tier network
		final OneTierConfig sRackConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 20);
		final TwoTierConfig substrateConfig = new TwoTierConfig();
		substrateConfig.setNumberOfRacks(2);
		substrateConfig.setRack(sRackConfig);
		final TwoTierNetworkGenerator subGen = new TwoTierNetworkGenerator(substrateConfig);
		subGen.createNetwork("sub", false);

		// Virtual network = one tier network
		final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 5);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");

		for (int i = 1; i <= 2; i++) {
			virtGen.createNetwork("virt_" + i, true);
			final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt_" + i);
			// Create and execute algorithm
			final TafAlgorithm taf = new TafAlgorithm();
			taf.prepare(sNet, Set.of(vNet));
			taf.execute();
		}

		// Save model to file
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");

		System.exit(0);
	}

}
