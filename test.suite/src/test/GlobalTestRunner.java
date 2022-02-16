package test;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import java.io.PrintWriter;
import java.nio.file.Path;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;

import io.github.gdiegel.junit5_html_report_generator.ExtentReportGeneratingListener;

/**
 * Global test runner class that can be used to trigger test execution, e.g.,
 * from CLI or within a CI.
 * 
 * @author Maximilian Kratz <maximilian.kratz@es.tu-darmstadt.de>
 */
public class GlobalTestRunner {

	/**
	 * Main method to start the execution.
	 * 
	 * @param args Parameters will be ignored.
	 */
	public static void main(final String[] args) {
		// Generate all listeners
		final SummaryGeneratingListener listener = new SummaryGeneratingListener();
		final LegacyXmlReportGeneratingListener xmlListener = new LegacyXmlReportGeneratingListener(
				Path.of("./build/reports"), new PrintWriter(System.out));
		final ExtentReportGeneratingListener extentReportGeneratingListener = new ExtentReportGeneratingListener();

		// Find tests and build request
		final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request() //
				.selectors(selectPackage("test")) //
				.filters(includeClassNamePatterns(".*Test")) //
				.build();

		// Build launcher and register all listeners
		final Launcher launcher = LauncherFactory.create();
		launcher.registerTestExecutionListeners(listener);
		launcher.registerTestExecutionListeners(xmlListener);
		launcher.registerTestExecutionListeners(extentReportGeneratingListener);

		// Run tests
		launcher.execute(request);

		// Print summary to console
		final TestExecutionSummary summary = listener.getSummary();
		summary.printTo(new PrintWriter(System.out));
		System.exit(0);
	}

}
