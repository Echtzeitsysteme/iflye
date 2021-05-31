package examples.algorithms;

import java.util.List;
import java.util.Set;
import algorithms.ilp.VneIlpPathAlgorithm;
import examples.scenarios.ModelConverter;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the VNE ILP algorithm implementation that reads a predetermined JSON file.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmExampleDualRead {

  /**
   * Main method to start the example. String array of arguments will be ignored.
   * 
   * @param args Will be ignored.
   */
  public static void main(final String[] args) {
    // Setup
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 2;

    // Read substrate network from file
    final List<String> sNetIds = ModelConverter.jsonToModel("snet.json", false);

    // Read all virtual networks from file
    final List<String> vNetIds = ModelConverter.jsonToModel("vnets.json", true);

    for (final String vNetId : vNetIds) {
      final SubstrateNetwork sNet =
          (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNetIds.get(0));
      final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById(vNetId);

      // Create and execute algorithm
      final VneIlpPathAlgorithm algo = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
      algo.execute();
    }

    // Save model to file
    ModelFacade.getInstance().persistModel();
    System.out.println("=> Execution finished.");
  }

}
