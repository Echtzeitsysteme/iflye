package test.algorithms.pm.pipeline;

import java.util.Set;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmPipelineThreeStagesB;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmTotalPathCostTest;

/**
 * Test class for the VNE pattern matching algorithm implementation for
 * minimizing the total path cost metric including the pipeline functionality.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineThreeStagesBTotalPathCostTest extends VnePmMdvneAlgorithmTotalPathCostTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
		algo = new VnePmMdvneAlgorithmPipelineThreeStagesB();
		algo.prepare(sNet, vNets);
	}

}
