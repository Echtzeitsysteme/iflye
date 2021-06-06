package scenarios.load;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import algorithms.AbstractAlgorithm;
import algorithms.ilp.VneIlpPathAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import facade.ModelFacade;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import model.converter.BasicModelConverter;
import model.converter.IncrementalModelConverter;

/**
 * Runnable (batch) scenario for VNE algorithms that reads specified files from resource folder.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class DissScenarioLoadBatch extends DissScenarioLoad {

  /**
   * Main method to start the example. String array of arguments will be parsed.
   * 
   * @param args See {@link #parseArgs(String[])}.
   */
  public static void main(final String[] args) {
    parseArgs(args);

    // Substrate network = read from file
    final List<String> sNetIds = IncrementalModelConverter.jsonToModel(subNetPath, false);

    if (sNetIds.size() != 1) {
      throw new UnsupportedOperationException("There is more than one substrate network.");
    }

    sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById(sNetIds.get(0));
    GlobalMetricsManager.startRuntime();

    /*
     * Every embedding starts here.
     */

    final List<String> vNetIds = BasicModelConverter.jsonToModel(virtNetsPath, true);
    final Set<VirtualNetwork> vNets = new HashSet<VirtualNetwork>();
    vNetIds.forEach(i -> vNets.add((VirtualNetwork) ModelFacade.getInstance().getNetworkById(i)));

    // Create and execute algorithm
    final AbstractAlgorithm algo;
    switch (algoConfig) {
      case "pm":
        algo = VnePmMdvneAlgorithm.prepare(sNet, vNets);
        break;
      case "ilp":
        algo = new VneIlpPathAlgorithm(sNet, vNets);
        break;
      default:
        throw new IllegalArgumentException("Configured algorithm not known.");
    }
    algo.execute();

    /*
     * End of every embedding.
     */

    GlobalMetricsManager.stopRuntime();

    /*
     * Evaluation.
     */

    // Save model to file
    ModelFacade.getInstance().persistModel();
    System.out.println("=> Execution finished.");
    printMetrics();

    System.exit(0);
  }

}
