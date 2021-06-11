package examples.algorithms;

import java.util.Set;
import algorithms.AbstractAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import metrics.AcceptedVnrMetric;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the VNE pattern matching VNE algorithm implementation that triggers the need
 * for an updated embedding in order to embed all requested virtual networks.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmExampleRejectUpdate {

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

    // Substrate network = one tier network
    final OneTierConfig subConfig = new OneTierConfig(3, 1, false, 4, 4, 4, 10);
    final OneTierNetworkGenerator subGen = new OneTierNetworkGenerator(subConfig);
    subGen.createNetwork("sub", false);

    // Virtual network = one tier network
    final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
    virtGen.createNetwork("virt", true);
    virtGen.createNetwork("virt2", true);

    SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");
    AbstractAlgorithm algo = VnePmMdvneAlgorithm.prepare(sNet, Set.of(vNet));
    algo.execute();

    sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt2");
    algo = VnePmMdvneAlgorithm.prepare(sNet, Set.of(vNet));
    algo.execute();

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
    System.out.println("=> Accepted VNR: " + (int) new AcceptedVnrMetric(sNet).getValue());

    System.exit(0);
  }

}
