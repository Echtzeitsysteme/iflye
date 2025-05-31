package metrics;

import java.util.UUID;

import iflye.dependencies.logging.IflyeLogger;
import metrics.manager.MetricsManager;

class MetricsExample extends IflyeLogger {
	public static void main(String[] args) {
		// Create a MetricsManager instance
		MetricsManager metricsManager = new MetricsManager.Default();

		try {
			// Set some tags for the metrics
			metricsManager.addTags("application", "example", //
					"substrate network", "small", //
					"algorithm", "GIPS");

			// Tell the providers that initialization is done
			// so they could prepare for incoming metrics
			metricsManager.initialized();

			// We wil simulate 10 requests to be observed
			for (int request = 0; request < 10; request++) {
				metricsManager.observe("exampleObservation", () -> {
					// Your code to be observed goes here
					// This could be a method call or any other code block
					logger.info("Executing observed code...");
					try {
						Thread.sleep(1000); // Simulate some work
					} catch (InterruptedException e) {
						// If an exception occurs, it needs to be propagated to upper levels
						// if it can't be handled right away.
						// This is important to allow reporting on exceptions.
						throw new RuntimeException(e);
					}
					logger.info("Finished observed code.");
				}, //
					// Again, we could add some tags that are specific to this observation
					// and will be inherited by any sub-observations
						"lastVNR", "VNR" + request, //
						// Some reporters group metrics by a certain tag, e.g., to have all metrics
						// related to a specific request in one CSV row. Those use by default the
						// "series group uuid" tag.
						"series group uuid", UUID.randomUUID().toString());

				// The reporters need to be flushed to ensure that all collected metrics are
				// sent to the desired destination
				// This has to be done at latest before the application is stopped
				// If there are multiple requests that should be observed and whose data should
				// be collected independently, reporters could be flushed after each request to
				// prevent data loss in case of an application crash later on.
				metricsManager.flush();
			}

			// Some reporters provide additional wrap-up features, e.g., calculating
			// statistics, which could be triggered during processing to give an interim
			// result or after all requests are processed.
			metricsManager.conclude();
		} finally {
			// At the end of the application, close the MetricsManager to release resources
			// and stop all reporters
			// This is especially important in the case of an exception, to stop any threads
			// and allow the JVM to shutdown properly.
			metricsManager.close();
		}
	}
}