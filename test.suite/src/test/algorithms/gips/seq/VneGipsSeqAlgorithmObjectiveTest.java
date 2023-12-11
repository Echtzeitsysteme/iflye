package test.algorithms.gips.seq;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsSeqAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.gips.VneGipsAlgorithmObjectiveTest;

/**
 * Test class for the VNE GIPS sequence algorithm implementation for minimizing
 * the total communication cost objective C.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsSeqAlgorithmObjectiveTest extends VneGipsAlgorithmObjectiveTest {

	/**
	 * Substrate network.
	 */
	SubstrateNetwork sNet;

	/**
	 * Virtual network.
	 */
	VirtualNetwork vNet;

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		// The algorithm is only able to use the total communication objective C because
		// it is hard-coded in GIPSL
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		algo = VneGipsSeqAlgorithm.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		if (algo != null) {
			((VneGipsSeqAlgorithm) algo).dispose();
		}
	}

	//
	// Tests
	//

	@Disabled
	@Test
	public void testAllOnOneRackLarge() {
	}

	@Disabled
	@Test
	public void testAllOnOneServerLarge() {
	}

	@Disabled
	@Test
	public void testAllOnMultipleRacksLarge() {
	}

}
