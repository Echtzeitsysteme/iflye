package examples.algorithms;

import java.util.List;
import java.util.Set;

import algorithms.AbstractAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import examples.AbstractIflyeExample;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.converter.BasicModelConverter;

/**
 * Runnable example for the VNE PM algorithm implementation that reads a
 * predetermined JSON file.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmExampleDualRead extends AbstractIflyeExample {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Setup
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 2;

		// Read substrate network from file
		final List<String> sNetIds = BasicModelConverter.jsonToModel("snet.json", false);

		// Read all virtual networks from file
		final List<String> vNetIds = BasicModelConverter.jsonToModel("vnets.json", true);

		for (final String vNetId : vNetIds) {
			final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNetIds.get(0));
			final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById(vNetId);

			// Create and execute algorithm
			final AbstractAlgorithm algo = new VnePmMdvneAlgorithm();
			algo.prepare(sNet, Set.of(vNet));
			algo.execute();
		}

		// Save model to file
		ModelFacade.getInstance().persistModel();
		logger.info("=> Execution finished.");

		System.exit(0);
	}

}
