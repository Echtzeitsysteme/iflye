package scenarios.load;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.cli.ParseException;

import algorithms.AbstractAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import io.micrometer.core.instrument.Tags;
import metrics.manager.Context;
import metrics.manager.MetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.converter.BasicModelConverter;
import model.converter.IncrementalModelConverter;

/**
 * Runnable (incremental) scenario for VNE algorithms that reads specified files
 * from resource folder.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class DissScenarioLoad extends AbstractExperiment {

	protected final MetricsManager metricsManager = new MetricsManager.Default();

	/**
	 * Main method to start the example. String array of arguments will be parsed.
	 *
	 * @param args See {@link #parseArgs(String[])}.
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, InterruptedException, ParseException {
		ExperimentConfigurator.of(new DissScenarioLoad(), args).run();
	}

	public DissScenarioLoad() {
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

			// Print maximum path length (after possible auto determination)
			if (ModelFacadeConfig.MAX_PATH_LENGTH_AUTO) {
				System.out.println("=> Using path length auto determination");
			}
			System.out.println("=> Using max path length " + ModelFacadeConfig.MAX_PATH_LENGTH);

			/*
			 * Every embedding starts here.
			 */

			String vNetId = IncrementalModelConverter.jsonToModelIncremental(virtNetsPath, true);

			metricsManager.addTags("series uuid", UUID.randomUUID().toString(), "started",
					OffsetDateTime.now().toString(), "implementation", algo.getAlgorithmName());
			metricsManager.initialized();

			while (vNetId != null) {
				final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById(vNetId);

				System.out.println("=> Embedding virtual network " + vNetId);

				final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance()
						.getNetworkById(sNetIds.get(0));

				boolean success = metricsManager.observe("algorithm",
						() -> new Context.VnetRootContext(sNet, Set.of(vNet), algo), () -> {
							// Create and execute algorithm
							MetricsManager.getInstance().observe("prepare", Context.PrepareStageContext::new,
									() -> algo.prepare(sNet, Set.of(vNet)));
							return MetricsManager.getInstance().observe("execute", Context.ExecuteStageContext::new,
									algo::execute);
						}, Tags.of("lastVNR", vNetId, "series group uuid", UUID.randomUUID().toString()));

				if (!success && removeUnembeddedVnets) {
					ModelFacade.getInstance().removeNetworkFromRoot(vNetId);
				}

				// Reload substrate network from model facade (needed for GIPS-based
				// algorithms.)
				metricsManager.flush();

				// Get next virtual network ID to embed
				vNetId = IncrementalModelConverter.jsonToModelIncremental(virtNetsPath, true);

				// Save model to file
				if (persistModel) {
					if (persistModelPath == null) {
						ModelFacade.getInstance().persistModel();
					} else {
						ModelFacade.getInstance().persistModel(persistModelPath);
					}
				}
			}

			/*
			 * End of every embedding.
			 */

			// Validate model
			ModelFacade.getInstance().validateModel();

			/*
			 * Evaluation.
			 */

			// Print metrics before saving the model
			metricsManager.conclude();
		} finally {
			algo.dispose();
			metricsManager.close();
			MetricsManager.closeAll();
		}

		System.out.println("=> Execution finished.");
		System.exit(0);
	}

	public MetricsManager getMetricsManager() {
		return metricsManager;
	}

}
