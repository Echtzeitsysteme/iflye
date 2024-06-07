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
 * minimizing the total communication cost metric A.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneFakeIlpBatchAlgorithmTotalCommunicationCostATest
		extends VneFakeIlpAlgorithmTotalCommunicationCostATest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_A;
		AlgorithmConfig.emb = Embedding.MANUAL;
		algo = VneFakeIlpBatchAlgorithm.prepare(sNet, vNets);
	}

}
