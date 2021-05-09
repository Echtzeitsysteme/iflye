package examples.generators;

import facade.ModelFacade;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;

/**
 * Runnable example for the one tier network generator. Creates one substrate and one virtual one
 * tier network within the model.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class OneTierGeneratorExample {

  public static void main(final String[] args) {
    // One tier network generation
    final OneTierConfig config = new OneTierConfig(2, 1, false, 10, 10, 10, 20);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
    gen.createNetwork("sub", false);
    gen.createNetwork("virt", true);

    // Save model to file
    ModelFacade.getInstance().persistModel();
    System.out.println("=> Execution finished.");
  }

}
