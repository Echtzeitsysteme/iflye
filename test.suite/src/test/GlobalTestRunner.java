package test;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.platform.engine.DiscoverySelector;
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
	 * @param args First parameter is the absolute path to the running jar file.
	 */
	public static void main(final String[] args) {
		String pathArg = "";
		if (args != null && args.length > 0) {
			pathArg = args[0];
		}

		// Generate all listeners
		final SummaryGeneratingListener listener = new SummaryGeneratingListener();
		final LegacyXmlReportGeneratingListener xmlListener = new LegacyXmlReportGeneratingListener(
				Path.of("./build/reports"), new PrintWriter(System.out));
		final ExtentReportGeneratingListener extentReportGeneratingListener = new ExtentReportGeneratingListener();

		// Find tests and build request
		LauncherDiscoveryRequest request;

		// If no path argument is set, the program runs in Eclipse
		if (pathArg.isBlank()) {
			// Automatically find all class files that end with '...Test.class'.
			request = LauncherDiscoveryRequestBuilder.request() //
					.selectors(selectPackage("test")) //
					.filters(includeClassNamePatterns(".*Test")) //
					.build();
		} else {
			// Path argument was given: The program runs from a jar file. Therefore, it has
			// to manually derive all of the test class files because the automatic selector
			// does not work in packaged jar files.
			request = LauncherDiscoveryRequestBuilder.request() //
					.selectors(convertClassesToSelectors(getTestClasses(pathArg))) //
					.build();
		}

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

	/**
	 * Searches fro test class files ('...Test.class') within a given jar files and
	 * returns them as a collection of classes.
	 * 
	 * @param jarPath Path to the jar file to search in.
	 * @return Collection of found classes.
	 */
	private static Collection<Class<?>> getTestClasses(final String jarPath) {
		final Collection<Class<?>> classes = new HashSet<Class<?>>();
		Set<String> classNames = new HashSet<>();

		// Iterate through the jar file and get all '...Test.class' files.
		try (final JarFile jarFile = new JarFile(new File(jarPath))) {
			final Enumeration<JarEntry> e = jarFile.entries();
			while (e.hasMoreElements()) {
				final JarEntry jarEntry = e.nextElement();
				if (jarEntry.getName().endsWith("Test.class")) {
					final String className = jarEntry.getName().replace("/", ".").replace(".class", "");
					classNames.add(className);
				}
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		// Convert all found names into classes themselves.
		classNames.forEach(c -> {
			System.out.println("=> Found test class " + c);
			try {
				classes.add(Class.forName(c));
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
			}
		});

		return classes;
	}

	/**
	 * Converts a given collection of (test)classes to an array of discovery
	 * selectors.
	 * 
	 * @param classes Collection of classes to convert.
	 * @return Array of discovery selectors.
	 */
	private static DiscoverySelector[] convertClassesToSelectors(final Collection<Class<?>> classes) {
		final DiscoverySelector[] selectors = new DiscoverySelector[classes.size()];
		int index = 0;
		final Iterator<Class<?>> it = classes.iterator();
		while (it.hasNext()) {
			final Class<?> cur = it.next();
			selectors[index++] = selectClass(cur);
		}
		return selectors;
	}

}
