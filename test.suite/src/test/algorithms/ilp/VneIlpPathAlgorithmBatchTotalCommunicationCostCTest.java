package test.algorithms.ilp;

import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.ilp.VneIlpPathAlgorithmBatch;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Test class for the VNE ILP algorithm (batch version) implementation for
 * minimizing the total communication cost metric C.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VneIlpPathAlgorithmBatchTotalCommunicationCostCTest
		extends VneIlpPathAlgorithmTotalCommunicationCostCTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_C;
		algo = new VneIlpPathAlgorithmBatch(sNet, vNets);
	}

	/**
	 * Tests if the algorithm prefers using already filled up substrate servers. The
	 * batch based ILP algoritm always removes all previously embedded virtual
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
