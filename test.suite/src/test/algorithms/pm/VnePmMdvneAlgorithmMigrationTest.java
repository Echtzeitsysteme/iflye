package test.algorithms.pm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Migration;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithm;
import facade.ModelFacade;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import metrics.TotalCommunicationCostMetricA;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.VirtualSwitch;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the VNE PM MdVNE algorithm implementation for checking the migration
 * functionality.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmMigrationTest extends AAlgorithmTest {

  /**
   * ModelFacade to work with.
   */
  final ModelFacade facade = ModelFacade.getInstance();

  /**
   * Initializes the algorithm to test.
   * 
   * @param sNet Substrate network.
   * @param vNets Set of virtual networks.
   */
  @Override
  public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    // Total communication cost A is needed for this test
    AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_A;
    algo = VnePmMdvneAlgorithm.prepare(sNet, vNets);
  }

  @AfterEach
  public void resetAlgo() {
    if (algo != null) {
      ((VnePmMdvneAlgorithm) algo).dispose();
    }
  }

  @Test
  public void testNoMigrationNormal() {
    AlgorithmConfig.mig = Migration.NEVER;
    testSetupAndCheckNormal(60, new int[] {1, 1, 10}, new int[] {1, 1, 1}, 5, 0);
  }

  @Test
  public void testAlwaysFreeMigrationNormal() {
    AlgorithmConfig.mig = Migration.ALWAYS_FREE;
    testSetupAndCheckNormal(6, new int[] {1, 1, 10}, new int[] {1, 1, 1}, 5, 1);
  }

  @Test
  public void testNoMigrationRemoval() {
    AlgorithmConfig.mig = Migration.NEVER;
    testSetupAndCheckRemoval(6, 0);
  }

  @Test
  public void testAlwaysFreeMigrationRemoval() {
    AlgorithmConfig.mig = Migration.ALWAYS_FREE;
    testSetupAndCheckRemoval(0, 1);
  }

  @Disabled("Removal of substrate servers is not yet possible")
  @Test
  public void testMappingRemovedMigration() {
    AlgorithmConfig.mig = Migration.MAPPING_REMOVED;
    // TODO: Implement this test after removal of substrate servers is possible.
  }

  /**
   * Low penalty allows optional migration.
   */
  @Test
  public void testAlwaysPenaltyLow() {
    AlgorithmConfig.mig = Migration.ALWAYS_PENALTY;
    testSetupAndCheckNormal(6, new int[] {1, 1, 10}, new int[] {1, 1, 1}, 5, 1);
  }

  /**
   * High penalty prevents optional migration.
   */
  @Test
  public void testAlwaysPenaltyHigh() {
    AlgorithmConfig.mig = Migration.ALWAYS_PENALTY;
    testSetupAndCheckNormal(60, new int[] {1, 1, 10}, new int[] {15, 15, 2}, 50, 0);
  }

  /*
   * Utility methods
   */

  /**
   * Sets an test environment with three virtual networks up and checks the placement of the last
   * virtual switch. If migration is enabled, the last network would better be placed on a virtual
   * server alone (including the migration of one of the previous networks), because the last
   * network does have a much higher total communication cost A metric.
   * 
   * @param totalComCostARef Reference for the total communication cost A metric after all
   *        embeddings.
   * @param vNetBws Bandwidths of the virtual networks.
   * @param slotsVirtualServer Number of CPU, memory, storage per virtual server for each network to
   *        generate.
   * @param slotsPerSubstrateServer Number of CPU, memory, storage per substrate server.
   * @param vsw3DepthRef The depth of the host of the virtual switch of the last virtual network.
   */
  private void testSetupAndCheckNormal(final int totalComCostARef, final int[] vNetBws,
      final int[] slotsVirtualServer, final int slotsPerSubstrateServer, final int vsw3DepthRef) {
    // Substrate network = one tier network
    final OneTierConfig substrateConfig = new OneTierConfig(2, 1, false, slotsPerSubstrateServer,
        slotsPerSubstrateServer, slotsPerSubstrateServer, 100);
    final OneTierNetworkGenerator subGen = new OneTierNetworkGenerator(substrateConfig);
    subGen.createNetwork("sub", false);

    /*
     * Embed virtual network 1, 2, 3
     */
    for (int i = 1; i <= 3; i++) {
      // Virtual network = one tier network
      final OneTierConfig virtualConfig = new OneTierConfig(3, 1, false, slotsVirtualServer[i - 1],
          slotsVirtualServer[i - 1], slotsVirtualServer[i - 1], vNetBws[i - 1]);
      final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
      virtGen.createNetwork("virt" + i, true);

      final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
      final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt" + i);

      // Create and execute algorithm
      initAlgo(sNet, Set.of(vNet));
      assertTrue(algo.execute());
    }

    // Test switch placement
    final VirtualSwitch virtSw3 = (VirtualSwitch) facade.getSwitchById("virt3_sw_0");
    assertEquals(vsw3DepthRef, virtSw3.getHost().getDepth());

    // Test total communication cost A metric
    final TotalCommunicationCostMetricA comCostA =
        new TotalCommunicationCostMetricA((SubstrateNetwork) facade.getNetworkById("sub"));
    assertEquals(totalComCostARef, comCostA.getValue());
  }

  /**
   * Sets an test environment with three virtual networks up, deletes the second on, embeds another
   * small one, and checks the placement of the last virtual switch. If migration is enabled, the
   * last network would better be placed on a virtual server alone.
   * 
   * @param totalComCostARef Reference for the total communication cost A metric after all
   *        embeddings.
   * @param vsw3DepthRef The depth of the host of the virtual switch of the last virtual network.
   */
  private void testSetupAndCheckRemoval(final int totalComCostARef, final int vsw3DepthRef) {
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

      final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
      final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt" + i);

      // Create and execute algorithm
      initAlgo(sNet, Set.of(vNet));
      assertTrue(algo.execute());
    }

    /*
     * Remove virtual network 2
     */
    facade.removeNetworkFromRoot("virt2");

    /*
     * Add another small virtual network (4) to trigger a migration of virtual network 3
     */
    final OneTierConfig smallVirtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator smallVirtGen = new OneTierNetworkGenerator(smallVirtConfig);
    smallVirtGen.createNetwork("virt4", true);
    final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt4");
    initAlgo(sNet, Set.of(vNet));
    assertTrue(algo.execute());

    // Test switch placement
    final VirtualSwitch virtSw3 = (VirtualSwitch) facade.getSwitchById("virt3_sw_0");
    assertEquals(vsw3DepthRef, virtSw3.getHost().getDepth());

    // Test total communication cost A metric
    final TotalCommunicationCostMetricA comCostA =
        new TotalCommunicationCostMetricA((SubstrateNetwork) facade.getNetworkById("sub"));
    assertEquals(totalComCostARef, comCostA.getValue());
  }

}
