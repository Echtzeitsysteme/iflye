package scenarios.load;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.cli.ParseException;

import algorithms.AbstractAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import metrics.manager.Context;
import metrics.manager.MetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.converter.BasicModelConverter;

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
	public static void main(final String[] args) throws IOException, InterruptedException, ParseException {
		final DissScenarioLoad experiment = new DissScenarioLoad();
		experiment.parseArgs(args);
	}

	public DissScenarioLoadBatch() {
		metricsManager.addMeter(new GipsIlpHandler());
	}

	@Override
	public void run() {
		final AbstractAlgorithm algo = algoFactory.apply(ModelFacade.getInstance());

		try {
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

			metricsManager.addTags("series uuid", UUID.randomUUID().toString(), "started",
					OffsetDateTime.now().toString(), "implementation", algo.getAlgorithmName());
			metricsManager.initialized();

			metricsManager.observe("batch", () -> new Context.VnetRootContext(sNet, vNets, algo), () -> {
				// Create and execute algorithm
				MetricsManager.getInstance().observe("prepare", Context.PrepareStageContext::new,
						() -> algo.prepare(sNet, vNets));
				return MetricsManager.getInstance().observe("execute", Context.ExecuteStageContext::new, algo::execute);
			});

			/*
			 * End of every embedding.
			 */

			// Save metrics to CSV file
			// Reload substrate network from model facade (needed for GIPS-based
			// algorithms.)
			sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNet.getName());
			metricsManager.flush();

			// Validate model
			ModelFacade.getInstance().validateModel();

			// Save model to file
			if (persistModel) {
				if (persistModelPath == null) {
					ModelFacade.getInstance().persistModel();
				} else {
					ModelFacade.getInstance().persistModel(persistModelPath);
				}
			}

			/*
			 * Evaluation.
			 */
			metricsManager.conclude();
		} finally {
			algo.dispose();
			metricsManager.close();
			MetricsManager.closeAll();
		}

		System.out.println("=> Execution finished.");
		System.exit(0);
	}

}
