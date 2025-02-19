package test.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;

/**
 * Test class for the ModelFacade that tests the persist feature for models.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ModelFacadePersistTest {

	private final String customFilePath = "./test-persistent-model.xmi";

	@BeforeEach
	public void resetModel() {
		ModelFacade.getInstance().resetAll();
	}

	@BeforeEach
	@AfterEach
	public void deleteDefaultPersistentModelFile() {
		final String path = ModelFacade.PERSISTENT_MODEL_PATH;
		delete(path);
	}

	@BeforeEach
	@AfterEach
	public void deleteCustomPersistentModelFile() {
		delete(customFilePath);
	}

	@Test
	public void testDefaultPersist() {
		generateNetwork();
		ModelFacade.getInstance().persistModel();
		checkIfFileGenerated(ModelFacade.PERSISTENT_MODEL_PATH);
	}

	@Test
	public void testCustomPersist() {
		generateNetwork();
		ModelFacade.getInstance().persistModel(customFilePath);
		checkIfFileGenerated(customFilePath);
	}

	@Test
	public void testUseAfterPersist() {
		generateNetwork();
		ModelFacade.getInstance().persistModel();

		// Even after a persist method call, the model within the ModelFacade should not
		// be broken
		try {
			assertNotNull(ModelFacade.getInstance().getRoot());
			assertNotNull(ModelFacade.getInstance().getAllNetworks());
			assertFalse(ModelFacade.getInstance().getAllNetworks().isEmpty());
			assertNotNull(ModelFacade.getInstance().getNetworkById("test"));
			assertNotNull(ModelFacade.getInstance().getServerById("srv1"));
		} catch (final IndexOutOfBoundsException ex) {
			Assert.fail("The model within the resource set of the ModelFacade was "
					+ "corrupted after previous persist call.");
		}
	}

	//
	// Utility methods.
	//

	/**
	 * Generates a small network that should be persisted to file.
	 */
	private void generateNetwork() {
		// Setup
		final String id = "test";
		ModelFacade.getInstance().addNetworkToRoot(id, false);
		ModelFacade.getInstance().addServerToNetwork("srv1", id, 1, 2, 3, 0);
	}

	/**
	 * This method checks if a source file was generated during build time for a
	 * given file path.
	 * 
	 * @param path File path to check file existence for.
	 */
	private static void checkIfFileGenerated(final String path) {
		if (path == null || path.isBlank()) {
			throw new IllegalArgumentException("Given path is invalid!");
		}

		final File f = new File(path);
		if (!(f.exists() && !f.isDirectory())) {
			Assert.fail("Expected file could not be found.");
		}
	}

	/**
	 * Deletes the file on given path if it exists and is not a directory.
	 * 
	 * @param path File path to delete.
	 */
	private void delete(final String path) {
		final File f = new File(path);
		if (f.exists() && !f.isDirectory()) {
			f.delete();
		}
	}

}
