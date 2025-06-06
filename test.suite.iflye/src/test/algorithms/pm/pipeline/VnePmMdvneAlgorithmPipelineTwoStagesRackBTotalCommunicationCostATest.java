package test.algorithms.pm.pipeline;

import java.util.Set;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackB;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmTotalCommunicationCostATest;

/**
 * Test class for the VNE PM MdVNE algorithm implementation for minimizing the
 * total communication cost metric A including the pipeline functionality.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesRackBTotalCommunicationCostATest
		extends VnePmMdvneAlgorithmTotalCommunicationCostATest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_A;
		algo = new VnePmMdvneAlgorithmPipelineTwoStagesRackB();
		algo.prepare(sNet, vNets);
	}

}
