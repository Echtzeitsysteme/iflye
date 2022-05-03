package scenarios.load;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import algorithms.AbstractAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.converter.BasicModelConverter;
import scenario.util.CsvUtil;

/**
 * Runnable (batch) scenario for VNE algorithms that reads specified files from
 * resource folder.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class DissScenarioLoadBatch extends DissScenarioLoad {

	/**
	 * Main method to start the example. String array of arguments will be parsed.
	 *
	 * @param args See {@link #parseArgs(String[])}.
	 */
	public static void main(final String[] args) {
		parseArgs(args);

		// Substrate network = read from file
		final List<String> sNetIds = BasicModelConverter.jsonToModel(subNetPath, false);

		if (sNetIds.size() != 1) {
			throw new UnsupportedOperationException("There is more than one substrate network.");
		}

		sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNetIds.get(0));

		// Print maximum path length (after possible auto determination)
		if (ModelFacadeConfig.MAX_PATH_LENGTH_AUTO) {
			System.out.println("=> Using path length auto determination");
		}
		System.out.println("=> Using max path length " + ModelFacadeConfig.MAX_PATH_LENGTH);

		/*
		 * Every embedding starts here.
		 */

		final List<String> vNetIds = BasicModelConverter.jsonToModel(virtNetsPath, true);
		final Set<VirtualNetwork> vNets = new HashSet<>();
		vNetIds.forEach(i -> vNets.add((VirtualNetwork) ModelFacade.getInstance().getNetworkById(i)));

		// Create and execute algorithm
		final AbstractAlgorithm algo = newAlgo(vNets);

		GlobalMetricsManager.startRuntime();
		algo.execute();
		GlobalMetricsManager.stopRuntime();

		/*
		 * End of every embedding.
		 */

		// Save metrics to CSV file
		// Reload substrate network from model facade (needed for GIPS-based
		// algorithms.)
		sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNet.getName());
		CsvUtil.appendCsvLine("batch-all", csvPath, sNet);
		GlobalMetricsManager.resetRuntime();

		// Validate model
		ModelFacade.getInstance().validateModel();

		/*
		 * Evaluation.
		 */

		// Save model to file
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");
		printMetrics();

		System.exit(0);
	}

}
