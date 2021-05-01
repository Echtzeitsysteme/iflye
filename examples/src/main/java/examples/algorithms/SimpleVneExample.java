package examples.algorithms;

import algorithms.simple.SimpleVne;
import facade.ModelFacade;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the simple VNE algorithm.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class SimpleVneExample {

  public static void main(final String[] args) {
    // Substrate network = one tier network
    final OneTierConfig substrateConfig = new OneTierConfig(2, 1, false, 10, 10, 10, 20);
    OneTierNetworkGenerator.createOneTierNetwork("sub", substrateConfig, false);

    // Virtual network = one tier network
    final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 5);
    OneTierNetworkGenerator.createOneTierNetwork("virt", virtualConfig, true);

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    // Create and execute algorithm
    final SimpleVne svne = new SimpleVne(sNet, vNet);
    svne.execute();

    // Save model to file
    ModelFacade.getInstance().persistModel();
    System.out.println("=> Execution finished.");
  }

}
