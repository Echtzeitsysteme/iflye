package test.algorithms.pm.pipeline;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;

import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackA;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AVneAlgorithmPathBandwidthBugTest;

/**
 * Test class to trigger the minimum path/link bandwidth bug.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
//@Disabled
public class VnePmMdvneAlgorithmPipelineTwoStagesRackAPathBandwidthBugTest extends AVneAlgorithmPathBandwidthBugTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		algo = VnePmMdvneAlgorithmPipelineTwoStagesRackA.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		if (algo != null) {
			((VnePmMdvneAlgorithmPipelineTwoStagesRackA) algo).dispose();
		}
	}

}
