package test.algorithms.pm.pipeline;

import java.util.Set;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmPipelineThreeStagesB;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmTotalCommunicationCostBTest;

/**
 * Test class for the VNE PM MdVNE algorithm implementation for minimizing the
 * total communication cost metric B including the pipeline functionality.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineThreeStagesBTotalCommunicationCostBTest
		extends VnePmMdvneAlgorithmTotalCommunicationCostBTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_B;
		algo = VnePmMdvneAlgorithmPipelineThreeStagesB.prepare(sNet, vNets);
	}

}
