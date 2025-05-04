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

public class NotionModule extends AbstractModule {
	protected final Option notionToken = Option.builder()//
			.longOpt("notion-token")//
			.desc("The Notion API token")//
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

	protected TriFunction<String, String, String, Reporter> notionReporterSupplier = NotionReporter.Default::new;

	public NotionModule() {
	}

	public NotionModule(final TriFunction<String, String, String, Reporter> notionReporterSupplier) {
		this.notionReporterSupplier = notionReporterSupplier;
	}

	@Override
	public void register(final Experiment experiment, final Options options) {
		options.addOption(notionToken);
		options.addOption(notionSeriesDB);
		options.addOption(notionMetricDB);
	}

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

	@FunctionalInterface
	public static interface TriFunction<S, T, U, R> {
		public R apply(S s, T t, U u);
	}
}
