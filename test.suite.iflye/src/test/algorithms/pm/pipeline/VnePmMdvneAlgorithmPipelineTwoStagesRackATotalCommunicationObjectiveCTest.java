package test.algorithms.pm.pipeline;

import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackA;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmTotalCommunicationObjectiveCTest;

/**
 * Test class for the VNE PM MdVNE algorithm implementation for minimizing the
 * total communication cost objective C including the pipeline functionality.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesRackATotalCommunicationObjectiveCTest
		extends VnePmMdvneAlgorithmTotalCommunicationObjectiveCTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
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

	@Override
	@Disabled
	@Test
	public void testPreferenceOfFilledServers() {
		// From the parent test case: Test expects that all virtual networks are placed
		// on the same
		// substrate server
		// This test is disabled, because the pipeline stage rack A can not embed a
		// virtual network onto
		// a substrate server only (desired behavior).
	}

}
