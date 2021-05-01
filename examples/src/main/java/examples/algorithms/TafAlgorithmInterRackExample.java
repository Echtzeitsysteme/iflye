package examples.algorithms;

import algorithms.heuristics.TafAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the TAF algorithm implementation.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class TafAlgorithmInterRackExample {

  public static void main(final String[] args) {
    // Setup
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.IGNORE_BW = true;

    // Substrate network = one tier network
    final OneTierConfig sRackConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 20);
    final TwoTierConfig substrateConfig = new TwoTierConfig();
    substrateConfig.setNumberOfRacks(2);
    substrateConfig.setRack(sRackConfig);

    TwoTierNetworkGenerator.createTwoTierNetwork("sub", substrateConfig, false);

    // Virtual network = one tier network
    final OneTierConfig virtualConfig = new OneTierConfig(4, 1, false, 1, 1, 1, 5);
    OneTierNetworkGenerator.createOneTierNetwork("virt", virtualConfig, true);

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    // Create and execute algorithm
    final TafAlgorithm taf = new TafAlgorithm(sNet, vNet);
    taf.execute();

    // Save model to file
    ModelFacade.getInstance().persistModel();
    System.out.println("=> Execution finished.");
  }

}
