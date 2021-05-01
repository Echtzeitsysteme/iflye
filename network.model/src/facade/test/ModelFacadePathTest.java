package facade.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.Link;
import model.Node;
import model.Path;
import model.Switch;

/**
 * Test class for the ModelFacade that tests all path related creations.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class ModelFacadePathTest {

	@BeforeEach
	public void resetModel() {
		ModelFacade.getInstance().resetAll();
	}
	
	@Test
	public void testNoPathsAfterNetworkCreation() {
		oneTierSetupTwoServers();
		assertTrue(ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());
	}
	
	@Test
	public void testOneTierPathCreationTwoServers() {
		oneTierSetupTwoServers();
		
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(allPaths.isEmpty());
		
		// Check total number of paths
		assertEquals(6, allPaths.size());
		
		// Check individual source and targets
		final Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("srv1", "sw");
		mapping.put("sw", "srv1");
		mapping.put("srv2", "sw");
		mapping.put("sw", "srv2");
		mapping.put("srv1", "srv2");
		mapping.put("srv2", "srv1");
		
		checkPathSourcesAndTargets(mapping, allPaths);
	}
	
	@Test
	public void testOneTierPathCreationFourServers() {
		oneTierSetupFourServers();
		
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(allPaths.isEmpty());
		
		// Check total number of paths
		assertEquals(20, allPaths.size());
		
		// Check individual source and targets
		final Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("srv1", "sw");
		mapping.put("sw", "srv1");
		mapping.put("srv2", "sw");
		mapping.put("sw", "srv2");
		mapping.put("srv3", "sw");
		mapping.put("sw", "srv3");
		mapping.put("srv4", "sw");
		mapping.put("sw", "srv4");
		mapping.put("srv1", "srv2");
		mapping.put("srv2", "srv1");
		mapping.put("srv1", "srv3");
		mapping.put("srv3", "srv1");
		mapping.put("srv1", "srv4");
		mapping.put("srv4", "srv1");
		mapping.put("srv2", "srv3");
		mapping.put("srv3", "srv2");
		mapping.put("srv2", "srv4");
		mapping.put("srv4", "srv2");
		mapping.put("srv3", "srv4");
		mapping.put("srv4", "srv3");

		checkPathSourcesAndTargets(mapping, allPaths);
	}
	
	@Test
	public void testTwoTierPathCreationFourServers() {
		twoTierSetupFourServers();
		
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(allPaths.isEmpty());
		
		// Check total number of paths
		assertEquals(36, allPaths.size());
		
		// Check individual source and targets
		final Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("srv1", "rsw1");
		mapping.put("srv1", "csw1");
		mapping.put("srv1", "rsw2");
		mapping.put("srv1", "srv2");
		mapping.put("srv1", "srv3");
		mapping.put("srv1", "srv4");
		
		mapping.put("srv2", "srv1");
		mapping.put("srv2", "srv3");
		mapping.put("srv2", "srv4");
		mapping.put("srv2", "rsw1");
		mapping.put("srv2", "rsw2");
		mapping.put("srv2", "csw1");
		
		mapping.put("srv3", "srv1");
		mapping.put("srv3", "srv2");
		mapping.put("srv3", "srv4");
		mapping.put("srv3", "rsw1");
		mapping.put("srv3", "rsw2");
		mapping.put("srv3", "csw1");
		
		mapping.put("srv4", "srv1");
		mapping.put("srv4", "srv2");
		mapping.put("srv4", "srv3");
		mapping.put("srv4", "rsw1");
		mapping.put("srv4", "rsw2");
		mapping.put("srv4", "csw1");
		
		mapping.put("rsw1", "srv1");
		mapping.put("rsw1", "srv2");
		mapping.put("rsw1", "srv3");
		mapping.put("rsw1", "srv4");
		
		mapping.put("rsw2", "srv1");
		mapping.put("rsw2", "srv2");
		mapping.put("rsw2", "srv3");
		mapping.put("rsw2", "srv4");
		
		mapping.put("csw1", "srv1");
		mapping.put("csw1", "srv2");
		mapping.put("csw1", "srv3");
		mapping.put("csw1", "srv4");

		checkPathSourcesAndTargets(mapping, allPaths);
	}
	
	@Test
	public void testTwoTierPathCreationFourServersTwoCoreSwitches() {
		twoTierSetupFourServersTwoCoreSwitches();
		
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(allPaths.isEmpty());
		
		// Check total number of paths
		assertEquals(44, allPaths.size());
		
		// Check individual source and targets
		final Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("srv1", "rsw1");
		mapping.put("srv1", "csw1");
		mapping.put("srv1", "csw2");
		mapping.put("srv1", "rsw2");
		mapping.put("srv1", "srv2");
		mapping.put("srv1", "srv3");
		mapping.put("srv1", "srv4");
		
		mapping.put("srv2", "srv1");
		mapping.put("srv2", "srv3");
		mapping.put("srv2", "srv4");
		mapping.put("srv2", "rsw1");
		mapping.put("srv2", "rsw2");
		mapping.put("srv2", "csw1");
		mapping.put("srv2", "csw2");
		
		mapping.put("srv3", "srv1");
		mapping.put("srv3", "srv2");
		mapping.put("srv3", "srv4");
		mapping.put("srv3", "rsw1");
		mapping.put("srv3", "rsw2");
		mapping.put("srv3", "csw1");
		mapping.put("srv3", "csw2");
		
		mapping.put("srv4", "srv1");
		mapping.put("srv4", "srv2");
		mapping.put("srv4", "srv3");
		mapping.put("srv4", "rsw1");
		mapping.put("srv4", "rsw2");
		mapping.put("srv4", "csw1");
		mapping.put("srv4", "csw2");
		
		mapping.put("rsw1", "srv1");
		mapping.put("rsw1", "srv2");
		mapping.put("rsw1", "srv3");
		mapping.put("rsw1", "srv4");
		
		mapping.put("rsw2", "srv1");
		mapping.put("rsw2", "srv2");
		mapping.put("rsw2", "srv3");
		mapping.put("rsw2", "srv4");
		
		mapping.put("csw1", "srv1");
		mapping.put("csw1", "srv2");
		mapping.put("csw1", "srv3");
		mapping.put("csw1", "srv4");
		
		mapping.put("csw2", "srv1");
		mapping.put("csw2", "srv2");
		mapping.put("csw2", "srv3");
		mapping.put("csw2", "srv4");

		checkPathSourcesAndTargets(mapping, allPaths);
	}
	
	@Test
	public void testOneTierNumberOfHopsPerPath() {
		oneTierSetupTwoServers();
		
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		
		for (final Path p : allPaths) {
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
		
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		
		int counterOneHop = 0;
		int counterTwoHops = 0;
		int counterThreeHops = 0;
		int counterFourHops = 0;
		
		for (final Path p : allPaths) {
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
		
		assertEquals(8, counterOneHop);
		assertEquals(12, counterTwoHops);
		assertEquals(8, counterThreeHops);
		assertEquals(8, counterFourHops);
	}
	
	@Test
	public void testOneTierBandwidthAmoutPerPath() {
		oneTierSetupTwoServers();
		
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		
		for (final Path p : allPaths) {
			if ((p.getSource().getName().equals("srv2") && p.getTarget().getName().equals("sw")) || 
					(p.getSource().getName().equals("sw") && 
							p.getTarget().getName().equals("srv2"))) {
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
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		
		for (final Path p : allPaths) {
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
		
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		
		List<Set<String>> linkNames = new LinkedList<Set<String>>();
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
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		
		for (final Path p : allPaths) {
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
		
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		
		// The reference nodes only have to be added one time
		List<Set<String>> nodeNames = new LinkedList<Set<String>>();
		
		for(int i = 0; i <= 1; i++) {
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
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		
		for (final Path p : allPaths) {
			assertNotNull(p.getName());
		}
	}
	
	@Test
	public void testNoPathsLowerLimit() {
		// Save old values
		final int oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
		final int oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
		
		// Setup for this test
		ModelFacadeConfig.MIN_PATH_LENGTH = 10;
		oneTierSetupFourServers();
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		
		final List<Path> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertTrue(generatedPaths.isEmpty());
		
		// Restore old values
		ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
		ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
	}
	
	@Test
	public void testNoPathsUpperLimit() {
		// Save old values
		final int oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
		final int oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
		
		// Setup for this test
		ModelFacadeConfig.MAX_PATH_LENGTH = 0;
		oneTierSetupFourServers();
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		
		final List<Path> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertTrue(generatedPaths.isEmpty());
		
		// Restore old values
		ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
		ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
	}
	
	@Test
	public void testOnlyPathsWithTwoHops() {
		// Save old values
		final int oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
		final int oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
		
		// Setup for this test
		ModelFacadeConfig.MIN_PATH_LENGTH = 2;
		ModelFacadeConfig.MAX_PATH_LENGTH = 2;
		twoTierSetupFourServers();
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		
		final List<Path> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(generatedPaths.isEmpty());
		
		for (final Path p : generatedPaths) {
			assertEquals(2, p.getHops());
			assertEquals(2, p.getLinks().size());
			assertEquals(3, p.getNodes().size());
		}
		
		// Restore old values
		ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
		ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
	}
	
	@Test
	public void testOnlyPathsWithThreeHops() {
		// Save old values
		final int oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
		final int oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
		
		// Setup for this test
		ModelFacadeConfig.MIN_PATH_LENGTH = 3;
		ModelFacadeConfig.MAX_PATH_LENGTH = 3;
		twoTierSetupFourServers();
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		
		final List<Path> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(generatedPaths.isEmpty());
		
		for (final Path p : generatedPaths) {
			assertEquals(3, p.getHops());
			assertEquals(3, p.getLinks().size());
			assertEquals(4, p.getNodes().size());
		}
		
		// Restore old values
		ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
		ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
	}
	
	/*
	 * Utility methods.
	 */
	
	/**
	 * Tests a list of a sets of strings against a list of paths. The check ensures, that all name
	 * sets are contained within the list of paths (with links).
	 * 
	 * @param linkNames List of sets of strings with link names for each path.
	 * @param pathsToCheck List of paths to check.
	 */
	private void checkPathLinkNames(final List<Set<String>> linkNames, 
			final List<Path> pathsToCheck) {
		List<Set<String>> pathLinks = new LinkedList<Set<String>>();
		for (final Path p : pathsToCheck) {
			final Set<String> fromPath = new HashSet<String>();
			for (Link l : p.getLinks()) {
				fromPath.add(l.getName());
			}
			pathLinks.add(fromPath);
		}
		
		assertTrue(linkNames.containsAll(pathLinks));
		assertTrue(pathLinks.containsAll(linkNames));
	}
	
	/**
	 * Tests a list of sets of strings against a list of paths. The check ensures, that all name
	 * sets are contained within the list of paths (with nodes).
	 * 
	 * @param nodeNames List of sets of strings with node names for each path.
	 * @param pathsToCheck List of paths to check.
	 */
	private void checkPathNodeNames(final List<Set<String>> nodeNames, 
			final List<Path> pathsToCheck) {
		List<Set<String>> pathNodes = new LinkedList<Set<String>>();
		for (final Path p : pathsToCheck) {
			final Set<String> fromPath = new HashSet<String>();
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
	 * Checks a given list of paths against a given map of strings to strings.
	 * The map represents the mapping of sourceID to targetID for all paths.
	 * 
	 * @param mapping SourceID to targetID mapping.
	 * @param pathsToCheck List of paths to check.
	 */
	private void checkPathSourcesAndTargets(final Map<String, String> mapping,
			final List<Path> pathsToCheck) {
		for (final String sourceId : mapping.keySet()) {
			final String targetId = mapping.get(sourceId);
			checkPathSourceAndTarget(sourceId, targetId, pathsToCheck);
		}
	}
	
	/**
	 * Checks a given list of paths for one specific sourceID and targetID. If no path with the
	 * given sourceID and targetID can be found, the check fails.
	 * 
	 * @param sourceId SourceID to search for.
	 * @param targetId TargetID to search for.
	 * @param pathsToCheck List of paths to search in.
	 */
	private void checkPathSourceAndTarget(final String sourceId, final String targetId,
			final List<Path> pathsToCheck) {
		for (final Path p : pathsToCheck) {
			if(p.getSource().getName().equals(sourceId)
					&& p.getTarget().getName().equals(targetId)) {
				return;
			}
		}
		
		Assertions.fail("No matching path was found for tuple: " + sourceId + " - " 
				+ targetId);
	}
	
	/**
	 * Creates a one tier network with two servers and one switch.
	 * The bandwidth of the servers is different (1 vs 2).
	 */
	private static void oneTierSetupTwoServers() {
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
	private static void oneTierSetupFourServers() {
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
	 * Creates a two tier network with four servers total, two rack switches, and one
	 * core switch.
	 */
	private static void twoTierSetupFourServers() {
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
		ModelFacade.getInstance().addLinkToNetwork("ln3", "net", 0, "rsw1", "srv1");
		ModelFacade.getInstance().addLinkToNetwork("ln4", "net", 0, "rsw1", "srv2");
		ModelFacade.getInstance().addLinkToNetwork("ln5", "net", 0, "srv3", "rsw2");
		ModelFacade.getInstance().addLinkToNetwork("ln6", "net", 0, "srv4", "rsw2");
		ModelFacade.getInstance().addLinkToNetwork("ln7", "net", 0, "rsw2", "srv3");
		ModelFacade.getInstance().addLinkToNetwork("ln8", "net", 0, "rsw2", "srv4");
		
		ModelFacade.getInstance().addLinkToNetwork("ln9", "net", 0, "rsw1", "csw1");
		ModelFacade.getInstance().addLinkToNetwork("ln10", "net", 0, "rsw2", "csw1");
		ModelFacade.getInstance().addLinkToNetwork("ln11", "net", 0, "csw1", "rsw1");
		ModelFacade.getInstance().addLinkToNetwork("ln12", "net", 0, "csw1", "rsw2");
	}
	
	/**
	 * Creates a two tier network with four servers total, two rack switches, and two
	 * core switches.
	 */
	private static void twoTierSetupFourServersTwoCoreSwitches() {
		twoTierSetupFourServers();
		
		ModelFacade.getInstance().addSwitchToNetwork("csw2", "net", 0);
		
		ModelFacade.getInstance().addLinkToNetwork("ln13", "net", 0, "rsw1", "csw2");
		ModelFacade.getInstance().addLinkToNetwork("ln14", "net", 0, "rsw2", "csw2");
		ModelFacade.getInstance().addLinkToNetwork("ln15", "net", 0, "csw2", "rsw1");
		ModelFacade.getInstance().addLinkToNetwork("ln16", "net", 0, "csw2", "rsw2");
	}
	
}
