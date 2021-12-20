package examples.algorithms;

import java.util.List;
import java.util.Set;

import algorithms.AbstractAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.converter.BasicModelConverter;

/**
 * Runnable example for the VNE PM algorithm implementation that reads a
 * predetermined JSON file.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmExampleRead {

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
		final OneTierConfig rackConfig = new OneTierConfig(2, 1, false, 32, 2_000_000_000, 2_000_000_000, 2_000);
		final TwoTierConfig substrateConfig = new TwoTierConfig();
		substrateConfig.setRack(rackConfig);
		substrateConfig.setCoreBandwidth(10_000);
		substrateConfig.setNumberOfCoreSwitches(1);
		substrateConfig.setNumberOfRacks(2);
		final TwoTierNetworkGenerator subGen = new TwoTierNetworkGenerator(substrateConfig);
		subGen.createNetwork("sub", false);

		// Virtual network = read from file
		final List<String> vNetIds = BasicModelConverter.jsonToModel("vnets.json", true);

		for (final String vNetId : vNetIds) {
			final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
			final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById(vNetId);

			// Create and execute algorithm
			final AbstractAlgorithm algo = VnePmMdvneAlgorithm.prepare(sNet, Set.of(vNet));
			algo.execute();
		}

		// Save model to file
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");

		System.exit(0);
	}

}
