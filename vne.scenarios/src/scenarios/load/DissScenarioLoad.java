package scenarios.load;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Embedding;
import algorithms.AlgorithmConfig.Objective;
import algorithms.ilp.VneIlpPathAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import metrics.AcceptedVnrMetric;
import metrics.AveragePathLengthMetric;
import metrics.TotalCommunicationCostMetricA;
import metrics.TotalCommunicationCostMetricB;
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
    GlobalMetricsManager.startRuntime();

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
        case "ilp":
          algo = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
          break;
        default:
          throw new IllegalArgumentException("Configured algorithm not known.");
      }
      algo.execute();

      // Get next virtual network ID to embed
      vNetId = IncrementalModelConverter.jsonToModelIncremental(virtNetsPath, true);
    }

    /*
     * End of every embedding.
     */

    GlobalMetricsManager.stopRuntime();

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
   * <li>#1: Objective "total-path", "total-comm-a" "total-comm-b"</li>
   * <li>#2: Embedding "emoflon" or "manual" [only relevant for VNE PM algorithm]
   * <li>#3: Maximum path length</li>
   * <li>#4: Substrate network file to load, e.g. "resources/two-tier-4-pods/snet.json"</li>
   * <li>#5: Virtual network(s) file to loag, e.g. "resources/two-tier-4-pods/vnets.json"</li>
   * </ol>
   * 
   * @param args Arguments to parse.
   */
  protected static void parseArgs(final String[] args) {
    if (args == null || args.length < 6) {
      throw new IllegalArgumentException("Arguments are invalid.");
    }

    // #0 Algorithm
    algoConfig = args[0];

    // #1 Objective
    switch (args[1]) {
      case "total-path":
        AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
        break;
      case "total-comm-a":
        AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_A;
        break;
      case "total-comm-b":
        AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_B;
        break;
    }

    // #2 Embedding
    switch (args[2]) {
      case "emoflon":
        AlgorithmConfig.emb = Embedding.EMOFLON;
        break;
      case "manual":
        AlgorithmConfig.emb = Embedding.MANUAL;
        break;
    }

    // #3 Maximum path length
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = Integer.valueOf(args[3]);

    // #4 Substrate network file path
    subNetPath = args[4];

    // #5 Virtual network file path
    virtNetsPath = args[5];

    // Print arguments into logs/system outputs
    System.out.println("=> Arguments: " + Arrays.toString(args));
  }

  /**
   * Prints out all captured metrics that are relevant.
   */
  protected static void printMetrics() {
    // Time measurements
    System.out.println("=> Elapsed time (total): "
        + GlobalMetricsManager.getRuntime().getValue() / 1_000_000_000 + " seconds");
    System.out.println("=> Elapsed time (PM): "
        + GlobalMetricsManager.getRuntime().getPmValue() / 1_000_000_000 + " seconds");
    System.out.println("=> Elapsed time (ILP): "
        + GlobalMetricsManager.getRuntime().getIlpValue() / 1_000_000_000 + " seconds");
    System.out.println("=> Elapsed time (rest): "
        + GlobalMetricsManager.getRuntime().getRestValue() / 1_000_000_000 + " seconds");

    // Embedding quality metrics
    System.out.println("=> Accepted VNRs: " + (int) new AcceptedVnrMetric(sNet).getValue());
    System.out.println("=> Total path cost: " + new TotalPathCostMetric(sNet).getValue());
    System.out.println("=> Average path length: " + new AveragePathLengthMetric(sNet).getValue());
    System.out.println(
        "=> Total communication cost A: " + new TotalCommunicationCostMetricA(sNet).getValue());
    System.out.println(
        "=> Total communication cost B: " + new TotalCommunicationCostMetricB(sNet).getValue());
  }

}