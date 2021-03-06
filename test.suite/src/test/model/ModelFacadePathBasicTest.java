package test.model;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.Link;
import model.Node;
import model.SubstrateNode;
import model.SubstratePath;
import model.Switch;

/**
 * Test class for the ModelFacade that tests all basic path related creations.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ModelFacadePathBasicTest {

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
	}

	@AfterEach
	public void restoreConfig() {
		ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
		ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
	}

	@Test
	public void testNoPathsAfterNetworkCreation() {
		oneTierSetupTwoServers();
		assertTrue(ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());
	}

	@Test
	public void testOneTierPathCreationTwoServers() {
		oneTierSetupTwoServers();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(6, allPaths.size());

		// Check individual source and targets
		final Set<Tuple<String, String>> mapping = new HashSet<>();
		mapping.add(new Tuple<>("srv1", "sw"));
		mapping.add(new Tuple<>("sw", "srv1"));
		mapping.add(new Tuple<>("srv2", "sw"));
		mapping.add(new Tuple<>("sw", "srv2"));
		mapping.add(new Tuple<>("srv1", "srv2"));
		mapping.add(new Tuple<>("srv2", "srv1"));

		checkPathSourcesAndTargets(mapping, allPaths);
	}

	@Test
	public void testOneTierPathCreationFourServers() {
		oneTierSetupFourServers();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(20, allPaths.size());

		// Check individual source and targets
		final Set<Tuple<String, String>> mapping = new HashSet<>();
		mapping.add(new Tuple<>("srv1", "sw"));
		mapping.add(new Tuple<>("sw", "srv1"));
		mapping.add(new Tuple<>("srv2", "sw"));
		mapping.add(new Tuple<>("sw", "srv2"));
		mapping.add(new Tuple<>("srv3", "sw"));
		mapping.add(new Tuple<>("sw", "srv3"));
		mapping.add(new Tuple<>("srv4", "sw"));
		mapping.add(new Tuple<>("sw", "srv4"));
		mapping.add(new Tuple<>("srv1", "srv2"));
		mapping.add(new Tuple<>("srv2", "srv1"));
		mapping.add(new Tuple<>("srv1", "srv3"));
		mapping.add(new Tuple<>("srv3", "srv1"));
		mapping.add(new Tuple<>("srv1", "srv4"));
		mapping.add(new Tuple<>("srv4", "srv1"));
		mapping.add(new Tuple<>("srv2", "srv3"));
		mapping.add(new Tuple<>("srv3", "srv2"));
		mapping.add(new Tuple<>("srv2", "srv4"));
		mapping.add(new Tuple<>("srv4", "srv2"));
		mapping.add(new Tuple<>("srv3", "srv4"));
		mapping.add(new Tuple<>("srv4", "srv3"));

		checkPathSourcesAndTargets(mapping, allPaths);
	}

	@Test
	public void testTwoTierPathCreationFourServers() {
		twoTierSetupFourServers();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(36, allPaths.size());

		// Check individual source and targets
		final Set<Tuple<String, String>> mapping = new HashSet<>();
		mapping.add(new Tuple<>("srv1", "rsw1"));
		mapping.add(new Tuple<>("srv1", "csw1"));
		mapping.add(new Tuple<>("srv1", "rsw2"));
		mapping.add(new Tuple<>("srv1", "srv2"));
		mapping.add(new Tuple<>("srv1", "srv3"));
		mapping.add(new Tuple<>("srv1", "srv4"));

		mapping.add(new Tuple<>("srv2", "srv1"));
		mapping.add(new Tuple<>("srv2", "srv3"));
		mapping.add(new Tuple<>("srv2", "srv4"));
		mapping.add(new Tuple<>("srv2", "rsw1"));
		mapping.add(new Tuple<>("srv2", "rsw2"));
		mapping.add(new Tuple<>("srv2", "csw1"));

		mapping.add(new Tuple<>("srv3", "srv1"));
		mapping.add(new Tuple<>("srv3", "srv2"));
		mapping.add(new Tuple<>("srv3", "srv4"));
		mapping.add(new Tuple<>("srv3", "rsw1"));
		mapping.add(new Tuple<>("srv3", "rsw2"));
		mapping.add(new Tuple<>("srv3", "csw1"));

		mapping.add(new Tuple<>("srv4", "srv1"));
		mapping.add(new Tuple<>("srv4", "srv2"));
		mapping.add(new Tuple<>("srv4", "srv3"));
		mapping.add(new Tuple<>("srv4", "rsw1"));
		mapping.add(new Tuple<>("srv4", "rsw2"));
		mapping.add(new Tuple<>("srv4", "csw1"));

		mapping.add(new Tuple<>("rsw1", "srv1"));
		mapping.add(new Tuple<>("rsw1", "srv2"));
		mapping.add(new Tuple<>("rsw1", "srv3"));
		mapping.add(new Tuple<>("rsw1", "srv4"));

		mapping.add(new Tuple<>("rsw2", "srv1"));
		mapping.add(new Tuple<>("rsw2", "srv2"));
		mapping.add(new Tuple<>("rsw2", "srv3"));
		mapping.add(new Tuple<>("rsw2", "srv4"));

		mapping.add(new Tuple<>("csw1", "srv1"));
		mapping.add(new Tuple<>("csw1", "srv2"));
		mapping.add(new Tuple<>("csw1", "srv3"));
		mapping.add(new Tuple<>("csw1", "srv4"));

		checkPathSourcesAndTargets(mapping, allPaths);
	}

	@Test
	public void testTwoTierPathCreationFourServersTwoCoreSwitches() {
		twoTierSetupFourServersTwoCoreSwitches();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(allPaths.isEmpty());

		// Check total number of paths
		assertEquals(44, allPaths.size());

		// Check individual source and targets
		final Set<Tuple<String, String>> mapping = new HashSet<>();

		mapping.add(new Tuple<>("srv1", "rsw1"));
		mapping.add(new Tuple<>("srv1", "csw1"));
		mapping.add(new Tuple<>("srv1", "csw2"));
		mapping.add(new Tuple<>("srv1", "rsw2"));
		mapping.add(new Tuple<>("srv1", "srv2"));
		mapping.add(new Tuple<>("srv1", "srv3"));
		mapping.add(new Tuple<>("srv1", "srv4"));

		mapping.add(new Tuple<>("srv2", "srv1"));
		mapping.add(new Tuple<>("srv2", "srv3"));
		mapping.add(new Tuple<>("srv2", "srv4"));
		mapping.add(new Tuple<>("srv2", "rsw1"));
		mapping.add(new Tuple<>("srv2", "rsw2"));
		mapping.add(new Tuple<>("srv2", "csw1"));
		mapping.add(new Tuple<>("srv2", "csw2"));

		mapping.add(new Tuple<>("srv3", "srv1"));
		mapping.add(new Tuple<>("srv3", "srv2"));
		mapping.add(new Tuple<>("srv3", "srv4"));
		mapping.add(new Tuple<>("srv3", "rsw1"));
		mapping.add(new Tuple<>("srv3", "rsw2"));
		mapping.add(new Tuple<>("srv3", "csw1"));
		mapping.add(new Tuple<>("srv3", "csw2"));

		mapping.add(new Tuple<>("srv4", "srv1"));
		mapping.add(new Tuple<>("srv4", "srv2"));
		mapping.add(new Tuple<>("srv4", "srv3"));
		mapping.add(new Tuple<>("srv4", "rsw1"));
		mapping.add(new Tuple<>("srv4", "rsw2"));
		mapping.add(new Tuple<>("srv4", "csw1"));
		mapping.add(new Tuple<>("srv4", "csw2"));

		mapping.add(new Tuple<>("rsw1", "srv1"));
		mapping.add(new Tuple<>("rsw1", "srv2"));
		mapping.add(new Tuple<>("rsw1", "srv3"));
		mapping.add(new Tuple<>("rsw1", "srv4"));

		mapping.add(new Tuple<>("rsw2", "srv1"));
		mapping.add(new Tuple<>("rsw2", "srv2"));
		mapping.add(new Tuple<>("rsw2", "srv3"));
		mapping.add(new Tuple<>("rsw2", "srv4"));

		mapping.add(new Tuple<>("csw1", "srv1"));
		mapping.add(new Tuple<>("csw1", "srv2"));
		mapping.add(new Tuple<>("csw1", "srv3"));
		mapping.add(new Tuple<>("csw1", "srv4"));

		mapping.add(new Tuple<>("csw2", "srv1"));
		mapping.add(new Tuple<>("csw2", "srv2"));
		mapping.add(new Tuple<>("csw2", "srv3"));
		mapping.add(new Tuple<>("csw2", "srv4"));

		checkPathSourcesAndTargets(mapping, allPaths);
	}

	@Test
	public void testOneTierNumberOfHopsPerPath() {
		oneTierSetupTwoServers();

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

		for (final SubstratePath p : allPaths) {
			if (p.getSource() instanceof Switch || p.getTarget() instanceof Switch) {
				assertEquals(1, p.getHops());
			} else {
				assertEquals(2, p.getHops());
			}
		}
	}

	@Test
	public void testTwoTierNumberOfHopsPerPath() {
		twoTierSetupFourServers();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

		int counterOneHop = 0;
		int counterTwoHops = 0;
		int counterThreeHops = 0;
		int counterFourHops = 0;

		for (final SubstratePath p : allPaths) {
			// Number of links must be number of hops
			assertEquals(p.getLinks().size(), p.getHops());

			// if source or target is a core switch
			if (p.getHops() == 1) {
				counterOneHop += 1;
			} else if (p.getHops() == 2) {
				counterTwoHops += 1;
			} else if (p.getHops() == 3) {
				counterThreeHops += 1;
			} else if (p.getHops() == 4) {
				counterFourHops += 1;
			}
		}

		assertEquals(16, counterOneHop);
		assertEquals(20, counterTwoHops);
		assertEquals(0, counterThreeHops);
		assertEquals(0, counterFourHops);
	}

	@Test
	public void testOneTierBandwidthAmoutPerPath() {
		oneTierSetupTwoServers();

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

		for (final SubstratePath p : allPaths) {
			if ((p.getSource().getName().equals("srv2") && p.getTarget().getName().equals("sw"))
					|| (p.getSource().getName().equals("sw") && p.getTarget().getName().equals("srv2"))) {
				assertEquals(2, p.getBandwidth());
			} else {
				assertEquals(1, p.getBandwidth());
			}
		}
	}

	@Test
	public void testOneTierContainedLinksAmount() {
		oneTierSetupTwoServers();

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

		for (final SubstratePath p : allPaths) {
			if (p.getSource() instanceof Switch || p.getTarget() instanceof Switch) {
				assertEquals(1, p.getLinks().size());
			} else {
				assertEquals(2, p.getLinks().size());
			}
		}
	}

	@Test
	public void testOneTierContainedLinksNames() {
		oneTierSetupTwoServers();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

		List<Set<String>> linkNames = new LinkedList<>();
		linkNames.add(Set.of("ln1"));
		linkNames.add(Set.of("ln2"));
		linkNames.add(Set.of("ln3"));
		linkNames.add(Set.of("ln4"));
		linkNames.add(Set.of("ln1", "ln4"));
		linkNames.add(Set.of("ln2", "ln3"));

		checkPathLinkNames(linkNames, allPaths);
	}

	@Test
	public void testOneTierContainedNodesAmount() {
		oneTierSetupTwoServers();

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

		for (final SubstratePath p : allPaths) {
			if (p.getSource() instanceof Switch || p.getTarget() instanceof Switch) {
				assertEquals(2, p.getNodes().size());
			} else {
				assertEquals(3, p.getNodes().size());
			}
		}
	}

	@Test
	public void testOneTierContainedNodesNames() {
		oneTierSetupTwoServers();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

		// The reference nodes only have to be added one time
		List<Set<String>> nodeNames = new LinkedList<>();

		for (int i = 0; i <= 1; i++) {
			nodeNames.add(Set.of("srv1", "sw"));
			nodeNames.add(Set.of("srv2", "sw"));
			nodeNames.add(Set.of("srv1", "srv2", "sw"));
		}

		checkPathNodeNames(nodeNames, allPaths);
	}

	@Test
	public void testTwoTierNoNameIsNull() {
		twoTierSetupFourServers();

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

		for (final SubstratePath p : allPaths) {
			assertNotNull(p.getName());
		}
	}

	@Test
	public void testNoPathsLowerLimit() {
		// Setup for this test
		ModelFacadeConfig.MIN_PATH_LENGTH = 10;
		oneTierSetupFourServers();
		ModelFacade.getInstance().createAllPathsForNetwork("net");

		final List<SubstratePath> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertTrue(generatedPaths.isEmpty());
	}

	@Test
	public void testNoPathsUpperLimit() {
		// Setup for this test
		ModelFacadeConfig.MAX_PATH_LENGTH = 0;
		oneTierSetupFourServers();
		ModelFacade.getInstance().createAllPathsForNetwork("net");

		final List<SubstratePath> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertTrue(generatedPaths.isEmpty());
	}

	@Test
	public void testOnlyPathsWithTwoHops() {
		// Setup for this test
		ModelFacadeConfig.MIN_PATH_LENGTH = 2;
		ModelFacadeConfig.MAX_PATH_LENGTH = 2;
		twoTierSetupFourServers();
		ModelFacade.getInstance().createAllPathsForNetwork("net");

		final List<SubstratePath> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(generatedPaths.isEmpty());

		for (final SubstratePath p : generatedPaths) {
			assertEquals(2, p.getHops());
			assertEquals(2, p.getLinks().size());
			assertEquals(3, p.getNodes().size());
		}
	}

	@Test
	public void testOnlyPathsWithThreeHops() {
		// Setup for this test
		ModelFacadeConfig.MIN_PATH_LENGTH = 3;
		ModelFacadeConfig.MAX_PATH_LENGTH = 3;
		twoTierSetupFourServers();
		ModelFacade.getInstance().createAllPathsForNetwork("net");

		final List<SubstratePath> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertTrue(generatedPaths.isEmpty());
	}

	@Test
	public void testResidualBandwidth() {
		oneTierSetupTwoServers();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(allPaths.isEmpty());

		for (final SubstratePath p : allPaths) {
			final SubstratePath sp = p;
			assertEquals(sp.getBandwidth(), sp.getResidualBandwidth());
		}
	}

	@Test
	public void testGetPathFromSourceToTargetNames() {
		oneTierSetupTwoServers();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(allPaths.isEmpty());

		for (final SubstratePath p : allPaths) {
			final SubstratePath sp = p;
			assertEquals(sp, ModelFacade.getInstance().getPathFromSourceToTarget(sp.getSource(), sp.getTarget()));
		}
	}

	@Test
	public void testGetPathFromSourceToTargetNodes() {
		oneTierSetupTwoServers();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 4;

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(allPaths.isEmpty());

		for (final SubstratePath p : allPaths) {
			final SubstratePath sp = p;
			assertEquals(sp, ModelFacade.getInstance().getPathFromSourceToTarget(sp.getSource().getName(),
					sp.getTarget().getName()));
		}
	}

	/*
	 * Negative tests
	 */

	@Test
	public void testRejectVirtualNetwork() {
		ModelFacade.getInstance().addNetworkToRoot("virt", true);
		assertThrows(UnsupportedOperationException.class, () -> {
			ModelFacade.getInstance().createAllPathsForNetwork("virt");
		});
	}

	@Test
	public void testNullGetPathFromSourceToTargetNodes() {
		oneTierSetupTwoServers();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 1;

		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final SubstrateNode source = (SubstrateNode) ModelFacade.getInstance().getServerById("srv1");
		final SubstrateNode target = (SubstrateNode) ModelFacade.getInstance().getServerById("srv2");
		assertNull(ModelFacade.getInstance().getPathFromSourceToTarget(source, target));
	}

	@Test
	public void testNoPathsGetPathFromSourceToTargetNodes() {
		oneTierSetupTwoServers();
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 1;

		final SubstrateNode source = (SubstrateNode) ModelFacade.getInstance().getServerById("srv1");
		final SubstrateNode target = (SubstrateNode) ModelFacade.getInstance().getServerById("srv2");
		assertNull(ModelFacade.getInstance().getPathFromSourceToTarget(source, target));
	}

	@Test
	public void testNoPathOverServer() {
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 2;

		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addServerToNetwork("1", "net", 1, 1, 1, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "net", 1, 1, 1, 0);
		ModelFacade.getInstance().addServerToNetwork("3", "net", 1, 1, 1, 0);
		ModelFacade.getInstance().addLinkToNetwork("l1", "net", 1, "1", "2");
		ModelFacade.getInstance().addLinkToNetwork("l2", "net", 1, "2", "1");
		ModelFacade.getInstance().addLinkToNetwork("l3", "net", 1, "2", "3");
		ModelFacade.getInstance().addLinkToNetwork("l4", "net", 1, "3", "2");

		ModelFacade.getInstance().createAllPathsForNetwork("net");

		final List<SubstratePath> paths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

		assertEquals(4, paths.size());
		paths.forEach(p -> {
			assertEquals(1, p.getHops());
			assertEquals(2, p.getNodes().size());
		});
	}

	@Test
	public void throwExceptionVirtualNetwork() {
		final OneTierConfig conf = new OneTierConfig(1, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(conf);
		gen.createNetwork("virt", true);

		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().getAllPathsOfNetwork("virt");
		});
	}

	/*
	 * Utility methods.
	 */

	/**
	 * Tests a list of a sets of strings against a list of paths. The check ensures,
	 * that all name sets are contained within the list of paths (with links).
	 *
	 * @param linkNames    List of sets of strings with link names for each path.
	 * @param pathsToCheck List of paths to check.
	 */
	static void checkPathLinkNames(final List<Set<String>> linkNames, final List<SubstratePath> pathsToCheck) {
		List<Set<String>> pathLinks = new LinkedList<>();
		for (final SubstratePath p : pathsToCheck) {
			final Set<String> fromPath = new HashSet<>();
			for (Link l : p.getLinks()) {
				fromPath.add(l.getName());
			}
			pathLinks.add(fromPath);
		}

		assertTrue(linkNames.containsAll(pathLinks));
		assertTrue(pathLinks.containsAll(linkNames));
	}

	/**
	 * Tests a list of sets of strings against a list of paths. The check ensures,
	 * that all name sets are contained within the list of paths (with nodes).
	 *
	 * @param nodeNames    List of sets of strings with node names for each path.
	 * @param pathsToCheck List of paths to check.
	 */
	static void checkPathNodeNames(final List<Set<String>> nodeNames, final List<SubstratePath> pathsToCheck) {
		List<Set<String>> pathNodes = new LinkedList<>();
		for (final SubstratePath p : pathsToCheck) {
			final Set<String> fromPath = new HashSet<>();
			for (Node n : p.getNodes()) {
				fromPath.add(n.getName());
			}
			pathNodes.add(fromPath);
		}

		// Ignore order
		assertTrue(nodeNames.containsAll(pathNodes));
		assertTrue(pathNodes.containsAll(nodeNames));
	}

	/**
	 * Checks a given list of paths against a given set of string mappings to
	 * string. The set represents the mapping of sourceID to targetID for all paths.
	 *
	 * @param mapping      SourceID to targetID mapping.
	 * @param pathsToCheck List of paths to check.
	 */
	static void checkPathSourcesAndTargets(final Set<Tuple<String, String>> mapping,
			final List<SubstratePath> pathsToCheck) {
		assertEquals(mapping.size(), pathsToCheck.size());
		final Iterator<Tuple<String, String>> it = mapping.iterator();
		while (it.hasNext()) {
			final Tuple<String, String> next = it.next();
			checkPathSourceAndTarget(next.x, next.y, pathsToCheck);
		}
	}

	/**
	 * Checks a given list of paths for one specific sourceID and targetID. If no
	 * path with the given sourceID and targetID can be found, the check fails.
	 *
	 * @param sourceId     SourceID to search for.
	 * @param targetId     TargetID to search for.
	 * @param pathsToCheck List of paths to search in.
	 */
	static private void checkPathSourceAndTarget(final String sourceId, final String targetId,
			final List<SubstratePath> pathsToCheck) {
		for (final SubstratePath p : pathsToCheck) {
			if (p.getSource().getName().equals(sourceId) && p.getTarget().getName().equals(targetId)) {
				return;
			}
		}

		fail("No matching path was found for tuple: " + sourceId + " - " + targetId);
	}

	/**
	 * Creates a one tier network with two servers and one switch. The bandwidth of
	 * the servers is different (1 vs 2).
	 */
	protected static void oneTierSetupTwoServers() {
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addSwitchToNetwork("sw", "net", 0);
		ModelFacade.getInstance().addServerToNetwork("srv1", "net", 0, 0, 0, 1);
		ModelFacade.getInstance().addServerToNetwork("srv2", "net", 0, 0, 0, 1);
		ModelFacade.getInstance().addLinkToNetwork("ln1", "net", 1, "srv1", "sw");
		ModelFacade.getInstance().addLinkToNetwork("ln2", "net", 2, "srv2", "sw");
		ModelFacade.getInstance().addLinkToNetwork("ln3", "net", 1, "sw", "srv1");
		ModelFacade.getInstance().addLinkToNetwork("ln4", "net", 2, "sw", "srv2");
	}

	/**
	 * Creates a one tier network with four servers and one switch.
	 */
	static void oneTierSetupFourServers() {
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addSwitchToNetwork("sw", "net", 0);
		ModelFacade.getInstance().addServerToNetwork("srv1", "net", 0, 0, 0, 1);
		ModelFacade.getInstance().addServerToNetwork("srv2", "net", 0, 0, 0, 1);
		ModelFacade.getInstance().addServerToNetwork("srv3", "net", 0, 0, 0, 1);
		ModelFacade.getInstance().addServerToNetwork("srv4", "net", 0, 0, 0, 1);
		ModelFacade.getInstance().addLinkToNetwork("ln1", "net", 0, "srv1", "sw");
		ModelFacade.getInstance().addLinkToNetwork("ln2", "net", 0, "srv2", "sw");
		ModelFacade.getInstance().addLinkToNetwork("ln3", "net", 0, "srv3", "sw");
		ModelFacade.getInstance().addLinkToNetwork("ln4", "net", 0, "srv4", "sw");
		ModelFacade.getInstance().addLinkToNetwork("ln5", "net", 0, "sw", "srv1");
		ModelFacade.getInstance().addLinkToNetwork("ln6", "net", 0, "sw", "srv2");
		ModelFacade.getInstance().addLinkToNetwork("ln7", "net", 0, "sw", "srv3");
		ModelFacade.getInstance().addLinkToNetwork("ln8", "net", 0, "sw", "srv4");
	}

	/**
	 * Creates a two tier network with four servers total, two rack switches, and
	 * one core switch.
	 */
	protected static void twoTierSetupFourServers() {
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addSwitchToNetwork("csw1", "net", 0);
		ModelFacade.getInstance().addSwitchToNetwork("rsw1", "net", 1);
		ModelFacade.getInstance().addSwitchToNetwork("rsw2", "net", 1);

		ModelFacade.getInstance().addServerToNetwork("srv1", "net", 0, 0, 0, 2);
		ModelFacade.getInstance().addServerToNetwork("srv2", "net", 0, 0, 0, 2);
		ModelFacade.getInstance().addServerToNetwork("srv3", "net", 0, 0, 0, 2);
		ModelFacade.getInstance().addServerToNetwork("srv4", "net", 0, 0, 0, 2);

		ModelFacade.getInstance().addLinkToNetwork("ln1", "net", 0, "srv1", "rsw1");
		ModelFacade.getInstance().addLinkToNetwork("ln2", "net", 0, "srv2", "rsw1");
		ModelFacade.getInstance().addLinkToNetwork("ln3", "net", 0, "srv3", "rsw1");
		ModelFacade.getInstance().addLinkToNetwork("ln4", "net", 0, "srv4", "rsw1");

		ModelFacade.getInstance().addLinkToNetwork("ln5", "net", 0, "srv1", "rsw2");
		ModelFacade.getInstance().addLinkToNetwork("ln6", "net", 0, "srv2", "rsw2");
		ModelFacade.getInstance().addLinkToNetwork("ln7", "net", 0, "srv3", "rsw2");
		ModelFacade.getInstance().addLinkToNetwork("ln8", "net", 0, "srv4", "rsw2");

		ModelFacade.getInstance().addLinkToNetwork("ln9", "net", 0, "rsw1", "srv1");
		ModelFacade.getInstance().addLinkToNetwork("ln10", "net", 0, "rsw1", "srv2");
		ModelFacade.getInstance().addLinkToNetwork("ln11", "net", 0, "rsw1", "srv3");
		ModelFacade.getInstance().addLinkToNetwork("ln12", "net", 0, "rsw1", "srv4");

		ModelFacade.getInstance().addLinkToNetwork("ln13", "net", 0, "rsw2", "srv1");
		ModelFacade.getInstance().addLinkToNetwork("ln14", "net", 0, "rsw2", "srv2");
		ModelFacade.getInstance().addLinkToNetwork("ln15", "net", 0, "rsw2", "srv3");
		ModelFacade.getInstance().addLinkToNetwork("ln16", "net", 0, "rsw2", "srv4");

		ModelFacade.getInstance().addLinkToNetwork("ln17", "net", 0, "rsw1", "csw1");
		ModelFacade.getInstance().addLinkToNetwork("ln18", "net", 0, "rsw2", "csw1");
		ModelFacade.getInstance().addLinkToNetwork("ln19", "net", 0, "csw1", "rsw1");
		ModelFacade.getInstance().addLinkToNetwork("ln20", "net", 0, "csw1", "rsw2");
	}

	/**
	 * Creates a two tier network with four servers total, two rack switches, and
	 * two core switches.
	 */
	protected static void twoTierSetupFourServersTwoCoreSwitches() {
		twoTierSetupFourServers();

		ModelFacade.getInstance().addSwitchToNetwork("csw2", "net", 0);

		ModelFacade.getInstance().addLinkToNetwork("ln21", "net", 0, "rsw1", "csw2");
		ModelFacade.getInstance().addLinkToNetwork("ln22", "net", 0, "rsw2", "csw2");
		ModelFacade.getInstance().addLinkToNetwork("ln23", "net", 0, "csw2", "rsw1");
		ModelFacade.getInstance().addLinkToNetwork("ln24", "net", 0, "csw2", "rsw2");
	}

}
