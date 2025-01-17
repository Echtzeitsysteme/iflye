package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
	private final String referenceModelFileWoPaths = "resources/pathLoadModel_noPaths.xmi";

	@BeforeEach
	public void resetModel() {
		ModelFacade.getInstance().resetAll();
	}

	@Test
	public void testGetAllPathsFromFile() {
		// Pre-test: no paths present
		assertThrows(IllegalArgumentException.class, () -> {
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
		assertThrows(IllegalArgumentException.class, () -> ModelFacade.getInstance().getPathById("path-srv1-sw-srv2"));
		assertThrows(IllegalArgumentException.class, () -> ModelFacade.getInstance().getPathById("path-srv2-sw-srv1"));
		assertThrows(IllegalArgumentException.class, () -> ModelFacade.getInstance().getPathById("path-srv1-sw"));
		assertThrows(IllegalArgumentException.class, () -> ModelFacade.getInstance().getPathById("path-sw-srv1"));
		assertThrows(IllegalArgumentException.class, () -> ModelFacade.getInstance().getPathById("path-srv2-sw"));
		assertThrows(IllegalArgumentException.class, () -> ModelFacade.getInstance().getPathById("path-sw-srv2"));

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

	@Test
	public void testPathLookupDataClear() {
		// Load a model file which contains paths
		ModelFacade.getInstance().loadModel(referenceModelFile);

		// Check existing of paths beforehand
		assertNotNull(ModelFacade.getInstance().getAllPathsOfNetwork("net"));
		assertFalse(ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());
		assertNotNull(ModelFacade.getInstance().getPathFromSourceToTarget(getNode("srv1"), getNode("srv2")));
		assertEquals(1, ModelFacade.getInstance().getPathsFromSourceToTarget(getNode("srv1"), getNode("srv2")).size());

		// Save old nodes for later look ups
		final SubstrateNode srv1old = getNode("srv1");
		final SubstrateNode srv2old = getNode("srv2");

		// Now load a model without any paths
		ModelFacade.getInstance().loadModel(referenceModelFileWoPaths);

		// Check non-existance of any paths (method will use the lookup data structure)
		assertNotNull(ModelFacade.getInstance().getAllPathsOfNetwork("net"));
		// Newer nodes must not be contained
		assertTrue(ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());
		assertNull(ModelFacade.getInstance().getPathFromSourceToTarget(getNode("srv1"), getNode("srv2")));

		// Old nodes must also not be contained
		assertNull(ModelFacade.getInstance().getPathFromSourceToTarget(srv1old, srv2old));
	}

	/**
	 * Utility method(s).
	 */

	private SubstrateNode getNode(final String id) {
		return (SubstrateNode) ModelFacade.getInstance().getNodeById(id);
	}

}
