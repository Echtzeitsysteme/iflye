package test.algorithms.fakeilp;

import java.util.Set;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Embedding;
import algorithms.AlgorithmConfig.Objective;
import algorithms.ilp.VneFakeIlpBatchAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Test class for the VNE fake ILP algorithm (batch version) implementation for
 * minimizing the total communication cost metric B.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneFakeIlpBatchAlgorithmTotalCommunicationCostBTest
		extends VneFakeIlpAlgorithmTotalCommunicationCostBTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_B;
		AlgorithmConfig.emb = Embedding.MANUAL;
		algo = new VneFakeIlpBatchAlgorithm();
		algo.prepare(sNet, vNets);
	}

}
