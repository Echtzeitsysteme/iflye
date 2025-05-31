package examples.algorithms;

import java.util.Set;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithm;
import examples.AbstractIflyeExample;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.FatTreeNetworkGenerator;
import generators.OneTierNetworkGenerator;
import generators.config.FatTreeConfig;
import generators.config.OneTierConfig;
import metrics.embedding.AcceptedVnrMetric;
import metrics.embedding.AveragePathLengthMetric;
import metrics.embedding.TotalCommunicationCostMetricA;
import metrics.embedding.TotalCommunicationCostMetricC;
import metrics.embedding.TotalCommunicationCostObjectiveC;
import metrics.embedding.TotalPathCostMetric;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the VNE pattern matching VNE algorithm implementation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmExampleFatTree extends AbstractIflyeExample {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Setup
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;
		ModelFacadeConfig.MAX_PATH_LENGTH_AUTO = false;

		// Algorithm objective
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_A;

		GlobalMetricsManager.startRuntime();

		// Substrate network = fat-tree network
		final OneTierConfig rackConfig = new OneTierConfig(1, 1, false, 1, 1, 1, 10);
		final FatTreeConfig substrateConfig = new FatTreeConfig(4);
		substrateConfig.setRack(rackConfig);
		substrateConfig.setBwAggrToEdge(10);
		substrateConfig.setBwCoreToAggr(40);
		final FatTreeNetworkGenerator subGen = new FatTreeNetworkGenerator(substrateConfig);
		subGen.createNetwork("sub", false);

		for (int i = 0; i < 1; i++) {
			// Virtual network = one tier network
			final OneTierConfig virtualConfig = new OneTierConfig(5, 1, false, 1, 1, 1, 1);
			final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
			virtGen.createNetwork("virt_" + i, true);

			final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
			final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt_" + i);

			// Create and execute algorithm
			logger.info("=> Embedding virtual network #" + i);
			final VnePmMdvneAlgorithm algo = new VnePmMdvneAlgorithm();
			algo.prepare(sNet, Set.of(vNet));
			algo.execute();
			// algo.dispose();
		}

		GlobalMetricsManager.stopRuntime();

		// Save model to file
		ModelFacade.getInstance().persistModel();
		logger.info("=> Execution finished.");

		// Time measurements
		logger.info("=> Elapsed time (total): " + GlobalMetricsManager.getRuntime().getValue() / 1_000_000_000
				+ " seconds");
		logger.info(
				"=> Elapsed time (PM): " + GlobalMetricsManager.getRuntime().getPmValue() / 1_000_000_000 + " seconds");
		logger.info("=> Elapsed time (ILP): " + GlobalMetricsManager.getRuntime().getIlpValue() / 1_000_000_000
				+ " seconds");
		logger.info("=> Elapsed time (rest): " + GlobalMetricsManager.getRuntime().getRestValue() / 1_000_000_000
				+ " seconds");

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final AcceptedVnrMetric acceptedVnrs = new AcceptedVnrMetric(sNet);
		logger.info("=> Accepted VNRs: " + (int) acceptedVnrs.getValue());
		final TotalPathCostMetric totalPathCost = new TotalPathCostMetric(sNet);
		logger.info("=> Total path cost: " + totalPathCost.getValue());
		final AveragePathLengthMetric averagePathLength = new AveragePathLengthMetric(sNet);
		logger.info("=> Average path length: " + averagePathLength.getValue());
		final TotalCommunicationCostMetricA tcca = new TotalCommunicationCostMetricA(sNet);
		logger.info("=> Total Communication Cost A: " + tcca.getValue());
		final TotalCommunicationCostMetricC tccc = new TotalCommunicationCostMetricC(sNet);
		logger.info("=> Total Communication Metric C: " + tccc.getValue());
		final TotalCommunicationCostObjectiveC tcoc = new TotalCommunicationCostObjectiveC(sNet);
		logger.info("=> Total Communication Objective C: " + tcoc.getValue());

		System.exit(0);
	}

}
