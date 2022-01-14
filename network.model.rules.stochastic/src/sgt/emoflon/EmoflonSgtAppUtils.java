package sgt.emoflon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import network.model.rules.stochastic.api.StochasticAPI;

public class EmoflonSgtAppUtils {

	/**
	 * Temporary directory (path).
	 */
	private static Path tempDir;

	private EmoflonSgtAppUtils() {
	}

	/**
	 * Creates the temporary directory if it does not exist before.
	 * 
	 * @return Path of the created temporary directory.
	 */
	public static Path createTempDir() {
		if (tempDir == null) {
			try {
				tempDir = Files.createTempDirectory("eMoflonTmp");
			} catch (final IOException e) {
				throw new RuntimeException("Unable to create temporary directory for eMoflon", e);
			}
		}
		return tempDir;
	}

	/**
	 * Extracts the specified 'ibex-patterns.xmi' file if not already present.
	 * 
	 * @param workspacePath The path of the workspace.
	 */
	public static void extractFiles(final String workspacePath) {
		final File target = new File(workspacePath + StochasticAPI.patternPath);
		if (target.exists()) {
			return;
		}
		try (final InputStream is = EmoflonSgtHiPEApp.class
				.getResourceAsStream("/network/model/rules/api/ibex-patterns.xmi")) {
			target.getParentFile().mkdirs();
			if (is == null) {
				throw new IllegalStateException("ibex-patterns are missing from the resources");
			}
			Files.copy(is, target.toPath());
			target.deleteOnExit();
		} catch (final IOException e) {
			throw new IllegalStateException("Something went wrong while copying emoflon resources", e);
		}
	}

}
