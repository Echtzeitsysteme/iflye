package examples.algorithms;

import algorithms.heuristics.TafAlgorithm;
import config.OneTierConfig;
import facade.ModelFacade;
import generator.OneTierNetworkGenerator;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the TAF algorithm implementation.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class TafAlgorithmExample {

	public static void main(final String[] args) {
		// Substrate network = one tier network
		final OneTierConfig substrateConfig = new OneTierConfig(2, 1, false, 10, 10, 10, 20);
		OneTierNetworkGenerator.createOneTierNetwork("sub", substrateConfig, false);
		
		// Virtual network = one tier network
		final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 5);
		OneTierNetworkGenerator.createOneTierNetwork("virt", virtualConfig, true);
		
		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");
		
		// Initialize and execute algorithm
		final TafAlgorithm taf = TafAlgorithm.init(vNet, sNet);
		taf.execute();
		
		// Save model to file
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");
	}

}
