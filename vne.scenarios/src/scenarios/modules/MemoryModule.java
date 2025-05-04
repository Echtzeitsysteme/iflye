package scenarios.modules;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import metrics.MetricConfig;
import scenarios.load.Experiment;

@Deprecated
public class MemoryModule extends AbstractModule {
	protected final Option memEnabled = Option.builder()//
			.option("g")//
			.longOpt("memmeasurement")//
			.desc("memory measurement metric enabled")//
			.deprecated()//
			.build();

	@Override
	public void register(final Experiment experiment, final Options options) {
		options.addOption(memEnabled);
	}

	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		MetricConfig.ENABLE_MEMORY = cmd.hasOption("memmeasurement");
	}
}
