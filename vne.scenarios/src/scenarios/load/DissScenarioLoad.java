package scenarios.load;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
import metrics.MetricConfig;
import metrics.MetricConsts;
import metrics.embedding.AcceptedVnrMetric;
import metrics.embedding.AveragePathLengthMetric;
import metrics.embedding.OperatingCostMetric;
import metrics.embedding.TotalCommunicationCostMetricA;
import metrics.embedding.TotalCommunicationCostMetricB;
import metrics.embedding.TotalCommunicationCostMetricC;
import metrics.embedding.TotalCommunicationCostMetricD;
import metrics.embedding.TotalCommunicationCostObjectiveC;
import metrics.embedding.TotalCommunicationCostObjectiveD;
import metrics.embedding.TotalPathCostMetric;
import metrics.embedding.TotalTafCommunicationCostMetric;
import metrics.manager.GlobalMetricsManager;
import metrics.memory.MemoryMetric;
import metrics.memory.MemoryPidMetric;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.converter.BasicModelConverter;
import model.converter.IncrementalModelConverter;
import scenario.util.CsvUtil;

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
	protected static SubstrateNetwork sNet;

	/**
	 * Configured algorithm to use for every embedding.
	 */
	protected static String algoConfig;

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

		String vNetId = IncrementalModelConverter.jsonToModelIncremental(virtNetsPath, true);

		while (vNetId != null) {
			final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById(vNetId);

			System.out.println("=> Embedding virtual network " + vNetId);

			// Create and execute algorithm
			final AbstractAlgorithm algo = newAlgo(Set.of(vNet));
			GlobalMetricsManager.startRuntime();
			algo.execute();
			GlobalMetricsManager.stopRuntime();

			// Save metrics to CSV file
			// Reload substrate network from model facade (needed for GIPS-based
			// algorithms.)
			sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNet.getName());
			CsvUtil.appendCsvLine(vNet.getName(), csvPath, sNet);
			GlobalMetricsManager.resetRuntime();
			GlobalMetricsManager.resetMemory();

			// Get next virtual network ID to embed
			vNetId = IncrementalModelConverter.jsonToModelIncremental(virtNetsPath, true);
		}

		/*
		 * End of every embedding.
		 */

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
		algoConfig = cmd.getOptionValue("algorithm");

		// #1 Objective
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
			if (pathLengthParam.equals("auto")) {
				ModelFacadeConfig.MAX_PATH_LENGTH_AUTO = true;
			} else {
				ModelFacadeConfig.MAX_PATH_LENGTH = Integer.valueOf(cmd.getOptionValue("path-length"));
			}
		}

		// #4 Substrate network file path
		subNetPath = cmd.getOptionValue("snetfile");

		// #5 Virtual network file path
		virtNetsPath = cmd.getOptionValue("vnetfile");

		// #6 Number of migration tries
		if (cmd.getOptionValue("tries") != null) {
			AlgorithmConfig.pmNoMigrations = Integer.valueOf(cmd.getOptionValue("tries"));
		}

		// #7: K fastest paths
		if (cmd.getOptionValue("kfastestpaths") != null) {
			final int K = Integer.valueOf(cmd.getOptionValue("kfastestpaths"));
			if (K > 1) {
				ModelFacadeConfig.YEN_PATH_GEN = true;
				ModelFacadeConfig.YEN_K = K;
			}
		}

		// #8: CSV metric file path
		if (cmd.getOptionValue("csvpath") != null) {
			csvPath = cmd.getOptionValue("csvpath");
		}

		// #9: ILP solver timeout value
		if (cmd.getOptionValue("ilptimeout") != null) {
			IlpSolverConfig.TIME_OUT = Integer.valueOf(cmd.getOptionValue("ilptimeout"));
		}

		// #10: ILP solver random seed
		if (cmd.getOptionValue("ilprandomseed") != null) {
			IlpSolverConfig.RANDOM_SEED = Integer.valueOf(cmd.getOptionValue("ilprandomseed"));
		}

		// #11: ILP solver optimality tolerance
		if (cmd.getOptionValue("ilpopttol") != null) {
			IlpSolverConfig.OPT_TOL = Double.valueOf(cmd.getOptionValue("ilpopttol"));
		}

		// #12: ILP solver objective scaling
		if (cmd.getOptionValue("ilpobjscaling") != null) {
			IlpSolverConfig.OBJ_SCALE = Double.valueOf(cmd.getOptionValue("ilpobjscaling"));
		}

		// #13: Memory measurement enabled
		MetricConfig.ENABLE_MEMORY = cmd.hasOption("memmeasurement");

		// #14: ILP solver objective logarithm
		IlpSolverConfig.OBJ_LOG = cmd.hasOption("ilpobjlog");

		// Print arguments into logs/system outputs
		System.out.println("=> Arguments: " + Arrays.toString(args));
	}

	/**
	 * Creates and returns a new instance of the configured embedding algorithm.
	 *
	 * @param vNets Virtual network(s) to embed.
	 */
	protected static AbstractAlgorithm newAlgo(final Set<VirtualNetwork> vNets) {
		switch (algoConfig) {
		case "pm":
			return VnePmMdvneAlgorithm.prepare(sNet, vNets);
		case "pm-migration":
			return VnePmMdvneAlgorithmMigration.prepare(sNet, vNets);
		case "pm-pipeline2-vnet":
			return VnePmMdvneAlgorithmPipelineTwoStagesVnet.prepare(sNet, vNets);
		case "pm-pipeline2-racka":
			return VnePmMdvneAlgorithmPipelineTwoStagesRackA.prepare(sNet, vNets);
		case "pm-pipeline2-rackb":
			return VnePmMdvneAlgorithmPipelineTwoStagesRackB.prepare(sNet, vNets);
		case "pm-pipeline3a":
			return VnePmMdvneAlgorithmPipelineThreeStagesA.prepare(sNet, vNets);
		case "pm-pipeline3b":
			return VnePmMdvneAlgorithmPipelineThreeStagesB.prepare(sNet, vNets);
		case "ilp":
			return VneFakeIlpAlgorithm.prepare(sNet, vNets);
		case "ilp-batch":
			return VneFakeIlpBatchAlgorithm.prepare(sNet, vNets);
		case "gips":
			return VneGipsAlgorithm.prepare(sNet, vNets);
		case "gips-mig":
			return VneGipsMigrationAlgorithm.prepare(sNet, vNets);
		case "gips-seq":
			return VneGipsSeqAlgorithm.prepare(sNet, vNets);
		case "gips-bwignore":
			return VneGipsBwIgnoreAlgorithm.prepare(sNet, vNets);
		case "taf":
			ModelFacadeConfig.IGNORE_BW = true;
			return new TafAlgorithm(sNet, vNets);
		case "random":
			ModelFacadeConfig.IGNORE_BW = true;
			return new RandomVneAlgorithm(sNet, vNets);
		default:
			throw new IllegalArgumentException("Configured algorithm not known.");
		}
	}

	/**
	 * Prints out all captured metrics that are relevant.
	 */
	protected static void printMetrics() {
		// Time measurements
		System.out.println("=> Elapsed time (total): "
				+ GlobalMetricsManager.getGlobalTimeArray()[0] / MetricConsts.NANO_TO_MILLI + " seconds");
		System.out.println("=> Elapsed time (PM): "
				+ GlobalMetricsManager.getGlobalTimeArray()[1] / MetricConsts.NANO_TO_MILLI + " seconds");
		System.out.println("=> Elapsed time (ILP): "
				+ GlobalMetricsManager.getGlobalTimeArray()[2] / MetricConsts.NANO_TO_MILLI + " seconds");
		System.out.println("=> Elapsed time (deploy): "
				+ GlobalMetricsManager.getGlobalTimeArray()[3] / MetricConsts.NANO_TO_MILLI + " seconds");
		System.out.println("=> Elapsed time (rest): "
				+ GlobalMetricsManager.getGlobalTimeArray()[4] / MetricConsts.NANO_TO_MILLI + " seconds");

		// Embedding quality metrics
		System.out.println("=> Accepted VNRs: " + (int) new AcceptedVnrMetric(sNet).getValue());
		System.out.println("=> Total path cost: " + new TotalPathCostMetric(sNet).getValue());
		System.out.println("=> Average path length: " + new AveragePathLengthMetric(sNet).getValue());
		System.out.println("=> Total communication cost A: " + new TotalCommunicationCostMetricA(sNet).getValue());
		System.out.println("=> Total communication cost B: " + new TotalCommunicationCostMetricB(sNet).getValue());
		System.out.println("=> Total communication cost C: " + new TotalCommunicationCostMetricC(sNet).getValue());
		System.out.println("=> Total communication cost D: " + new TotalCommunicationCostMetricD(sNet).getValue());
		System.out.println(
				"=> Total communication objective C: " + new TotalCommunicationCostObjectiveC(sNet).getValue());
		System.out.println(
				"=> Total communication objective D: " + new TotalCommunicationCostObjectiveD(sNet).getValue());
		System.out.println("=> Total TAF communication cost: " + new TotalTafCommunicationCostMetric(sNet).getValue());
		System.out.println("=> Operation cost: " + new OperatingCostMetric(sNet).getValue());

		// Memory measurements
		System.out.println("=> Memory metric (current): " + new MemoryMetric().getValue() + " MiB");
		System.out.println("=> Memory PID metric (maximum): " + new MemoryPidMetric().getValue() + " MiB");
	}

}
