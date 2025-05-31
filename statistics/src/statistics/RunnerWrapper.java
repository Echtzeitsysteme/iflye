package statistics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import iflye.dependencies.logging.IflyeLogger;

/**
 * Runner wrapper class that searches for CSV metric files and starts the Runner
 * class accordingly.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class RunnerWrapper extends IflyeLogger {

	/**
	 * Private constructor ensures no object instantiation.
	 */
	private RunnerWrapper() {
	}

	/**
	 * Main method to start the runner wrapper. Argument must contain the base path
	 * to search CSV files recursive in.
	 *
	 * @param args Arguments to parse, i.e., args[0] must hold the base path.
	 */
	public static void main(final String[] args) {
		if (args == null || args.length < 1) {
			throw new IllegalArgumentException("Please specify the experiment name to load files from.");
		}

		final String path = args[0];
		final Set<String> experiments = new HashSet<>();

		try {
			Files.walk(Paths.get(path)) //
					.filter(Files::isRegularFile) //
					.forEach(p -> {
						final String pabs = p.toAbsolutePath().toString();
						if (pabs.contains(".csv") && pabs.contains("_run")) {
							// Cut of "_run1.csv" from filename
							experiments.add(pabs.substring(0, pabs.indexOf(".csv") - 5));
						}
					});
		} catch (final IOException e) {
			logger.warning("=> Catched an IOException. Halting.");
			e.printStackTrace();
			System.exit(1);
		}

		// Start basic runner for each experiment
		experiments.forEach(e -> {
			Runner.main(new String[] { e });
		});
		logger.info("=> Runner wrapper finished.");
	}

}
