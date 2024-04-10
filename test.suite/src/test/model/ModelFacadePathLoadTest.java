package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import model.SubstrateNode;
import model.SubstratePath;

/**
 * Test class for the ModelFacade that tests the correct loading mechanism of
 * the ModelFacade.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ModelFacadePathLoadTest {

	private final String referenceModelFile = "resources/pathLoadModel.xmi";

	@BeforeEach
	public void resetModel() {
		ModelFacade.getInstance().resetAll();
	}

	@Test
	public void testGetAllPathsFromFile() {
		// Pre-test: no paths present
		assertThrows(IndexOutOfBoundsException.class, () -> {
			ModelFacade.getInstance().getAllPathsOfNetwork("net");
		});

		// Load the model file
		ModelFacade.getInstance().loadModel(referenceModelFile);

		final List<SubstratePath> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

		// Check total number of paths
		assertFalse(allPaths.isEmpty());
		assertEquals(6, allPaths.size());

		// Check individual source and targets
		final Set<Tuple<String, String>> mapping = new HashSet<>();
		mapping.add(new Tuple<>("srv1", "sw"));
		mapping.add(new Tuple<>("sw", "srv1"));
		mapping.add(new Tuple<>("srv2", "sw"));
		mapping.add(new Tuple<>("sw", "srv2"));
		mapping.add(new Tuple<>("srv1", "srv2"));
		mapping.add(new Tuple<>("srv2", "srv1"));

		ModelFacadePathBasicTest.checkPathSourcesAndTargets(mapping, allPaths);
	}

	@Test
	public void testGetPathByIdFromFile() {
		// Pre-test: no paths present
		assertNull(ModelFacade.getInstance().getPathById("path-srv1-sw-srv2"));
		assertNull(ModelFacade.getInstance().getPathById("path-srv2-sw-srv1"));
		assertNull(ModelFacade.getInstance().getPathById("path-srv1-sw"));
		assertNull(ModelFacade.getInstance().getPathById("path-sw-srv1"));
		assertNull(ModelFacade.getInstance().getPathById("path-srv2-sw"));
		assertNull(ModelFacade.getInstance().getPathById("path-sw-srv2"));

		// Load the model file
		ModelFacade.getInstance().loadModel(referenceModelFile);

		assertNotNull(ModelFacade.getInstance().getPathById("path-srv1-sw-srv2"));
		assertNotNull(ModelFacade.getInstance().getPathById("path-srv2-sw-srv1"));
		assertNotNull(ModelFacade.getInstance().getPathById("path-srv1-sw"));
		assertNotNull(ModelFacade.getInstance().getPathById("path-sw-srv1"));
		assertNotNull(ModelFacade.getInstance().getPathById("path-srv2-sw"));
		assertNotNull(ModelFacade.getInstance().getPathById("path-sw-srv2"));

	}

	@Test
	public void testGetPathFromSourceToTargetNodeFromFile() {
		// Load the model file
		ModelFacade.getInstance().loadModel(referenceModelFile);

		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget(getNode("srv1"), getNode("srv2")));
		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget(getNode("srv2"), getNode("srv1")));
		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget(getNode("srv1"), getNode("sw")));
		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget(getNode("sw"), getNode("srv1")));
		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget(getNode("srv2"), getNode("sw")));
		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget(getNode("sw"), getNode("srv2")));
	}

	@Test
	public void testGetPathFromSourceToTargetIdFromFile() {
		// Load the model file
		ModelFacade.getInstance().loadModel(referenceModelFile);

		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget("srv1", "srv2"));
		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget("srv2", "srv1"));
		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget("srv1", "sw"));
		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget("sw", "srv1"));
		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget("srv2", "sw"));
		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget("sw", "srv2"));
	}

	@Test
	public void testGetPathsFromSourceToTargetNodeFromFile() {
		// Load the model file
		ModelFacade.getInstance().loadModel(referenceModelFile);

		assertNotNull(ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("srv1"), getNode("srv2")));
		assertEquals(1, ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("srv1"), getNode("srv2")).size());

		assertNotNull(ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("srv2"), getNode("srv1")));
		assertEquals(1, ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("srv2"), getNode("srv1")).size());

		assertNotNull(ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("srv1"), getNode("sw")));
		assertEquals(1, ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("srv1"), getNode("sw")).size());

		assertNotNull(ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("sw"), getNode("srv1")));
		assertEquals(1, ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("sw"), getNode("srv1")).size());

		assertNotNull(ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("srv2"), getNode("sw")));
		assertEquals(1, ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("srv2"), getNode("sw")).size());

		assertNotNull(ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("sw"), getNode("srv2")));
		assertEquals(1, ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("sw"), getNode("srv2")).size());
	}

	/**
	 * Utility method(s).
	 */

	private SubstrateNode getNode(final String id) {
		return (SubstrateNode) ModelFacade.getInstance().getNodeById(id);
	}

}
