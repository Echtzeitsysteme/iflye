package test.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.SubstratePath;

/**
 * Test class for the ModelFacade that tests the correct behavior in regards to
 * the bandwidth ignoring flag.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ModelFacadePathResidualBandwidthIgnoreTest {

	private final String referenceModelFile = "resources/pathLoadModel_embedding.xmi";

	private boolean oldIgnoreBandwidth = false;

	@BeforeEach
	public void resetModel() {
		ModelFacade.getInstance().resetAll();
	}

	@AfterEach
	public void restoreModelFacadeConfig() {
		ModelFacadeConfig.IGNORE_BW = oldIgnoreBandwidth;
	}

	@Test
	public void testGetAllPathsFromFile() {
		oldIgnoreBandwidth = ModelFacadeConfig.IGNORE_BW;
		ModelFacadeConfig.IGNORE_BW = true;

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

		// Test update of all path's residual bandwidths
		assertDoesNotThrow(() -> {
			ModelFacade.getInstance().updateAllPathsResidualBandwidth("net");
		});
	}

}
