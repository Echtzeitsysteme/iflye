package examples.algorithms;

import java.util.Set;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Embedding;
import algorithms.ilp.VneFakeIlpBatchAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the VNE fake ILP algorithm implementation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneFakeIlpBatchAlgorithmExampleOneTier {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Setup
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 1;
		AlgorithmConfig.emb = Embedding.MANUAL;

		// Substrate network = two tier network
		final OneTierConfig substrateConfig = new OneTierConfig(10, 1, false, 10, 10, 10, 10);
		final OneTierNetworkGenerator subGen = new OneTierNetworkGenerator(substrateConfig);
		subGen.createNetwork("sub", false);

		// Virtual network = one tier network
		final OneTierConfig virtualConfig = new OneTierConfig(10, 1, false, 10, 10, 10, 10);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
		virtGen.createNetwork("virt", true);

		ModelFacade.getInstance().persistModel("model-before.xmi");
		ModelFacade.getInstance().loadModel("model-before.xmi");

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		// Create and execute algorithm
		final AbstractAlgorithm algo = VneFakeIlpBatchAlgorithm.prepare(sNet, Set.of(vNet));
		algo.execute();

		// Save model to file
		ModelFacade.getInstance().persistModel("model-after.xmi");
		System.out.println("=> Execution finished.");

		System.exit(0);
	}

}
