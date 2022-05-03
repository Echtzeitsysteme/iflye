package test.algorithms.gips;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.gips.VneGipsAlgorithm;
import generators.OneTierNetworkGenerator;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import metrics.embedding.TotalCommunicationCostMetricC;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the VNE GIPS algorithm implementation for minimizing the total
 * communication cost objective C.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsAlgorithmObjectiveTest extends AAlgorithmTest {

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
		algo = VneGipsAlgorithm.prepare(sNet, vNets);
	}

	@AfterEach
	public void resetAlgo() {
		facade.resetAll();
		((VneGipsAlgorithm) algo).dispose();
	}

	//
	// Tests
	//

	@Test
	public void testAllOnOneServerSmall() {
		sNet = setUpSubNet(2, 2);
		vNet = setUpVirtNet(2);

		embedAndCheckCostValue(12 - 3);
	}

	@Test
	public void testAllOnOneRackSmall() {
		sNet = setUpSubNet(2, 2);
		vNet = setUpVirtNet(4);

		// Costs: Servers: 3 + 3, links: 4 * (1 + 1)
		embedAndCheckCostValue(14);
	}

	@Test
	public void testAllOnMultipleRacksSmall() {
		sNet = setUpSubNet(2, 2);
		vNet = setUpVirtNet(8);

		// Costs: Servers: 0, links: 8 * (2 + 2)
		embedAndCheckCostValue(32);
	}

	@Test
	public void testAllOnOneServerLarge() {
		sNet = setUpSubNet(10, 4);
		vNet = setUpVirtNet(2);

		embedAndCheckCostValue(120 - 3);
	}

	// TODO: Flaky?
	@Test
	public void testAllOnOneRackLarge() {
		sNet = setUpSubNet(10, 4);
		vNet = setUpVirtNet(4);

		// Costs: 2 servers fully utilized, 4 bidirectional virtual links embedded
		embedAndCheckCostValue(120 - (3 * 2) + 4 * 2);
	}

	@Test
	public void testAllOnMultipleRacksLarge() {
		sNet = setUpSubNet(4, 4);
		vNet = setUpVirtNet(16);

		// Costs: 8 servers fully utilized, 16 bidirectional virtual links over 2 hops
		// embedded
		embedAndCheckCostValue(16 * 3 - (3 * 8) + 2 * 16 * 2);
	}

	//
	// Utility methods
	//

	public void embedAndCheckCostValue(final int expected) {
		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		// Get new network objects because the model was reloaded from file
		sNet = (SubstrateNetwork) facade.getNetworkById(sNet.getName());
		vNet = (VirtualNetwork) facade.getNetworkById(vNet.getName());
		checkAllElementsEmbeddedOnSubstrateNetwork(sNet, Set.of(vNet));

		checkCost(expected, sNet);
		facade.validateModel();
	}

	public SubstrateNetwork setUpSubNet(final int numberOfServersPerRack, final int numberOfRacks) {
		final TwoTierConfig config = new TwoTierConfig();
		config.setCoreBandwidth(10);
		config.setCoreSwitchesConnected(false);
		config.setNumberOfCoreSwitches(1);
		config.setNumberOfRacks(numberOfRacks);
		final OneTierConfig rack = new OneTierConfig(numberOfServersPerRack, 1, false, 2, 2, 2, 10);
		config.setRack(rack);
		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(config);
		gen.createNetwork("sub", false);
		facade.createAllPathsForNetwork("sub");
		return (SubstrateNetwork) facade.getNetworkById("sub");
	}

	public VirtualNetwork setUpVirtNet(final int numberOfServers) {
		final OneTierConfig config = new OneTierConfig(numberOfServers, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(config);
		gen.createNetwork("virt", true);
		return (VirtualNetwork) facade.getNetworkById("virt");
	}

	public void checkCost(final int expected, final SubstrateNetwork sNet) {
		final TotalCommunicationCostMetricC metric = new TotalCommunicationCostMetricC(sNet);
		assertEquals(expected, metric.getValue());
	}

}
