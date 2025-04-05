package examples.algorithms;

import java.util.Set;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsSeqAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the VNE GIPS algorithm implementation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsSeqAlgorithmExampleLarge {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Setup
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 6;
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;

		GlobalMetricsManager.startRuntime();

		// Substrate network = two tier network
		final OneTierConfig rackConfig = new OneTierConfig(20, 1, false, 10, 10, 10, 10);
		final TwoTierConfig substrateConfig = new TwoTierConfig();
		substrateConfig.setRack(rackConfig);
		substrateConfig.setCoreBandwidth(100);
		substrateConfig.setNumberOfCoreSwitches(1);
		substrateConfig.setNumberOfRacks(4);
		final TwoTierNetworkGenerator subGen = new TwoTierNetworkGenerator(substrateConfig);
		subGen.createNetwork("sub", false);

		// Virtual network = one tier network
		final OneTierConfig virtualConfig = new OneTierConfig(4, 1, false, 10, 1, 1, 1);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
		virtGen.createNetwork("virt", true);

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		// Create and execute algorithm
		final AbstractAlgorithm algo = new VneGipsSeqAlgorithm();
		algo.prepare(sNet, Set.of(vNet));
		algo.execute();

		GlobalMetricsManager.stopRuntime();

		// Save model to file
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");

		// Time measurements
		System.out.println("=> Elapsed time (total): " + GlobalMetricsManager.getRuntime().getValue() / 1_000_000_000
				+ " seconds");
		System.exit(0);
	}

}
