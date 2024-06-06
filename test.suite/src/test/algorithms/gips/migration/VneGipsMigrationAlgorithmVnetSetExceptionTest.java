package test.algorithms.gips.migration;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsMigrationAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the VNE GIPS migration algorithm implementation to check its
 * rejection of an invalid model state.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsMigrationAlgorithmVnetSetExceptionTest extends AAlgorithmTest {

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
		// it is hard-coded in RSLANG
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		algo = VneGipsMigrationAlgorithm.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		if (algo != null) {
			((VneGipsMigrationAlgorithm) algo).dispose();
		}
	}

	//
	// Tests
	//

	// No virtual elements

	@Test
	public void testTwoVirtualNetworksEmbedding() {
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");

		// Second virtual network should trigger an exception
		facade.addNetworkToRoot("vnet2", true);

		checkAndValidate();
	}

	//
	// Utility methods
	//

	private void checkAndValidate() {
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");

		assertThrows(IllegalStateException.class, () -> {
			initAlgo(sNet, Set.of(vNet));
		});
	}

}
