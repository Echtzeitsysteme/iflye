package scenarios.load;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
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
import scenarios.modules.AlgorithmModule;
import scenarios.modules.CsvModule;
import scenarios.modules.MemoryModule;
import scenarios.modules.ModelConfigurationModule;
import scenarios.modules.Module;
import scenarios.modules.NotionModule;

/**
 * Runnable (incremental) scenario for VNE algorithms that reads specified files
 * from resource folder.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class DissScenarioLoad {

	/**
	 * Substrate network to use.
	 */
	protected SubstrateNetwork sNet;

	/**
	 * File path for the JSON file to load the substrate network from.
	 */
	protected String subNetPath;

	/**
	 * File path for the JSON file to load all virtual networks from.
	 */
	protected String virtNetsPath;

	/**
	 * File path for the metric CSV output.
	 */
	protected static String csvPath = null;

	/**
	 * The algorithm to use
	 */
	protected Function<ModelFacade, AbstractAlgorithm> algoFactory = null;

	/**
	 * If the model should be persisted after execution, optionally supply the file
	 * name.
	 */
	protected boolean persistModel = false;

	/**
	 * The path to the file where the model should be persisted.
	 */
	protected String persistModelPath;

	/**
	 * If VNets that where not successfully embedded should be removed from the
	 * model to prevent from blocking further embeddings.
	 */
	protected boolean removeUnembeddedVnets = false;

	protected final MetricsManager metricsManager = new MetricsManager.Default();

	/**
	 * Main method to start the example. String array of arguments will be parsed.
	 *
	 * @param args See {@link #parseArgs(String[])}.
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, InterruptedException, ParseException {
		final DissScenarioLoad experiment = new DissScenarioLoad();
		experiment.parseArgs(args);
	}

	public DissScenarioLoad() {
		metricsManager.addMeter(new GipsIlpHandler());
	}

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

	/**
	 * Parses the given arguments to configure the scenario.
	 *
	 * @param args Arguments to parse.
	 */
	protected void parseArgs(final String[] args) throws ParseException {
		final Options options = new Options();

		final Option help = Option.builder().option("h").longOpt("help").desc("Display this help message").build();
		options.addOption(help);

		final List<Module> modules = List.of(//
				new AlgorithmModule(this), //
				new CsvModule(this), //
				new MemoryModule(this), //
				new ModelConfigurationModule(this), //
				new NotionModule(this)//
		);

		modules.forEach((module) -> module.register(options));

		final CommandLineParser parser = new DefaultParser();
		final HelpFormatter formatter = new HelpFormatter();

		try {
			final CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption(help)) {
				formatter.printHelp("cli parameters", options);
				System.exit(0);
				return;
			}

			for (final Module module : modules) {
				module.configure(cmd);
			}

			// Print arguments into logs/system outputs
			System.out.println("=> Arguments: " + Arrays.toString(args));
		} catch (final ParseException e) {
			System.err.println(e.getMessage());
			System.err.println();
			formatter.printHelp("cli parameters", options);
			System.exit(1);
			return;
		}
	}

	public Function<ModelFacade, AbstractAlgorithm> getAlgoFactory() {
		return algoFactory;
	}

	public void setAlgoFactory(Function<ModelFacade, AbstractAlgorithm> algoFactory) {
		this.algoFactory = algoFactory;
	}

	public boolean isPersistModel() {
		return persistModel;
	}

	public void setPersistModel(boolean persistModel) {
		this.persistModel = persistModel;
	}

	public String getPersistModelPath() {
		return persistModelPath;
	}

	public void setPersistModelPath(String persistModelPath) {
		this.persistModelPath = persistModelPath;
	}

	public boolean isRemoveUnembeddedVnets() {
		return removeUnembeddedVnets;
	}

	public void setRemoveUnembeddedVnets(boolean removeUnembeddedVnets) {
		this.removeUnembeddedVnets = removeUnembeddedVnets;
	}

	public MetricsManager getMetricsManager() {
		return metricsManager;
	}

	public String getSubNetPath() {
		return subNetPath;
	}

	public void setSubNetPath(String subNetPath) {
		this.subNetPath = subNetPath;
	}

	public String getVirtNetsPath() {
		return virtNetsPath;
	}

	public void setVirtNetsPath(String virtNetsPath) {
		this.virtNetsPath = virtNetsPath;
	}

}
