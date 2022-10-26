package test.algorithms.gips.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Test class for the VNE GIPS algorithm implementation for neccessary
 * migration.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsMigrationAlgorithmMigIsNecessaryTest extends AAlgorithmTest {

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
		algo = VneGipsMigrationAlgorithm.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		((VneGipsMigrationAlgorithm) algo).dispose();
	}

	//
	// Tests
	//

	@Test
	public void testOneSrvWithMigration() {
		facade.addServerToNetwork("ssrv1", "sub", 2, 2, 2, 2);
		facade.addServerToNetwork("ssrv2", "sub", 1, 1, 1, 1);
		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);

		facade.embedNetworkToNetwork("sub", "virt");
		facade.embedServerToServer("ssrv1", "vsrv1");

		facade.addNetworkToRoot("virt2", true);
		facade.addServerToNetwork("vsrv2", "virt2", 2, 2, 2, 2);

		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt2");
		checkAndValidate();
	}

	@Test
	public void testTwoSrvsWithMigration() {
		facade.addServerToNetwork("ssrv1", "sub", 2, 2, 2, 2);
		facade.addServerToNetwork("ssrv2", "sub", 2, 2, 2, 2);
		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);
		facade.addServerToNetwork("vsrv2", "virt", 1, 1, 1, 1);

		facade.embedNetworkToNetwork("sub", "virt");
		facade.embedServerToServer("ssrv1", "vsrv1");
		facade.embedServerToServer("ssrv2", "vsrv2");

		facade.addNetworkToRoot("virt2", true);
		facade.addServerToNetwork("vsrv2", "virt2", 2, 2, 2, 2);

		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt2");
		checkAndValidate();
	}

	@Test
	public void testMultipleSrvsWithMigration() {
		facade.addServerToNetwork("ssrv1", "sub", 10, 10, 10, 10);
		facade.addServerToNetwork("ssrv2", "sub", 1, 1, 1, 1);
		facade.addServerToNetwork("ssrv3", "sub", 1, 1, 1, 1);
		facade.addServerToNetwork("ssrv4", "sub", 1, 1, 1, 1);

		facade.embedNetworkToNetwork("sub", "virt");

		for (int i = 1; i <= 4; i++) {
			facade.addServerToNetwork("vsrv" + i, "virt", 1, 1, 1, 1);
			facade.embedServerToServer("ssrv1", "vsrv" + i);
		}

		facade.addNetworkToRoot("virt2", true);
		facade.addServerToNetwork("vsrv2", "virt2", 8, 8, 8, 8);

		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt2");
		checkAndValidate();
	}

	//
	// Utility methods
	//

	private void checkAndValidate() {
//		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
//		vNet = (VirtualNetwork) facade.getNetworkById("virt");

		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		// Get new network objects because the model was reloaded from file
		sNet = (SubstrateNetwork) facade.getNetworkById(sNet.getName());
		vNet = (VirtualNetwork) facade.getNetworkById(vNet.getName());
		assertEquals(sNet, vNet.getHost());
		assertTrue(sNet.getGuests().contains(vNet));
		facade.validateModel();
	}

}
