package examples.gips;

import java.util.HashSet;
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
 * Runnable example for the VNE fake ILP algorithm implementation for the use by
 * GIPS.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneFakeIlpAlgorithmExampleGipsTwoTierMultipleVns {

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

		// Virtual networks = one tier networks
		final Set<String> virtNetIds = new HashSet<>();
		for (int i = 0; i <= 3; i++) {
			final OneTierConfig virtualConfig = new OneTierConfig(10, 1, false, 10, 10, 10, 10);
			final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
			final String actId = "virt" + i;
			virtGen.createNetwork(actId, true);
			virtNetIds.add(actId);
		}

		ModelFacade.getInstance().persistModel("model-before.xmi");

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final Set<VirtualNetwork> virtNets = new HashSet<>();
		virtNetIds.forEach(v -> {
			virtNets.add((VirtualNetwork) ModelFacade.getInstance().getNetworkById(v));
		});

		// Create and execute algorithm
		final AbstractAlgorithm algo = VneFakeIlpBatchAlgorithm.prepare(sNet, virtNets);
		algo.execute();

		// Save model to file
		ModelFacade.getInstance().persistModel("model-after.xmi");
		System.out.println("=> Execution finished.");

		System.exit(0);
	}

}
