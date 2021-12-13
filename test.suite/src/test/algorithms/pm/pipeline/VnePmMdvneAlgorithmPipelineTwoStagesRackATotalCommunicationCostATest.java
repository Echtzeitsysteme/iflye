package test.algorithms.pm.pipeline;

import java.util.Set;

import org.junit.jupiter.api.Disabled;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackA;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmTotalCommunicationCostATest;

/**
 * Test class for the VNE PM MdVNE algorithm implementation for minimizing the
 * total communication cost metric A including the pipeline functionality.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesRackATotalCommunicationCostATest
		extends VnePmMdvneAlgorithmTotalCommunicationCostATest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_A;
		algo = VnePmMdvneAlgorithmPipelineTwoStagesRackA.prepare(sNet, vNets);
	}

	@Override
	@Disabled
	public void testAllOnOneServer() {
		// This test is disabled, because the pipeline stage rack A can not embed a
		// virtual network onto
		// a substrate server only (desired behavior).
	}

}
