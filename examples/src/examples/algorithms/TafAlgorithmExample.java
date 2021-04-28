package examples.algorithms;

import algorithms.heuristics.TafAlgorithm;
import config.OneTierConfig;
import facade.ModelFacade;
import generator.OneTierNetworkGenerator;
import model.SubstrateNetwork;
import model.VirtualNetwork;

public class TafAlgorithmExample {

	public static void main(String[] args) {
		final OneTierConfig substrateConfig = new OneTierConfig(2, 1, false, 10, 10, 10, 20);
		OneTierNetworkGenerator.createOneTierNetwork("sub", substrateConfig, false);
		
		final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 5);
		OneTierNetworkGenerator.createOneTierNetwork("virt", virtualConfig, true);
		
		SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");
		
		TafAlgorithm taf = TafAlgorithm.init(vNet, sNet);
		taf.execute();
		
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");
	}

}
