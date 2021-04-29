package examples.algorithms;

import algorithms.simple.SimpleVne;
import config.OneTierConfig;
import facade.ModelFacade;
import generator.OneTierNetworkGenerator;

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
		
		// Execute algorithm
		SimpleVne.execute("sub", "virt");
		
		// Save model to file
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");
	}

}
