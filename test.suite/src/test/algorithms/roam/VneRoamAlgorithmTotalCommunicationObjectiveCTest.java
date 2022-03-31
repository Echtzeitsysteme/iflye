package test.algorithms.roam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.roam.VneRoamAlgorithm;
import model.SubstrateElement;
import model.SubstrateNetwork;
import model.SubstratePath;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;
import test.algorithms.generic.AAlgorithmMultipleVnsTest;

/**
 * Test class for the VNE PM MdVNE algorithm implementation for minimizing the
 * total communication cost objective C.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneRoamAlgorithmTotalCommunicationObjectiveCTest extends AAlgorithmMultipleVnsTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		algo = VneRoamAlgorithm.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		if (algo != null) {
			((VneRoamAlgorithm) algo).dispose();
		}
	}

	/**
	 * TODO: Currently, the VNE Roam algorithm can only embed all virtual networks
	 * from the model. Because of this limitation, this test case must be disabled.
	 */
	@Override
	@Disabled
	@Test
	public void testMultipleVnsExistButEmbedOnlyOne() {
	}

	/**
	 * This test has to be overwritten, because of the fact that this
	 * metric/objective drives the algorithm to place the switch not necessarily on
	 * the core switch.
	 */
	@Override
	@Test
	public void testAllOnMultipleRacks() {
		oneTierSetupThreeServers("virt", 1);
		twoTierSetupFourServers("sub", 1);

		facade.createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		// Test switch placement
		final VirtualSwitch virtSw = (VirtualSwitch) facade.getSwitchById("virt_sw");
		assertNotNull(virtSw.getHost());
		final String refSwHostName = virtSw.getHost().getName();
		assertEquals(1, virtSw.getHost().getDepth());

		// Test server placements
		final VirtualServer vSrv1 = (VirtualServer) facade.getServerById("virt_srv1");
		final VirtualServer vSrv2 = (VirtualServer) facade.getServerById("virt_srv2");
		final VirtualServer vSrv3 = (VirtualServer) facade.getServerById("virt_srv3");
		assertNotNull(vSrv1.getHost());
		assertNotNull(vSrv2.getHost());
		assertNotNull(vSrv3.getHost());
		final String serverHost1 = vSrv1.getHost().getName();
		final String serverHost2 = vSrv2.getHost().getName();
		final String serverHost3 = vSrv3.getHost().getName();

		assertNotEquals(serverHost1, serverHost2);
		assertNotEquals(serverHost1, serverHost3);
		assertNotEquals(serverHost2, serverHost3);

		// Test link placements
		final VirtualLink vLn1 = (VirtualLink) facade.getLinkById("virt_ln1");
		final VirtualLink vLn2 = (VirtualLink) facade.getLinkById("virt_ln2");
		final VirtualLink vLn3 = (VirtualLink) facade.getLinkById("virt_ln3");
		final VirtualLink vLn4 = (VirtualLink) facade.getLinkById("virt_ln4");
		final VirtualLink vLn5 = (VirtualLink) facade.getLinkById("virt_ln5");
		final VirtualLink vLn6 = (VirtualLink) facade.getLinkById("virt_ln6");

		// Link 1
		assertNotNull(vLn1.getHost());
		final SubstratePath pLn1 = (SubstratePath) vLn1.getHost();
		assertEquals(serverHost1, pLn1.getSource().getName());
		assertEquals(refSwHostName, pLn1.getTarget().getName());

		// Link 2
		assertNotNull(vLn2.getHost());
		final SubstratePath pLn2 = (SubstratePath) vLn2.getHost();
		assertEquals(serverHost2, pLn2.getSource().getName());
		assertEquals(refSwHostName, pLn2.getTarget().getName());

		// Link 3
		assertNotNull(vLn3.getHost());
		final SubstratePath pLn3 = (SubstratePath) vLn3.getHost();
		assertEquals(serverHost3, pLn3.getSource().getName());
		assertEquals(refSwHostName, pLn3.getTarget().getName());

		// Link 4
		assertNotNull(vLn4.getHost());
		final SubstratePath pLn4 = (SubstratePath) vLn4.getHost();
		assertEquals(refSwHostName, pLn4.getSource().getName());
		assertEquals(serverHost1, pLn4.getTarget().getName());

		// Link 5
		assertNotNull(vLn5.getHost());
		final SubstratePath pLn5 = (SubstratePath) vLn5.getHost();
		assertEquals(refSwHostName, pLn5.getSource().getName());
		assertEquals(serverHost2, pLn5.getTarget().getName());

		// Link 6
		assertNotNull(vLn6.getHost());
		final SubstratePath pLn6 = (SubstratePath) vLn6.getHost();
		assertEquals(refSwHostName, pLn6.getSource().getName());
		assertEquals(serverHost3, pLn6.getTarget().getName());
	}

	/**
	 * Tests if the algorithm prefers using already filled up substrate servers.
	 */
	// TODO: Activate if VNE Roam algorithm is able to embed in multiple steps.
	@Disabled
	@Test
	public void testPreferenceOfFilledServers() {
		// Setup
		oneTierSetupThreeServers("sub", 4);
		oneTierSetupTwoServers("virt", 1);
		facade.createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) facade.getNetworkById("virt");

		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		// Actual test starts here
		facade.addNetworkToRoot("virt2", true);
		oneTierSetupTwoServers("virt2", 1);
		final VirtualNetwork vNet2 = (VirtualNetwork) facade.getNetworkById("virt2");

		initAlgo(sNet, Set.of(vNet2));
		assertTrue(algo.execute());

		// Test expects that all virtual networks are placed on the same substrate
		// server
		final SubstrateElement ref = ((VirtualServer) vNet.getNodes().get(1)).getHost();

		vNet.getNodes().forEach(n -> {
			if (n instanceof VirtualServer) {
				final VirtualServer vsrv = (VirtualServer) n;
				assertEquals(ref, vsrv.getHost());
			}
		});

		vNet2.getNodes().forEach(n -> {
			if (n instanceof VirtualServer) {
				final VirtualServer vsrv = (VirtualServer) n;
				assertEquals(ref, vsrv.getHost());
			}
		});
	}

}
