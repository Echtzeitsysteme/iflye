package test.algorithms.fakeilp;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Embedding;
import algorithms.AlgorithmConfig.Objective;
import algorithms.ilp.VneFakeIlpAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmMultipleVnsTest;

/**
 * Test class for the VNE fake ILP algorithm (incremental version)
 * implementation for minimizing the total path cost metric.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneFakeIlpAlgorithmTotalPathCostTest extends AAlgorithmMultipleVnsTest {

	@AfterEach
	public void resetAlgo() {
		if (algo != null) {
			((VneFakeIlpAlgorithm) algo).dispose();
		}
	}

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
		AlgorithmConfig.emb = Embedding.MANUAL;
		algo = VneFakeIlpAlgorithm.prepare(sNet, vNets);
	}

}
