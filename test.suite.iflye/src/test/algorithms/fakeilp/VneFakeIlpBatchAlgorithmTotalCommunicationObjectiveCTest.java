package test.algorithms.fakeilp;

import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Embedding;
import algorithms.AlgorithmConfig.Objective;
import algorithms.ilp.VneFakeIlpBatchAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Test class for the VNE fake ILP algorithm (batch version) implementation for
 * minimizing the total communication cost objective C.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneFakeIlpBatchAlgorithmTotalCommunicationObjectiveCTest
		extends VneFakeIlpAlgorithmTotalCommunicationObjectiveCTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		AlgorithmConfig.emb = Embedding.MANUAL;
		algo = VneFakeIlpBatchAlgorithm.prepare(sNet, vNets);
	}

	/**
	 * Tests if the algorithm prefers using already filled up substrate servers. The
	 * batch based ILP algorithm always removes all previously embedded virtual
	 * networks from the substrate network. Thus, the metric
	 * TotcalCommunicationCostC does *not* trigger an embedding on one substrate
	 * server only, because the residual resources are equal for every substrate
	 * server. (They are always 0 for this algorithm, because all previous
	 * embeddings were removed.)
	 */
	@Override
	@Disabled
	@Test
	public void testPreferenceOfFilledServers() {
		// Disabled test from parent class.
	}

}
