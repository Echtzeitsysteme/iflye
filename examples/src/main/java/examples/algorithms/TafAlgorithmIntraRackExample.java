package examples.algorithms;

import algorithms.heuristics.TafAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the TAF algorithm implementation.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TafAlgorithmIntraRackExample {

  public static void main(final String[] args) {
    // Setup
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.IGNORE_BW = true;

    // Substrate network = one tier network
    final OneTierConfig substrateConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 20);
    final OneTierNetworkGenerator subGen = new OneTierNetworkGenerator(substrateConfig);
    subGen.createNetwork("sub", false);

    // Virtual network = one tier network
    final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 5);
    final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
    virtGen.createNetwork("virt", true);

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
