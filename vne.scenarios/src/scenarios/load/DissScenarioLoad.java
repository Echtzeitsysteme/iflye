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
import algorithms.heuristics.TafAlgorithm;
import algorithms.ilp.VneIlpPathAlgorithm;
import algorithms.ilp.VneIlpPathAlgorithmBatch;
import algorithms.pm.VnePmMdvneAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithmMigration;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import ilp.wrapper.config.IlpSolverConfig;
import metrics.AcceptedVnrMetric;
import metrics.AveragePathLengthMetric;
import metrics.TotalCommunicationCostMetricA;
import metrics.TotalCommunicationCostMetricB;
import metrics.TotalCommunicationCostMetricC;
import metrics.TotalCommunicationCostMetricD;
import metrics.TotalPathCostMetric;
import metrics.TotalTafCommunicationCostMetric;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.converter.IncrementalModelConverter;
import scenario.util.CsvUtil;

/**
 * Runnable (incremental) scenario for VNE algorithms that reads specified files from resource
 * folder.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
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
    final List<String> sNetIds = IncrementalModelConverter.jsonToModel(subNetPath, false);

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
      CsvUtil.appendCsvLine(vNet.getName(), csvPath, sNet);
      GlobalMetricsManager.resetRuntime();

      // Get next virtual network ID to embed
      vNetId = IncrementalModelConverter.jsonToModelIncremental(virtNetsPath, true);
    }

    /*
     * End of every embedding.
     */

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
   * <li>#0: Algorithm "pm", "pm-migration", "ilp", "ilp-batch" or "taf"</li>
   * <li>#1: Objective "total-path", "total-comm-a", "total-comm-b", "total-comm-c", "total-comm-d",
   * "total-taf-comm"</li>
   * <li>#2: Embedding "emoflon", "emoflon_wo_update" or "manual" [only relevant for VNE PM
   * algorithm]
   * <li>#3: Maximum path length: int or "auto"</li>
   * <li>#4: Substrate network file to load, e.g. "resources/two-tier-4-pods/snet.json"</li>
   * <li>#5: Virtual network(s) file to load, e.g. "resources/two-tier-4-pods/vnets.json"</li>
   * <li>#6: Number of migration tries [only relevant for VNE PM algorithm]</li>
   * <li>#7: K fastest paths between two nodes</li>
   * <li>#8: CSV metric file path</li>
   * <li>#9: ILP solver timeout value</li>
   * <li>#10: ILP solver random seed value</li>
   * <li>#11: ILP solver optimality tolerance</li>
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
    final Option tries =
        new Option("t", "tries", true, "number of migration tries for the PM algorithm");
    tries.setRequired(false);
    options.addOption(tries);

    // K fastest paths to generate
    final Option paths =
        new Option("k", "kfastestpaths", true, "k fastest paths between two nodes to generate");
    paths.setRequired(false);
    options.addOption(paths);

    // CSV output path
    final Option csv = new Option("c", "csvpath", true, "file path for the CSV metric file");
    csv.setRequired(false);
    options.addOption(csv);

    // ILP solver timeout
    final Option ilpTimeout =
        new Option("i", "ilptimeout", true, "ILP solver timeout value in seconds");
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
      case "total-comm-c":
        AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_C;
        break;
      case "total-comm-d":
        AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_D;
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
      case "ilp":
        return new VneIlpPathAlgorithm(sNet, vNets);
      case "ilp-batch":
        return new VneIlpPathAlgorithmBatch(sNet, vNets);
      case "taf":
        ModelFacadeConfig.IGNORE_BW = true;
        return new TafAlgorithm(sNet, vNets);
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
        + GlobalMetricsManager.getGlobalTimeArray()[0] / 1_000_000_000 + " seconds");
    System.out.println("=> Elapsed time (PM): "
        + GlobalMetricsManager.getGlobalTimeArray()[1] / 1_000_000_000 + " seconds");
    System.out.println("=> Elapsed time (ILP): "
        + GlobalMetricsManager.getGlobalTimeArray()[2] / 1_000_000_000 + " seconds");
    System.out.println("=> Elapsed time (deploy): "
        + GlobalMetricsManager.getGlobalTimeArray()[3] / 1_000_000_000 + " seconds");
    System.out.println("=> Elapsed time (rest): "
        + GlobalMetricsManager.getGlobalTimeArray()[4] / 1_000_000_000 + " seconds");

    // Embedding quality metrics
    System.out.println("=> Accepted VNRs: " + (int) new AcceptedVnrMetric(sNet).getValue());
    System.out.println("=> Total path cost: " + new TotalPathCostMetric(sNet).getValue());
    System.out.println("=> Average path length: " + new AveragePathLengthMetric(sNet).getValue());
    System.out.println(
        "=> Total communication cost A: " + new TotalCommunicationCostMetricA(sNet).getValue());
    System.out.println(
        "=> Total communication cost B: " + new TotalCommunicationCostMetricB(sNet).getValue());
    System.out.println(
        "=> Total communication cost C: " + new TotalCommunicationCostMetricC(sNet).getValue());
    System.out.println(
        "=> Total communication cost D: " + new TotalCommunicationCostMetricD(sNet).getValue());
    System.out.println(
        "=> Total TAF communication cost: " + new TotalTafCommunicationCostMetric(sNet).getValue());
  }

}
