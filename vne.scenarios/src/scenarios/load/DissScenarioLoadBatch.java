package scenarios.load;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import algorithms.AbstractAlgorithm;
import algorithms.ilp.VneIlpPathAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import metrics.AcceptedVnrMetric;
import metrics.AveragePathLengthMetric;
import metrics.TotalPathCostMetric;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.converter.BasicModelConverter;
import model.converter.IncrementalModelConverter;

/**
 * Runnable scenario for VNE algorithms that reads specified files from resource folder.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class DissScenarioLoadBatch {

  /**
   * Main method to start the example. String array of arguments will be ignored.
   * 
   * @param args Will be ignored.
   */
  public static void main(final String[] args) {
    /*
     * Setup.
     */
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;

    // Substrate network = read from file
    final List<String> sNetIds =
        IncrementalModelConverter.jsonToModel("resources/two-tier-4-pods/snet.json", false);

    if (sNetIds.size() != 1) {
      throw new UnsupportedOperationException("There is more than one substrate network.");
    }

    GlobalMetricsManager.startRuntime();

    /*
     * Embeddings themselves.
     */

    final List<String> vNetIds =
        BasicModelConverter.jsonToModel("resources/two-tier-4-pods/vnets_reduced.json", true);
    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNetIds.get(0));
    final Set<VirtualNetwork> vNets = new HashSet<VirtualNetwork>();
    vNetIds.forEach(i -> vNets.add((VirtualNetwork) ModelFacade.getInstance().getNetworkById(i)));

    final AbstractAlgorithm algo = new VneIlpPathAlgorithm(sNet, vNets);
    // final VnePmMdvneAlgorithm algo = VnePmMdvneAlgorithm.prepare(sNet, Set.of(vNet));
    // final AbstractAlgorithm algo = new VneIlpPathAlgorithmBatch(sNet, Set.of(vNet));
    algo.execute();

    GlobalMetricsManager.stopRuntime();

    /*
     * Evaluation.
     */

    // Save model to file
    ModelFacade.getInstance().persistModel();
    System.out.println("=> Execution finished.");

    // Time measurements
    System.out.println("=> Elapsed time (total): "
        + GlobalMetricsManager.getRuntime().getValue() / 1_000_000_000 + " seconds");
    System.out.println("=> Elapsed time (PM): "
        + GlobalMetricsManager.getRuntime().getPmValue() / 1_000_000_000 + " seconds");
    System.out.println("=> Elapsed time (ILP): "
        + GlobalMetricsManager.getRuntime().getIlpValue() / 1_000_000_000 + " seconds");
    System.out.println("=> Elapsed time (rest): "
        + GlobalMetricsManager.getRuntime().getRestValue() / 1_000_000_000 + " seconds");

    // final SubstrateNetwork sNet =
    // (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNetIds.get(0));
    final AcceptedVnrMetric acceptedVnrs = new AcceptedVnrMetric(sNet);
    System.out.println("=> Accepted VNRs: " + (int) acceptedVnrs.getValue());
    final TotalPathCostMetric totalPathCost = new TotalPathCostMetric(sNet);
    System.out.println("=> Total path cost: " + totalPathCost.getValue());
    final AveragePathLengthMetric averagePathLength = new AveragePathLengthMetric(sNet);
    System.out.println("=> Average path length: " + averagePathLength.getValue());
  }

}
