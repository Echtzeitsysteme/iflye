package test.algorithms.gips.lookahead;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsLookaheadAlgorithm;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.gips.VneGipsAlgorithmSimpleTest;

/**
 * Test class for the VNE GIPS lookahead algorithm implementation for simple
 * checks and debugging.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsLookaheadAlgorithmSimpleTest extends VneGipsAlgorithmSimpleTest {

	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets, final String vNetId) {
		// The algorithm is only able to use the total communication objective C because
		// it is hard-coded in GIPSL
		AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
		ModelFacadeConfig.IGNORE_BW = true;
		algo = new VneGipsLookaheadAlgorithm();
		algo.prepare(sNet, vNets);
		((VneGipsLookaheadAlgorithm) algo).setVNetId(vNetId);
	}

	@Override
	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		((VneGipsLookaheadAlgorithm) algo).dispose();
	}

	//
	// Tests
	//

	// No virtual elements

	@Override
	@Test
	public void testNopEmbedding() {
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		checkAndValidate("virt");
	}

	// Only one substrate element

	@Override
	@Test
	public void testOneSrv2Srv() {
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		checkAndValidate("virt");
	}

	@Override
	@Test
	public void testOneSw2Sw() {
		facade.addSwitchToNetwork("ssw1", "sub", 0);
		facade.addSwitchToNetwork("vsw1", "virt", 0);
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		checkAndValidate("virt");
	}

	@Override
	@Test
	public void testOneSw2Srv() {
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		facade.addSwitchToNetwork("vsw1", "virt", 0);
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		checkAndValidate("virt");
	}

	@Override
	@Test
	public void testSwLSw2Srv() {
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		facade.addSwitchToNetwork("vsw1", "virt", 0);
		facade.addSwitchToNetwork("vsw2", "virt", 0);
		facade.addLinkToNetwork("vl1", "virt", 1, "vsw1", "vsw2");
		checkAndValidate("virt");
	}

	@Override
	@Test
	public void testSwLSrv2Srv() {
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		facade.addSwitchToNetwork("vsw1", "virt", 0);
		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);
		facade.addLinkToNetwork("vl1", "virt", 1, "vsw1", "vsrv1");
		checkAndValidate("virt");
	}

	@Override
	@Test
	public void testSrvLSrv2Srv() {
		facade.addServerToNetwork("ssrv1", "sub", 2, 2, 2, 1);
		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);
		facade.addServerToNetwork("vsrv2", "virt", 1, 1, 1, 1);
		facade.addLinkToNetwork("vl1", "virt", 1, "vsrv1", "vsrv2");
		checkAndValidate("virt");
	}

	// Multiple substrate elements

	@Override
	@Test
	public void testOneSrv2MultipleSrvs() {
		facade.addServerToNetwork("ssrv1", "sub", 1, 1, 1, 1);
		facade.addServerToNetwork("ssrv2", "sub", 1, 1, 1, 1);
		facade.addServerToNetwork("vsrv1", "virt", 1, 1, 1, 1);
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		checkAndValidate("virt");
	}

	@Override
	@Test
	public void testOneSw2MultipleSws() {
		facade.addSwitchToNetwork("ssw1", "sub", 0);
		facade.addSwitchToNetwork("ssw2", "sub", 0);
		facade.addSwitchToNetwork("vsw1", "virt", 0);
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");
		checkAndValidate("virt");
	}

	//
	// Utility methods
	//

	private void checkAndValidate(final String vNetId) {
		sNet = (SubstrateNetwork) facade.getNetworkById("sub");
		vNet = (VirtualNetwork) facade.getNetworkById("virt");

		initAlgo(sNet, Set.of(vNet), vNetId);
		assertTrue(algo.execute());

		// Get new network objects because the model was reloaded from file
		sNet = (SubstrateNetwork) facade.getNetworkById(sNet.getName());
		vNet = (VirtualNetwork) facade.getNetworkById(vNet.getName());
		assertEquals(sNet, vNet.getHost());
		assertTrue(sNet.getGuests().contains(vNet));
		facade.validateModel();
	}

}
