package test.algorithms.gips.bwignore;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import algorithms.gips.VneGipsBwIgnoreAlgorithm;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the VNE GIPS algorithm implementation that ignores the
 * bandwidth constraints for simple checks and debugging.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsBwIgnoreAlgorithmConfigTest extends AAlgorithmTest {

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
		algo = VneGipsBwIgnoreAlgorithm.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		if (algo != null) {
			((VneGipsBwIgnoreAlgorithm) algo).dispose();
		}
	}

	//
	// Tests
	//

	@Test
	public void testBwIgnoreException() {
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");

		ModelFacadeConfig.IGNORE_BW = false;

		initAlgo(sNet, Set.of(vNet));

		assertThrows(UnsupportedOperationException.class, () -> {
			algo.execute();
		});
	}

	@Test
	public void testBwIgnoreNoException() {
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");

		ModelFacadeConfig.IGNORE_BW = true;

		initAlgo(sNet, Set.of(vNet));

		assertDoesNotThrow(() -> {
			algo.execute();
		});
	}

}
