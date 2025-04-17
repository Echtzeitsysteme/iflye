package scenarios.load;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Embedding;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsAlgorithm;
import algorithms.gips.VneGipsBwIgnoreAlgorithm;
import algorithms.gips.VneGipsMigrationAlgorithm;
import algorithms.gips.VneGipsSeqAlgorithm;
import algorithms.heuristics.TafAlgorithm;
import algorithms.ilp.VneFakeIlpAlgorithm;
import algorithms.ilp.VneFakeIlpBatchAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithmMigration;
import algorithms.pm.VnePmMdvneAlgorithmPipelineThreeStagesA;
import algorithms.pm.VnePmMdvneAlgorithmPipelineThreeStagesB;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackA;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackB;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesVnet;
import algorithms.random.RandomVneAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import ilp.wrapper.config.IlpSolverConfig;
import io.micrometer.core.instrument.Tags;
import metrics.MetricConfig;
import metrics.manager.Context;
import metrics.manager.MetricsManager;
import metrics.reporter.CsvReporter;
import metrics.reporter.NotionReporter;
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
public class DissScenarioLoad {

	/**
	 * Orchestrate the collection and reporting of embedding-related metrics
	 */
	protected static MetricsManager metricsManager = new MetricsManager.Default();

	/**
	 * Substrate network to use.
	 */
	protected static SubstrateNetwork sNet;

	/**
	 * File path for the JSON file to load the substrate network from.
	 */
	protected static String subNetPath;

	/**
	 * File path for the JSON file to load all virtual networks from.
	 */
	protected static String virtNetsPath;

	/**
	 * File path for the metric CSV output.
	 */
	protected static String csvPath = null;

	/**
	 * The algorithm to use
	 */
	protected static Function<ModelFacade, AbstractAlgorithm> algoFactory = null;

	/**
	 * If the model should be persisted after execution, optionally supply the file
	 * name.
	 */
	protected static boolean persistModel = false;

	/**
	 * The path to the file where the model should be persisted.
	 */
	protected static String persistModelPath;

	/**
	 * If VNets that where not successfully embedded should be removed from the
	 * model to prevent from blocking further embeddings.
	 */
	protected static boolean removeUnembeddedVnets = false;

	/**
	 * Main method to start the example. String array of arguments will be parsed.
	 *
	 * @param args See {@link #parseArgs(String[])}.
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, InterruptedException {
		parseArgs(args);
		metricsManager.addMeter(new GipsIlpHandler());

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

		String vNetId = IncrementalModelConverter.jsonToModelIncremental(virtNetsPath, true);
		final AbstractAlgorithm algo = algoFactory.apply(ModelFacade.getInstance());

		metricsManager.addTags("series uuid", UUID.randomUUID().toString(), "started", OffsetDateTime.now().toString(),
				"algorithm", algo.getClass().getSimpleName());
		metricsManager.initialized();

		while (vNetId != null) {
			final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById(vNetId);

			System.out.println("=> Embedding virtual network " + vNetId);

			boolean success = metricsManager.observe("algorithm",
					() -> new Context.VnetRootContext(sNet, Set.of(vNet), algo), () -> {
						// Create and execute algorithm
						MetricsManager.getInstance().observe("prepare", Context.PrepareStageContext::new,
								() -> algo.prepare(sNet, Set.of(vNet)));
						return MetricsManager.getInstance().observe("execute", Context.ExecuteStageContext::new,
								algo::execute);
					}, Tags.of("lastVNR", vNetId));

			if (!success && removeUnembeddedVnets) {
				ModelFacade.getInstance().removeNetworkFromRoot(vNetId);
			}

			// Save metrics to CSV file
			// Reload substrate network from model facade (needed for GIPS-based
			// algorithms.)
			sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNet.getName());
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
		metricsManager.close();
		MetricsManager.closeAll();

		System.out.println("=> Execution finished.");
		System.exit(0);
	}

	/**
	 * Parses the given arguments to configure the scenario.
	 * <ol>
	 * <li>#0: Algorithm "pm", "pm-migration", "pm-pipeline2-vnet",
	 * "pm-pipeline2-racka", "pm-pipeline2-rackb", "pm-pipeline3a", "pm-pipeline3b",
	 * "ilp", "ilp-batch", "gips", "gips-mig", "gips-seq", "gips-bwignore", random,
	 * or "taf" (required)</li>
	 * <li>#1: Objective "total-path", "total-comm-a", "total-comm-b",
	 * "total-obj-c", "total-obj-d", "total-taf-comm" (required)</li>
	 * <li>#2: Embedding "emoflon", "emoflon_wo_update" or "manual" [only relevant
	 * for VNE PM algorithm] (optional)
	 * <li>#3: Maximum path length: int or "auto" (optional)</li>
	 * <li>#4: Substrate network file to load, e.g.
	 * "resources/two-tier-4-pods/snet.json" (required)</li>
	 * <li>#5: Virtual network(s) file to load, e.g. "resources/40-vnets/vnets.json"
	 * (required)</li>
	 * <li>#6: Number of migration tries [only relevant for VNE PM algorithm]
	 * (optional)</li>
	 * <li>#7: K fastest paths between two nodes (optional)</li>
	 * <li>#8: CSV metric file path (optional)</li>
	 * <li>#9: ILP solver timeout value (optional)</li>
	 * <li>#10: ILP solver random seed value (optional)</li>
	 * <li>#11: ILP solver optimality tolerance (optional)</li>
	 * <li>#12: ILP solver objective scaling (optional)</li>
	 * <li>#13: Memory measurement enabled (optional)</li>
	 * <li>#14: ILP solver objective logarithm (optional)</li>
	 * </ol>
	 *
	 * @param args Arguments to parse.
	 */
	protected static void parseArgs(final String[] args) {
		final Options options = new Options();

		// Algorithm
		final Option algo = new Option("a", "algorithm", true, "algorithm to use");
		algo.setRequired(true);
		options.addOption(algo);

		// Objective function
		final Option obj = new Option("o", "objective", true, "objective to use");
		obj.setRequired(true);
		options.addOption(obj);

		// Embedding strategy (only for the PM algorithm)
		final Option emb = new Option("e", "embedding", true, "embedding to use for the PM algorithm");
		emb.setRequired(false);
		options.addOption(emb);

		// Maximum path length to generate paths with
		final Option pathLength = new Option("l", "path-length", true, "maximum path length");
		pathLength.setRequired(false);
		options.addOption(pathLength);

		// JSON file for the substrate network to load
		final Option subNetFile = new Option("s", "snetfile", true, "substrate network file to load");
		subNetFile.setRequired(true);
		options.addOption(subNetFile);

		// JSON file for the virtual network(s) to load
		final Option virtNetFile = new Option("v", "vnetfile", true, "virtual network(s) file to load");
		virtNetFile.setRequired(true);
		options.addOption(virtNetFile);

		// Number of tries for the updating functionality of the PM algorithm
		final Option tries = new Option("t", "tries", true, "number of migration tries for the PM algorithm");
		tries.setRequired(false);
		options.addOption(tries);

		// K fastest paths to generate
		final Option paths = new Option("k", "kfastestpaths", true, "k fastest paths between two nodes to generate");
		paths.setRequired(false);
		options.addOption(paths);

		// CSV output path
		final Option csv = new Option("c", "csvpath", true, "file path for the CSV metric file");
		csv.setRequired(false);
		options.addOption(csv);

		// ILP solver timeout
		final Option ilpTimeout = new Option("i", "ilptimeout", true, "ILP solver timeout value in seconds");
		ilpTimeout.setRequired(false);
		options.addOption(ilpTimeout);

		// ILP solver random seed
		final Option ilpRandomSeed = new Option("r", "ilprandomseed", true, "ILP solver random seed");
		ilpRandomSeed.setRequired(false);
		options.addOption(ilpRandomSeed);

		// ILP solver optimality tolerance
		final Option ilpOptTol = new Option("m", "ilpopttol", true, "ILP solver optimality tolerance");
		ilpOptTol.setRequired(false);
		options.addOption(ilpOptTol);

		// ILP solver objective scaling
		final Option ilpObjScaling = new Option("y", "ilpobjscaling", true, "ILP solver objective scaling");
		ilpObjScaling.setRequired(false);
		options.addOption(ilpObjScaling);

		// Memory measurement enabled
		final Option memEnabled = new Option("g", "memmeasurement", false, "Memory measurement metric enabled");
		memEnabled.setRequired(false);
		options.addOption(memEnabled);

		// ILP solver logarithm function
		final Option ilpObjLog = new Option("x", "ilpobjlog", false, "ILP solver objective logarithm");
		ilpObjLog.setRequired(false);
		options.addOption(ilpObjLog);

		// Notion: API Token
		final Option notionToken = new Option("notion-token", true, "The Notion API token.");
		notionToken.setRequired(false);
		notionToken.setType(String.class);
		options.addOption(notionToken);

		// Notion: ID of the series configuration DB
		final Option notionSeriesDB = new Option("notion-series-db", true,
				"The ID of the Notion database to which to store the series configurations");
		notionSeriesDB.setRequired(false);
		notionSeriesDB.setType(String.class);
		options.addOption(notionSeriesDB);

		// Notion: ID of the metrics DB
		final Option notionMetricDB = new Option("notion-metric-db", true,
				"The ID of the Notion database to which to store the metrics");
		notionMetricDB.setRequired(false);
		notionMetricDB.setType(String.class);
		options.addOption(notionMetricDB);

		// Model: Persist after run
		final Option modelPersist = new Option("persist-model", true,
				"If the model should be persisted after execution, optionally supply the file name.");
		modelPersist.setRequired(false);
		modelPersist.setOptionalArg(true);
		modelPersist.setType(String.class);
		options.addOption(modelPersist);

		// Model: Remove unembedded VNets
		final Option removeUnembeddedVnetsOption = new Option("remove-unembedded-vnets", false,
				"If VNets that where not successfully embedded should be removed from the model to prevent from blocking further embeddings");
		removeUnembeddedVnetsOption.setRequired(false);
		options.addOption(removeUnembeddedVnetsOption);

		final CommandLineParser parser = new DefaultParser();
		final HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (final ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("cli parameters", options);
			System.exit(1);
		}

		// Parsing finished. Here starts the configuration.

		// #0 Algorithm
		final String algoConfig = cmd.getOptionValue("algorithm");
		algoFactory = getAlgoFactory(algoConfig);
		metricsManager.addTags("algorithm", algoConfig);

		// #1 Objective
		metricsManager.addTags("objective", cmd.getOptionValue("objective"));
		switch (cmd.getOptionValue("objective")) {
		case "total-path":
			AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
			break;
		case "total-comm-a":
			AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_A;
			break;
		case "total-comm-b":
			AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_B;
			break;
		case "total-obj-c":
			AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
			break;
		case "total-obj-d":
			AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_D;
			break;
		case "total-taf-comm":
			AlgorithmConfig.obj = Objective.TOTAL_TAF_COMMUNICATION_COST;
			break;
		}

		// #2 Embedding
		if (cmd.getOptionValue("embedding") != null) {
			metricsManager.addTags("embedding", cmd.getOptionValue("embedding"));
			switch (cmd.getOptionValue("embedding")) {
			case "emoflon":
				AlgorithmConfig.emb = Embedding.EMOFLON;
				break;
			case "emoflon_wo_update":
				AlgorithmConfig.emb = Embedding.EMOFLON_WO_UPDATE;
				break;
			case "manual":
				AlgorithmConfig.emb = Embedding.MANUAL;
				break;
			}
		}

		// #3 Maximum path length
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		String pathLengthParam = cmd.getOptionValue("path-length");
		if (pathLengthParam != null) {
			metricsManager.addTags("path-length", pathLengthParam);
			if (pathLengthParam.equals("auto")) {
				ModelFacadeConfig.MAX_PATH_LENGTH_AUTO = true;
			} else {
				ModelFacadeConfig.MAX_PATH_LENGTH = Integer.valueOf(cmd.getOptionValue("path-length"));
			}
		}

		// #4 Substrate network file path
		subNetPath = cmd.getOptionValue("snetfile");
		metricsManager.addTags("substrate network", getNetworkConfigurationName(subNetPath));

		// #5 Virtual network file path
		virtNetsPath = cmd.getOptionValue("vnetfile");
		metricsManager.addTags("virtual network", getNetworkConfigurationName(virtNetsPath));

		// #6 Number of migration tries
		if (cmd.getOptionValue("tries") != null) {
			AlgorithmConfig.pmNoMigrations = Integer.valueOf(cmd.getOptionValue("tries"));
			metricsManager.addTags("tries", cmd.getOptionValue("tries"));
		}

		// #7: K fastest paths
		if (cmd.getOptionValue("kfastestpaths") != null) {
			final int K = Integer.valueOf(cmd.getOptionValue("kfastestpaths"));
			metricsManager.addTags("kfastestpaths", cmd.getOptionValue("kfastestpaths"));
			if (K > 1) {
				ModelFacadeConfig.YEN_PATH_GEN = true;
				ModelFacadeConfig.YEN_K = K;
			}
		}

		// #8: CSV metric file path
		if (cmd.getOptionValue("csvpath") != null) {
			csvPath = cmd.getOptionValue("csvpath");
			metricsManager.addReporter(new CsvReporter(new File(csvPath)));
		}

		// #9: ILP solver timeout value
		if (cmd.getOptionValue("ilptimeout") != null) {
			IlpSolverConfig.TIME_OUT = Integer.valueOf(cmd.getOptionValue("ilptimeout"));
			metricsManager.addTags("ilptimeout", cmd.getOptionValue("ilptimeout"));
		}

		// #10: ILP solver random seed
		if (cmd.getOptionValue("ilprandomseed") != null) {
			IlpSolverConfig.RANDOM_SEED = Integer.valueOf(cmd.getOptionValue("ilprandomseed"));
			metricsManager.addTags("ilprandomseed", cmd.getOptionValue("ilprandomseed"));
		}

		// #11: ILP solver optimality tolerance
		if (cmd.getOptionValue("ilpopttol") != null) {
			IlpSolverConfig.OPT_TOL = Double.valueOf(cmd.getOptionValue("ilpopttol"));
			metricsManager.addTags("ilpopttol", cmd.getOptionValue("ilpopttol"));
		}

		// #12: ILP solver objective scaling
		if (cmd.getOptionValue("ilpobjscaling") != null) {
			IlpSolverConfig.OBJ_SCALE = Double.valueOf(cmd.getOptionValue("ilpobjscaling"));
			metricsManager.addTags("ilpobjscaling", cmd.getOptionValue("ilpobjscaling"));
		}

		// #13: Memory measurement enabled
		MetricConfig.ENABLE_MEMORY = cmd.hasOption("memmeasurement");

		// #14: ILP solver objective logarithm
		IlpSolverConfig.OBJ_LOG = cmd.hasOption("ilpobjlog");
		if (cmd.hasOption("ilpobjlog")) {
			metricsManager.addTags("ilpobjlog", String.valueOf(cmd.hasOption("ilpobjlog")));
		}

		final boolean withNotion = cmd.hasOption(notionToken);
		if (withNotion) {
			final String tokenFile = cmd.getOptionValue(notionToken);
			final String seriesDb = cmd.getOptionValue(notionSeriesDB, "");
			final String metricDb = cmd.getOptionValue(notionMetricDB, "");

			final Path path = Paths.get(tokenFile);
			if (!path.toFile().exists() || !path.toFile().isFile() || !path.toFile().canRead()) {
				throw new RuntimeException("Notion Token File does not exist or is not readable: "
						+ path.toAbsolutePath().normalize().toString());
			}
			try {
				final String token = Files.readAllLines(path).get(0).trim();
				metricsManager.addReporter(new NotionReporter(token, seriesDb.isBlank() ? null : seriesDb,
						metricDb.isBlank() ? null : metricDb));
			} catch (IOException e) {
				throw new RuntimeException(
						"Notion Token is not readable at path " + path.toAbsolutePath().normalize().toString());
			}
		}

		if (cmd.hasOption(modelPersist)) {
			final String filePath = cmd.getOptionValue(modelPersist, "");
			persistModel = true;
			persistModelPath = filePath.isBlank() ? null : filePath;
		}

		removeUnembeddedVnets = cmd.hasOption(removeUnembeddedVnetsOption);

		// Print arguments into logs/system outputs
		System.out.println("=> Arguments: " + Arrays.toString(args));
	}

	/**
	 * Creates and returns a new instance of the configured embedding algorithm.
	 *
	 * @param vNets Virtual network(s) to embed.
	 */
	protected static Function<ModelFacade, AbstractAlgorithm> getAlgoFactory(final String algoConfig) {
		switch (algoConfig) {
		case "pm":
			return VnePmMdvneAlgorithm::new;
		case "pm-migration":
			return VnePmMdvneAlgorithmMigration::new;
		case "pm-pipeline2-vnet":
			return VnePmMdvneAlgorithmPipelineTwoStagesVnet::new;
		case "pm-pipeline2-racka":
			return VnePmMdvneAlgorithmPipelineTwoStagesRackA::new;
		case "pm-pipeline2-rackb":
			return VnePmMdvneAlgorithmPipelineTwoStagesRackB::new;
		case "pm-pipeline3a":
			return VnePmMdvneAlgorithmPipelineThreeStagesA::new;
		case "pm-pipeline3b":
			return VnePmMdvneAlgorithmPipelineThreeStagesB::new;
		case "ilp":
			return VneFakeIlpAlgorithm::new;
		case "ilp-batch":
			return VneFakeIlpBatchAlgorithm::new;
		case "gips":
			return VneGipsAlgorithm::new;
		case "gips-mig":
			return VneGipsMigrationAlgorithm::new;
		case "gips-seq":
			return VneGipsSeqAlgorithm::new;
		case "gips-bwignore":
			return VneGipsBwIgnoreAlgorithm::new;
		case "taf":
			ModelFacadeConfig.IGNORE_BW = true;
			return TafAlgorithm::new;
		case "random":
			ModelFacadeConfig.IGNORE_BW = true;
			return RandomVneAlgorithm::new;
		default:
			throw new IllegalArgumentException("Configured algorithm not known.");
		}
	}

	public static String getNetworkConfigurationName(final String filePath) {
		Path p = Paths.get(filePath);
		return p.getParent().getFileName().toString();
	}

}
