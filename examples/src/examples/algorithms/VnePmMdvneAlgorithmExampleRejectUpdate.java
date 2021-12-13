package examples.algorithms;

import java.util.Set;

import algorithms.AbstractAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithmMigration;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import metrics.embedding.AcceptedVnrMetric;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the VNE pattern matching VNE algorithm implementation
 * that triggers the need for an updated embedding in order to embed all
 * requested virtual networks.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmExampleRejectUpdate {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Setup
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		GlobalMetricsManager.startRuntime();

		// Substrate network = one tier network
		final OneTierConfig subConfig = new OneTierConfig(3, 1, false, 4, 4, 4, 10);
		final OneTierNetworkGenerator subGen = new OneTierNetworkGenerator(subConfig);
		subGen.createNetwork("sub", false);

		/*
		 * First virtual network
		 */
		final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
		virtGen.createNetwork("virt", true);

		SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");
		AbstractAlgorithm algo = VnePmMdvneAlgorithmMigration.prepare(sNet, Set.of(vNet));
		algo.execute();

		/*
		 * Second virtual network
		 */
		virtGen.createNetwork("virt2", true);
		sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt2");
		algo = VnePmMdvneAlgorithmMigration.prepare(sNet, Set.of(vNet));
		algo.execute();

		/*
		 * Third virtual network
		 */
		virtGen.createNetwork("virt3", true);
		sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt3");
		algo = VnePmMdvneAlgorithmMigration.prepare(sNet, Set.of(vNet));
		algo.execute();

		// Remove second virtual network to get a scenario in which two substrate
		// servers are half
		// filled with guest networks.
		ModelFacade.getInstance().removeNetworkFromRoot("virt2");

		/*
		 * Fourth virtual network
		 */
		virtualConfig.setCpuPerServer(3);
		virtualConfig.setMemoryPerServer(3);
		virtualConfig.setStoragePerServer(3);
		virtGen = new OneTierNetworkGenerator(virtualConfig);
		virtGen.createNetwork("virt4", true);
		sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt4");
		algo = VnePmMdvneAlgorithmMigration.prepare(sNet, Set.of(vNet));
		algo.execute();

		GlobalMetricsManager.stopRuntime();

		// Save model to file
		ModelFacade.getInstance().persistModel();
		System.out.println("=> Execution finished.");

		// Time measurements
		System.out.println("=> Elapsed time (total): " + GlobalMetricsManager.getRuntime().getValue() / 1_000_000_000
				+ " seconds");
		System.out.println(
				"=> Elapsed time (PM): " + GlobalMetricsManager.getRuntime().getPmValue() / 1_000_000_000 + " seconds");
		System.out.println("=> Elapsed time (ILP): " + GlobalMetricsManager.getRuntime().getIlpValue() / 1_000_000_000
				+ " seconds");
		System.out.println("=> Elapsed time (rest): " + GlobalMetricsManager.getRuntime().getRestValue() / 1_000_000_000
				+ " seconds");
		System.out.println("=> Accepted VNR: " + (int) new AcceptedVnrMetric(sNet).getValue());

		System.exit(0);
	}

}
