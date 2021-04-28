package examples.algorithms;

import algorithms.simple.SimpleVne;
import config.OneTierConfig;
import facade.ModelFacade;
import generator.OneTierNetworkGenerator;

public class SimpleVneExample {
	
	public static void main(String[] args) {
		final OneTierConfig substrateConfig = new OneTierConfig(2, 1, false, 10, 10, 10, 20);
		OneTierNetworkGenerator.createOneTierNetwork("sub", substrateConfig, false);
		
		final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 5);
		OneTierNetworkGenerator.createOneTierNetwork("virt", virtualConfig, true);
		
		SimpleVne.execute("sub", "virt");
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");
	}

}
