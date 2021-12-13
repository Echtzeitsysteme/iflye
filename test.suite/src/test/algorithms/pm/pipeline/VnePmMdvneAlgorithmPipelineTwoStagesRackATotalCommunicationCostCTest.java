package test.algorithms.pm.pipeline;

import java.util.Set;

import org.junit.jupiter.api.Disabled;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackA;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmTotalCommunicationCostCTest;

/**
 * Test class for the VNE PM MdVNE algorithm implementation for minimizing the
 * total communication cost metric C including the pipeline functionality.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesRackATotalCommunicationCostCTest
		extends VnePmMdvneAlgorithmTotalCommunicationCostCTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_C;
		algo = VnePmMdvneAlgorithmPipelineTwoStagesRackA.prepare(sNet, vNets);
	}

	@Override
	@Disabled
	public void testAllOnOneServer() {
		// This test is disabled, because the pipeline stage rack A can not embed a
		// virtual network onto
		// a substrate server only (desired behavior).
	}

	@Override
	public void testPreferenceOfFilledServers() {
		// From the parent test case: Test expects that all virtual networks are placed
		// on the same
		// substrate server
		// This test is disabled, because the pipeline stage rack A can not embed a
		// virtual network onto
		// a substrate server only (desired behavior).
	}

}
