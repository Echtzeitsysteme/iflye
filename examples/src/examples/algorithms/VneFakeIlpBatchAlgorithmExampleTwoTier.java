package examples.algorithms;

import java.util.Set;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Embedding;
import algorithms.ilp.VneFakeIlpBatchAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the VNE fake ILP algorithm implementation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneFakeIlpBatchAlgorithmExampleTwoTier {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Setup
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 2;
		AlgorithmConfig.emb = Embedding.MANUAL;

		// Substrate network = two tier network
		final TwoTierConfig substrateConfig = new TwoTierConfig();
		final OneTierConfig rackConfig = new OneTierConfig(10, 1, false, 10, 10, 10, 10);
		substrateConfig.setRack(rackConfig);
		substrateConfig.setNumberOfCoreSwitches(2);
		substrateConfig.setNumberOfRacks(4);
		substrateConfig.setCoreSwitchesConnected(false);
		substrateConfig.setCoreBandwidth(100);
		final TwoTierNetworkGenerator subGen = new TwoTierNetworkGenerator(substrateConfig);
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
		final AbstractAlgorithm algo = new VneFakeIlpBatchAlgorithm();
		algo.prepare(sNet, Set.of(vNet));
		algo.execute();

		// Save model to file
		ModelFacade.getInstance().persistModel("model-after.xmi");
		System.out.println("=> Execution finished.");

		System.exit(0);
	}

}
