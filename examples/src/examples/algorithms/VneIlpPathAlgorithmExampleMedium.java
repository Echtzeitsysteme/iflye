package examples.algorithms;

import java.util.Set;
import algorithms.ilp.VneIlpPathAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import metrics.embedding.AcceptedVnrMetric;
import metrics.embedding.AveragePathLengthMetric;
import metrics.embedding.TotalPathCostMetric;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the VNE ILP algorithm implementation.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VneIlpPathAlgorithmExampleMedium {

  /**
   * Main method to start the example. String array of arguments will be ignored.
   * 
   * @param args Will be ignored.
   */
  public static void main(final String[] args) {
    // Setup
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;

    GlobalMetricsManager.startRuntime();

    // Substrate network = two tier network
    final OneTierConfig rackConfig = new OneTierConfig(10, 1, false, 10, 10, 10, 10);
    final TwoTierConfig substrateConfig = new TwoTierConfig();
    substrateConfig.setRack(rackConfig);
    substrateConfig.setCoreBandwidth(100);
    substrateConfig.setNumberOfCoreSwitches(1);
    substrateConfig.setNumberOfRacks(6);
    final TwoTierNetworkGenerator subGen = new TwoTierNetworkGenerator(substrateConfig);
    subGen.createNetwork("sub", false);

    for (int i = 0; i < 10; i++) {
      // Virtual network = one tier network
      final OneTierConfig virtualConfig = new OneTierConfig(6, 1, false, 10, 1, 1, 1);
      final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
      virtGen.createNetwork("virt_" + i, true);

      final SubstrateNetwork sNet =
          (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
      final VirtualNetwork vNet =
          (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt_" + i);

      // Create and execute algorithm
      System.out.println("=> Embedding virtual network #" + i);
      final VneIlpPathAlgorithm algo = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
      algo.execute();
    }

    GlobalMetricsManager.stopRuntime();

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

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final AcceptedVnrMetric acceptedVnrs = new AcceptedVnrMetric(sNet);
    System.out.println("=> Accepted VNRs: " + (int) acceptedVnrs.getValue());
    final TotalPathCostMetric totalPathCost = new TotalPathCostMetric(sNet);
    System.out.println("=> Total path cost: " + totalPathCost.getValue());
    final AveragePathLengthMetric averagePathLength = new AveragePathLengthMetric(sNet);
    System.out.println("=> Average path length: " + averagePathLength.getValue());

    System.exit(0);
  }

}
