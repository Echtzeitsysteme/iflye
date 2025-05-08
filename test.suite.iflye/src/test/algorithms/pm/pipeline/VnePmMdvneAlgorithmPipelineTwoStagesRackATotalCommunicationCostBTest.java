package test.algorithms.pm.pipeline;

import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackA;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmTotalCommunicationCostBTest;

/**
 * Test class for the VNE PM MdVNE algorithm implementation for minimizing the
 * total communication cost metric B including the pipeline functionality.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesRackATotalCommunicationCostBTest
		extends VnePmMdvneAlgorithmTotalCommunicationCostBTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_B;
		algo = new VnePmMdvneAlgorithmPipelineTwoStagesRackA();
		algo.prepare(sNet, vNets);
	}

	@Override
	@Disabled
	@Test
	public void testAllOnOneServer() {
		// This test is disabled, because the pipeline stage rack A can not embed a
		// virtual network onto
		// a substrate server only (desired behavior).
	}

}
