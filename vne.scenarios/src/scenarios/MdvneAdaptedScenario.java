package scenarios;

import java.util.Set;
import algorithms.AbstractAlgorithm;
import algorithms.heuristics.TafAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Adapted evaluation scenario of the dissertation [1]. For further description, please check
 * {@link MdvneAdaptedScenario}.
 * 
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
 * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class MdvneAdaptedScenario extends AMdvneAdaptedScenario implements IScenario {

  /**
   * Number of substrate racks.
   */
  private static int racks = 8;

  /**
   * Number of virtual network requests (VNRs).
   */
  private static int numberOfVnrs = 40;

  /**
   * Entry point method for this scenario.
   * 
   * @param args Runtime arguments that will be ignored.
   */
  public static void main(final String[] args) {
    // TODO: Remove bandwidth ignoring after changing the VNE algorithm instance below.
    ModelFacadeConfig.IGNORE_BW = true;
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;

    final MdvneAdaptedScenario scen = new MdvneAdaptedScenario();

    scen.substrateSetup("sub");
    final SubstrateNetwork sub = (SubstrateNetwork) facade.getNetworkById("sub");

    for (int i = 0; i < numberOfVnrs; i++) {
      final String virtualNetworkId = "v" + i;
      scen.virtualSetup(virtualNetworkId);
      final VirtualNetwork virt = (VirtualNetwork) facade.getNetworkById(virtualNetworkId);
      // TODO: Change the algorithm instance later on.
      final AbstractAlgorithm algo = new TafAlgorithm(sub, Set.of(virt));
      final boolean success = algo.execute();

      if (success) {
        System.out.println("Embedded VNR: " + virtualNetworkId);
      } else {
        System.err.println("Rejected VNR: " + virtualNetworkId);
      }
    }

    // Save model to file
    ModelFacade.getInstance().persistModel();
    System.out.println("=> Scenario finished.");
  }

  /**
   * Sets the substrate network up (according to the configuration).
   * 
   * @param substrateNetworkId The ID for the substrate network to build.
   */
  private void substrateSetup(final String substrateNetworkId) {
    final OneTierConfig substrateRackConfig = new OneTierConfig(serversPerRack, 1, false,
        substrateCpu, substrateMem, substrateSto, substrateBwSrv);
    final TwoTierConfig substrateConfig = new TwoTierConfig();
    substrateConfig.setRack(substrateRackConfig);
    substrateConfig.setNumberOfCoreSwitches(2);
    substrateConfig.setNumberOfRacks(racks);
    substrateConfig.setCoreBandwidth(substrateBwCore);
    final TwoTierNetworkGenerator subGen = new TwoTierNetworkGenerator(substrateConfig);
    subGen.createNetwork(substrateNetworkId, false);
  }

}
