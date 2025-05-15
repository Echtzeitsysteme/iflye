package scenarios.modules;

import java.io.File;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import metrics.Reporter;
import metrics.manager.MetricsManager;
import metrics.reporter.CsvReporter;
import scenarios.load.Experiment;

/**
 * Configure an experiment to use a CSV file as a reporter.
 * 
 * Options: -c / --csvpath <arg>
 */
public class CsvModule extends AbstractModule {
	protected final Option csvPath = Option.builder()//
			.option("c")//
			.longOpt("csvpath")//
			.desc("file path for the CSV metric file")//
			.hasArg()//
			.build();

	/**
	 * The factory to create a new CSV reporter with the configured CSV file path.
	 */
	protected Function<File, Reporter> csvReporterSupplier;

	/**
	 * Initialize with the default CsvReporter.
	 */
	public CsvModule() {
		this(CsvReporter.Default::new);
	}

	/**
	 * Initialize with the given CsvReporter factory.
	 * 
	 * @param csvReporterSupplier the factory to create a new CsvReporter with the
	 *                            given CSV file path.
	 */
	public CsvModule(final Function<File, Reporter> csvReporterSupplier) {
		this.csvReporterSupplier = csvReporterSupplier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final Experiment experiment, final Options options) {
		options.addOption(csvPath);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		if (cmd.getOptionValue(csvPath) != null) {
			final String csvPath = cmd.getOptionValue("csvpath");
			MetricsManager.getInstance().addReporter(this.csvReporterSupplier.apply(new File(csvPath)));
		}
	}
}
