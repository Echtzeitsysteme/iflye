package examples.generators;

import facade.ModelFacade;
import generators.TwoTierNetworkGenerator;
import generators.config.TwoTierConfig;

/**
 * Runnable example for the two tier network generator. Creates one substrate network within the
 * model.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TwoTierGeneratorExample {

  /**
   * Main method to start the example. String array of arguments will be ignored.
   * 
   * @param args Will be ignored.
   */
  public static void main(final String[] args) {
    // Two tier network generation
    final TwoTierConfig config = new TwoTierConfig();
    config.setCoreBandwidth(20);
    config.getRack().setBandwidthPerLink(10);
    config.getRack().setCpuPerServer(1);
    config.getRack().setMemoryPerServer(1);
    config.getRack().setStoragePerServer(1);
    config.setNumberOfRacks(4);
    final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(config);
    gen.createNetwork("sub", false);

    // Save model to file
    ModelFacade.getInstance().persistModel();
    System.out.println("=> Execution finished.");
  }

}
