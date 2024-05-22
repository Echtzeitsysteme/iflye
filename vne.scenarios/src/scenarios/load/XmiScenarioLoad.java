package scenarios.load;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsAlgorithm;
import algorithms.gips.VneGipsMigrationAlgorithm;
import algorithms.gips.VneGipsSeqAlgorithm;
import algorithms.heuristics.TafAlgorithm;
import algorithms.ilp.VneFakeIlpAlgorithm;
import algorithms.ilp.VneFakeIlpBatchAlgorithm;
import algorithms.ml.MlVneAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithmMigration;
import algorithms.pm.VnePmMdvneAlgorithmPipelineThreeStagesA;
import algorithms.pm.VnePmMdvneAlgorithmPipelineThreeStagesB;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackA;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackB;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesVnet;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
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
import mlmodel.ModelPrediction;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.converter.BasicModelConverter;
import scenario.util.CsvUtil;
import transform.encoding.NodeAttributesEncoding;
import transform.label.MultiClassLabel;

/**
 * Runnable (incremental) scenario for VNE algorithms that reads specified XMI
 * files from resource folder.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class XmiScenarioLoad {

	/**
	 * Configured algorithm to use for every embedding.
	 */
	protected static String algoConfig;

	/**
	 * File path for the metric CSV output.
	 */
	protected static String csvPath = null;

	/**
	 * File path for the XMI file to load.
	 */
	protected static String xmiPath = null;

	/**
	 * Substrate network to use.
	 */
	protected static SubstrateNetwork sNet;

	/**
	 * TODO
	 */
	protected static ModelPrediction mlmodel = null;

	/**
	 * TODO
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		parseArgs(args);

		// Load complete XMI model (with all virtual networks)
		ModelFacade.getInstance().loadModel(xmiPath);

		final List<String> vNetIds = new LinkedList<String>();
		ModelFacade.getInstance().getAllNetworks().forEach(n -> {
			if (n instanceof VirtualNetwork) {
				vNetIds.add(n.getName());
			} else if (n instanceof SubstrateNetwork) {
				sNet = (SubstrateNetwork) n;
			}
		});

		// Persist every virtual network to an own JSON file
		for (int i = 0; i < vNetIds.size(); i++) {
			BasicModelConverter.modelToJson(Set.of(vNetIds.get(i)), "./vnet" + i + ".json");
		}

		// Remove all virtual networks from the model
		removeVirtualNetworks(vNetIds);

		/*
		 * Every embedding starts here.
		 */

		for (int i = 0; i < vNetIds.size(); i++) {
			final String vNetId = vNetIds.get(i);
			sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNet.getName());

			// Create new virtual network based on the previously persisted JSON file
			BasicModelConverter.jsonToModel("./vnet" + i + ".json", true);
			final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById(vNetId);

			System.out.println("=> Embedding virtual network " + vNetId);

			// Create and execute algorithm
			final AbstractAlgorithm algo = newAlgo(Set.of(vNet));
			GlobalMetricsManager.startRuntime();
			algo.execute();
			GlobalMetricsManager.stopRuntime();

			// If the algorithm is the MlVneAlgorithm, it must be disposed
			if (algo instanceof MlVneAlgorithm) {
				MlVneAlgorithm.dispose();
			}

			// Save metrics to CSV file
			// Reload substrate network from model facade (needed for GIPS-based
			// algorithms.)
			sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNet.getName());
			CsvUtil.appendCsvLine(vNet.getName(), csvPath, sNet);
			GlobalMetricsManager.resetRuntime();
			GlobalMetricsManager.resetMemory();
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
		printMetrics();
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");

		System.exit(0);
	}

	private static void setUpMlModel() {
		mlmodel = new ModelPrediction();
		mlmodel.setInEncoding(new NodeAttributesEncoding());
		mlmodel.setStandardizeInputVector(true);
		mlmodel.setEmbeddingLabel(new MultiClassLabel());
	}

	/**
	 * TODO
	 * 
	 * @param ids
	 * @param keep
	 */
	private static void removeVirtualNetworks(final Collection<String> ids) {
		for (final String id : ids) {
			ModelFacade.getInstance().removeNetworkFromRoot(id);
		}
	}

	/**
	 * TODO
	 * 
	 * @param args
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

		// Path for XMI file to load
		final Option subNetFile = new Option("x", "xmipath", true, "XMI file to load");
		subNetFile.setRequired(true);
		options.addOption(subNetFile);

		// CSV output path
		final Option csv = new Option("c", "csvpath", true, "file path for the CSV metric file");
		csv.setRequired(false);
		options.addOption(csv);

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

		// Algorithm
		algoConfig = cmd.getOptionValue("algorithm");

		// Objective
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

		// XMI file path
		xmiPath = cmd.getOptionValue("xmipath");

		// CSV metric file path
		if (cmd.getOptionValue("csvpath") != null) {
			csvPath = cmd.getOptionValue("csvpath");
		}

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
		case "taf":
			ModelFacadeConfig.IGNORE_BW = true;
			return new TafAlgorithm(sNet, vNets);
		case "ml":
			ModelFacadeConfig.IGNORE_BW = true;
			setUpMlModel();
			return MlVneAlgorithm.prepare(sNet, vNets, mlmodel);
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
