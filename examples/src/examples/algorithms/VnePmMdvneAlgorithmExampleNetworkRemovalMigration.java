package examples.algorithms;

import java.util.Set;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Migration;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import metrics.TotalCommunicationCostMetricA;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the VNE pattern matching VNE algorithm implementation that demonstrates the
 * removal and the migration of a virtual network.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmExampleNetworkRemovalMigration {

  /**
   * Main method to start the example. String array of arguments will be ignored.
   * 
   * @param args Will be ignored.
   */
  public static void main(final String[] args) {
    // Setup
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    AlgorithmConfig.mig = Migration.ALWAYS_FREE;
    AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_A;

    // Substrate network = one tier network
    final OneTierConfig substrateConfig = new OneTierConfig(2, 1, false, 5, 5, 5, 100);
    final OneTierNetworkGenerator subGen = new OneTierNetworkGenerator(substrateConfig);
    subGen.createNetwork("sub", false);

    /*
     * Embed virtual network 1, 2, 3
     */

    for (int i = 1; i <= 3; i++) {
      // Virtual network = one tier network
      final OneTierConfig virtualConfig = new OneTierConfig(3, 1, false, 1, 1, 1, 1);
      final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
      virtGen.createNetwork("virt" + i, true);

      final SubstrateNetwork sNet =
          (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
      final VirtualNetwork vNet =
          (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt" + i);

      // Create and execute algorithm
      final VnePmMdvneAlgorithm algo = VnePmMdvneAlgorithm.prepare(sNet, Set.of(vNet));
      algo.execute();
    }

    /*
     * Remove virtual network 2
     */
    ModelFacade.getInstance().removeNetworkFromRoot("virt2");

    /*
     * Add another small virtual network (4) to trigger a migration of virtual network 3
     */
    final OneTierConfig smallVirtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator smallVirtGen = new OneTierNetworkGenerator(smallVirtConfig);
    smallVirtGen.createNetwork("virt4", true);
    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt4");
    final VnePmMdvneAlgorithm algo = VnePmMdvneAlgorithm.prepare(sNet, Set.of(vNet));
    algo.execute();

    // Save model to file
    ModelFacade.getInstance().persistModel();
    System.out.println("=> Execution finished.");
    final TotalCommunicationCostMetricA comCostA = new TotalCommunicationCostMetricA(
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub"));
    System.out.println("=> Total communication cost A: " + comCostA.getValue());
  }

}
