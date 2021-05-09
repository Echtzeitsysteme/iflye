package examples.generators;

import facade.ModelFacade;
import generators.GoogleFatTreeNetworkGenerator;
import generators.config.GoogleFatTreeConfig;

/**
 * Runnable example for the Google fat tree network generator. Creates one substrate network within
 * the model.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class GoogleFatTreeGeneratorExample {

  public static void main(final String[] args) {
    // Google fat tree network generation
    final GoogleFatTreeConfig config = new GoogleFatTreeConfig(4);
    config.setBwCoreToAggr(100);
    config.setBwAggrToEdge(40);
    final GoogleFatTreeNetworkGenerator gen = new GoogleFatTreeNetworkGenerator(config);
    gen.createNetwork("sub", false);

    // Save model to file
    ModelFacade.getInstance().persistModel();
    System.out.println("=> Execution finished.");
  }

}
