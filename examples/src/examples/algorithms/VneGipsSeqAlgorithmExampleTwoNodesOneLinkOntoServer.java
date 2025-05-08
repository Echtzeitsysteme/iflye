package examples.algorithms;

import java.util.Set;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsSeqAlgorithm;
import facade.ModelFacade;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the VNE GIPS algorithm implementation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsSeqAlgorithmExampleTwoNodesOneLinkOntoServer {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Setup
//		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
//		ModelFacadeConfig.MAX_PATH_LENGTH = 2;
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;

//		GlobalMetricsManager.startRuntime();

		// Substrate network = two tier network
		ModelFacade.getInstance().addNetworkToRoot("sub", false);
		ModelFacade.getInstance().addServerToNetwork("s1", "sub", 10, 10, 10, 0);

		ModelFacade.getInstance().addServerToNetwork("s2", "sub", 10, 10, 10, 0);

		// Virtual network = one tier network
		ModelFacade.getInstance().addNetworkToRoot("virt", true);
		ModelFacade.getInstance().addServerToNetwork("vsrv1", "virt", 1, 1, 1, 0);
		ModelFacade.getInstance().addSwitchToNetwork("vsw1", "virt", 0);
		ModelFacade.getInstance().addLinkToNetwork("vl1", "virt", 1, "vsrv1", "vsw1");
		ModelFacade.getInstance().addLinkToNetwork("vl2", "virt", 1, "vsw1", "vsrv1");

		ModelFacade.getInstance().validateModel();

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		// Create and execute algorithm
		final AbstractAlgorithm algo = new VneGipsSeqAlgorithm();
		algo.prepare(sNet, Set.of(vNet));
		algo.execute();

//		GlobalMetricsManager.stopRuntime();

		ModelFacade.getInstance().validateModel();

		// Save model to file
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");

		// Time measurements
//		System.out.println("=> Elapsed time (total): " + GlobalMetricsManager.getRuntime().getValue() / 1_000_000_000
//				+ " seconds");
		System.exit(0);
	}

}
