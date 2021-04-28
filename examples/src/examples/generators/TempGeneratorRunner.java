package examples.generators;

import config.OneTierConfig;
import config.TwoTierConfig;
import facade.ModelFacade;
import generator.TwoTierNetworkGenerator;

public class TempGeneratorRunner {
	public static void main(final String[] args) {
		// One tier network test
//		final OneTierConfig config = new OneTierConfig(2, 1, false, 10, 10, 10, 20);
//		OneTierNetworkGenerator.createOneTierNetwork("test", config, false);
//		OneTierNetworkGenerator.createOneTierNetwork("test2", config, true);
//		
//		ModelFacade.getInstance().persistModel();
//		ModelFacade.getInstance().resetAll();
//		ModelFacade.getInstance().loadModel();
//		ModelFacade.getInstance().dummy();
		
		// Two tier network test
		final TwoTierConfig config = new TwoTierConfig();
		config.setCoreBandwidth(20);
		config.getRack().setBandwidthPerLink(10);
		config.getRack().setCpuPerServer(1);
		config.getRack().setMemoryPerServer(1);
		config.getRack().setStoragePerServer(1);
		config.setNumberOfRacks(4);
		TwoTierNetworkGenerator.createTwoTierNetwork("test", config, false);
		ModelFacade.getInstance().persistModel();
		
		System.out.println("=> Execution finished.");
	}
	
}
