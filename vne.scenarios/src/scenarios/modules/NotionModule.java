package scenarios.modules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import metrics.Reporter;
import metrics.manager.MetricsManager;
import metrics.reporter.NotionReporter;
import scenarios.load.Experiment;

/**
 * Configure an experiment to use Notion as a reporter.
 * 
 * This module requires the Notion API token to be provided as a file path.
 * 
 * Options: --notion-token <path>, --notion-series-db <arg>, --notion-metric-db
 * <arg>
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class NotionModule extends AbstractModule {
	protected final Option notionToken = Option.builder()//
			.longOpt("notion-token")//
			.desc("The path to a text file containing the Notion API token")//
			.hasArg()//
			.type(String.class)//
			.build();

	protected final Option notionSeriesDB = Option.builder()//
			.longOpt("notion-series-db")//
			.desc("The ID of the Notion database to which to store the series configurations")//
			.hasArg()//
			.type(String.class)//
			.build();

	protected final Option notionMetricDB = Option.builder()//
			.longOpt("notion-metric-db")//
			.desc("The ID of the Notion database to which to store the metrics")//
			.hasArg()//
			.type(String.class)//
			.build();

	/**
	 * The factory to create a new NotionReporter from the token, series and metric
	 * DB IDs.
	 */
	protected NotionReporterFactory notionReporterSupplier;

	/**
	 * Initialize with the default NotionReporter.
	 */
	public NotionModule() {
		this(NotionReporter.Default::new);
	}

	/**
	 * Initialize with the given NotionReporter factory.
	 * 
	 * @param notionReporterSupplier the factory to create a new
	 *                               {@link NotionReporter} with the given token,
	 *                               series DB and metrics DB.
	 */
	public NotionModule(final NotionReporterFactory notionReporterSupplier) {
		this.notionReporterSupplier = notionReporterSupplier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final Experiment experiment, final Options options) {
		options.addOption(notionToken);
		options.addOption(notionSeriesDB);
		options.addOption(notionMetricDB);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		final boolean withNotion = cmd.hasOption(notionToken);
		if (!withNotion) {
			return;
		}

		final String tokenFile = cmd.getOptionValue(notionToken);
		final String seriesDb = cmd.getOptionValue(notionSeriesDB, "");
		final String metricDb = cmd.getOptionValue(notionMetricDB, "");

		final Path path = Paths.get(tokenFile);
		if (!path.toFile().exists() || !path.toFile().isFile() || !path.toFile().canRead()) {
			throw new RuntimeException("Notion Token File does not exist or is not readable: "
					+ path.toAbsolutePath().normalize().toString());
		}
		try {
			final String token = Files.readAllLines(path).get(0).trim();
			MetricsManager.getInstance().addReporter(this.notionReporterSupplier.apply(token,
					seriesDb.isBlank() ? null : seriesDb, metricDb.isBlank() ? null : metricDb));
		} catch (IOException e) {
			throw new RuntimeException(
					"Notion Token is not readable at path " + path.toAbsolutePath().normalize().toString());
		}
	}

	/**
	 * Factory to create a new {@link NotionReporter}.
	 */
	@FunctionalInterface
	public static interface NotionReporterFactory {

		/**
		 * Create a new {@link NotionReporter}.
		 * 
		 * @param token    the notion API token.
		 * @param seriesDb the ID of the series DB, null if not provided.
		 * @param metricDb the ID of the metrics DB, null if not provided.
		 * @return a new {@link Reporter} instance.
		 */
		public Reporter apply(String token, String seriesDb, String metricDb);

	}

}
