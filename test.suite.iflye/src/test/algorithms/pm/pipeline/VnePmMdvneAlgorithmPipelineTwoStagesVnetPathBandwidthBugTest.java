package test.algorithms.pm.pipeline;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;

import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesVnet;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AVneAlgorithmPathBandwidthBugTest;

/**
 * Test class to trigger the minimum path/link bandwidth bug.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesVnetPathBandwidthBugTest extends AVneAlgorithmPathBandwidthBugTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		algo = new VnePmMdvneAlgorithmPipelineTwoStagesVnet();
		algo.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		if (algo != null) {
			((VnePmMdvneAlgorithmPipelineTwoStagesVnet) algo).dispose();
		}
	}

}
