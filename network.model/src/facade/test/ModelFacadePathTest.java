package facade.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import model.Path;

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
	
	private static void oneTierSetupTwoServers() {
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addSwitchToNetwork("sw", "net", 0);
		ModelFacade.getInstance().addServerToNetwork("srv1", "net", 0, 0, 0, 1);
		ModelFacade.getInstance().addServerToNetwork("srv2", "net", 0, 0, 0, 1);
		ModelFacade.getInstance().addLinkToNetwork("ln1", "net", 0, "srv1", "sw");
		ModelFacade.getInstance().addLinkToNetwork("ln2", "net", 0, "srv2", "sw");
		ModelFacade.getInstance().addLinkToNetwork("ln3", "net", 0, "sw", "srv1");
		ModelFacade.getInstance().addLinkToNetwork("ln4", "net", 0, "sw", "srv2");
	}
	
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
	
	private void checkPathSourcesAndTargets(final Map<String, String> mapping,
			final List<Path> pathsToCheck) {
		for (String sourceId : mapping.keySet()) {
			final String targetId = mapping.get(sourceId);
			checkPathSourceAndTarget(sourceId, targetId, pathsToCheck);
		}
	}
	
	private void checkPathSourceAndTarget(final String sourceId, final String targetId,
			final List<Path> pathsToCheck) {
		for (Path p : pathsToCheck) {
			if(p.getSource().getName().equals(sourceId)
					&& p.getTarget().getName().equals(targetId)) {
				return;
			}
		}
		
		Assertions.fail("No matching path was found for tuple: " + sourceId + " - " 
				+ targetId);
	}
	
}
