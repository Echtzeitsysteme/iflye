package scenarios.load;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Embedding;
import algorithms.AlgorithmConfig.Objective;
import algorithms.ilp.VneIlpPathAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithmUpdate;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import metrics.AcceptedVnrMetric;
import metrics.AveragePathLengthMetric;
import metrics.TotalCommunicationCostMetricA;
import metrics.TotalCommunicationCostMetricB;
import metrics.TotalCommunicationCostMetricC;
import metrics.TotalPathCostMetric;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.converter.IncrementalModelConverter;

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
   * File path for the JSON file to load all substrate networks from.
   */
  protected static String virtNetsPath;

  /**
   * File path for the metric CSV output.
   */
  protected static String csvPath = null;

  /**
   * Counter for the number of lines within the CSV output file.
   */
  protected static int csvCounter = 0;

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

    /*
     * Every embedding starts here.
     */

    String vNetId = IncrementalModelConverter.jsonToModelIncremental(virtNetsPath, true);

    while (vNetId != null) {
      final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById(vNetId);

      System.out.println("=> Embedding virtual network " + vNetId);

      // Create and execute algorithm
      final AbstractAlgorithm algo;
      switch (algoConfig) {
        case "pm":
          algo = VnePmMdvneAlgorithm.prepare(sNet, Set.of(vNet));
          break;
        case "pm-update":
          algo = VnePmMdvneAlgorithmUpdate.prepare(sNet, Set.of(vNet));
          break;
        case "ilp":
          algo = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
          break;
        default:
          throw new IllegalArgumentException("Configured algorithm not known.");
      }
      GlobalMetricsManager.startRuntime();
      algo.execute();
      GlobalMetricsManager.stopRuntime();

      // Save metrics to CSV file
      appendCsvLine(vNet.getName());
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
   * <li>#0: Algorithm "pm" or "ilp"</li>
   * <li>#1: Objective "total-path", "total-comm-a", "total-comm-b", "total-comm-c"</li>
   * <li>#2: Embedding "emoflon", "emoflon_wo_update" or "manual" [only relevant for VNE PM
   * algorithm]
   * <li>#3: Maximum path length</li>
   * <li>#4: Substrate network file to load, e.g. "resources/two-tier-4-pods/snet.json"</li>
   * <li>#5: Virtual network(s) file to load, e.g. "resources/two-tier-4-pods/vnets.json"</li>
   * <li>#6: Number of update tries [only relevant for VNE PM algorithm]</li>
   * <li>#7: K fastest paths between two nodes</li>
   * <li>#8: CSV metric file path</li>
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
    pathLength.setRequired(true);
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
        new Option("t", "tries", true, "number of update tries for the PM algorithm");
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
    ModelFacadeConfig.MAX_PATH_LENGTH = Integer.valueOf(cmd.getOptionValue("path-length"));

    // #4 Substrate network file path
    subNetPath = cmd.getOptionValue("snetfile");

    // #5 Virtual network file path
    virtNetsPath = cmd.getOptionValue("vnetfile");

    // #6 Number of update tries
    if (cmd.getOptionValue("tries") != null) {
      AlgorithmConfig.pmNoUpdates = Integer.valueOf(cmd.getOptionValue("tries"));
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

    // Print arguments into logs/system outputs
    System.out.println("=> Arguments: " + Arrays.toString(args));
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
  }

  /**
   * Appends the current state of the metrics to the CSV file.
   * 
   * @param lastVnr The Name of the last embedded virtual network (request).
   */
  protected static void appendCsvLine(final String lastVnr) {
    // If file path is null, do not create a file at all
    if (csvPath == null) {
      return;
    }

    try {
      BufferedWriter out;
      // If file does not exist, write header to it
      if (Files.notExists(Path.of(csvPath))) {
        out = Files.newBufferedWriter(Paths.get(csvPath), StandardOpenOption.APPEND,
            StandardOpenOption.CREATE);
        try (final CSVPrinter printer = new CSVPrinter(out,
            CSVFormat.DEFAULT.withHeader("counter", "timestamp", "lastVNR", "time_pm", "time_ilp",
                "time_deploy", "time_rest", "accepted_vnrs", "total_path_cost",
                "average_path_length", "total_communication_cost_a", "total_communication_cost_b",
                "total_communication_cost_c"))) {
          printer.close();
        }
      }

      out = Files.newBufferedWriter(Paths.get(csvPath), StandardOpenOption.APPEND,
          StandardOpenOption.CREATE);
      try (final CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
        printer.printRecord( //
            csvCounter++, // line counter
            java.time.LocalDateTime.now(), // time stamp
            lastVnr, // name of the last embedded virtual network
            GlobalMetricsManager.getRuntime().getPmValue() / 1_000_000_000, // PM time
            GlobalMetricsManager.getRuntime().getIlpValue() / 1_000_000_000, // ILP time
            GlobalMetricsManager.getRuntime().getDeployValue() / 1_000_000_000, // Deploy time
            GlobalMetricsManager.getRuntime().getRestValue() / 1_000_000_000, // Rest time
            (int) new AcceptedVnrMetric(sNet).getValue(), //
            new TotalPathCostMetric(sNet).getValue(), //
            new AveragePathLengthMetric(sNet).getValue(), //
            new TotalCommunicationCostMetricA(sNet).getValue(), //
            new TotalCommunicationCostMetricB(sNet).getValue(), //
            new TotalCommunicationCostMetricC(sNet).getValue() //
        );
        printer.close();
      }
      out.close();
    } catch (final IOException e) {
      // TODO: Error handling
      e.printStackTrace();
    }
  }

}
