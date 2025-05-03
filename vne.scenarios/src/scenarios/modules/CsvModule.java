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
import scenarios.load.DissScenarioLoad;

public class CsvModule extends AbstractModule {
	protected final Option csvPath = Option.builder()//
			.option("c")//
			.longOpt("csvpath")//
			.desc("file path for the CSV metric file")//
			.hasArg()//
			.build();

	protected Function<File, Reporter> csvReporterSupplier = CsvReporter.Default::new;

	public CsvModule(final DissScenarioLoad experiment) {
		super(experiment);
	}

	public CsvModule(final DissScenarioLoad experiment, final Function<File, Reporter> csvReporterSupplier) {
		this(experiment);
		this.csvReporterSupplier = csvReporterSupplier;
	}

	@Override
	public void register(final Options options) {
		options.addOption(csvPath);
	}

	@Override
	public void configure(final CommandLine cmd) throws ParseException {
		if (cmd.getOptionValue(csvPath) != null) {
			final String csvPath = cmd.getOptionValue("csvpath");
			MetricsManager.getInstance().addReporter(this.csvReporterSupplier.apply(new File(csvPath)));
		}
	}
}
