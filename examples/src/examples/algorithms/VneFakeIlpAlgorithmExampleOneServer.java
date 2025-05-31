package examples.algorithms;

import java.util.Set;

import algorithms.AbstractAlgorithm;
import algorithms.ilp.VneFakeIlpBatchAlgorithm;
import examples.AbstractIflyeExample;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the VNE fake ILP algorithm implementation to embed a
 * network onto one server.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneFakeIlpAlgorithmExampleOneServer extends AbstractIflyeExample {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Setup
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		// Substrate network = two tier network
		final OneTierConfig rackConfig = new OneTierConfig(2, 1, false, 10, 10, 10, 10);
		final TwoTierConfig substrateConfig = new TwoTierConfig();
		substrateConfig.setRack(rackConfig);
		substrateConfig.setCoreBandwidth(100);
		substrateConfig.setNumberOfCoreSwitches(1);
		substrateConfig.setNumberOfRacks(1);
		final TwoTierNetworkGenerator subGen = new TwoTierNetworkGenerator(substrateConfig);
		subGen.createNetwork("sub", false);

		// Virtual network = one tier network
		final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 5, 1, 1, 1);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
		virtGen.createNetwork("virt", true);

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		// Create and execute algorithm
		final AbstractAlgorithm algo = new VneFakeIlpBatchAlgorithm();
		algo.prepare(sNet, Set.of(vNet));
		algo.execute();

		// Save model to file
		ModelFacade.getInstance().persistModel();
		logger.info("=> Execution finished.");

		System.exit(0);
	}

}
