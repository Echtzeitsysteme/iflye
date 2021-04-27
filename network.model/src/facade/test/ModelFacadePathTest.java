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
	
	private static void oneTierSetup() {
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addSwitchToNetwork("sw", "net", 0);
		ModelFacade.getInstance().addServerToNetwork("srv1", "net", 0, 0, 0, 1);
		ModelFacade.getInstance().addServerToNetwork("srv2", "net", 0, 0, 0, 1);
		ModelFacade.getInstance().addLinkToNetwork("ln1", "net", 0, "srv1", "sw");
		ModelFacade.getInstance().addLinkToNetwork("ln2", "net", 0, "srv2", "sw");
		ModelFacade.getInstance().addLinkToNetwork("ln3", "net", 0, "sw", "srv1");
		ModelFacade.getInstance().addLinkToNetwork("ln4", "net", 0, "sw", "srv2");
	}
	
	@Test
	public void testNoPathsAfterNetworkCreation() {
		oneTierSetup();
		assertTrue(ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());
	}
	
	@Test
	public void testOneTierPathCreation() {
		oneTierSetup();
		
		ModelFacade.getInstance().createAllPathsForNetwork("net");
		final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
		assertFalse(ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());
		
		// Check total number of paths
		assertEquals(4, allPaths.size());
		
		// Check individual source and targets
		final Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("srv1", "sw");
		mapping.put("srv1", "srv2");
		mapping.put("srv2", "sw");
		mapping.put("srv2", "srv1");
		
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
		
		Assertions.fail("No matching path was found!");
	}
	
}
