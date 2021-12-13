package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.FatTreeNetworkGenerator;
import generators.config.FatTreeConfig;
import model.SubstratePath;

/**
 * Test class for the ModelFacade that tests all Yen path related creations for
 * Fat Tree networks. This test class only tests the number of created paths
 * against a reference function.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadePathYenFatTreeTest {
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

	/**
	 * Old K parameter.
	 */
	private int oldK;

	/**
	 * Old Yen flag.
	 */
	private boolean oldYen;

	@BeforeEach
	public void resetModel() {
		ModelFacade.getInstance().resetAll();

		// Save old values
		oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
		oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
		oldK = ModelFacadeConfig.YEN_K;
		oldYen = ModelFacadeConfig.YEN_PATH_GEN;

		// Setup itself
		ModelFacadeConfig.YEN_PATH_GEN = true;
		// ModelFacadeConfig.YEN_K = 2;
	}

	@AfterEach
	public void restoreConfig() {
		ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
		ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
		ModelFacadeConfig.YEN_K = oldK;
		ModelFacadeConfig.YEN_PATH_GEN = oldYen;
	}

	@Test
	public void testFatTreeAggrPlanePathLength1() {
		setExactPathLength(1);
		final List<SubstratePath> allPaths = createPlaneNetworkAndGetPaths();

		assertFalse(allPaths.isEmpty());
		assertEquals(8, allPaths.size());
	}

	@Test
	public void testFatTreeAggrPlanePathLength2() {
		setExactPathLength(2);
		ModelFacadeConfig.YEN_K = 3;
		final List<SubstratePath> allPaths = createPlaneNetworkAndGetPaths();

		assertFalse(allPaths.isEmpty());
		assertEquals(5 * 4, allPaths.size());
	}

	@Test
	public void testFatTreeAggrPlanePathLength3() {
		setExactPathLength(3);
		ModelFacadeConfig.YEN_K = 3;
		final List<SubstratePath> allPaths = createPlaneNetworkAndGetPaths();

		assertFalse(allPaths.isEmpty());
		assertEquals(2 * 4 * 2, allPaths.size());
	}

	@Test
	public void testFatTreeAggrPlanePathLength4() {
		setExactPathLength(4);
		ModelFacadeConfig.YEN_K = 3;
		final List<SubstratePath> allPaths = createPlaneNetworkAndGetPaths();

		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(4 * 6 + 2 * 4, allPaths.size());
	}

	@Test
	public void testK4FatTreePathLength1() {
		ModelFacadeConfig.YEN_K = 1;
		setExactPathLength(1);
		final List<SubstratePath> allPaths = createNetworkAndGetPaths(4);

		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(calcNumberOfPathsRef(4, 1), allPaths.size());
	}

	@Test
	public void testK4FatTreePathLength2() {
		ModelFacadeConfig.YEN_K = 1;
		setExactPathLength(2);
		final List<SubstratePath> allPaths = createNetworkAndGetPaths(4);

		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(calcNumberOfPathsRef(4, 2), allPaths.size());
	}

	@Test
	public void testK4FatTreePathLength3() {
		ModelFacadeConfig.YEN_K = 10;
		setExactPathLength(3);
		final List<SubstratePath> allPaths = createNetworkAndGetPaths(4);

		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(calcNumberOfPathsRef(4, 3), allPaths.size());
	}

	@Test
	public void testK4FatTreePathLength4() {
		ModelFacadeConfig.YEN_K = 3;
		setExactPathLength(4);
		final List<SubstratePath> allPaths = createNetworkAndGetPaths(4);

		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(calcNumberOfPathsRef(4, 4), allPaths.size());
	}

	@Test
	public void testK6FatTreePathLength1() {
		ModelFacadeConfig.YEN_K = 1;
		setExactPathLength(1);
		final List<SubstratePath> allPaths = createNetworkAndGetPaths(6);

		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(calcNumberOfPathsRef(6, 1), allPaths.size());
	}

	@Test
	public void testK6FatTreePathLength2() {
		ModelFacadeConfig.YEN_K = 1;
		setExactPathLength(2);
		final List<SubstratePath> allPaths = createNetworkAndGetPaths(6);

		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(calcNumberOfPathsRef(6, 2), allPaths.size());
	}

	@Disabled("Long runtime")
	@Test
	public void testK6FatTreePathLength3() {
		ModelFacadeConfig.YEN_K = 4;
		setExactPathLength(3);
		final List<SubstratePath> allPaths = createNetworkAndGetPaths(6);

		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(calcNumberOfPathsRef(6, 3), allPaths.size());
	}

	@Disabled("Long runtime")
	@Test
	public void testK6FatTreePathLength4() {
		ModelFacadeConfig.YEN_K = 3;
		setExactPathLength(4);
		final List<SubstratePath> allPaths = createNetworkAndGetPaths(6);

		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(calcNumberOfPathsRef(6, 4), allPaths.size());
	}

	/*
	 * Utility methods
	 */

	/**
	 * Creates a Fat Tree network for given parameter k.
	 *
	 * @param k Parameter.
	 * @return All paths of the created network.
	 */
	private List<SubstratePath> createNetworkAndGetPaths(final int k) {
		final FatTreeConfig subConfig = new FatTreeConfig(k);
		final FatTreeNetworkGenerator gen = new FatTreeNetworkGenerator(subConfig);
		gen.createNetwork("sub", false);

		ModelFacade.getInstance().createAllPathsForNetwork("sub");

		return ModelFacade.getInstance().getAllPathsOfNetwork("sub");
	}

	/**
	 * Creates one plane of a Fat Tree network.
	 *
	 * @return All paths of the plane.
	 */
	private List<SubstratePath> createPlaneNetworkAndGetPaths() {
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addSwitchToNetwork("csw1", "net", 0);
		ModelFacade.getInstance().addSwitchToNetwork("csw2", "net", 0);
		ModelFacade.getInstance().addSwitchToNetwork("rsw1", "net", 1);
		ModelFacade.getInstance().addSwitchToNetwork("rsw2", "net", 1);

		ModelFacade.getInstance().addServerToNetwork("srv1", "net", 0, 0, 0, 2);
		ModelFacade.getInstance().addServerToNetwork("srv2", "net", 0, 0, 0, 2);
		ModelFacade.getInstance().addServerToNetwork("srv3", "net", 0, 0, 0, 2);
		ModelFacade.getInstance().addServerToNetwork("srv4", "net", 0, 0, 0, 2);

		ModelFacade.getInstance().addLinkToNetwork("ln1", "net", 0, "srv1", "rsw1");
		ModelFacade.getInstance().addLinkToNetwork("ln2", "net", 0, "srv2", "rsw1");

		ModelFacade.getInstance().addLinkToNetwork("ln7", "net", 0, "srv3", "rsw2");
		ModelFacade.getInstance().addLinkToNetwork("ln8", "net", 0, "srv4", "rsw2");

		ModelFacade.getInstance().addLinkToNetwork("ln9", "net", 0, "rsw1", "srv1");
		ModelFacade.getInstance().addLinkToNetwork("ln10", "net", 0, "rsw1", "srv2");

		ModelFacade.getInstance().addLinkToNetwork("ln15", "net", 0, "rsw2", "srv3");
		ModelFacade.getInstance().addLinkToNetwork("ln16", "net", 0, "rsw2", "srv4");

		ModelFacade.getInstance().addLinkToNetwork("ln17", "net", 0, "rsw1", "csw1");
		ModelFacade.getInstance().addLinkToNetwork("ln18", "net", 0, "rsw2", "csw1");
		ModelFacade.getInstance().addLinkToNetwork("ln19", "net", 0, "csw1", "rsw1");
		ModelFacade.getInstance().addLinkToNetwork("ln20", "net", 0, "csw1", "rsw2");

		ModelFacade.getInstance().addLinkToNetwork("ln21", "net", 0, "rsw1", "csw2");
		ModelFacade.getInstance().addLinkToNetwork("ln22", "net", 0, "rsw2", "csw2");
		ModelFacade.getInstance().addLinkToNetwork("ln23", "net", 0, "csw2", "rsw1");
		ModelFacade.getInstance().addLinkToNetwork("ln24", "net", 0, "csw2", "rsw2");

		ModelFacade.getInstance().createAllPathsForNetwork("net");

		return ModelFacade.getInstance().getAllPathsOfNetwork("net");
	}

	/**
	 * Sets the exact path length to the given parameter.
	 *
	 * @param length Path length to set.
	 */
	private void setExactPathLength(final int length) {
		ModelFacadeConfig.MIN_PATH_LENGTH = length;
		ModelFacadeConfig.MAX_PATH_LENGTH = length;
	}

	/**
	 * Calculates the number of paths for a given parameter k and the given
	 * parameter of number of hops.
	 *
	 * @param k    Fat Tree parameter.
	 * @param hops Number of hops.
	 * @return Number of possible paths.
	 */
	private int calcNumberOfPathsRef(final int k, final int hops) {
		int counter = 0;

		final int numberOfEdgeSwitchesPerPod = k / 2;
		final int numberOfAggrSwitchesPerPod = k / 2;
		final int numberOfCoreSwitches = (int) Math.pow(k / 2, 2);
		final int numberOfServersPerPod = (int) Math.pow(k / 2, 2);
		final int numberOfServersPerEdgeSwitch = numberOfServersPerPod / numberOfEdgeSwitchesPerPod;

		switch (hops) {
		case 1:
			counter = (numberOfServersPerPod * k) * 2;
			break;
		case 2:
			counter = numberOfServersPerPod * k * (numberOfServersPerEdgeSwitch - 1)
					+ numberOfServersPerPod * k * numberOfAggrSwitchesPerPod * 2;
			break;
		case 3:
			counter = numberOfServersPerPod * k * numberOfAggrSwitchesPerPod * 2
					+ numberOfServersPerPod * k * numberOfCoreSwitches * 2;
			break;
		case 4:
			// counter =
			// numberOfServersPerPod * k * numberOfServersPerEdgeSwitch *
			// numberOfAggrSwitchesPerPod
			// + numberOfServersPerPod * k * numberOfAggrSwitchesPerPod
			// + numberOfServersPerPod * k * (k - 1);
			counter = numberOfServersPerPod * k * (int) Math.pow(k, 2) * 2;
			break;
		default:
			throw new IllegalArgumentException();
		}

		return counter;
	}

}
