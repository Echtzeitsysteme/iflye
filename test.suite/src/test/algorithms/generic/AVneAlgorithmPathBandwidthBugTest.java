package test.algorithms.generic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Abstract test class for a VNE algorithm implementation to trigger the minimum
 * path/link bandwidth bug. This test is based on a scenario created by Marco
 * Volle in his master's thesis.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public abstract class AVneAlgorithmPathBandwidthBugTest extends AAlgorithmTest {

	/**
	 * Substrate network.
	 */
	SubstrateNetwork sNet;

	/**
	 * Virtual network.
	 */
	VirtualNetwork vNet;

	@Override
	public abstract void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets);

	@AfterEach
	public abstract void resetAlgo();

	@BeforeEach
	public void reset() {
		facade.resetAll();
	}

	//
	// Tests
	//

	@Test
	public void testScenario34() {
		// Load the model file
		facade.loadModel("resources/triggerPathResidualBwBug.xmi");
		assertNotNull(facade.getNetworkById("sub"));
		assertFalse(facade.getAllPathsOfNetwork("sub").isEmpty());

		assertThrows(InternalError.class, () -> {
			facade.validateModel();
		});

		facade.updateAllPathsResidualBandwidth("sub");

		// validation must not fail before the embedding
		facade.validateModel();

		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("v4");

		// Sanity check
		assertNotNull(sNet);
		assertNotNull(vNet);
		assertNull(vNet.getHost());

		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		// validation must not fail
		facade.validateModel();
	}

}
