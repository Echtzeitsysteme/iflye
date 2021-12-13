package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.TwoTierNetworkGenerator;
import generators.config.OneTierConfig;
import generators.config.TwoTierConfig;
import model.SubstrateNetwork;

/**
 * Test class for the ModelFacade that tests two tier path related creations
 * with large scale scenarios. This tests do only check the number of created
 * paths.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadePathLargeScaleTest {

	/*
	 * Variables to save the ModelFacade's configuration of path limits to.
	 */

	/**
	 * Old lower limit value.
	 */
	private int oldLowerLimit;

	/**
	 * Old upper limit value.
	 */
	private int oldUpperLimit;

	@BeforeEach
	public void resetModel() {
		ModelFacade.getInstance().resetAll();

		// Save old values
		oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
		oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;

		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;
	}

	@AfterEach
	public void restoreConfig() {
		ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
		ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
	}

	/**
	 * This is the scenario I sketched in my notebook.
	 */
	@Test
	public void testTwoTier2Racks2Servers() {
		ModelFacadeConfig.MIN_PATH_LENGTH = 2;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		final OneTierConfig rackConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		final TwoTierConfig subConfig = new TwoTierConfig(rackConfig, 1, 2, false, 1);

		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(subConfig);
		gen.createNetwork("sub", false);
		final SubstrateNetwork sub = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");

		assertEquals(calcNumberOfPaths(4, 2, 1) - 8, sub.getPaths().size());
	}

	@Test
	public void testTwoTier4Racks2Servers() {
		final OneTierConfig rackConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		final TwoTierConfig subConfig = new TwoTierConfig(rackConfig, 1, 4, false, 1);

		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(subConfig);
		gen.createNetwork("sub", false);
		final SubstrateNetwork sub = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");

		assertEquals(calcNumberOfPaths(8, 4, 1), sub.getPaths().size());
	}

	@Test
	public void testTwoTier2Racks4Servers() {
		final OneTierConfig rackConfig = new OneTierConfig(4, 1, false, 1, 1, 1, 1);
		final TwoTierConfig subConfig = new TwoTierConfig(rackConfig, 1, 2, false, 1);

		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(subConfig);
		gen.createNetwork("sub", false);
		final SubstrateNetwork sub = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");

		assertEquals(calcNumberOfPaths(8, 2, 1), sub.getPaths().size());
	}

	@Test
	public void testTwoTier10Racks10Servers() {
		final OneTierConfig rackConfig = new OneTierConfig(10, 1, false, 1, 1, 1, 1);
		final TwoTierConfig subConfig = new TwoTierConfig(rackConfig, 1, 10, false, 1);

		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(subConfig);
		gen.createNetwork("sub", false);
		final SubstrateNetwork sub = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");

		assertEquals(calcNumberOfPaths(100, 10, 1), sub.getPaths().size());
	}

	@Test
	public void testTwoTier1Rack20Servers() {
		final OneTierConfig rackConfig = new OneTierConfig(20, 1, false, 1, 1, 1, 1);
		final TwoTierConfig subConfig = new TwoTierConfig(rackConfig, 1, 1, false, 1);

		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(subConfig);
		gen.createNetwork("sub", false);
		final SubstrateNetwork sub = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");

		assertEquals(calcNumberOfPaths(20, 1, 1), sub.getPaths().size());
	}

	@Test
	public void testTwoTier20Racks1Server() {
		final OneTierConfig rackConfig = new OneTierConfig(1, 1, false, 1, 1, 1, 1);
		final TwoTierConfig subConfig = new TwoTierConfig(rackConfig, 1, 20, false, 1);

		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(subConfig);
		gen.createNetwork("sub", false);
		final SubstrateNetwork sub = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");

		assertEquals(calcNumberOfPaths(20, 20, 1), sub.getPaths().size());
	}

	@Test
	public void testTwoTierMultipleCoreSwitchesSmall() {
		final OneTierConfig rackConfig = new OneTierConfig(5, 1, false, 1, 1, 1, 1);
		final TwoTierConfig subConfig = new TwoTierConfig(rackConfig, 3, 2, false, 1);

		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(subConfig);
		gen.createNetwork("sub", false);
		final SubstrateNetwork sub = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");

		assertEquals(calcNumberOfPaths(10, 2, 3), sub.getPaths().size());
	}

	@Test
	public void testTwoTierMultipleCoreSwitchesLarge() {
		final OneTierConfig rackConfig = new OneTierConfig(10, 1, false, 1, 1, 1, 1);
		final TwoTierConfig subConfig = new TwoTierConfig(rackConfig, 10, 10, false, 1);

		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(subConfig);
		gen.createNetwork("sub", false);
		final SubstrateNetwork sub = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");

		assertEquals(calcNumberOfPaths(100, 10, 10), sub.getPaths().size());
	}

	@Test
	public void testTwoTierScenario() {
		final OneTierConfig rackConfig = new OneTierConfig(10, 1, false, 1, 1, 1, 1);
		final TwoTierConfig subConfig = new TwoTierConfig(rackConfig, 1, 6, false, 1);

		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(subConfig);
		gen.createNetwork("sub", false);
		final SubstrateNetwork sub = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");

		assertEquals(calcNumberOfPaths(60, 6, 1), sub.getPaths().size());
	}

	@Test
	public void testTwoTierScenarioMinPathLength2() {
		ModelFacadeConfig.MIN_PATH_LENGTH = 2;
		final OneTierConfig rackConfig = new OneTierConfig(10, 1, false, 1, 1, 1, 1);
		final TwoTierConfig subConfig = new TwoTierConfig(rackConfig, 1, 6, false, 1);

		final TwoTierNetworkGenerator gen = new TwoTierNetworkGenerator(subConfig);
		gen.createNetwork("sub", false);
		final SubstrateNetwork sub = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");

		assertEquals(calcNumberOfPaths(60, 6, 1) - (60 * 2), sub.getPaths().size());
	}

	/**
	 * Returns the number of paths for a two tier network with given parameters.
	 * This calculation is only correct, if the minimum (maximum) path length is set
	 * to 1 (4).
	 *
	 * @param numberOfServers      Number of servers (total).
	 * @param numberOfRackSwitches Number of rack switches (total).
	 * @param numberOfCoreSwitches Number of core switches (total).
	 * @return Number of paths within the network.
	 */
	private int calcNumberOfPaths(final int numberOfServers, final int numberOfRackSwitches,
			final int numberOfCoreSwitches) {
		int val = 0;

		val += ((numberOfServers - 1) * numberOfServers);
		val += ((numberOfServers * numberOfCoreSwitches) * 2);
		val += ((numberOfServers * numberOfRackSwitches) * 2);

		return val;
	}

}
